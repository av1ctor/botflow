import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {useDrop, useDrag} from 'react-dnd';
import OrgChart from './OrgChart';

// eslint-disable-next-line react/prop-types
const DraggableNode = ({node, onMove, className, children}) =>
{
	const [{isDragging}, drag] = useDrag({
		item: {
			node, 
			type: 'node'
		},
		end: (item, monitor) => 
		{
			const dest = monitor.getDropResult();
			if (item && dest) 
			{
				onMove(node, dest);
			}
		},
		collect: (monitor) => ({
			isDragging: monitor.isDragging(),
		}),
	});

	const style = {
		opacity: isDragging ? 0.4 : 1
	};

	return (
		<div ref={drag} className={className} style={style}>
			{children}
		</div>
	);
};

// eslint-disable-next-line react/prop-types
const DroppableNode = ({node, children}) =>
{
	const isSelf = (other) => node.id === other.id;
	const isChild = (children) => 
	{
		if(!children)
		{
			return false;
		}

		return !!children.find(child => 
			child.id === node.id || 
			isChild(child.children));
	};

	const [, drop] = useDrop({
		accept: 'node',
		drop: () => node,
		canDrop: (item) => !isSelf(item.node) && 
			!isChild(item.node.children)
	});
 
	return (
		<div ref={drop}>
			{children}
		</div>
	);	
};

export default class OrgChartDraggable extends PureComponent
{
	static propTypes = {
		value: PropTypes.array.isRequired,
		onMove: PropTypes.func.isRequired,
		nodeTemplate: PropTypes.func,
		className: PropTypes.string
	};

	renderNode = (node) =>
	{
		return (
			<DraggableNode 
				className={this.props.className}
				node={node} 
				onMove={this.props.onMove}>
				<DroppableNode node={node}>
					{this.props.nodeTemplate(node)}
				</DroppableNode>
			</DraggableNode>
		);
	}

	render()
	{
		return (
			<OrgChart
				value={this.props.value}
				nodeTemplate={this.renderNode}
			/>
		);
	}
}