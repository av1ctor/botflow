import React, {lazy, Suspense, useCallback, useContext, useEffect, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import {Switch, useHistory} from 'react-router-dom';
import {ScrollPanel} from 'primereact/components/scrollpanel/ScrollPanel';
import {ContextMenu} from 'primereact/components/contextmenu/ContextMenu';
import AuthRoute from '../../components/Route/AuthRoute';
import LoadingSpinner from '../../components/Misc/LoadingSpinner';
import ConfirmDialog from '../../components/Dialog/ConfirmDialog';
import CollectionTemplatesPanel from '../Modules/Collections/Templates/TemplatesPanel';
import CollectionPropsPanel from '../Modules/Collections/Properties/Panel';
import CollectionPermsPanel from '../Modules/Collections/Permissions/Panel';
import CollectionDeleteDiag from '../Modules/Collections/Operations/DeleteDialog';
import Menu from './Menu';
import Toolbar from './Toolbar';
import {WorkspaceContext} from '../../contexts/WorkspaceContext';
import {SessionContext} from '../../contexts/SessionContext';
import CollectionStore from '../../stores/CollectionStore';
import CollectionLogic from '../../business/CollectionLogic';

import '../../layout/layout.css';

const Dashboard = lazy(() => import('../Modules/Dashboard/Dashboard'));
const Collection = lazy(() => import('../Modules/Collections/CollectionWrapper'));

const AccountOptions = lazy(() => import('../Modules/Config/Account/Options'));
const AccountWorkspaces = lazy(() => import('../Modules/Config/Account/Workspaces'));
const WorkspaceOptions = lazy(() => import('../Modules/Config/Workspace/Options'));
const WorkspacePlan = lazy(() => import('../Modules/Config/Workspace/Plan'));
const WorkspaceUsers = lazy(() => import('../Modules/Config/Workspace/Users'));
const WorkspaceGroups = lazy(() => import('../Modules/Config/Workspace/Groups'));
const WorkspaceCredentials = lazy(() => import('../Modules/Config/Workspace/Credentials'));
const WorkspaceProviders = lazy(() => import('../Modules/Config/Workspace/Providers'));
const WorkspaceApps = lazy(() => import('../Modules/Config/Workspace/Apps/Apps'));

const Main = observer(props =>
{
	const history = useHistory();

	const workspace = useContext(WorkspaceContext);
	const session = useContext(SessionContext);
	
	const [menuState, setMenuState] = useState({
		staticMenuInactive: false,
		mobileMenuActive: false,
	});

	const [contextMenuModel, setContextMenuModel] = useState(null);

	const [confirmDiagProps, setConfirmDiagProps] = useState({
		visible: false,
		text: null,
		callback: null,
		data: null
	});

	const [templatesPanelProps, setTemplatesPanelProps] = useState({
		visible: false,
		parentId: null, 
		published: true, 
		allowFolder: true
	});

	const [delDiagProps, setDelDiagProps] = useState({
		visible: false,
		store: null, 
		position: 0
	});

	const [collectionPropsPanel, setCollectionPropsPanel] = useState({
		visible: false,
		store: null, 
	});

	const [collectionPermsPanel, setCollectionPermsPanel] = useState({
		visible: false,
		store: null, 
	});

	const instance = useRef({
		menuIndex: 0,
		menuClick: false,
		contextMenuElm: null,
		layoutMenuScrollerElm: null,
		sidebarElm: null,
	});

	useEffect(() =>
	{
		loadMenu();
		const subs = document.addEventListener('rf::updated::menu', handleReloadCollectionsMenu);
		return () => document.removeEventListener('rf::updated::menu', subs);
	}, []);

	useEffect(() =>
	{
		changeTheme(session.user.theme);
	}, [session.user.theme]);

	useEffect(() =>
	{
		if (menuState.mobileMenuActive)
		{
			addClass(document.body, 'body-overflow-hidden');
		}
		else
		{
			removeClass(document.body, 'body-overflow-hidden');
		}
	}, [menuState.mobileMenuActive]);

	const changeTheme = (theme) =>
	{
		document.body.parentNode.setAttribute('rf-theme', theme || 'default');
	};

	const _replaceSubdomain = (href, subdomain) =>
	{
		return href.replace(/[a-z]+:\/\/(.*?)\./.exec(href)[1], subdomain);
	};

	const _replaceHash = (href, hash) =>
	{
		return href.replace(/#\/(.*)/.exec(href)[1], hash);
	};

	const switchWorkspace = async (id) =>
	{
		const res = await workspace.switchTo(id);
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		window.location.href = _replaceSubdomain(
			_replaceHash(
				window.location.href, 
				`auth/switch?workspace=${id}&user=${session.user.id}&token=${res.data.token}&expiration=${res.data.expiration}&remember=${session.rememberMe}`), 
			res.data.workspace.name);
	};

	const redirect = (path) =>
	{
		props.container.redirect(path);
	};

	const isAdmin = () =>
	{
		return session.user.admin? 
			true: 
			false;
	};

	const loadMenu = async () =>
	{
		const res = await workspace.loadMenu();
		if(res.errors === null)
		{
			const menu = res.data;

			const collectionsMenu = findCollectionsMenu(menu);
			collectionsMenu.id = session.workspace.idRootCol;
			collectionsMenu.items = await loadCollectionsMenu(collectionsMenu.id);
			
			workspace.setMenu(_transformMenu(null, menu));
		}
	};

	const reloadCollectionsMenu = async (parentId = null) =>
	{
		if(parentId && parentId !== session.workspace.idRootCol)
		{
			return handleLoadCollectionFolder(parentId, true);
		}

		const collectionsMenu = findCollectionsMenu(workspace.menu);
		const items = await loadCollectionsMenu(collectionsMenu.id);
		collectionsMenu.items = _transformMenu(collectionsMenu, items);
		workspace.setMenu(Array.from(workspace.menu));
	};

	const transformCollectionsMenu = (collections, parentId) =>
	{
		const menus = collections.map(col => 
			({
				id: col.id,
				label: col.name,
				icon: col.type === 'FOLDER'? 
					'inter-folder': 
					'inter-' + col.icon,
				command: col.type === 'FOLDER'? 
					() => (!col.items?
						handleLoadCollectionFolder(col.id):
						null): 
					`c/${col.id}/${col.name}`,
				pos: col.order,
				items: null,
				contextMenu: isAdmin()? 
					(event) => handleShowCollectionContextMenu(event, col, parentId): 
					null
			}));
		
		return isAdmin()?
			menus.concat(
				{
					label: 'Add...',
					icon: null,
					command: () => handleShowTemplatesPanel(parentId, true, true)
				}):
			menus;
	};

	const loadCollectionsMenuFor = async (parentId) =>
	{
		const res = await workspace.loadCollectionsMenu(parentId);
		if(res.errors !== null)
		{
			return null;
		}

		return transformCollectionsMenu(res.data, parentId);
	};

	const findCollectionsMenu = (items) =>
	{
		return items.find(item => item.alias === 'COLLECTION');
	};

	const findMenuItem = (items, id) =>
	{
		const res = items.find(i => i.id === id);
		if(res)
		{
			return res;
		}

		for(let i = 0; i < items.length; i++)
		{
			const item = items[i];
			if(item.items)
			{
				const res = findMenuItem(item.items, id);
				if(res)
				{
					return res;
				}
			}
		}

		return null;
	};

	const handleLoadCollectionFolder = async (id, force = false) =>
	{
		const collectionsMenu = findCollectionsMenu(workspace.menu);
		const sub = findMenuItem(collectionsMenu.items, id);
		if(sub.items && !force)
		{
			return;
		}

		const items = await loadCollectionsMenuFor(id);
		if(items === null)
		{
			return;
		}

		sub.items = _transformMenu(sub, items);

		workspace.setMenu(Array.from(workspace.menu));
	};

	const loadCollectionsMenu = async (rootId) =>
	{
		return await loadCollectionsMenuFor(rootId);
	};

	const handleReloadCollectionsMenu = (event) =>
	{
		loadCollectionsMenuFor(event.id);
	};

	const handleShowCollectionProps = (collection) =>
	{
		setCollectionPropsPanel({
			visible: true,
			store: new CollectionStore(
				new CollectionLogic(props.api), collection),
		});
	};

	const handleHideCollectionProps = useCallback(() =>
	{
		setCollectionPropsPanel({
			visible: true,
			store: {},
		});
	}, []);
		
	const handleShowCollectionPerms = (collection) =>
	{
		setCollectionPermsPanel({
			visible: true,
			store: new CollectionStore(
				new CollectionLogic(props.api), collection),
		});
	};

	const handleHideCollectionPerms = useCallback(() =>
	{
		setCollectionPermsPanel({
			visible: true,
			store: {},
		});
	}, []);
		
	const handleDeleteCollection = useCallback((collection) =>
	{
		setDelDiagProps({
			visible: true,
			store: new CollectionStore(
				new CollectionLogic(props.api), collection),
		});
	}, []);

	const handleCloseDeleteDiag = useCallback(() =>
	{
		setDelDiagProps({
			visible: false,
		});
	}, []);

	const handleMoveCollection = async (collection, parentId, items, dir) =>
	{
		const inc = (dir === 'up' || dir === 'first'? -1: 1);
		const other = dir === 'first'? 
			0:
			dir === 'last'?
				items.length-2:
				items.findIndex(item => item.id === collection.id) + inc;
		const pos = items[other].pos || 0;

		const res = await props.api.patch(`collections/${collection.id}/loc/${pos !== collection.order? pos: pos + inc}`);

		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		reloadCollectionsMenu(parentId);
	};

	const buildCollectionContextMenu = useCallback((collection, parentId) =>
	{
		const hideMenu = () => instance.current.contextMenuElm.hide();

		const parent = findMenuItem(workspace.menu, parentId);
		
		return [
			{
				label: 'Properties',
				icon: 'la la-cog',
				disabled: collection.type === 'SCHEMA',
				command: () => 
				{
					hideMenu();
					handleShowCollectionProps(collection, parentId);
				}
			},
			{
				label: 'Permissions',
				icon: 'la la-key',
				disabled: collection.type === 'SCHEMA',
				command: () => 
				{
					hideMenu();
					handleShowCollectionPerms(collection, parentId);
				}
			},
			{
				separator: true
			},
			{
				label: 'Delete',
				icon: 'la la-trash',
				disabled: collection.type === 'SCHEMA',
				command: () => 
				{
					hideMenu();
					handleDeleteCollection(collection, parentId);
				}
			},
			{
				separator: true
			},
			{
				label: 'Move',
				icon: 'la la-arrows',
				items: [
					{
						label: 'First',
						icon: 'la la-angle-double-up',
						disabled: parent.items[0].id === collection.id,
						command: () => 
						{
							hideMenu();
							handleMoveCollection(collection, parentId, parent.items, 'first');
						}
					},
					{
						label: 'Up',
						icon: 'la la-angle-up',
						disabled: parent.items[0].id === collection.id,
						command: () => 
						{
							hideMenu();
							handleMoveCollection(collection, parentId, parent.items, 'up');
						}
					},
					{
						label: 'Down',
						icon: 'la la-angle-down',
						disabled: parent.items[parent.items.length-2].id === collection.id,
						command: () => 
						{
							hideMenu();
							handleMoveCollection(collection, parentId, parent.items, 'down');
						}
					},
					{
						label: 'Last',
						icon: 'la la-angle-double-down',
						disabled: parent.items[parent.items.length-2].id === collection.id,
						command: () => 
						{
							hideMenu();
							handleMoveCollection(collection, parentId, parent.items, 'last');
						}
					}
				]
			}
		];
	}, []);

	const handleShowCollectionContextMenu = (event, collection, parentId) =>
	{
		setContextMenuModel(buildCollectionContextMenu(collection, parentId));

		instance.current.contextMenuElm.show(event);
	};

	const handleHideCollectionContextMenu = useCallback(() =>
	{
	}, []);

	const handleShowTemplatesPanel = (parentId, published, allowFolder, onSelected = null) =>
	{
		setTemplatesPanelProps({
			visible: true,
			parentId, 
			published, 
			allowFolder, 
			onSelected
		});
	};

	const handleCloseTemplatesPanel = useCallback(() =>
	{
		setTemplatesPanelProps({
			visible: false,
		});
	}, []);

	const _transformMenu = (parent, items) =>
	{
		return items
			.filter(item => item.command !== null || (item.items !== null && item.items.length > 0))
			.map((item) => 
			{
				if(!item.index)
				{
					const icon = item.icon || '';
					item.parentIndex = parent? 
						parent.index: 
						null;
					item.index = ++instance.current.menuIndex;
					item.icon = icon && icon.indexOf('inter-') === -1? 
						'la la-' + icon: 
						icon;
					item.path = '/' + (item.command !== null && item.command.constructor === String? item.command: null);
					if(item.command !== null && item.command.constructor === String)
					{
						item.command = () => history.push(item.path);
					}
				}

				if(item.items && item.items.length > 0)
				{
					item.items = _transformMenu(item, item.items);
				}
				else
				{
					item.items = null;
				}

				return item;
			});
	};

	const onToggleMenu = useCallback((event) =>
	{
		instance.current.menuClick = true;

		if (isDesktop()) 
		{
			setMenuState(state => ({
				...state,
				staticMenuInactive: !state.staticMenuInactive
			}));
		}
		else 
		{
			setMenuState(state => ({
				...state,
				mobileMenuActive: !state.mobileMenuActive
			}));
		}
       
		event.preventDefault();
	}, []);

	const onWrapperClick = useCallback(() =>
	{
		if (!instance.current.menuClick) 
		{
			setMenuState(state => ({
				...state,
				mobileMenuActive: false
			}));
		}

		instance.current.menuClick = false;
	}, []);

	const onSidebarClick = useCallback(() =>
	{
		instance.current.menuClick = true;
		setTimeout(() => 
		{
			if(instance.current.layoutMenuScrollerElm && 
				instance.current.layoutMenuScrollerElm.moveBar)
			{
				instance.current.layoutMenuScrollerElm.moveBar(); 
			}
		}, 500);
	}, []);
	
	const onMenuItemClick = useCallback((event) =>
	{
		if(!event.item.items) 
		{
			setMenuState(state => ({
				...state,
				mobileMenuActive: false
			}));
		}
	}, []);
	
	const addClass = (element, className) =>
	{
		if (element.classList)
			element.classList.add(className);
		else
			element.className += ' ' + className;
	};

	const removeClass = (element, className) =>
	{
		if (element.classList)
			element.classList.remove(className);
		else
			element.className = element.className.replace(
				new RegExp('(^|\\b)' + className.split(' ').join('|') + '(\\b|$)', 'gi'), ' ');
	};

	const isDesktop = () =>
	{
		return window.innerWidth > 1024;
	};

	const showUserConfirmation = (callback, data, text = 'Are you sure?') =>
	{
		setConfirmDiagProps({
			visible: true,
			text: text,
			callback: callback,
			data: data,
		});
	};

	const handleConfirmed = useCallback(async () =>
	{
		await confirmDiagProps.callback(confirmDiagProps.data);
		handleHideConfirmed();
	}, [confirmDiagProps]);

	const handleHideConfirmed = useCallback(() =>
	{
		setConfirmDiagProps({
			visible: false,
			text: null,
			callback: null,
			data: null
		});
	}, []);

	const showDashboard = useCallback(() =>
	{
		redirect('');
	}, []);

	const isMenuVisible = () =>
	{
		return !menuState.staticMenuInactive ||
			menuState.mobileMenuActive;
	};

	const container = useRef({});
	Object.assign(container.current, props.container, {
		isAdmin,
		redirect,
		changeTheme,
		switchWorkspace,
		showUserConfirmation,
		reloadCollectionsMenu,
		showTemplatesPanel: handleShowTemplatesPanel
	});

	const wrapperClass = classNames(
		'layout-wrapper', 
		'layout-static',
		{
			'layout-static-sidebar-inactive': menuState.staticMenuInactive,
			'layout-mobile-sidebar-active': menuState.mobileMenuActive
		}
	);

	const sidebarClassName = classNames('layout-sidebar', 'layout-sidebar-dark');

	const childProps = {
		api: props.api,
		history: history,
		container: container.current,
	};

	const isAuthenticated = session.isAuthenticated;

	return (
		<>
			<div 
				className={wrapperClass} 
				onClick={onWrapperClick}>
				<div 
					ref={(el) => instance.current.sidebarElm = el} 
					className={sidebarClassName} 
					onClick={onSidebarClick}
				>
					<ScrollPanel 
						ref={(el) => instance.current.layoutMenuScrollerElm = el} 
						style={{height:'100%'}}
					>
						<div className="layout-sidebar-scroll-content">
							<div className="layout-logo">
								<img 
									alt="Logo" 
									className="rf-logo" 
									src="assets/layout/images/logo-white.svg"
									onClick={showDashboard} />
							</div>
							<Toolbar
								session={session}
								container={props.container}
							/>
							<Menu 
								items={workspace.menu} 
								onMenuItemClick={onMenuItemClick} 
							/>
						</div>
					</ScrollPanel>
					<div 
						className="layout-sidebar-toggle"
						onClick={onToggleMenu}>
						<i className={`la la-angle-double-${isMenuVisible()? 'left': 'right'}`}/>
					</div>
				</div>

				<div 
					className={`layout-sidebar-opener ${!isMenuVisible() || !isDesktop()? 'visible': 'hidden'}`} 
					title="Mostrar menu"
				>
					<i 
						className="la la-angle-double-right" 
						onClick={onToggleMenu} />
				</div>

				<div className="layout-mask"></div>
				
				<div className="layout-main">
					<Suspense 
						fallback={<LoadingSpinner visible />}
					>
						<Switch>
							<AuthRoute path="/" exact component={Dashboard} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/account/options" exact component={AccountOptions} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/account/workspaces" exact component={AccountWorkspaces} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/workspace/options" exact component={WorkspaceOptions} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/workspace/plan" exact component={WorkspacePlan} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/workspace/users" exact component={WorkspaceUsers} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/workspace/groups" exact component={WorkspaceGroups} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/workspace/credentials" exact component={WorkspaceCredentials} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/workspace/providers" exact component={WorkspaceProviders} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/config/workspace/apps" exact component={WorkspaceApps} props={childProps} isAuthenticated={isAuthenticated} />
							<AuthRoute path="/c/:id/:name?" param='id' component={Collection} props={{childProps, visible: true}} isAuthenticated={isAuthenticated} />
						</Switch>
					</Suspense>
				</div>
			</div>

			<ContextMenu
				ref={el => instance.current.contextMenuElm = el}
				model={contextMenuModel}
				onHide={handleHideCollectionContextMenu} 
			/>

			<CollectionTemplatesPanel
				{...childProps}
				{...templatesPanelProps}
				onHide={handleCloseTemplatesPanel}
			/>

			<CollectionDeleteDiag
				{...childProps}
				{...delDiagProps}
				onHide={handleCloseDeleteDiag}
				onDeleted={handleCloseDeleteDiag} 
			/>

			<CollectionPropsPanel
				{...childProps}
				{...collectionPropsPanel}
				icon="book"
				title="Properties"
				compo="general"
				onHide={handleHideCollectionProps}
			/>

			<CollectionPermsPanel
				{...childProps}
				{...collectionPermsPanel}
				icon="key"
				title="Permissions"
				compo="general"
				onHide={handleHideCollectionPerms}
			/>

			<ConfirmDialog
				{...confirmDiagProps}
				onConfirmed={handleConfirmed}
				onHide={handleHideConfirmed} 
			/>
		</>
	);
});

Main.propTypes = {
	api: PropTypes.object,
	container: PropTypes.object,
};

export default Main;
