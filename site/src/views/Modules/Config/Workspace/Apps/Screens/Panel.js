import React, {useCallback, useState} from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import FocusLock from 'react-focus-lock';
import {Nav, NavItem, NavLink, TabContent, TabPane, Button, Row, Col, Container} from 'reactstrap';
import SidebarResizeable from '../../../../../../components/Bar/SidebarResizeable';
import PanelHorzSlider from '../../../../../../components/Slider/PanelHorzSlider';
import Misc from '../../../../../../libs/Misc';
import Screen from './Screen';
import ScreenProperties from './Properties/Panel';

import '../../../../Apps/Screen.css';
import './Editor.css';

const Panel = (props) =>
{
	const [activeTab, setActiveTab] = useState(0);
	const [propsPanelProps, setPropsPanelProps] = useState({visible: false});

	const handleShowProps = (index) =>
	{
		setPropsPanelProps({
			compo: 'general',
			app: props.app,
			screens: props.screens, 
			index, 
			visible: true
		});
	};

	const handleCloseProps = useCallback(() =>
	{
		setPropsPanelProps({
			compo: null,
			app: null,
			screens: null,
			visible: false
		});
	}, [propsPanelProps]);
	
	const showScreen = (index) =>
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

	const handleShowMenu = (event, screen, index) =>
	{
		showPopupMenu(event, buildMenu(screen, index, event.target));
	};

	const handleChange = useCallback(async (id, e) =>
	{
		const name = e.target.name || e.target.id;
		const value = e.target.value;

		const screens = Array.from(props.screens);

		const screen = id? 
			screens.find(f => f.id === id):
			screens[0];

		Misc.setFieldValue(screen, name, value);

		props.onChange(screens);
	}, [props.screens]);

	const createScreen = (index) =>
	{
		props.container.createScreen(index);
	};

	const handleCreateFirstScreen = () =>
	{
		props.container.createScreen(0);
	};

	const duplicateScreen = (index) =>
	{
		props.container.duplicateScreen(index);
		setActiveTab(props.screens.length);
	};

	const deleteScreen = (data) =>
	{
		props.container.deleteScreen(data.index);
		setActiveTab(activeTab => activeTab === data.index? 
			(data.index > 0? data.index - 1: 0):
			activeTab);
	};

	const handleDeleteScreen = (screen, index) =>
	{
		props.container.showUserConfirmation(deleteScreen, {screen, index}, 'Delete screen?');
	};	

	const moveScreen = (from, to) =>
	{
		props.container.moveScreen(from, to);
		setActiveTab(to);
	};

	const renderScreen = (screen, visible, index) =>
	{
		return (
			<Screen 
				key={index}
				api={props.api}
				container={props.container}
				visible={visible}
				app={props.app}
				screen={screen}
				onChange={(e) => handleChange(screen.id, e)}
			/>
		);
	};

	const buildMenu = (screen, index) =>
	{
		const hideMenu = () => handleHidePopupMenu();
		const screens = props.screens;
		
		return [
			{
				label: 'Properties',
				icon: 'la la-cog',
				command: () => 
				{
					hideMenu();
					handleShowProps(index);
				}
			},
			{
				separator: true
			},
			{
				label: 'Create',
				icon: 'la la-plus-circle',
				command: async () => 
				{
					hideMenu();
					await createScreen(index);
				}
			},
			{
				label: 'Duplicate',
				icon: 'la la-copy',
				command: async () => 
				{
					hideMenu();
					await duplicateScreen(index);
				}
			},
			{
				label: 'Delete',
				icon: 'la la-trash',
				command: async () => 
				{
					hideMenu();
					await handleDeleteScreen(screen, index);
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
						label: 'Begin',
						icon: 'la la-angle-double-left',
						disabled: index === 0,
						command: async () => 
						{
							hideMenu();
							await moveScreen(index, 0);
						}
					},
					{
						label: 'Left',
						icon: 'la la-angle-left',
						disabled: index === 0,
						command: async () => 
						{
							hideMenu();
							await moveScreen(index, index-1);
						}
					},
					{
						label: 'Right',
						icon: 'la la-angle-right',
						disabled: index === screens.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveScreen(index, index+1);
						}
					},
					{
						label: 'Final',
						icon: 'la la-angle-double-right',
						disabled: index === screens.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveScreen(index, screens.length - 1);
						}
					}				
				]
			},
		];
	};

	if(!props.screens || !props.visible)
	{
		return null;
	}
	
	return (
		<>
			<SidebarResizeable 
				visible={props.visible} 
				position="right" 
				onHide={props.onHide} 
				fullScreen={props.fullScreen}>
				<div className="w-100 h-100">
					<FocusLock disabled={!props.visible} returnFocus className="rf-flex-100">
						<div className="rf-panel-tabs w-100">
							<h4><i className="la la-mobile-alt" /> Screens</h4>
							{props.screens.length === 0?
								<Container fluid className="rf-panel-tab-pane-empty">
									<Row>
										<Col xs="12" md="3">
											<Button
												color="primary"
												onClick={handleCreateFirstScreen}>
												Create first screen
											</Button>
										</Col>
									</Row>
								</Container>:
								<>
									<PanelHorzSlider>
										<Nav tabs>
											{props.screens.map((screen, index) =>
												<NavItem key={index}>
													<NavLink
														className={classnames({active: activeTab === index})}
														onClick={() => showScreen(index)}>
														{screen.title}
														<div className="rf-menu-handle">
															<i 
																className="la la-caret-down" 
																onClick={(e) => handleShowMenu(e, screen, index)} />
														</div>

													</NavLink>
												</NavItem>
											)}
										</Nav>
									</PanelHorzSlider>
									<TabContent activeTab={activeTab}>
										{props.screens.map((screen, index) =>
											<TabPane key={index} tabId={index} className="rf-panel-tab-pane">
												{renderScreen(screen, activeTab === index, index)}
											</TabPane>
										)}
									</TabContent>
								</>
							}
						</div>
					</FocusLock>
				</div>
			</SidebarResizeable>

			<ScreenProperties
				{...propsPanelProps}
				onHide={handleCloseProps}
				onChange={props.onChange}
			/>
		</>
	);
};

Panel.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
	fullScreen: PropTypes.bool,
	app: PropTypes.object,
	screens: PropTypes.array,
	onHide: PropTypes.func.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Panel;