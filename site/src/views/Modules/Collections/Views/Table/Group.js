import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import CollectionUtil from '../../../../../libs/CollectionUtil';

const Group = (props) =>
{
	const {group} = props;
	
	const colStyle = 
	{
		width: props.width,
		height: CollectionUtil.GROUP_HEIGHT + 'px',
		lineHeight: CollectionUtil.GROUP_HEIGHT + 'px',
		left: 0
	};

	const vstyle = props.view.style || {};
	const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;
	const border = vstyle.border || {};

	return (
		<div style={props.style}>
			<div className={classNames(
				'rf-col-row rf-col-row-item',
				{
					[color]: color,
					[border.color]: border.color,
					[border.style]: border.style,
					'rounded': border.rounded,
				}
			)}>
				<div 
					className="rf-col-cell rf-col-cell-group frozen"
					style={colStyle} 
					onClick={() => props.container.toggleGroup(props.row)}>
					<i className={`la la-caret-${!group.expanded? 'right': 'down'}`}></i> {group.name}
				</div>
			</div>
		</div>
	);
};

Group.propTypes = {
	container: PropTypes.object,
	view: PropTypes.object,
	group: PropTypes.object,
	row: PropTypes.object,
	style: PropTypes.object,
	width: PropTypes.any,
};

export default Group;