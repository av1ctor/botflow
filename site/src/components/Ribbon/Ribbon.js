import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import {TabContent, TabPane, Nav, NavItem, NavLink} from 'reactstrap';
import PanelHorzSlider from '../PanelHorzSlider';

import './Ribbon.css';

//adapted from https://github.com/lgkonline/
export default class Ribbon extends PureComponent
{
	static propTypes = 
	{
		children: PropTypes.any.isRequired
	};

	constructor() 
	{
		super();

		this.state = {
			mobileCurrentGroup: 0,
		};
	}

	handleChange(index) 
	{
		this.setState({
			mobileCurrentGroup: (index|0)
		});
	}

	render() 
	{
		const children = Array.isArray(this.props.children)? 
			this.props.children:
			[this.props.children];

		const activeTab = this.state.mobileCurrentGroup;

		return (
			<div>
				<div className={classnames('mobile-ribbon-container d-md-none')}>
					<div className="rf-col-props-body"> 
						<PanelHorzSlider className="rf-col-props-body-nav">
							<Nav tabs>
								{children.map((tab, index) =>
									<NavItem key={index}>
										<NavLink
											className={classnames({active: activeTab === index})}
											onClick={() => this.handleChange(index)}>
											<div className="rf-col-props-tab">
												<div className="rf-col-props-name">{tab.props.title}</div>
											</div>
										</NavLink>
									</NavItem>
								)}
							</Nav>
						</PanelHorzSlider>
					</div>

					<div className="rf-col-props-body-content pt-2 pl-1 pr-1">
						<TabContent activeTab={activeTab} className="w-100 h-100">
							{children.map((tab, index) => 
								<TabPane key={index} tabId={index} className="rf-col-props-container">
									<div className="row row-2px h-100">
										{tab.props.children}
									</div>
								</TabPane>
							)}
						</TabContent>
					</div>
				</div>

				<div className="d-none d-md-block">
					<div className="ribbon">
						<div className="row row-2px" style={{flex: '0 0 auto', flexWrap: 'nowrap'}}>
							{this.props.children}
						</div>
					</div>
				</div>
			</div>
		);
	}
}
