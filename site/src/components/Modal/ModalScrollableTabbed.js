import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import {TabContent, TabPane, Nav, NavItem, NavLink, Badge} from 'reactstrap';
import {Scrollbars} from 'react-custom-scrollbars';
import Modal from './Modal';
import PanelHorzSlider from '../PanelHorzSlider';

import './ModalScrollableTabbed.css';

const ModalScrollableTabbed = (props) =>
{
	const [activeTab, setActiveTab] = useState(props.activeTab);
	const [tabs, setTabs] = useState(props.tabs);
	const [inited, setInited] = useState(false);

	useEffect(() =>
	{
		setTabs(props.tabs);
	}, [props.tabs]);

	const changeTab = (index) =>
	{
		setInited(true);
		setActiveTab(index);
	};

	const setBadge = (index, value) =>
	{
		setTabs(tabs.map((tab, i) => 
			i !== index? 
				tab: 
				{
					...tab, 
					badge: value
				})
		);
	};

	const isTabDisabled = (tab) =>
	{
		if(!tab.disabled)
		{
			return false;
		}

		if(tab.disabled.constructor === Function)
		{
			return tab.disabled(props.childProps);
		}

		return true;
	};

	const renderTabCompo = (tab, index) => 
	{
		if(isTabDisabled(tab))
		{
			return null;
		}

		const {compo: C} = tab;

		return C? 
			<C 
				{...props.childProps} 
				visible={activeTab === index} 
				setBadge={(value) => setBadge(index, value)} />: 
			null;
	};

	return (
		<Modal 
			isOpen={props.isOpen} 
			onHide={props.onHide} 
			className="tab-pane-modal">
			<div className="rf-col-props-body">
				{!props.showInitGrid || inited?
					<>
						<PanelHorzSlider className="rf-col-props-body-nav">
							<Nav tabs>
								{tabs.map((tab, index) => 
									<NavItem key={tab.title}>
										<NavLink
											className={classnames({active: activeTab === index})}
											onClick={() => changeTab(index)}
											disabled={isTabDisabled(tab)}>
											<div className="rf-col-props-tab">
												<div className="rf-col-props-icon"><i className={`la la-${tab.icon}`} /></div>
												<div className="rf-col-props-name">{tab.title}</div>
												{!(activeTab === index) && tab.badge && <Badge pill color="info" className="rf-col-badge">
													{tab.badge}
												</Badge>}
											</div>
										</NavLink>
									</NavItem>
								)}
							</Nav>
						</PanelHorzSlider>		
						<div className="rf-col-props-body-content">
							<TabContent activeTab={activeTab} className="rf-col-tab-contents">
								{tabs.map((tab, index) => 
									<TabPane key={index} tabId={index} className="rf-col-props-container">
										<Scrollbars>
											{renderTabCompo(tab, index)}
										</Scrollbars>
									</TabPane>)}
							</TabContent>
						</div>
					</>:
					<div className="tabspanel-grid-row">
						{tabs.map((tab, index) => 
							<div 
								key={index} 
								className="tabspanel-grid-col" 
								onClick={() => !isTabDisabled(tab)? changeTab(index): null}>
								<div>
									<div><h1><i className={`la la-${tab.icon}`} /></h1></div>
									<div>{tab.title}</div>
								</div>
							</div>
						)}
					</div>
				}
			</div>
		</Modal>
	);
};

ModalScrollableTabbed.propTypes = 
{
	isOpen: PropTypes.bool.isRequired,
	title: PropTypes.string.isRequired,
	icon: PropTypes.string.isRequired,
	tabs: PropTypes.array.isRequired,
	activeTab: PropTypes.number,
	showInitGrid: PropTypes.bool,
	childProps: PropTypes.object,
	onHide: PropTypes.func.isRequired
};

ModalScrollableTabbed.defaultProps = 
{
	activeTab: 0,
	showInitGrid: false,
	childProps: {}
};

export default ModalScrollableTabbed;