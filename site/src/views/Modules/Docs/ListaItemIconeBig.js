import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {DragSource} from 'react-dnd';
import DocumentoDropZone from './DocumentoDropZone';

/** 
 * 
*/
class ListaItemIconeBig_class extends PureComponent
{
	static propTypes = 
	{
		container: PropTypes.object.isRequired,
		node: PropTypes.object,
		selected: PropTypes.bool,
		filesSelected: PropTypes.any,
		isDragging: PropTypes.bool,
		connectDragSource: PropTypes.func,
		connectDropTarget: PropTypes.func
	};

	thumb(node)
	{
		if(!node.thumbUrl || node.thumbUrl.length === 0)
		{
			return <span className={'file-icon-hyper ' + node.icon}></span>;
		}
		else
		{
			return <img className="box-file-list-bigicon-img" alt="thumbnail" src={node.thumbUrl} />
		}
	}

	render()
	{
		let node = this.props.node;
		
		return this.props.connectDragSource(
			<div 
				style={{opacity: this.props.isDragging? 0.5: 1}} 
				className={this.props.selected? 'box-file-list-bigicon-col-selected' : 'box-file-list-bigicon-col'}
				data-tip={node.highlightText} data-for={'tip_'+node.id}>
				<DocumentoDropZone 
					node={node} 
					onDropAccepted={this.props.container.handleOnMoveFile}
					className="col justify-content-center">
					<div className="justify-content-center"
						onMouseOver={(e) => node.highlightText && this.props.container.showOverlay(e, node.highlightText)} 
						onMouseOut={() => node.highlightText && this.props.container.hideOverlay()}>
						{this.thumb(node)}
					</div>
					<div className="justify-content-center">
						<b>{node.name}</b>
					</div>
				</DocumentoDropZone>
			</div>);
	}
}

const documentoDndSource = 
{
	beginDrag(props) 
	{
		return props.node;
	},

	canDrag(props)
	{
		return !props.node.isSystem;
	}
};

function documentoDnDSourceCollect(connect, monitor) 
{
	return {
		connectDragSource: connect.dragSource(),
		isDragging: monitor.isDragging()
	};
}

export const ListaItemIconeBig = DragSource('document', documentoDndSource, documentoDnDSourceCollect)(ListaItemIconeBig_class);

/** 
 * 
*/
export class ListaHeaderIconeBig extends PureComponent
{
	render()
	{
		return null;
	}
}
