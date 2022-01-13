import React from 'react';
import PropTypes from 'prop-types';
import {useDrag} from 'react-dnd';
import classNames from 'classnames';
import ItemDropZone from './ItemDropZone';
import CollectionUtil from '../../../../../../libs/CollectionUtil';
import {OtherCells, SortCell} from './Item';

const ItemDraggable = (props) =>
{
	const [collected, drag, dragPreview] = useDrag({
		type: 'ITEM',
		item: {
			type: 'ITEM',
			item: props.item,
		}
	});

	const {view, item, style, moved, isEven} = props;
	
	const vstyle = view.style || {};
	const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;
	const border = vstyle.border || {};

	return (
		<div 
			ref={dragPreview} 
			{...collected} 
			style={style}>
			<ItemDropZone 
				item={item} 
				onDropAccepted={(source) => props.container.moveItem(source, item)} 
				className={classNames('rf-col-row rf-col-row-item', {
					[color]: color, 
					[border.color]: border.color,
					[border.style]: border.style,
					'rounded': border.rounded,
					moved: moved,
					'odd': vstyle.striped && !isEven})}>
				<SortCell ref={drag} {...props} draggable />
				<OtherCells 
					{...props}
				/>
			</ItemDropZone>
		</div>
	);
};

ItemDraggable.propTypes = {
	container: PropTypes.object,
	row: PropTypes.object, 
	item: PropTypes.object, 
	height: PropTypes.number, 
	dirty: PropTypes.bool,
	view: PropTypes.object,
	style: PropTypes.object, 
	moved: PropTypes.bool,
	isEven: PropTypes.bool
};

export default ItemDraggable;

//{props.connectDragSource()}