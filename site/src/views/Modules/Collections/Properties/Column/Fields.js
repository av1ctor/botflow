import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import {Col, Row, CustomInput, Input, InputGroup, InputGroupAddon, Button} from 'reactstrap';
import DraggableExtensibleList from '../../../../../components/DraggableExtensibleList';
import Misc from '../../../../../libs/Misc';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import FilterValueInput from '../../../../../components/Common/Collection/View/FilterValueInput';
import FilterOpSelect from '../../../../../components/Common/Collection/View/FilterOpSelect';
import Common from '../Common';

export default class Fields extends PureComponent 
{
	static propTypes = 
	{
		api: PropTypes.object.isRequired,
		container: PropTypes.object.isRequired,
		parent: PropTypes.object.isRequired,
		collection: PropTypes.object.isRequired,
		view: PropTypes.object,
		name: PropTypes.string,
		column: PropTypes.object,
		field: PropTypes.object
	};

	constructor(props)
	{
		super(props);

		const schema = props.collection.schemaObj;

		this.state = {
			isLoading: false,
			errors: [],
			view: Object.assign({}, props.view),
			column: Object.assign({}, props.column),
			field: Object.assign({}, props.field),
			fields: this.transformFields(schema, props.field),
			columns: this.transformFColumns(schema, props.column),
		};

		this.form = [
			{width: 2, label: 'Nome', name: 'column'},
			{width: 3, label: 'Ocultar', name: 'hidden'},
			{width: 4, label: 'Desabilitar', name: 'disabled'},
		];

		this.handleChange = this.handleChange.bind(this);
		this.handleSubmit = this.handleSubmit.bind(this);
	}

	componentDidUpdate(prevProps)
	{
		const {view, column, field} = this.props;
		if(prevProps.view !== view || 
			prevProps.column !== column ||
			prevProps.field !== field)
		{
			const schema = this.props.collection.schemaObj;
			this.setState({
				view: Object.assign({}, view),
				column: Object.assign({}, column),
				field:  Object.assign({}, field),
				fields: this.transformFields(schema, field),
				columns: this.transformFColumns(schema, column),
			});
		}
	}

	transformFields(schema, field)
	{
		return Object.entries(field.fields)
			.map(([k, f]) => ({
				column: k, 
				...f,
				disabled: Common.transformDisabled(null, f, schema),
				hidden: Common.transformHidden(null, f, schema)
			}))
			.sort((a, b) => (a.index||0) - (b.index||0));
	}

	transformFColumns(schema, column)
	{
		return Object.entries(CollectionUtil.getClassByName(schema, column.class).props)
			.map(([k, p]) => ({name: k, ...p}));
	}

	handleChange(event, index, propName = null)
	{
		const fields = Array.from(this.state.fields);

		this.props.parent.handleChange(event, fields[index], propName);

		this.setState({
			fields: fields
		});
	}

	async handleSubmit(event)
	{
		event.preventDefault();

		try 
		{
			this.setState({
				isLoading: true
			});
			
			const {parent, name, column: curColumn, field: curField} = this.props;
			const {column: newColumn, field: newField} = this.state;

			let schema = Misc.cloneObject(this.props.collection.schemaObj);
			const columns = schema.columns;

			let changeCnt = 0;
			let reload = false;

			if(changeCnt > 0)
			{
				if(await parent.updateCollectionSchema(schema))
				{
					return;
				}

				parent.handleSchemaUpdated(schema, {reload: reload}, this.props.view.name);
			}

			return false;
		} 
		finally 
		{
			this.setState({
				isLoading: false
			});
		}
	}

	sortItems = (items) =>
	{
		this.setState({
			fields: items
		});
	}

	renderHeader = ({col}) =>
	{
		return (col.label);
	}

	getColumnType = (name) => 
	{
		const col = CollectionUtil.getColumn(this.props.collection.schemaObj, name, null);
		return (col && (col.type !== 'array'? col.type : col.subtype)) || 'string';
	}

	getColumnLabel = (value) =>
	{
		const col = this.state.columns.find(c => c.name === value);
		return col? col.label || col.name: '';
	}

	renderColumnOptions(index)
	{
		return [{name: '', label: ''}]
			.concat(this.state.columns
				.filter(col => !this.state.fields
					.find((field, i) => field.column === col.name && i !== index)
				)
			)
			.map((col, index) =>
			<option key={index} value={col.name}>{col.label}</option>);
	}

	renderHidden(item, index) 
	{
		const hidden = item.hidden;
		
		return (
			<>
				<CustomInput 
					type="checkbox" 
					id="hidden.data.always"
					checked={hidden.data.always || false}
					onChange={(e) => this.handleChange(e, index)} 
					label="Sempre" 
					className="flex-grow-0" />

				<div className={classnames('flex-grow-0 ml-3 mr-1 text-nowrap', {'text-secondary': hidden.data.always})}>Se Usuário</div>
				<InputGroup className="flex-grow-1 d-flex">
					<InputGroupAddon addonType="prepend" className="flex-grow-0">
						<Input 
							type="select" 
							id="hidden.data.user.op"
							value={hidden.data.user.op}
							onChange={(e) => this.handleChange(e, index)}
							disabled={hidden.data.always}>
							{Common.renderUserOps()}
						</Input>
					</InputGroupAddon>
					<Input 
						type="text" 
						id="hidden.data.user.email"
						value={hidden.data.user.email} 
						onChange={(e) => this.handleChange(e, index)}
						className="flex-grow-1"
						disabled={hidden.data.always} />
				</InputGroup>
			</>);
	}

	renderDisabled(item, index) 
	{
		const disabled = item.disabled;
		
		const renderFieldValues = () => 
		{
			return [<option key="" value=""></option>].concat(
				Object.entries(this.props.collection.schemaObj.columns).map(([name, column]) => 
					<option key={name} value={name}>{column.label || name}</option>
				));
		};

		const renderDisabledFor = (name, title, hasWhen) => 
		{
			const data = disabled.data[name];

			const scalarType = hasWhen? 
				(data.when.type !== 'array'? data.when.type : data.when.column.subtype) || 'string':
				null;

			return (
				<div className="d-block">
					<div className="d-block">
						{title}
					</div>
					<div className="d-block mb-1">
						<CustomInput 
							type="checkbox" 
							id={`disabled.data[${name}].always`} 
							checked={data.always}
							onChange={(e) => this.handleChange(e, index)} 
							label="Sempre" />
					</div>
					<div className="d-flex mb-1">
						<div className={classnames('flex-grow-0 mr-1 text-nowrap', {'text-secondary': data.always})}>Se Usuário</div>
						<InputGroup className="flex-grow-1 d-flex">
							<InputGroupAddon addonType="prepend" className="flex-grow-0">
								<Input 
									type="select"
									name={`disabled.data[${name}].user.op`} 
									value={data.user.op || ''}
									onChange={(e) => this.handleChange(e, index)}
									disabled={data.always}>
									{Common.renderUserOps()}
								</Input>
							</InputGroupAddon>
							<Input 
								type="text" 
								name={`disabled.data.${name}.user.email`}
								value={data.user.email || ''}
								onChange={(e) => this.handleChange(e, index)}
								className="flex-grow-1"
								disabled={data.always}/>
						</InputGroup>
					</div>
					{hasWhen?
						<div className="d-flex">
							<div className={classnames('flex-grow-0 mr-1 text-nowrap', {'text-secondary': data.always})}>
								ou se Campo
							</div>
							<InputGroup className="flex-grow-1 d-flex">
								<InputGroupAddon addonType="prepend" className="flex-grow-0">
									<Input 
										type="select" 
										value={data.when.field || ''}
										onChange={(e) => this.handleChangeWhenField(e, name)}
										disabled={data.always}>
										{renderFieldValues()}
									</Input>
								</InputGroupAddon>
								<FilterOpSelect
									name={`disabled.data.${name}.when.data.op`}
									op={data.when.data.op}
									scalarType={scalarType}
									className="flex-grow-0 rf-col-cell-filter-op ignore" 
									onChange={(e) => this.handleChange(e, index)}
									disabled={data.always || data.when.field === ''}
									isModal />
								<InputGroupAddon addonType="append" className="flex-grow-1">
									<FilterValueInput 
										filter={data.when}
										scalarType={scalarType}
										disabled={data.always || data.when.field === ''}
										className="flex-grow-1"
										onChange={(e, field) => this.handleChange(e, index, `disabled.data.${name}.when.data.${field}`)} />
								</InputGroupAddon>
							</InputGroup>
						</div>:
						null}
				</div>
			);
		};

		return (
			<>
				{renderDisabledFor('insert', 'Criação', false)}
				{renderDisabledFor('update', 'Edição', true)}
			</>
		);
	}

	renderItem = ({item, index, col, isPreview}) => 
	{
		if(isPreview)
		{
			const value = item[col.name] || '';
			switch(col.name)
			{
			case 'column':
				return value? 
					this.getColumnLabel(value):
					'';
			
			case 'hidden':
				return null;

			case 'disabled':
				return null;
	
			default:
				return value;
			}
	
		}

		switch(col.name)
		{
		case 'column':
			return (
				<Input
					required 
					name="column"
					type="select" 
					value={item[col.name]} 
					onChange={(e) => this.handleChange(e, index)}>
					{this.renderColumnOptions(index)}
				</Input>
			);

		case 'hidden':
			return this.renderHidden(item, index);

		case 'disabled':
			return this.renderDisabled(item, index);
		}

	}

	renderErrors()
	{
		return this.state.errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	}

	canAddItem = (items) =>
	{
		return true;
	}

	addItem = () =>
	{
		const field = {
			column: '',
			disabled: {
				data: {
					always: false,
					insert: {
						always: false, 
						user: {op: '', email: ''}
					},
					update: {
						always: false, 
						user: {
							op: '', email: ''
						}, 
						when: {
							field: '',
							type: null, 
							column: null, 
							data: {
								op: 'eq',
								values: ['', '']
							}
						}
					}
				}
			},
			hidden: {
				data: {
					always: false,
					user: {
						op: '', email: ''
					}
				}
			}
		};
		
		const fields = Array.from(this.state.fields);
		fields.push(field);

		this.setState({
			fields: fields
		});

		return field;
	}

	delItem = (item, index) =>
	{
		const fields = this.state.fields.filter((f, i) => i !== index);
		this.setState({
			fields: fields
		});
	}

	render()
	{
		const {column, fields} = this.state;

		if(!column)
		{
			return null;
		}

		return (
			<>
				<Row>
					<Col sm="12">
						<DraggableExtensibleList
							className="modal-zindex"
							columns={this.form} 
							items={fields} 
							renderHeader={this.renderHeader}
							renderItem={this.renderItem}
							canAdd={this.canAddItem}
							addItem={this.addItem}
							delItem={this.delItem}
							onSort={this.sortItems} />
					</Col>
				</Row>
				<Row>
					<Col md="12">
						{this.renderErrors()}
					</Col>
				</Row>
				<Row>
					<Col sm="12">
						<Button
							color="primary"
							disabled={this.state.isLoading}
							type="submit"
							onClick={this.handleSubmit}
							className="mt-4"
						>Atualizar</Button>
					</Col>
				</Row >
			</>
		);
	}
}