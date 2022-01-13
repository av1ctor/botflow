import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Row, Col} from 'reactstrap';
import {DragSource} from 'react-dnd';
import DocumentoDropZone from './DocumentoDropZone';

/** 
 * 
*/
class ListaItemDetalhe_class extends PureComponent
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

	render()
	{
		let node = this.props.node;

		return this.props.connectDragSource(
			<div style={{opacity: this.props.isDragging? 0.5: 1}}>
				<DocumentoDropZone 
					node={node} 
					onDropAccepted={this.props.container.handleOnMoveFile} >
					<Row className={`box-file-list-row ${this.props.selected? 'box-file-list-row-selected': ''}`}>
						<Col sm="3">
							<div className="overflow-hidden" 
								onMouseOver={(e) => node.highlightText && this.props.container.showOverlay(e, node.highlightText)} 
								onMouseOut={() => node.highlightText && this.props.container.hideOverlay()}>
								<span className={'file-icon-tiny ' + node.icon}></span>
								<span><b>{node.name}</b></span>
							</div>
						</Col>
						<Col sm="3" className="overflow-hidden d-none d-sm-block">{node.updatedAt}</Col>
						<Col sm="2" className="overflow-hidden d-none d-sm-block">{node.mimeType}</Col>
						<Col sm="2" className="overflow-hidden d-none d-md-block">{node.size}</Col>
						<Col sm="2" className="overflow-hidden d-none d-lg-block">{node.owner.email}</Col>
					</Row>
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

export const ListaItemDetalhe = DragSource('document', documentoDndSource, documentoDnDSourceCollect)(ListaItemDetalhe_class);

/** 
 * 
*/
export class ListaHeaderDetalhe extends PureComponent
{
	static propTypes = 
	{
		sortBy: PropTypes.string,
		onSortChanged: PropTypes.func
	};

	constructor(props)
	{
		super(props);

		this.state = {
			ascending: true
		};
	}

	handleOnClick(sortBy)
	{
		this.props.onSortChanged(sortBy, !this.state.ascending);
		this.setState({
			ascending: !this.state.ascending
		});
	}
	
	render()
	{
		let sortByColHeader = (size, name, title, klass) =>
		{
			let selected = this.props.sortBy === name;
			return (
				<Col sm={size} onClick={() => this.handleOnClick(name)} className={`box-file-list-header-col ${selected? 'box-file-list-header-col-selected': ''} ${klass}`}>
					<span className="float-left">{title}</span>
					{selected? 
						<span className="float-left"><i className={`la la-caret-${this.state.ascending? 'up': 'down'}`} /></span>:
						null
					}
				</Col>
			);
		};

		return (
			<Row className="p-component box-file-list-header">
				{sortByColHeader(3, 'name', 'Nome', '')}
				{sortByColHeader(3, 'updatedAt', 'Atualizado em', 'd-none d-sm-block')}
				{sortByColHeader(2, 'extensao', 'Tipo', 'd-none d-sm-block')}
				{sortByColHeader(2, 'size', 'Tamanho', 'd-none d-md-block')}
				{sortByColHeader(2, 'owner', 'Dono', 'd-none d-lg-block')}
			</Row>
		);
	}
}
