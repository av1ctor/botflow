import React from 'react';
import PropTypes from 'prop-types';

import './FloatingButtonBar.css';

export default class FloatingButtonBar extends React.PureComponent
{
	static propTypes = {
		children: PropTypes.array,
		width: PropTypes.any,
		height: PropTypes.any,
		visible: PropTypes.bool
	};

	static defaultProps = {
		width: 'fit-content',
		height: 'fit-content',
		visible: true
	}

	render()
	{
		const {width, height, visible, children} = this.props;

		const style = {
			width: width,
			height: height,
			display: visible? 'block': 'none'
		};
		
		return (
			<div 
				className="fbb-bar"
				style={style}>
				<div className="fbb-children">
					{(children? children.constructor === Array? children: [children]: [])
						.map((child, index) => 
							<div key={index} className="fbb-child">
								{child}
							</div>)}
				</div>
			</div>
		);
	}
}