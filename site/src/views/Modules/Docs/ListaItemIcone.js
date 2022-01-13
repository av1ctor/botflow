import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Row, Col} from 'reactstrap';
import {DragSource} from 'react-dnd';
import DocumentoDropZone from './DocumentoDropZone';

/** 
 * 
*/
class ListaItemIcone_class extends PureComponent
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
			return <span className={'file-icon-big ' + node.icon}></span>;
		}
		else
		{
			return <img style={{maxHeight:80, maxWidth:80}} alt="thumbnail" src={node.thumbUrl} />
		}
	}

	render()
	{
		let node = this.props.node;
		
		return this.props.connectDragSource(
			<div style={{opacity: this.props.isDragging? 0.5: 1}}>
				<DocumentoDropZone 
					node={node} 
					onDropAccepted={this.props.container.handleOnMoveFile}
					className={`row box-file-list-row ${this.props.selected? 'box-file-list-row-selected': ''} justify-content-center`}>
					<Col md="2">
						<div className="justify-content-center" style={{textAlign: 'center'}}
							onMouseOver={(e) => node.highlightText && this.props.container.showOverlay(e, node.highlightText)} 
							onMouseOut={() => node.highlightText && this.props.container.hideOverlay()}>
							{this.thumb(node)}
						</div>
					</Col>
					<Col md="10">
						<Row>
							<Col md="6">Nome:</Col>
							<Col><b>{node.name}</b></Col>
				
							<Col md="6">Dono:</Col>
							<Col>{node.owner.email}</Col>
				
							<Col md="6">Criado em:</Col>
							<Col>{node.createdAt}</Col>
				
							<Col md="6">Atualizado em:</Col>
							<Col>{node.updatedAt}</Col>
						</Row>
					</Col>
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

export const ListaItemIcone = DragSource('document', documentoDndSource, documentoDnDSourceCollect)(ListaItemIcone_class);

/** 
 * 
*/
export class ListaHeaderIcone extends PureComponent
{
	render()
	{
		return null;
	}
}
