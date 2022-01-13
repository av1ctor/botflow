import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

export default class Item extends PureComponent 
{
	static propTypes = 
	{
		children: PropTypes.any.isRequired,
		className: PropTypes.any
	};
	
	render() 
	{
		return (
			<div className={classnames('ribbon-item', 'row-2px-col', this.props.className || 'col-6')}>
				{this.props.children}
			</div>
		);
	}
}