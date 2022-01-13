import React, {useCallback, useState} from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import FocusLock from 'react-focus-lock';
import {Button, Nav, NavItem, NavLink, TabContent, TabPane, Container, Row, Col} from 'reactstrap';
import SidebarResizeable from '../../../../../../components/Bar/SidebarResizeable';
import PanelHorzSlider from '../../../../../../components/Slider/PanelHorzSlider';
import Misc from '../../../../../../libs/Misc';
import Bar from './Bar';
import BarProperties from './Properties/Panel';

import '../../../../Apps/Bar.css';
import './Editor.css';

const Panel = (props) =>
{
	const [activeTab, setActiveTab] = useState(0);
	const [propsPanelProps, setPropsPanelProps] = useState({visible: false});

	const handleShowProps = (index) =>
	{
		setPropsPanelProps({
			compo: 'general',
			index, 
			visible: true
		});
	};

	const handleCloseProps = useCallback(() =>
	{
		setPropsPanelProps({
			compo: null,
			visible: false
		});
	}, [propsPanelProps]);
	
	const showBar = (index) =>
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

	const handleShowMenu = (event, bar, index) =>
	{
		showPopupMenu(event, buildMenu(bar, index, event.target));
	};

	const handleChange = useCallback(async (id, e) =>
	{
		const name = e.target.name || e.target.id;
		const value = e.target.value;

		const bars = Array.from(props.bars);

		const bar = id? 
			bars.find(f => f.id === id):
			bars[0];

		Misc.setFieldValue(bar, name, value);

		props.onChange(bars);
	}, [props.bars]);

	const createBar = (index, type) =>
	{
		props.container.createBar(index, type);
	};

	const handleCreateFirstBar = () =>
	{
		props.container.createBar(0);
	};

	const deleteBar = (data) =>
	{
		props.container.deleteBar(data.index);
		setActiveTab(activeTab => activeTab === data.index? 
			(data.index > 0? data.index - 1: 0):
			activeTab);
	};

	const handleDeleteBar = (bar, index) =>
	{
		props.container.showUserConfirmation(deleteBar, {bar, index}, 'Delete bar?');
	};	

	const renderBar = (bar, visible, index) =>
	{
		return (
			<Bar 
				key={index}
				api={props.api}
				container={props.container}
				visible={visible}
				app={props.app}
				screens={props.screens}
				bar={bar}
				onChange={(e) => handleChange(bar.id, e)}
			/>
		);
	};

	const buildMenu = (bar, index) =>
	{
		const hideMenu = () => handleHidePopupMenu();
		const bars = props.bars;
		
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
				items: [
					{
						label: 'Top',
						icon: 'la la-caret-up',
						disabled: bars.find(b => b.type === 'top')? true: false,
						command: () => 
						{
							hideMenu();
							createBar(index, 'top');
						}
					},
					/*{
						label: 'Left',
						icon: 'la la-caret-left',
						disabled: bars.find(b => b.type === 'left')? true: false,
						command: () => 
						{
							hideMenu();
							createBar(index, 'left');
						}
					},
					{
						label: 'Right',
						icon: 'la la-caret-right',
						disabled: bars.find(b => b.type === 'right')? true: false,
						command: () => 
						{
							hideMenu();
							createBar(index, 'right');
						}
					},*/
					{
						label: 'Bottom',
						icon: 'la la-caret-down',
						disabled: bars.find(b => b.type === 'bottom')? true: false,
						command: () => 
						{
							hideMenu();
							createBar(index, 'bottom');
						}
					},
				]
			},
			{
				label: 'Delete',
				icon: 'la la-trash',
				command: async () => 
				{
					hideMenu();
					await handleDeleteBar(bar, index);
				}
			},
		];
	};

	if(!props.bars || !props.visible)
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
							<h4><i className="la la-bars" /> Bars</h4>
							{props.bars.length === 0?
								<Container fluid className="rf-panel-tab-pane-empty">
									<Row>
										<Col xs="12" md="3">
											<Button
												color="primary"
												onClick={handleCreateFirstBar}>
												Create first bar
											</Button>
										</Col>
									</Row>
								</Container>:
								<>
									<PanelHorzSlider>
										<Nav tabs>
											{props.bars.map((bar, index) =>
												<NavItem key={index}>
													<NavLink
														className={classnames({active: activeTab === index})}
														onClick={() => showBar(index)}>
														{bar.type}
														<div className="rf-menu-handle">
															<i 
																className="la la-caret-down" 
																onClick={(e) => handleShowMenu(e, bar, index)} />
														</div>
													</NavLink>
												</NavItem>
											)}
										</Nav>
									</PanelHorzSlider>
									<TabContent activeTab={activeTab}>
										{props.bars.map((bar, index) =>
											<TabPane key={index} tabId={index} className="rf-panel-tab-pane">
												{renderBar(bar, activeTab === index, index)}
											</TabPane>
										)}
									</TabContent>
								</>
							}
						</div>
					</FocusLock>
				</div>
			</SidebarResizeable>

			<BarProperties
				{...propsPanelProps}
				app={props.app}
				screens={props.screens}
				bars={props.bars}
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
	bars: PropTypes.array,
	onHide: PropTypes.func.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Panel;