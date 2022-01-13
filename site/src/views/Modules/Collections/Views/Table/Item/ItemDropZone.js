import React, { useCallback } from 'react';
import PropTypes from 'prop-types';
import {useDrop} from 'react-dnd';
import CollectionUtil from '../../../../../../libs/CollectionUtil';

const ItemDropZone = (props) =>
{
	const onDrop = useCallback((item, monitor) =>
	{
		const source = item.item;
		const destine = props.item;
	
		props.onDropAccepted(source, destine);
	}, []);

	const canDrop = (item, monitor) =>
	{
		const source = item.item;
		const destine = props.item;
	
		const id = CollectionUtil.ID_NAME;
	
		return source[id] !== destine[id];
	};
	
	const [collectedProps, drop] = useDrop({
		accept: 'ITEM',
		drop: onDrop,
		canDrop: canDrop
	});
	
	const {className, style, height, width} = props;
	
	return (
		<div 
			ref={drop} 
			{...collectedProps}
			className={className} 
			style={style} 
			height={height} 
			width={width}
		> 
			{props.children}
		</div>
	);	
};

ItemDropZone.propTypes = 
{
	className: PropTypes.string,
	style: PropTypes.any,
	width: PropTypes.any,
	height: PropTypes.any,
	item: PropTypes.object.isRequired,
	onDropAccepted: PropTypes.func.isRequired,
	children: PropTypes.any,
};

export default ItemDropZone;