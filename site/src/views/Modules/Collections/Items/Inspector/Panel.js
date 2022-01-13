import React, {useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import {TabContent, TabPane, Nav, NavItem, NavLink} from 'reactstrap';
import classnames from 'classnames';
import SidebarResizeable from '../../../../../components/Bar/SidebarResizeable';
import PanelHorzSlider from '../../../../../components/Slider/PanelHorzSlider';
import Topics from './Topics';
import Docs from './Docs';
import Logs from './Logs';
import Flows from './Flows';
import View from './View';

const defaultTabs = [
	{name: 'View', icon: 'binoculars', compo: View},
	{name: 'Flows', icon: 'project-diagram', compo: Flows},
	{name: 'Documents', icon: 'copy', compo: Docs},
	{name: 'Topics', icon: 'comments', compo: Topics},
	{name: 'Logs', icon: 'pen-square', compo: Logs}
];

const InspectorPanel = observer(props =>
{
	const store = props.store;
	
	const [activeTab, setActiveTab] = useState(0);
	const [resizeEvent, setResizeEvent] = useState({});

	const changeTab = (index) =>
	{
		setActiveTab(index);
	};

	const onResize = (event) =>
	{
		setResizeEvent(Object.assign({}, event));
	};

	if(!props.visible || !props.item)
	{
		return null;
	}

	const form = (store.collection.schemaObj.forms || []).find(f => f.use === 'view');
	const tabs = form? 
		defaultTabs:
		defaultTabs.slice(1);

	return (
		<SidebarResizeable 
			visible={props.visible || false} 
			position="right" 
			onHide={props.onHide} 
			onResize={onResize}
			fullScreen={props.fullScreen}>
			<div className="w-100 h-100">
				<FocusLock 
					disabled={!props.visible} 
					returnFocus 
					className="rf-flex-100">
					<div className="rf-col-details-tabs w-100">
						<h4>Inspector</h4>
						<PanelHorzSlider>
							<Nav tabs>
								{tabs.map(({name, icon}, index) =>
									<NavItem key={index}>
										<NavLink
											className={classnames({active: activeTab === index})}
											onClick={() => changeTab(index)}>
											<div className="rf-col-tab-link">
												<div className="rf-col-tab-icon"><i className={`la la-${icon}`} /></div>
												<div className="rf-col-tab-name">{name}</div>
											</div>
										</NavLink>
									</NavItem>
								)}
							</Nav>
						</PanelHorzSlider>
						<TabContent activeTab={activeTab}>
							{tabs.map(({compo: C}, index) =>
								<TabPane 
									key={index} 
									tabId={index} 
									className="rf-col-details-tab-pane">
									<C 
										{...props}
										visible={activeTab === index}
										resizeEvent={resizeEvent} />
								</TabPane>
							)}
						</TabContent>
					</div>
				</FocusLock>
			</div>
		</SidebarResizeable>
	);
});

InspectorPanel.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default InspectorPanel;

