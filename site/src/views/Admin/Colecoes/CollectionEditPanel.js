import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import {Col, Row, Label, Input} from 'reactstrap';
import CollectionUtil from '../../../libs/CollectionUtil';
import SidebarResizeable from '../../../components/SidebarResizeable';
import BaseList from '../../../components/BaseList';
import BaseForm from '../../../components/BaseForm';
import InputMask from '../../../components/InputMask';

const MAX_COLS = 6;

export default class CollectionEditPanel extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		history: PropTypes.object,
		container: PropTypes.object,
		fullScreen: PropTypes.bool
	};

	constructor(props)
	{
		super(props);

		this.state = {
			visible: false,
			collection: null
		};

		this.references = {};

		this.show = this.show.bind(this);
		this.hide = this.hide.bind(this);
		this.getHeader = this.getHeader.bind(this);
		this.getFilters = this.getFilters.bind(this);
		this.renderItem = this.renderItem.bind(this);
		this.renderFields = this.renderFields.bind(this);
		this.validateFields = this.validateFields.bind(this);
		this.getItemId = this.getItemId.bind(this);
		this.getSortBy = this.getSortBy.bind(this);
	}

	async show(collection)
	{
		this.references = await CollectionUtil.setup(collection, this.props.api);
		
		this.setState({
			collection: collection,
			visible: true
		});
	}

	hide()
	{
		this.setState({
			collection: null,
			visible: false
		});
	}

	validateFields(/*item*/)
	{
		return true;
	}

	transformForSubmitting(item)
	{
		return {
			vars: item
		};
	}

	getItemId(item)
	{
		return '_id';
	}

	renderFields(item, handleOnChange)
	{
		const schema = this.state.collection.schemaObj;

		return (
			<Row>
				{Object.entries(schema.columns).map(([name, column], index) =>
				{
					return (
						<Col key={index} sm="12" md="6" lg="4">
							<Label className="base-form-label">{CollectionUtil.getColumnLabel(schema, name)}</Label>
							<Input 
								required={!column.nullable} 
								name={name}
								var-type={column.type}
								type="text"
								value={item[name] || ''}
								onChange={handleOnChange} />
						</Col>);
				})}
			</Row>);
	}

	calcColSize()
	{
		return Math.floor(12 / Math.min(MAX_COLS, 
			Object.keys(this.state.collection.schemaObj.columns).length));
	}

	renderItem(item, isFirstRow)
	{
		if(!this.state.collection)
		{
			return null;
		}
		
		if(isFirstRow)
		{
			const schema = this.state.collection.schemaObj;
			const getValue = (k) => item[k];
			const getValueAsString = (k, column) => 
			{
				const value = getValue(k);
				const klass = column.class? CollectionUtil.getClassByName(schema, column.class): null;
				return klass && klass.methods && klass.methods.toString?
					klass.methods.toString.func({'props': value}):
					value;
			};
			const size = this.calcColSize();

			return Object.entries(schema.columns).filter((e, index) => index < MAX_COLS).map(([name, column], index) => 
				<Col key={index} xs={size}>
					{column.mask? 
						InputMask.format(getValue(name), column.mask): 
						getValueAsString(name, column)
					}
				</Col>);
		}

		let id = this.getItemId(item);

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={id? id: 'col-empty-item-form'} 
							api={this.props.api}
							history={this.props.history}
							parent={this.baseList}
							controller={this.state.collection? `collections/${this.state.collection.id}/items`: null}
							item={item} 
							enabled
							render={this.renderFields} 
							validate={this.validateFields} 
							onSubmit={this.transformForSubmitting}
						/>
					</Col>
				</Row>
			</Col>
		);		
	}

	getFilters()
	{
		return [];
	}

	hasIndex(column)
	{
		const tb = this.state.collection.schemaObj;
		return (tb.key && tb.key === column) ||
			(tb.indexes && tb.indexes.find(i => i.columns.find(c => c === column)));
	}

	getHeader()
	{
		if(!this.state.collection)
		{
			return [];
		}
		
		const size = this.calcColSize();
		const schema = this.state.collection.schemaObj;

		return Object.entries(schema.columns).filter((e,i) => i < MAX_COLS).map(([name]) => {
			return {
				name: this.hasIndex(name)? name: null, 
				title: CollectionUtil.getColumnLabel(schema, name), 
				size: {xs:size}, 
				klass: ''
			};
		});
	}

	getSortBy()
	{
		return '_id';
	}

	render()
	{
		if(!this.state.collection)
		{
			return null;
		}

		return (
			<SidebarResizeable visible={this.state.visible} position="right" onHide={this.hide} fullScreen={this.props.fullScreen}>
				<div className="w-100 h-100">
					<FocusLock disabled={!this.state.visible} returnFocus className="w-100 h-100">
						<BaseList
							ref={(el) => this.baseList = el}
							{...this.props}
							title="Itens da Coleção"
							controller={`collections/${this.state.collection.id}/items`}
							updateHistory={false}
							sortBy={this.getSortBy}
							ascending={false}
							inline={true}
							getItemId={this.getItemId}
							getFilters={this.getFilters}
							getHeader={this.getHeader}
							renderItem={this.renderItem}
						/>
					</FocusLock>
				</div>
			</SidebarResizeable>
		);
	}
}