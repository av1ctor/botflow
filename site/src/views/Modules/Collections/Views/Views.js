import React, {useCallback, useContext, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import {TabContent, TabPane, Nav, NavItem, NavLink, Badge} from 'reactstrap';
import {SizeMe} from 'react-sizeme';
import PanelHorzSlider from '../../../../components/Slider/PanelHorzSlider';
import Table from './Table/Table';
import Grid from './Grid/Grid';
import Kanban from './Kanban/Kanban';
import InspectorPanel from '../Items/Inspector/Panel';
import ViewPropsPanel from '../Properties/View/Panel';
import ColumnPropsPanel from '../Properties/Column/Panel';
import CollectionUtil from '../../../../libs/CollectionUtil';
import {SessionContext} from '../../../../contexts/SessionContext';

const panelsList = [
	'viewProps', 'fieldProps', 'details',
];

const propertiesMenuItems = [
	{title: 'Gerais', icon: 'book', compo: 'general'},
	{title: 'Filtros', icon: 'filter', compo: 'filters'},
	{title: 'Exibição', icon: 'eye', compo: 'visuals'}
];

const typeToIcon = {
	'grid': 'th-list',
	'kanban': 'trello',
	'table': 'table',
};

const Views = observer(props =>
{
	const session = useContext(SessionContext);
	const store = props.store;

	const [panels, setPanels] = useState(() => 
		panelsList.reduce((obj, key) => {obj[key] = {visible: false}; return obj;}, {}));

	const showView = (view, index) =>
	{
		store.setActiveView(index);
	};

	const moveView = async (from, to, schema = null) =>
	{
		const res = await store.moveView(from, to, schema);
		if(res.errors)
		{
			return false;
		}

		return true;
	};

	const createView = (index, type) =>
	{
		store.createView(index, type);
	};
	
	const duplicateView = (index) =>
	{
		store.duplicateView(index);
	};

	const deleteView = useCallback((data) =>
	{
		store.deleteView(data.index);
	}, []);

	const handleDeleteView = (view, index) =>
	{
		props.container.showUserConfirmation(deleteView, {view, index}, 'Remover view?');
	};

	const calcInsertableFields = (view) =>
	{
		const fields = view.fields;
		if(!fields)
		{
			return 0;
		}

		const user = session.user;

		return Object.values(fields)
			.reduce(
				(cnt, field) => 
					cnt + (!CollectionUtil.isDisabledForInsert(field, user)? 1: 0)
				, 0
			);
	};

	const handleHideFieldProps = useCallback(() =>
	{
		hidePanel('fieldProps');
	}, []);

	const handleHideViewProps = useCallback(() =>
	{
		hidePanel('viewProps');
	}, []);

	const showDetails = (item) =>
	{
		showPanel('details', {
			item: item
		});
	};

	const handleHideDetails = useCallback(() =>
	{
		hidePanel('details');
	}, []);
	
	const showPanel = useCallback((panel, args = {}) =>
	{
		setPanels(state => ({
			...state,
			[panel]: {
				visible: true,
				...args,
			}
		}));
	}, []);

	const hidePanel = useCallback((panel) =>
	{
		setPanels(state => ({
			...state,
			[panel]: {
				visible: false,
			}
		}));
	}, []);

	const handleShowViewMenu = useCallback((event, view, index) =>
	{
		props.container.showPopupMenu(event, buildViewMenu(view, index, event.target));
	}, []);

	const buildViewMenu = useCallback((view, index, element) =>
	{
		const views = store.collection.schemaObj.views;

		const hideMenu = () => props.container.hidePopupMenu();
		
		return [
			{
				label: 'Propriedades',
				icon: 'la la-cog',
				items: propertiesMenuItems.map(sub => ({
					label: sub.title,
					icon: 'la la-' + sub.icon,
					command: () => 
					{
						hideMenu();
						showPanel('viewProps', {...sub, viewId: view.id});
					}
				}))
			},
			{
				separator: true
			},
			{
				label: 'Criar',
				icon: 'la la-plus-circle',
				items: [
					{
						label: 'Grid',
						icon: 'la la-table',
						command: async () => 
						{
							hideMenu();
							await createView(index, 'grid');
						}
					}
				]
			},
			{
				label: 'Duplicar',
				icon: 'la la-copy',
				command: async () => 
				{
					hideMenu();
					await duplicateView(index, element);
				}
			},
			{
				label: 'Apagar',
				icon: 'la la-trash',
				disabled: views.length === 0,
				command: async () => 
				{
					hideMenu();
					await handleDeleteView(view, index, element);
				}
			},
			{
				separator: true
			},
			{
				label: 'Mover',
				icon: 'la la-arrows',
				items: [
					{
						label: 'Início',
						icon: 'la la-angle-double-left',
						disabled: index === 0,
						command: async () => 
						{
							hideMenu();
							await moveView(index, 0);
						}
					},
					{
						label: 'Esquerda',
						icon: 'la la-angle-left',
						disabled: index === 0,
						command: async () => 
						{
							hideMenu();
							await moveView(index, index-1);
						}
					},
					{
						label: 'Direita',
						icon: 'la la-angle-right',
						disabled: index === views.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveView(index, index+1);
						}
					},
					{
						label: 'Final',
						icon: 'la la-angle-double-right',
						disabled: index === views.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveView(index, views.length - 1);
						}
					}				
				]
			},
		];
	}, []);

	const renderView = (view, height, visible) =>
	{
		const insertableFields = calcInsertableFields(view);
		
		switch(view.type)
		{
		case 'table':
			return (
				<Table
					{...props}
					container={container.current}
					viewId={view.id}
					height={height}
					insertableFieldsCount={insertableFields}
					visible={visible}
				/>				
			);
		
		case 'grid':
			return (
				<Grid
					{...props}
					container={container.current}
					viewId={view.id}
					height={height}
					insertableFieldsCount={insertableFields}
					visible={visible}
				/>				
			);

		case 'kanban':
			return (
				<Kanban
					{...props}
					container={container.current}
					viewId={view.id}
					height={height}
					insertableFieldsCount={insertableFields}
					visible={visible}
				/>				
			);
		}
	};

	const container = useRef({});
	Object.assign(container.current, props.container, {
		showPanel,
		hidePanel,
		showDetails
	});

	const collection = store.collection;
	if(!collection)
	{
		return null;
	}
	
	const schema = collection.schemaObj;

	const activeView = store.activeView;

	const views = schema.views.map(v => store.views.get(v.id));
	const activeTab = views.findIndex(view => view.id === activeView.id);
	const isCreator = props.container.isCreator();

	return (
		<>
			<ViewPropsPanel 
				container={container.current}
				store={store}
				onHide={handleHideViewProps}
				{...panels.viewProps} 
			/>

			<ColumnPropsPanel
				container={container.current}
				store={store}
				onHide={handleHideFieldProps}
				{...panels.fieldProps} 
			/>

			{!props.isAux && 
				<div className="rf-col-details-container">
					<InspectorPanel
						api={props.api}
						container={container.current}
						store={store}
						fullScreen={props.fullScreen} 
						onHide={handleHideDetails}
						{...panels.details} 
					/>
				</div>
			}

			<div className="rf-col-main-body">
				<PanelHorzSlider className="rf-col-main-body-nav">
					<Nav tabs>
						{views.map((view, index) => 
							<NavItem key={view.name}>
								<NavLink
									className={classnames({active: activeView.id === view.id})}
									onClick={() => showView(view, index)}>
									<div className="rf-col-view-tab">
										<div className="rf-col-view-icon"><i className={`la la-${typeToIcon[view.type]}`} /></div>
										<div className="rf-col-view-name">{view.name}</div>
										{view.meta.updatedCnt > 0 && <Badge pill color="info" className="rf-col-badge">
											{view.meta.updatedCnt}
										</Badge>}
										{isCreator &&
											<div className="rf-menu-handle">
												<i 
													className="la la-caret-down" 
													onClick={(e) => handleShowViewMenu(e, view, index)}
													title="Abrir menu" />
											</div>
										}
									</div>
								</NavLink>
							</NavItem>
						)}
					</Nav>
				</PanelHorzSlider>
				
				<div className="rf-col-main-body-content">
					<SizeMe 
						monitorHeight={true} 
						monitorWidth={false}>
						{({ size }) =>	
							<TabContent 
								activeTab={activeTab} 
								className="w-100 h-100">
								{views.map((view, index) => 
									<TabPane 
										key={view.id} 
										tabId={index} 
										className="rf-col-view-container">
										{renderView(
											view, 
											Math.max(size.height, 200), 
											props.visible && view.id === activeView.id)
										}
									</TabPane>					
								)}
							</TabContent>
						}
					</SizeMe>
				</div>
			</div>
		</>
	);
});

Views.propTypes = {
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	isAux: PropTypes.bool,
	fullScreen: PropTypes.bool,
	insertableFieldsCount: PropTypes.number,
};

export default Views;