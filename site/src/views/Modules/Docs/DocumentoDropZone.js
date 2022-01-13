import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {DropTarget} from 'react-dnd';

class DocumentoDropZone extends PureComponent
{
	static propTypes = 
	{
		node: PropTypes.any,
		children: PropTypes.oneOfType([PropTypes.element, PropTypes.arrayOf(PropTypes.element)]),
		onDropAccepted: PropTypes.func,
		connectDropTarget: PropTypes.func,
	};

	render()
	{
		// eslint-disable-next-line
		const {className, style, height, width} = this.props;
		
		return this.props.connectDropTarget(
			<div className={className} style={style} height={height} width={width}>
				{this.props.children}
			</div>
		);
	}
}

const documentoDndTarget = 
{
	canDrop(props, monitor) 
	{
		let source = monitor.getItem();
		let destine = props.node.constructor === Function? props.node.call(): props.node;

		return destine.type === 'DIRETORIO'? 
			source.id !== destine.id:
			destine.parent.id !== (source.parent? source.parent.id: 0);
	},

	drop(props, monitor, component) 
	{
		if (monitor.didDrop()) 
		{
			return;
		}
	
		let source = monitor.getItem();
		let destine = props.node.constructor === Function? props.node.call(): props.node;

		if(destine.type !== 'DIRETORIO')
		{
			destine = destine.parent;
		}
	
		props.onDropAccepted(source, destine);
	}	
}

function documentoDndTargetCollect(connect, monitor) 
{
	return {
		connectDropTarget: connect.dropTarget(),
		isOver: monitor.isOver(),
		isOverCurrent: monitor.isOver({ shallow: true }),
		canDrop: monitor.canDrop(),
		itemType: monitor.getItemType()
	};
}

export default DropTarget('document', documentoDndTarget, documentoDndTargetCollect)(DocumentoDropZone);