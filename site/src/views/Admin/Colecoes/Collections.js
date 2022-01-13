import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {withRouter} from 'react-router-dom';
import {Col, Row, Label, Input, Button, CustomInput} from 'reactstrap';
import BaseList from '../../../components/BaseList';
import BaseForm from '../../../components/BaseForm';
import CollectionInsertPanel from './CollectionInsertPanel';
import CollectionEditPanel from './CollectionEditPanel';
import JsonEditor from '../../../components/JsonEditor';
import Misc from '../../../libs/Misc';

import './Collections.css';

class Collections extends Component
{
	static propTypes = 
	{
		api: PropTypes.object,
		history: PropTypes.object,
		isMobile: PropTypes.bool,
		container: PropTypes.object
	};

	constructor(props)
	{
		super(props);

		this.isMobile = props.isMobile;

		this.renderItem = this.renderItem.bind(this);
		this.renderFields = this.renderFields.bind(this);
		this.renderActions = this.renderActions.bind(this);
		this.handleTruncate = this.handleTruncate.bind(this);
		this.handleInsert = this.handleInsert.bind(this);
		this.truncateItems = this.truncateItems.bind(this);
		this.handleEdit = this.handleEdit.bind(this);
		this.handlePublish = this.handlePublish.bind(this);
		this.handleUnpublish = this.handleUnpublish.bind(this);
		this.transformForEditing = this.transformForEditing.bind(this);
		this.transformForSubmitting = this.transformForSubmitting.bind(this);
	}

	handleTruncate(item)
	{
		this.baseList.showUserConfirmation(this.truncateItems, item, 'Tem certeza que deseja apagar definitivamente todos os itens dessa coleção?');
	}

	async truncateItems(item)
	{
		var res = await this.props.api.del(`collections/${item.id}/items/all`);

		if(res.errors !== null)
		{
			this.baseList.showMessage(res.errors, 'error');
			return;
		}

		this.baseList.showMessage('Todos os itens da coleção foram apagados', 'success');
	}

	handleInsert(item)
	{
		this.insert.show(item);
	}

	handleEdit(item)
	{
		this.edit.show(item);
	}

	async handlePublish(item)
	{
		let res = await this.props.api.patch(`collections/${item.id}/pub`);

		if(res.errors !== null)
		{
			this.baseList.showMessage(res.errors, 'error');
			return;
		}
		
		this.baseList.updateItem(item, {publishedAt: res.data.publishedAt, publishedBy: res.data.publishedBy});

		this.baseList.showMessage('Coleção publicada!', 'success');
	}

	async handleUnpublish(item)
	{
		let res = await this.props.api.patch(`collections/${item.id}/unpub`);

		if(res.errors !== null)
		{
			this.baseList.showMessage(res.errors, 'error');
			return;
		}
		
		this.baseList.updateItem(item, {publishedAt: res.data.publishedAt, publishedBy: res.data.publishedBy});

		this.baseList.showMessage('Coleção despublicada.', 'success');
	}

	validateFields(item, errors)
	{
		if(!item.schema || item.schema.length === 0)
		{
			errors.push('O campo Schema deve ser definido');
			return false;
		}

		return true;
	}

	transformForEditing(item, isUpdate)
	{
		if(isUpdate)
		{
			item.schema = Misc.patch(
				item.schema, 
				this.baseList.findItemById(item.id).schema
			);
		}

		item.schemaObj = JSON.parse(item.schema);
		return item;
	}

	transformForSubmitting(item)
	{
		item = Object.assign({}, item);

		delete item.schemaObj;
		
		if(item.id)
		{
			item.schema = Misc.diff(this.baseList.findItemById(item.id).schema, item.schema);
		}

		return item;
	}

	renderFields(item, handleOnChange)
	{
		return (
			<>
				<Row>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Id</Label>
						<Input disabled={true} required name="" var-type="string" type="text" value={item.id? item.id: ''} readOnly />
					</Col>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Nome</Label>
						<Input required name="name" var-type="string" type="text" value={item.name || ''} onChange={handleOnChange} />
					</Col>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Descrição</Label>
						<Input required name="desc" var-type="string" type="textarea" rows="1" value={item.desc || ''} onChange={handleOnChange} />
					</Col>
				</Row>
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Schema</Label>
						<JsonEditor name="schema" value={item.schema || ''} handleOnChange={handleOnChange} />
					</Col>
				</Row>
				<Row>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Menu ícone</Label>
						<Input name="icon" var-type="string" type="text" value={item.icon || ''} onChange={handleOnChange} />
					</Col>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Menu order</Label>
						<Input name="order" var-type="number" type="number" value={item.order || '0'} onChange={handleOnChange} />
					</Col>
				</Row>
				<Row>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Created at</Label>
						<Input disabled={true} required name="" var-type="datetime" type="text" value={item.createdAt || ''} readOnly />
					</Col>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Created by</Label>
						<Input disabled={true} required name="" var-type="email" type="email" value={item.createdBy? item.createdBy.email: ''} readOnly />
					</Col>
				</Row>
				<Row>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Updated at</Label>
						<Input disabled={true} required name="" var-type="datetime" type="text" value={item.updatedAt || ''} readOnly />
					</Col>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Updated by</Label>
						<Input disabled={true} required name="" var-type="email" type="email" value={item.updatedBy? item.updatedBy.email: ''} readOnly />
					</Col>
					<Col sm="12" md="6" lg="4">
						<CustomInput type="checkbox" label="Editável" id={`editable${item.id}`} checked={item.editable} onChange={e => handleOnChange(e, 'editable')} />
					</Col>
				</Row>
				<Row>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Data publicação</Label>
						<Input disabled={true} name="" var-type="datetime" type="text" value={item.publishedAt || ''} readOnly />
					</Col>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Publicado por</Label>
						<Input disabled={true} name="" var-type="email" type="email" value={item.publishedBy? item.publishedBy.email: ''} readOnly />
					</Col>
				</Row>
			</>
		);
	}

	renderItem(item, isFirstRow)
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="4" md="3">
						<b>{item.name}</b>
					</Col>
					<Col md="3" className="d-none d-md-block">
						{item.createdBy.email}
					</Col>
					<Col xs="4" md="3">
						<span className={`badge badge-pill badge-${item.editable? 'success': 'secondary'}`}>{item.editable? 'Sim': 'Não'}</span>
					</Col>
					<Col md="2" className="d-none d-md-block">
						{item.createdAt}
					</Col>
					<Col xs="4" md="1">
						<span className={`badge badge-pill badge-${item.publishedAt? 'success': 'secondary'}`}>{item.publishedAt? 'Sim': 'Não'}</span>
					</Col>
				</>
			);
		}

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={item.id? item.id: 'empty-item-form'} 
							api={this.props.api}
							history={this.props.history}
							parent={this.baseList}
							controller={'collections'}
							item={item} 
							enabled
							onLoad={this.transformForEditing}
							render={this.renderFields} 
							validate={this.validateFields} 
							onSubmit={this.transformForSubmitting}
						/>
					</Col>
				</Row>
				{item.id &&
					<>
						<Row>
							<Col xs="12">
								<hr />
							</Col>
						</Row>
						<Row>
							<Col xs="12" md="2">
								Ações:
							</Col>
							<Col xs="12" md="2">
								<Button color="primary" block className="m-1" onClick={() => this.handleInsert(item)}>
									Inserir itens
								</Button>
							</Col>
							<Col xs="12" md="2">
								<Button color="secondary" block className="m-1" onClick={() => this.handleEdit(item)}>
									Editar itens
								</Button>
							</Col>
							<Col xs="8" md="2">
								<Button color="primary" block className="m-1"onClick={() => item.publishedAt !== null? this.handleUnpublish(item): this.handlePublish(item)}>
									{item.publishedAt !== null? 'Despublicar': 'Publicar'}
								</Button>
							</Col>
							<Col xs="12" md="2">
								<Button color="danger" block className="m-1" onClick={() => this.handleTruncate(item)}>
									Apagar itens
								</Button>
							</Col>
						</Row>
					</>
				}
			</Col>
		);		
	}

	renderActions(item)
	{
		let isPublished = item.publishedAt !== null;

		return (
			<>
				<Button 
					size="xs" outline className="base-list-button" 
					onClick={() => isPublished? this.handleUnpublish(item): this.handlePublish(item)} 
					title={isPublished? 'Despublicar': 'Publicar'}>
					<i className={`base-list-button-add-icon la la-${!isPublished? 'bullhorn': 'undo'}`}></i>
				</Button>
				<Button size="xs" outline className="base-list-button" onClick={() => this.handleEdit(item)} title="Editar itens">
					<i className="base-list-button-add-icon la la-edit"></i>
				</Button>
				<Button size="xs" outline className="base-list-button" onClick={() => this.handleInsert(item)} title="Inserir itens">
					<i className="base-list-button-add-icon la la-list-ol"></i>
				</Button>
			</>
		);
	}


	getFilters()
	{
		return [
			{name: 'pubId', title: 'Id', type: 'text'},
			{name: 'name', title: 'Nome', type: 'text'},
			{name: 'createdBy', title: 'Dono', type: 'text'},
		];
	}

	getHeader()
	{
		return [
			{name: 'name', title: 'Nome', size: {xs:4, md:3}, klass: ''},
			{name: null, title: 'Dono', size: {md:3}, klass: 'd-none d-md-block'},
			{name: null, title: 'Editável', size: {xs:4, md:3}, klass: ''},
			{name: 'createdAt', title: 'Created at', size: {md:2}, klass: 'd-none d-md-block'},
			{name: null, title: 'Publicada', size: {xs:4, md:1}, klass: ''},
		];
	}

	getItemTemplate()
	{
		return {
			id: null,
			name: '',
			desc: null,
			schema: '',
			editable: false
		};
	}

	handleItemsLoaded(items) 
	{
		return items.map(item => this.transformForEditing(item, false));
	}

	render()
	{
		return (
			<>
				<div className="rf-page-header pt-2">
					<div>
						<div className="rf-page-title-icon">
							<i className="la la-cog" />
						</div>
						<div className="rf-page-title-other">
							<div className="row">
								<div className="col-auto rf-page-title-container">
									<div className="rf-page-title">
									Administração / Coleções
									</div>
								</div>
							</div>
							<div className="row">
								<div className="col">
									<small>Relação de coleções</small>
								</div>
							</div>
						</div>
					</div>
				</div>

				<BaseList
					ref={(el) => this.baseList = el}
					{...this.props}					
					title="Collections"
					controller={`collections/${this.props.api.getWorkspace().idRootCol}/children`}
					sortBy='name'
					itemTemplate={this.getItemTemplate()}
					getFilters={this.getFilters}
					getHeader={this.getHeader}
					onLoad={this.handleItemsLoaded}
					renderItem={this.renderItem}
					renderActions={this.renderActions}
				/>
			
				<div className="collections-insert-container">
					<CollectionInsertPanel
						ref={el => this.insert = el}
						showMessage={(...args) => this.baseList.showMessage(...args)}
						{...this.props}
						fullScreen={this.isMobile} />
				</div>

				<div className="colecoes-edit-container">
					<CollectionEditPanel
						ref={el => this.edit = el}
						{...this.props}
						fullScreen={this.isMobile} />
				</div>
			</>
		);
	}
}

export default withRouter(Collections);