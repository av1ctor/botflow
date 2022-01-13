import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

export default class Group extends PureComponent
{
	static propTypes = 
	{
		title: PropTypes.any.isRequired,
		children: PropTypes.any.isRequired,
		className: PropTypes.any
	};

	render() 
	{
		const {title, children, className} = this.props;

		return (
			<div className={classnames('ribbon-col', 'row-2px-col', className || 'col-sm-6')}>
				<div className="ribbon-group">
					<div className="ribbon-group-content">
						<div className="row row-2px h-100">
							{children}
						</div>
					</div>
					<div className="ribbon-group-title">
						<div>{title}</div>
					</div>
				</div>
			</div>            
		);
	}
}