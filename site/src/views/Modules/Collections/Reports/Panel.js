import React, {useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import {TabContent, TabPane, Nav, NavItem, NavLink} from 'reactstrap';
import classnames from 'classnames';
import SidebarResizeable from '../../../../components/Bar/SidebarResizeable';
import PanelHorzSlider from '../../../../components/Slider/PanelHorzSlider';
import Report from './Report';
import Misc from '../../../../libs/Misc';

const propertiesMenuItems = [
	{
		title: 'Geral', 
		icon: 'book', 
		compo: 'general'
	},
	{
		title: 'Campos', 
		icon: 'table', 
		compo: 'fields', 
	},
	{
		title: 'Filtros', 
		icon: 'filter', 
		compo: 'filters', 
	},
	{
		title: 'Gráfico', 
		icon: 'chart-bar', 
		compo: 'chart',
	},
];

const ReportsPanel = observer(props =>
{
	const store = props.store;
	
	const [activeTab, setActiveTab] = useState(0);

	const showReport = (report, index) =>
	{
		setActiveTab(index);
	};

	const showPopupMenu = (event, menu) =>
	{
		props.container.showPopupMenu(event, menu);
	};

	const handleHidePopupMenu = () =>
	{
		props.container.hidePopupMenu();
	};

	const handleShowReportMenu = (event, view, index) =>
	{
		showPopupMenu(event, buildReportMenu(view, index, event.target));
	};

	const duplicateReport = async (index) =>
	{
		const res = await store.duplicateReport(index);
		if(!res.errors)
		{
			setActiveTab(reports.length);
		}
	};

	const deleteReport = async (data) =>
	{
		const res = await store.deleteReport(data.index);
		if(!res.errors)
		{
			setActiveTab(activeTab => activeTab === data.index? 
				(data.index > 0? data.index - 1: 0):
				activeTab);
		}
	};

	const handleDeleteReport = (report, index) =>
	{
		props.container.showUserConfirmation(deleteReport, {report, index}, 'Delete report?');
	};
	
	const moveReport = async (from, to) =>
	{
		const res = await store.moveReport(from, to);
		if(!res.errors)
		{
			setActiveTab(to);
		}
	};

	const handleShowReportProps = (sub, index, report) =>
	{
		props.container.showPanel('reportProps', {
			index,
			id: report.id,
			...sub,
		});
	};

	const createReport = (index) =>
	{
		props.container.showPanel('reportProps', {
			index: index + 1,
			id: Misc.genId(),
			title: 'Create',
			icon: 'file',
			compo: 'general'
		});
	};

	const buildReportMenu = (report, index, element) =>
	{
		const reports = store.collection.schemaObj.reports;

		const hideMenu = () => handleHidePopupMenu();
		
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
						handleShowReportProps(sub, index, report);
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
					await createReport(index);
				}
			},
			{
				label: 'Duplicar',
				icon: 'la la-copy',
				command: async () => 
				{
					hideMenu();
					await duplicateReport(index, element);
				}
			},
			{
				label: 'Apagar',
				icon: 'la la-trash',
				disabled: reports.length === 0,
				command: async () => 
				{
					hideMenu();
					await handleDeleteReport(report, index, element);
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
							await moveReport(index, 0);
						}
					},
					{
						label: 'Esquerda',
						icon: 'la la-angle-left',
						disabled: index === 0,
						command: async () => 
						{
							hideMenu();
							await moveReport(index, index-1);
						}
					},
					{
						label: 'Direita',
						icon: 'la la-angle-right',
						disabled: index === reports.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveReport(index, index+1);
						}
					},
					{
						label: 'Final',
						icon: 'la la-angle-double-right',
						disabled: index === reports.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveReport(index, reports.length - 1);
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

	const isCreator = props.container.isCreator();
	const reports = store.collection.schemaObj.reports;

	return (
		<SidebarResizeable 
			visible={props.visible} 
			position="right" 
			onHide={props.onHide} 
			fullScreen={props.fullScreen}>
			<div className="w-100 h-100">
				<FocusLock disabled={!props.visible} returnFocus className="rf-flex-100">
					<div className="rf-col-reports-tabs w-100">
						<h4><i className="la la-bar-chart" /> Relatórios</h4>
						<PanelHorzSlider>
							<Nav tabs>
								{reports.map((relatorio, index) =>
									<NavItem key={index}>
										<NavLink
											className={classnames({active: activeTab === index})}
											onClick={() => showReport(relatorio, index)}>
											{relatorio.name}
											{isCreator &&
												<div className="rf-menu-handle">
													<i 
														className="la la-caret-down" 
														onClick={(e) => handleShowReportMenu(e, relatorio, index)} />
												</div>
											}
										</NavLink>
									</NavItem>
								)}
							</Nav>
						</PanelHorzSlider>
						<TabContent activeTab={activeTab}>
							{reports.map((report, index) =>
								<TabPane key={index} tabId={index} className="rf-col-reports-tab-pane">
									<Report 
										{...props}
										report={report}
										visible={activeTab === index} />
								</TabPane>
							)}
						</TabContent>
					</div>
				</FocusLock>
			</div>
		</SidebarResizeable>
	);
});

ReportsPanel.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object,
	title: PropTypes.string,
	icon: PropTypes.string,
	compo: PropTypes.string,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default ReportsPanel;