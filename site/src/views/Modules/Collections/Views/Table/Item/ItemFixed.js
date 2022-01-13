import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import CollectionUtil from '../../../../../../libs/CollectionUtil';
import {OtherCells, SortCell} from './Item';

const ItemFixed = (props) =>
{
	const {view, style, moved, isEven} = props;
	
	const vstyle = view.style || {};
	const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;
	const border = vstyle.border || {};

	return (
		<div style={style}>
			<div 
				className={classNames('rf-col-row rf-col-row-item', {
					[color]: color, 
					moved: moved,
					[border.color]: border.color,
					[border.style]: border.style,
					'rounded': border.rounded,
					'odd': vstyle.striped && !isEven})}>
				<SortCell 
					{...props}
				/>
				<OtherCells
					{...props}
				/>
			</div>
		</div>
	);
};

ItemFixed.propTypes = {
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
	row: PropTypes.object, 
	item: PropTypes.object, 
	height: PropTypes.number, 
	draggable: PropTypes.bool,
	dirty: PropTypes.bool,
	view: PropTypes.object,
	style: PropTypes.object, 
	moved: PropTypes.bool,
	isEven: PropTypes.bool
};


export default ItemFixed;