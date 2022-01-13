import React, {useCallback, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import Misc from '../../../libs/Misc';
import PropertiesPanel from './Properties/Panel';
import DuplicateDialog from './Operations/DuplicateDialog';
import DeleteDialog from './Operations/DeleteDialog';
import ImportPanel from './Operations/Import/Panel';
import PermissionsPanel from './Permissions/Panel';
import IntegrationsPanel from './Integrations/Panel';
import AutomationsPanel from './Automations/Panel';
import AuxsPanel from './Auxs/Panel';
import ReportsPanel from './Reports/Panel';
import ReportProperties from './Reports/Properties/Panel';
import FlowsPanel from './Flows/Panel';
import FormsPanel from './Forms/Editor/Panel';
import FormProperties from './Forms/Editor/Properties/Panel';
import Views from './Views/Views';
import CollectionUtil from '../../../libs/CollectionUtil';
import {propertiesMenuItems, permissionsMenuItems, importMenuItems} from './MenuItems';

import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import 'diagram-js-minimap/assets/diagram-js-minimap.css';
import './Collection.css';

const panelsList = [
	'properties', 'duplicate', 'delete', 'import', 'permissions', 'integrations', 
	'forms', 'automations', 'reports', 'flows', 'auxs', 
	'formProps', 'viewProps', 'fieldProps', 'reportProps',
];

const Collection = observer(props =>
{
	const store = props.store;

	const [panels, setPanels] = useState(() => 
		panelsList.reduce((obj, key) => {obj[key] = {visible: false}; return obj;}, {}));

	const isCreator = () =>
	{
		return store.user.role <= CollectionUtil.ROLE_CREATOR;
	};

	const redirect = (id) =>
	{
		props.container.redirect(id? `/c/${id}`: '/');
	};

	const handleOpenReportPanel = useCallback(() =>
	{
		const collection = store.collection;
		if(collection.schemaObj.reports)
		{
			showPanel('reports');
		}
		else
		{
			showPanel('reportProps', {
				index: 0,
				id: Misc.genId(),
				title: 'Create',
				icon: 'file',
				compo: 'general'
			});
		}
	}, []);

	const createAux = (collection, position, onCreated) =>
	{
		props.container.showTemplatesPanel(collection.parent.id, false, false, async (templateId) => 
		{
			const res = await store.createAux(templateId, position);
			if(res.errors)
			{
				props.container.showMessage(res.errors, 'error');
				return;
			}

			onCreated(res.data[position || 0], position);
		});

	};

	const handleCreateAux = (collection, position = 0, onCreated = null) =>
	{
		if(!onCreated)
		{
			onCreated = () => 
			{
				showPanel('auxs');
			};
		}
		
		createAux(collection, position, onCreated);
	};

	const handleOpenAuxsPanel = async () =>
	{
		const collection = store.collection;

		let auxs = store.auxs;
		if(!auxs)
		{
			const res = await store.loadAuxs();
			if(res.errors)
			{
				props.container.showMessage(res.errors, 'error');
				return;
			}

			auxs = res.data;
		}

		if(auxs && auxs.length !== 0)
		{
			showPanel('auxs');
		}
		else
		{
			handleCreateAux(collection);
		}
	};

	const showUserConfirmation = (...args) =>
	{
		return props.container.showUserConfirmation(...args);
	};


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

	const handleShowProperties = (title, icon, compo) =>
	{
		showPanel('properties', {
			title,
			icon,
			compo
		});
	};

	const handleHideProperties = useCallback(() =>
	{
		hidePanel('properties');
	}, []);

	const handleShowDuplicate = () =>
	{
		showPanel('duplicate', {
			store: store
		});
	};
	
	const handleHideDuplicate = useCallback(() =>
	{
		hidePanel('duplicate');
	}, []);

	const handleShowDelete = () =>
	{
		showPanel('delete', {
			store: store
		});
	};

	const handleHideDelete = useCallback(() =>
	{
		hidePanel('delete');
	}, []);

	const handleShowImport = (title, icon, compo) =>
	{
		showPanel('import', {
			title,
			icon,
			compo
		});
	};

	const handleHideImport = useCallback(() =>
	{
		hidePanel('import');
	}, []);

	const handleShowPermissions = (title, icon, compo) =>
	{
		showPanel('permissions', {
			title,
			icon,
			compo
		});
	};

	const handleHidePermissions = useCallback(() =>
	{
		hidePanel('permissions');
	}, []);

	const handleShowIntegrations = () =>
	{
		showPanel('integrations', {});
	};

	const handleHideIntegrations = useCallback(() =>
	{
		hidePanel('integrations');
	}, []);

	const handleShowAutomations = () =>
	{
		showPanel('automations');
	};

	const handleHideAutomations = useCallback(() =>
	{
		hidePanel('automations');
	}, []);

	const handleShowForms = () =>
	{
		showPanel('forms', {});
	};

	const handleHideForms = useCallback(() =>
	{
		hidePanel('forms');
	}, []);

	const handleHideFormProps = useCallback(() =>
	{
		hidePanel('formProps');
	}, []);

	const handleShowReports = () =>
	{
		handleOpenReportPanel();
	};

	const handleHideReports = useCallback(() =>
	{
		hidePanel('reports');
	}, []);

	const handleHideReportProps = useCallback(() =>
	{
		hidePanel('reportProps');
	}, []);

	const handleShowFlows = () =>
	{
		showPanel('flows');
	};
	
	const handleHideFlows = useCallback(() =>
	{
		hidePanel('flows');
	}, []);

	const handleShowAuxs = () =>
	{
		handleOpenAuxsPanel();
	};

	const handleHideAuxs = useCallback(() =>
	{
		hidePanel('auxs');
	}, []);

	const handleShowCollectionMenu = (event) =>
	{
		props.container.showPopupMenu(event, buildCollectionMenu());
	};

	const buildCollectionMenu = useCallback(() =>
	{
		const hideMenu = () => props.container.hidePopupMenu();
		
		return [
			{
				label: 'Properties',
				icon: 'la la-cog',
				items: propertiesMenuItems.map(sub => ({
					label: sub.title,
					icon: 'la la-' + sub.icon,
					command: () => 
					{
						hideMenu();
						handleShowProperties(sub.title, sub.icon, sub.compo);
					}
				})) 
			},
			{
				label: 'Actions',
				icon: 'la la-table',
				items: [
					{
						label: 'Duplicate',
						icon: 'la la-copy',
						command: async () => 
						{
							hideMenu();
							handleShowDuplicate();
						}
					},
					{
						label: 'Delete',
						icon: 'la la-trash',
						command: async () => 
						{
							hideMenu();
							handleShowDelete();
						}
					},					
					{
						separator: true
					},
					{
						label: 'Insert',
						icon: 'la la-cloud-upload',
						items: importMenuItems.map(sub => ({
							label: sub.title,
							icon: 'la la-' + sub.icon,
							command: () => 
							{
								hideMenu();
								handleShowImport(sub.title, sub.icon, sub.compo);
							}
						}))
					},
					{
						label: 'Export',
						icon: 'la la-cloud-download',
						command: async () => 
						{
							hideMenu();
						}
					}
				]
			},
			{
				label: 'Permissions',
				icon: 'la la-key',
				items: permissionsMenuItems.map(sub => ({
					label: sub.title,
					icon: 'la la-' + sub.icon,
					command: () => 
					{
						hideMenu();
						handleShowPermissions(sub.title, sub.icon, sub.compo);
					}
				}))
			},
			{
				label: 'Integrations',
				icon: 'la la-chain',
				command: () => 
				{
					hideMenu();
					handleShowIntegrations();
				}
			},
			{
				label: 'Automations',
				icon: 'la la-cogs',
				command: () => 
				{
					hideMenu();
					handleShowAutomations();
				}
			},
			{
				label: 'Flows',
				icon: 'la la-project-diagram',
				command: async () => 
				{
					hideMenu();
					handleShowFlows();
				}
			},
			{
				label: 'Auxs',
				icon: 'la la-people-carry',
				command: async () => 
				{
					hideMenu();
					handleShowAuxs();
				}
			},
			{
				label: 'Forms',
				icon: 'la la-wpforms',
				command: async () => 
				{
					hideMenu();
					handleShowForms();
				}
			},
			{
				label: 'Reports',
				icon: 'la la-bar-chart',
				command: async () => 
				{
					hideMenu();
					handleShowReports();
				}
			},
		];
	}, []);

	const container = useRef({});
	Object.assign(container.current, props.container, {	
		isCreator,
		redirect,
		showUserConfirmation,
		showPanel,
		hidePanel,
		createAux,
	});

	const collection = store.collection;
	if(!collection)
	{
		return null;
	}
	
	const {isAux, isMobile} = props;

	return (
		<>
			{!isAux && 
				<div className="rf-col-flows-container">
					<FlowsPanel 
						api={props.api}
						container={container.current}
						store={store}
						fullScreen={isMobile}
						onHide={handleHideFlows}
						{...panels.flows} 
					/>
				</div>
			}

			{!isAux && 
				<ReportProperties 
					container={container.current} 
					store={store}
					fullScreen={isMobile}
					onHide={handleHideReportProps}
					{...panels.reportProps} 
				/>
			}

			{!isAux && 
				<div className="rf-col-reports-container">
					<ReportsPanel 
						api={props.api}
						container={container.current}
						store={store}
						fullScreen={isMobile}
						onHide={handleHideReports}
						{...panels.reports} 
					/>
				</div>
			}

			{!isAux && 
				<div className="rf-col-auxs-container">
					<AuxsPanel 
						api={props.api}
						container={container.current}
						store={store}
						fullScreen={isMobile}
						onHide={handleHideAuxs}
						{...panels.auxs} 
					/>
				</div>
			}

			{!isAux && 
				<FormProperties 
					container={container.current} 
					store={store}
					fullScreen={isMobile}
					onHide={handleHideFormProps}
					{...panels.formProps} 
				/>
			}

			{!isAux &&
				<div className="rf-col-forms-container">
					<FormsPanel 
						container={container.current}
						store={store}
						fullScreen={isMobile}
						onHide={handleHideForms}
						{...panels.forms}
					/>
				</div>
			}

			<div className="rf-col-main">
				{!isAux && 
					<div className="rf-col-main-header pt-2 pb-4">
						<div>
							<div className="rf-col-title-icon">
								<i className={`inter-${collection.icon}`} />
							</div>
							<div className="rf-col-title-other">
								<div className="row">
									<div className="col-auto rf-col-title-container">
										<div className="rf-col-title">
											{collection.name}
										</div>
										{isCreator() &&
											<div className="rf-menu-icon" title="Exibir menu da coleção">
												<i className="la la-caret-down" onClick={handleShowCollectionMenu} />
											</div>
										}
									</div>
								</div>
								<div className="row">
									<div className="col">
										<small>{collection.desc}</small>
									</div>
								</div>
							</div>
						</div>
					</div>
				}

				<Views 
					api={props.api}
					container={container.current}
					store={store}
					isAux={isAux}
					fullScreen={isMobile}
					visible={props.visible}
				/>
			</div>

			<PropertiesPanel 
				api={props.api}
				container={container.current}
				store={store}
				isAux={isAux}
				onHide={handleHideProperties}
				{...panels.properties}
			/>

			<DuplicateDialog
				container={container.current}
				onHide={handleHideDuplicate}
				{...panels.duplicate}
			/>

			<DeleteDialog
				container={container.current}
				onHide={handleHideDelete}
				onDeleted={handleHideDelete}
				{...panels.delete}
			/>

			<ImportPanel 
				api={props.api}
				container={container.current}
				store={store}
				isAux={isAux} 
				onHide={handleHideImport}
				{...panels.import}
			/>

			<PermissionsPanel 
				api={props.api}
				container={container.current}
				store={store}
				isAux={isAux} 
				onHide={handleHidePermissions}
				{...panels.permissions}
			/>

			<IntegrationsPanel 
				api={props.api}
				container={container.current}
				store={store}
				isAux={isAux} 
				onHide={handleHideIntegrations}
				{...panels.integrations}
			/>

			<AutomationsPanel 
				api={props.api}
				container={container.current}
				store={store}
				isAux={isAux} 
				onHide={handleHideAutomations}
				{...panels.automations}
			/>
		</>
	);
});

Collection.propTypes = {
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
};

export default Collection;