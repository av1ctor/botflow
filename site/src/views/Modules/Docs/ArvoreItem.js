import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {DragSource} from 'react-dnd';
import DocumentoDropZone from './DocumentoDropZone';

class ArvoreItem extends PureComponent
{
	static propTypes = 
	{
		node: PropTypes.object,
		isDragging: PropTypes.bool,
		connectDragSource: PropTypes.func,
		connectDropTarget: PropTypes.func,
		onMoveFolder: PropTypes.func
	};

	render()
	{
		let node = this.props.node;
		
		return this.props.connectDragSource(
			<div style={{opacity: this.props.isDragging? 0.5: 1}} className="box-file-tree-item">
				<DocumentoDropZone 
					node={node} 
					onDropAccepted={this.props.onMoveFolder}>
					<span className={'p-treenode-icon ' + node.icon}></span>
					<span className="box-file-tree-item-label">
						{node.label}
					</span>
				</DocumentoDropZone>
			</div>);
	}
}

const documentoDndSource = 
{
	beginDrag(props) 
	{
		return props.node;
	}
};

function documentoDnDSourceCollect(connect, monitor) 
{
	return {
		connectDragSource: connect.dragSource(),
		isDragging: monitor.isDragging()
	};
}

export default DragSource('document', documentoDndSource, documentoDnDSourceCollect)(ArvoreItem);

