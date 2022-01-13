import React, {useCallback, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import {TabContent, TabPane, Nav, NavItem, NavLink} from 'reactstrap';
import classnames from 'classnames';
import SidebarResizeable from '../../../../components/Bar/SidebarResizeable';
import PanelHorzSlider from '../../../../components/Slider/PanelHorzSlider';
import Aux from './Aux_';
import {propertiesMenuItems, permissionsMenuItems, importMenuItems} from '../MenuItems';

const AuxsPanel = observer(props =>
{
	const store = props.store;
	
	const [activeTab, setActiveTab] = useState(0);

	const showAux = (index) =>
	{
		setActiveTab(index);
	};

	const handleShowAuxMenu = useCallback((event, aux, index) =>
	{
		props.container.showPopupMenu(event, buildAuxMenu(aux, index));
	}, []);

	const handleDuplicate = (auxStore, index) =>
	{
		props.container.showPanel('duplicate', {
			store: auxStore,
			position: index,
			onDuplicated: () => props.container.hidePanel('duplicate')
		});
	};

	const handleDelete = (auxStore, index) =>
	{
		props.container.showPanel('delete', {
			store: auxStore,
			position: index,
			onDeleted: () => {
				props.container.hidePanel('delete');
				setActiveTab(index > 0? index - 1: 0);
			}
		});
	};

	const handleCreate = (index) =>
	{
		props.container.createAux(
			store.collection, 
			index, 
			() => setActiveTab(index));
	};

	const moveAux = async (auxStore, dir, from, to) =>
	{
		const res = await store.moveAux(auxStore.collection, dir, from, to);
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}
		else
		{
			setActiveTab(to);
		}
	};

	const buildAuxMenu = (auxStore, index) =>
	{
		const auxs = store.auxs;

		const hideMenu = () => props.container.hidePopupMenu();
		
		return [
			{
				label: 'Geral',
				icon: 'la la-list',
				items: [
					{
						label: 'Propriedades',
						icon: 'la la-cog',
						items: propertiesMenuItems.map(sub => ({
							label: sub.title,
							icon: 'la la-' + sub.icon,
							command: () => 
							{
								hideMenu();
								props.container.showPanel('properties', {
									store: auxStore,
									...sub
								});
							}
						})) 
					},
					{
						separator: true
					},
					{
						label: 'Criar',
						icon: 'la la-plus-circle',
						command: async () => 
						{
							hideMenu();
							await handleCreate(index);
						}
					},
					{
						label: 'Duplicar',
						icon: 'la la-copy',
						command: async () => 
						{
							hideMenu();
							handleDuplicate(auxStore, index);
						}
					},
					{
						label: 'Apagar',
						icon: 'la la-trash',
						disabled: auxs.length === 0,
						command: async () => 
						{
							hideMenu();
							await handleDelete(auxStore, index);
						}
					},
				]
			},
			{
				label: 'Dados',
				icon: 'la la-database',
				items: [
					{
						label: 'Importar',
						icon: 'la la-cloud-upload',
						items: importMenuItems.map(sub => ({
							label: sub.title,
							icon: 'la la-' + sub.icon,
							command: () => 
							{
								hideMenu();
								props.container.showPanel('import', {
									store: auxStore,
									...sub
								});
							}
						})) 
					},
					{
						label: 'Exportar',
						icon: 'la la-cloud-download',
						command: async () => 
						{
							hideMenu();
							props.container.showPanel('export', {
								store: auxStore
							});
						}
					},
				]
			},
			{
				label: 'Permissões',
				icon: 'la la-users',
				items: permissionsMenuItems.map(sub => ({
					label: sub.title,
					icon: 'la la-' + sub.icon,
					command: () => 
					{
						hideMenu();
						props.container.showPanel('permissions', {
							store: auxStore,
							...sub
						});
					}
				})) 
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
							await moveAux(auxStore, 'FIRST', index, 0);
						}
					},
					{
						label: 'Esquerda',
						icon: 'la la-angle-left',
						disabled: index === 0,
						command: async () => 
						{
							hideMenu();
							await moveAux(auxStore, 'LEFT', index, index-1);
						}
					},
					{
						label: 'Direita',
						icon: 'la la-angle-right',
						disabled: index === auxs.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveAux(auxStore, 'RIGHT', index, index+1);
						}
					},
					{
						label: 'Final',
						icon: 'la la-angle-double-right',
						disabled: index === auxs.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveAux(auxStore, 'LAST', index, auxs.length - 1);
						}
					}				
				]
			},
		];
	};

	if(!props.visible)
	{
		return null;
	}
	
	const auxs = store.auxs;
	if(!auxs || auxs.length === 0)
	{
		return null;
	}
	
	const isCreator = props.container.isCreator();

	return (
		<SidebarResizeable 
			visible={props.visible} 
			position="right" 
			onHide={props.onHide} 
			fullScreen={props.fullScreen}>
			<div className="w-100 h-100">
				<FocusLock disabled={!props.visible} returnFocus className="rf-flex-100">
					<div className="rf-col-auxs-tabs w-100">
						<h4><i className="la la-people-carry" /> Auxiliares</h4>
						<PanelHorzSlider>
							<Nav tabs>
								{auxs.map((auxStore, index) =>
									<NavItem key={index}>
										<NavLink
											className={classnames({active: activeTab === index})}
											onClick={() => showAux(index)}>
											{auxStore.collection.name}
											{isCreator &&
												<div className="rf-menu-handle">
													<i 
														className="la la-caret-down" 
														onClick={(e) => handleShowAuxMenu(e, auxStore, index)} />
												</div>
											}
										</NavLink>
									</NavItem>
								)}
							</Nav>
						</PanelHorzSlider>
						<TabContent activeTab={activeTab}>
							{auxs.map((auxStore, index) =>
								<TabPane 
									key={auxStore.collection.id} 
									tabId={index}>
									<Aux
										store={auxStore}
										visible={activeTab === index}
										childProps={props}
									/>
								</TabPane>
							)}
						</TabContent>
					</div>
				</FocusLock>
			</div>
		</SidebarResizeable>
	);
});

AuxsPanel.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default AuxsPanel;
