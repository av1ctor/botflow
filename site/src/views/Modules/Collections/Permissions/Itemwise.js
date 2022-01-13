import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, InputGroup, InputGroupAddon, Button} from 'reactstrap';
import SimpleCollapse from '../../../../components/Collapse/SimpleCollapse';
import FilterValueInput from '../../../../components/Common/Collection/View/FilterValueInput';
import FilterOpSelect from '../../../../components/Common/Collection/View/FilterOpSelect';
import CollectionUtil from '../../../../libs/CollectionUtil';
import Misc from '../../../../libs/Misc';
import LogicalTree from '../../../../components/Tree/LogicalTree';
import InputMultiSelectObject from '../../../../components/Input/InputMultiSelectObject';

// eslint-disable-next-line react/prop-types
const Field = ({children, label}) =>
	<div className="row">
		<div className="col-12">
			<div className="pt-2">{label}</div>
			{children}
		</div>
	</div>;

const genEmpty = () =>
{
	return {
		when: '',
		user: {
			columns: []
		},
		column: {
			key: '',
			column: {},
			type: '',
			op: '',
			values: ['','']
		}
	};
};

const genEmptyAuth = () =>
{
	return {
		create: genEmpty(),
		edit: genEmpty(),
		read: genEmpty()
	};
};

const transformSectionForEditing = (columns, section) =>
{
	return LogicalTree.unpack(
		section,
		cond => 
		{
			switch(cond.when)
			{
			case 'USER_SAME':
				break;

			case 'COLUMN_VALUE':
			{
				const col = cond.column;
				const column = columns.find(c => c.name === col.name);
				const type = (column.type !== 'array'? column.type : column.subtype) || 'string';
				cond.column = {
					key: col.name, 
					column: column,
					op: col.op, 
					values: FilterValueInput.toInput(type, col.op, col.value || '')
				};
				break;
			}

			}
	
			return Object.assign(genEmpty(), cond);
		}
	);
};

const transformColumnForEditing = (columns, auth) =>
{
	auth.create = transformSectionForEditing(columns, auth.create);
	auth.edit = transformSectionForEditing(columns, auth.edit);
	auth.read = transformSectionForEditing(columns, auth.read);

	return auth;		
};

const transformForEditing = (collection, columns) =>
{
	return transformColumnForEditing(columns, Misc.cloneObject(collection.schemaObj.auth) || {});
};

const transformSectionForSubmitting = (section) =>
{
	const tree = LogicalTree.pack(
		section,
		cond => 
		{
			if(!cond || !cond.when)
			{
				return undefined;
			}

			switch(cond.when)
			{
			case 'USER_SAME':
				if(!cond.user.columns.length === 0)
				{
					return undefined;
				}
				
				return {
					when: cond.when,
					user: {
						...cond.user
					}
				};

			case 'COLUMN_VALUE':
			{
				if(!cond.column.key)
				{
					return undefined;
				}
				
				const col = cond.column;

				return {
					when: cond.when,
					column: {
						name: col.key,
						op: col.op,
						value: FilterValueInput.fromInput(
							col.type, col.column.subtype, col.op, col.values)
					}
				};
			}

			default:
				return undefined;
			}
		});

	return tree?
		tree:
		undefined;
};

const filterColumnSuggestionTypes = (columns) =>
{
	return columns.filter(
		col => col.type === 'string' || 
		(col.type === 'array' && col.subtype === 'string'));
};

const Itemwise = observer(props =>
{
	const store = props.store;

	const [isLoading, setIsLoading] = useState(false);
	const [auth, setAuth] = useState(() => 
		transformForEditing(store.collection, props.columns));
	const [columnSuggestions, setColumnSuggestions] = useState(() => 
		filterColumnSuggestionTypes(props.columns));

	useEffect(() =>
	{
		if(props.visible)
		{
			setAuth(transformForEditing(store.collection, props.columns));
			setColumnSuggestions(filterColumnSuggestionTypes(props.columns));
		}
	}, [props.columns]);

	const handleOnChangeColumnArray = useCallback((event) => 
	{
		const key = event.target.name;
		const value = event.target.value.map(value => value.name);

		setAuth(auth =>
		{
			const to = Object.assign({}, auth);

			Misc.setFieldValue(to, key, value);
			
			return to;
		});
	}, []);

	const handleDelete = useCallback((/*item*/) =>
	{

	}, []);

	const handleChange = useCallback((e) =>
	{
		const key = e.target.name || e.target.id;
		const value = e.target.type === 'checkbox'? 
			e.target.checked: 
			e.target.value;
		
		setAuth(auth =>	
		{
			const to = Object.assign({}, auth);
			Misc.setFieldValue(to, key, value);
			return to;
		});
	}, []);

	const handleOnChangeColumn = useCallback((e) =>
	{
		const key = e.target.value;

		handleChange({
			target: {
				name: e.target.name,
				value: {
					key: key,
					column: props.columns.find(c => c.name === key),
					op: '',
					values: ['', '']
				}
			}
		});
	}, []);

	const updateSection = useCallback((name, tree) =>
	{
		handleChange({
			target: {
				name: name,
				value: tree
			}
		});
	}, []);

	const handleSubmit = useCallback(async (e) =>
	{
		e.preventDefault();

		const schema = Misc.cloneObject(store.collection.schemaObj);
		
		schema.auth = {
			create: transformSectionForSubmitting(auth.create),
			edit: transformSectionForSubmitting(auth.edit),
			read: transformSectionForSubmitting(auth.read)
		};

		const res = await store.updateSchema(schema);
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
		}
		else
		{
			props.container.showMessage('Atualização das permissões de itens realizada!', 'success');
		}
	}, [auth]);

	const renderColumnOptions = () =>
	{
		return [{value: '', label: ''}].concat(props.columns)
			.map((col, i) => 
				<option value={col.name} key={i}>{col.label}</option>);
	};

	const renderOperatorOptions = (column) =>
	{
		return CollectionUtil.getFilterOps(column.type).map((f, index) =>
			<option key={index} value={f.value}>{f.label}</option>
		);
	};

	const renderColumn = (base, column) =>
	{
		const getColumnScalarType = (col) => (col && (col.type !== 'array'? col.type : col.subtype)) || 'string';
		
		return (
			<InputGroup className="flex-grow-1 d-flex">
				<InputGroupAddon addonType="prepend" className="flex-grow-0">
					<Input 
						required 
						name={`${base}.column`}
						type="select" 
						value={column.key || ''}
						onChange={handleOnChangeColumn}>
						{renderColumnOptions()}
					</Input>
				</InputGroupAddon>
				<FilterOpSelect
					name={`${base}.column.op`}
					scalarType={getColumnScalarType(column.column)}
					op={column.op || ''}
					className="flex-grow-0 rf-col-cell-filter-op ignore" 
					onChange={handleChange}
					isModal
					disabled={!column.column} />
				<InputGroupAddon addonType="append" className="flex-grow-1">
					<FilterValueInput.renderInput
						name={`${base}.column.values`}
						column={column.column}
						type={column.column.type || 'string'}
						scalarType={getColumnScalarType(column.column)}
						op={column.op || ''}
						values={column.values}
						className="flex-grow-1" 
						onChange={handleChange}
						disabled={!column.column || !column.op} />
				</InputGroupAddon>
			</InputGroup>						
		);
	};

	const renderNode = (name, section, id) =>
	{
		const renderWhenOptions = (name) =>
			CollectionUtil.getAuthActionWhenTypes(name)
				.map((opt, index) => 
					<option key={index} value={opt.value}>{opt.label}</option>);

		const base = `${name}[${id}]`;

		let compo = null;

		switch(section.when)
		{
		case 'USER_SAME':
			compo = (
				<Field label="Campo(s)">
					<InputMultiSelectObject 
						name={`${base}.user.columns`} 
						values={section.user.columns.map(name => props.columns.find(c => c.name === name))} 
						objectKey="name"
						objectLabel="label"
						options={columnSuggestions}
						onChange={handleOnChangeColumnArray} 
					/>
				</Field>
			);
			break;

		case 'COLUMN_VALUE':
			compo = (
				<Field label="Campo/Operação/Valor">
					{renderColumn(base, section.column)}
				</Field>
			);
			break;
		}
		
		return (
			<>
				<Field label="Condição">
					<Input 
						name={`${base}.when`}
						value={section.when}
						type="select"
						onChange={handleChange}>
						{renderWhenOptions(name)}
					</Input>
				</Field>
				{compo}
			</>
		);
	};

	const renderPreview = (node) =>
	{
		const types = CollectionUtil.getAuthActionWhenTypes();

		const base = types.find(t => t.value === node.when).label;

		const columnKeyToLabel = (key) =>
		{
			return CollectionUtil.getColumnLabel(
				store.collection.schemaObj, key, store.references);
		};
		
		const columnKeysToLabel = (keys) =>
		{
			return keys.map(key => columnKeyToLabel(key));
		};

		const findOp = (op) =>
			CollectionUtil.getFilterOp(op) || {};

		const getValue = (col) =>
			!col.op || col.op === 'isnull' || col.op === 'notnull'? 
				'':
				col.op !== 'between'? 
					`"${col.values[0]}"`:
					`"${col.values.join('":"')}"`;

		switch(node.when)
		{
		case 'USER_SAME':
			return `${base} "${columnKeysToLabel(node.user.columns).join(', ')}"`;
		case 'COLUMN_VALUE':
			return `${base} "${columnKeyToLabel(node.column.key)}" ${findOp(node.column.op).symbol || ''} ${getValue(node.column)}`;
		default:
			return `${base}`;
		}
	};

	const createCondition = (name) =>
	{
		setAuth(auth =>
		{
			const to = Object.assign({}, auth);
			to[name] = transformSectionForEditing(props.columns, LogicalTree.createPackedNode('and', genEmpty()));
			return to;
		});
	};
	
	const renderEmptyCondition = (name) =>
	{
		return (
			<div>
				<span>Qualquer usuário que possua role de acesso correspondente</span>&nbsp;
				<Button
					onClick={() => createCondition(name)}>
					Restringir
				</Button>
			</div>
		);
	};

	const renderSection = (name) =>
	{
		const section = auth[name];

		return (
			<LogicalTree 
				tree={section}
				allowNot={false}
				getNodePrototype={genEmpty}
				updateTree={(tree) => updateSection(name, tree)}
				renderPreview={renderPreview}
				renderNode={(node, id) => renderNode(name, node, id)}
				renderEmpty={() => renderEmptyCondition(name)}
				onChange={(e) => handleChange({target:{...e.target, name: `${name}${e.target.name}`}})}
				className="modal-zindex"
			/>
		);
	};

	return (
		<>
			<Row className="mb-3">
				<Col sm="12">
					<SimpleCollapse 
						isOpen={true} 
						title={() => <><i className="la la-pen" /><span> Edição</span></>}>
						{renderSection('edit')}
					</SimpleCollapse>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<SimpleCollapse 
						isOpen={true} 
						title={() => <><i className="la la-book-reader" /><span> Leitura</span></>}>
						{renderSection('read')}
					</SimpleCollapse>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Button
						color="primary"
						disabled={isLoading}
						type="submit"
						onClick={handleSubmit}
						className="mt-4"
					>Atualizar</Button>
				</Col>
			</Row>
		</>
	);
});

Itemwise.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
	columns: PropTypes.array.isRequired,
	setBadge: PropTypes.func
};

export default Itemwise;
