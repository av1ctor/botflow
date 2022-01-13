import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {DropTarget} from 'react-dnd';
import {NativeTypes} from 'react-dnd-html5-backend';

class DropFileZone extends PureComponent
{
	static propTypes = 
	{
		children: PropTypes.element,
		onDropAccepted: PropTypes.func,
		connectDropTarget: PropTypes.func,
	};

	render()
	{
		return this.props.connectDropTarget(
			<div style={{height:'100%', width:'100%'}}>
				{this.props.children}
			</div>
		);
	}
}

const fileDndTarget = 
{
	drop(props, monitor, component) 
	{
		if (monitor.didDrop()) 
		{
			return;
		}

		const source = monitor.getItem();

		props.onDropAccepted(source.files);
	}	
};

function fileDndTargetCollect(connect, monitor) 
{
	return {
		connectDropTarget: connect.dropTarget(),
		isOver: monitor.isOver(),
		canDrop: monitor.canDrop()
	};
}

export default DropTarget(NativeTypes.FILE, fileDndTarget, fileDndTargetCollect)(DropFileZone);