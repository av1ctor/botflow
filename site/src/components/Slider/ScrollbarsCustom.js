import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Scrollbars} from 'react-custom-scrollbars';

import './ScrollbarsCustom.css';

export default class ScrollbarsCustom extends PureComponent
{
	static propTypes = 
	{
		children: PropTypes.any
	};

	trackV = (props) => (
		<div className="track scrollbars-track-vertical" {...props} />
	);
	
	thumbV = (props) => (
		<div className="thumb scrollbars-thumb-vertical" {...props} />
	);
	
	trackH = (props) => (
		<div className="track scrollbars-track-horizontal" {...props} />
	);
	
	thumbH = (props) => (
		<div className="thumb scrollbars-thumb-horizontal" {...props} />
	);

	render()
	{
		const {children, ...props} = this.props;

		return (
			<Scrollbars 
				renderTrackHorizontal={this.trackH}
				renderThumbHorizontal={this.thumbH}
				renderThumbVertical={this.thumbV}
				renderTrackVertical={this.trackV}
				{...props}>
				{children}
			</Scrollbars>
		);
	}
}