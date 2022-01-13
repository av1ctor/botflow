import React, {useCallback} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, CustomInput, InputGroup, InputGroupAddon, Label, Card, Button} from 'reactstrap';
import {Chips} from 'primereact/chips';
import ExtensibleList from '../../../../components/List/ExtensibleList';
import LogicalTree from '../../../../components/Tree/LogicalTree';
import FilterOpSelect from '../../../../components/Common/Collection/View/FilterOpSelect';
import FilterValueInput from '../../../../components/Common/Collection/View/FilterValueInput';
import UserFilterOpSelect from '../../../../components/Common/Collection/View/UserFilterOpSelect';
import CollectionUtil from '../../..//../libs/CollectionUtil';

// eslint-disable-next-line react/prop-types
const Field = ({children, label}) =>
	<div className="row">
		<div className="col-12">
			<div className="pt-2">{label}</div>
			{children}
		</div>
	</div>;

export const transformForEditing = (schema, key, column, references) =>
{
	const transformMethods = (methods) => methods? 
		Object.entries(methods).map(([k, method]) => ({name: k, script: method.script})): 
		[];

	const transformValue = (type, value) =>
		type === 'object'?
			(value? 
				value.constructor !== Array?
					JSON.stringify(value): 
					value.map(v => JSON.stringify(v)):
				null):
			value;

	const defaultToInput = (col, def) => (def? 
		{
			type: def.script? 
				'script': 
				def.function? 
					'function': 
					'value', 
			value: def.script || 
				def.function || 
				transformValue(
					col.type === 'array'? 
						col.subtype: 
						col.type, 
					def.value)
		}: 
		{
			type: '',
			value: null
		});

	const dependsToInput = (dep) =>
	{
		const emptyCol = {
			column: {
				name: ''
			}, 
			type: '', 
			data: {
				op: '', 
				values: ['', '']
			}				
		};

		const emptyFlow = {
			flow: {
				name: ''
			}				
		};

		if(!dep)
		{
			return {
				type: '',
				value: {
					...emptyCol,
					...emptyFlow,
				}
			};	

		}

		if(dep.column)
		{
			const col = dep.column;
			const colRef = {
				name: col.name, 
				...CollectionUtil.getColumn(schema, col.name, references)
			};

			return {
				type: 'column',
				value: {
					column: colRef,
					type: colRef.type || 'string',
					data: {
						op: col.op || 'eq',
						values: FilterValueInput.toInput(
							colRef.type, 
							col.op, 
							col.value)
					},
					...emptyFlow
				}
			};
		}
		else
		{
			const flow = dep.flow;
			return {
				type: 'flow',
				value: {
					...emptyCol,
					flow: {
						name: flow.name,
					}
				}
			};

		}
	};

	const disabledToInput = (dis) =>
	{
		const proto = {
			when: '',
			value: {
				column: column,
				type: column.type || 'string',
				data: {
					op: '', 
					values: ['', '']
				}
			}
		};

		if(!dis)
		{
			return {...proto};
		}

		if(dis.when !== 'VALUE')
		{
			return Object.assign({}, proto, dis);
		}

		const value = dis.value;
		return {
			...dis,
			value: {
				column: column,
				type: column.type || 'string',			
				data: {
					op: value.op || 'eq',
					values: FilterValueInput.toInput(
						column.type, 
						value.op, 
						value.value)
				}
			}
		};
	};

	const disabled = column.disabled || {};

	return {
		name: key,
		...column,
		label: column.label || key,
		default: defaultToInput(column, column.default),
		ref: column.ref? column.ref: {},
		depends: LogicalTree.unpack(column.depends, dependsToInput),
		disabled: {
			insert: LogicalTree.unpack(disabled.insert, disabledToInput),
			update: LogicalTree.unpack(disabled.update, disabledToInput)
		},
		methods: transformMethods(column.methods) 
	};
};

export const transformForSubmitting = (column, errors) =>
{
	const transformValue = (type, value) =>
		type === 'object'?
			(value? 
				value.constructor !== Array? 
					JSON.parse(value):
					value.map(v => JSON.parse(v)): 
				null):
			value;

	const inputToDefault = (col, def) => def && def.type? 
		{
			[def.type]: transformValue(col.type === 'array'? col.subtype: col.type, def.value)
		}: 
		undefined;
	
	const inputToDepends = (dep) => 
	{
		if(!dep || !dep.type)
		{
			return undefined;
		}

		const value = dep.value;
		if(dep.type === 'column')
		{
			return {
				column: {
					name: value.column.name, 
					op: value.data.op, 
					value: FilterValueInput.fromInput(
						value.column.type, 
						value.column.subtype, 
						value.data.op, 
						value.data.values)
				}
			};
		}
		
		return {
			flow: {
				name: value.flow.name
			}
		};
	};

	const inputToDisabled = (dis) => 
	{
		if(!dis || !dis.when)
		{
			return undefined;
		}

		switch(dis.when)
		{
		case 'ALWAYS':
			return {
				when: 'ALWAYS'
			};

		case 'USER':
			return {
				...dis,
				value: undefined
			};

		case 'VALUE':
		{
			const value = dis.value;
			return {
				...dis,
				user: undefined,
				value: {
					op: value.data.op, 
					value: FilterValueInput.fromInput(
						column.type, 
						column.subtype, 
						value.data.op, 
						value.data.values)
				}
			};
		}

		}
	};
	
	let methods = {};
	if(!column.label)
	{
		errors.push('Nome deve ser definido');
	}
	
	if(!column.type)
	{
		errors.push('Tipo deve ser definido');
	}		
	
	if(column.type === 'array' && !column.subtype)
	{
		errors.push('Subtipo deve ser definido');
	}		
	
	if((column.type === 'object' || column.subtype === 'object') && !column.class)
	{
		errors.push('Classe deve ser definida');
	}		
	
	if(column.methods && column.methods.length > 0)
	{
		for(let k = 0; k < column.methods.length; k++)
		{
			const meth = column.methods[k];
			if(!meth.name)
			{
				errors.push(`Métodos[${1+k}]: Nome deve ser definido`);
			}
			else if(!meth.script)
			{
				errors.push(`Métodos[${1+k}]: Script deve ser definido`);
			}
			else
			{
				methods[meth.name] = {
					script: meth.script
				};
			}
		}
	}
	else
	{
		methods = undefined;
	}
				
	const name = column.name?
		column.name:
		CollectionUtil.labelToKey(column.label);

	const depTree = LogicalTree.pack(
		column.depends, 
		dep => inputToDepends(dep));

	const disInsertTree = LogicalTree.pack(
		column.disabled.insert,
		inputToDisabled);
	const disUpdateTree = LogicalTree.pack(
		column.disabled.update,
		inputToDisabled);

	return {
		name: name,
		column: {
			...column,
			default: column.nullable? 
				undefined: 
				inputToDefault(column, column.default), 
			depends: !depTree?
				undefined:
				depTree,
			ref: !column.ref.name?
				undefined:
				column.ref,
			hidden: !column.hidden || !column.hidden.type?
				undefined:
				column.hidden,
			disabled: !disInsertTree && !disUpdateTree?
				undefined:
				{
					insert: !disInsertTree?
						undefined:
						disInsertTree,
					update: !disUpdateTree?
						undefined:
						disUpdateTree,
				},
			name: undefined, 
			methods: methods
		}
	};
};

const methodsColumns  = [
	{width: 7, label: 'Nome', name: 'name'},
	{width: 0, label: 'Script', name: 'script'},
];

const FieldForm = observer((props) =>
{
	const store = props.store;
	
	const handleChange = useCallback((event, extra = null) =>
	{
		props.onChange(event, extra);
	}, []);

	const handleDependsColumnChange = useCallback((event) =>
	{
		const val = event.target.value;
		const column = val !== ''? 
			{
				name: val, 
				...CollectionUtil.getColumn(store.collection.schemaObj, val, store.references)
			}:
			null;
		
		const value = {
			column: column,
			type: column? column.type || 'string': null,
			data: {
				op: 'eq',
				values: ['', '']
			},
			flow: {
				name: ''
			}
		};
		
		handleChange({
			target: {
				name: event.target.name,
				value: value
			}
		});		
	}, []);

	const handleDependsFlowChange = useCallback((event) =>
	{
		const value = {
			column: '',
			type: '',
			data: {
				op: '',
				values: ['', '']
			},
			flow: {
				name: event.target.value
			}
		};

		handleChange({
			target: {
				name: event.target.name, 
				value: value
			}
		});
	}, []);	

	const handleHiddenTypeChange = useCallback((event) =>
	{
		const value = {
			type: event.target.value
		};

		handleChange({
			target: {
				name: event.target.name,
				value: value
			}
		});
	}, []);

	const getDisabledPrototype = (column) =>
	{
		return {
			when: '',
			user: {
				op: '',
				email: ''
			},
			value: {
				column: column,
				type: column.type || 'string',
				data: {
					op: '',
					values: ['', '']
				}
			}
		};
	};

	const updateDisabled = (disabled, for_) =>
	{
		handleChange({
			target: {
				name: `disabled.${for_}`,
				value: disabled
			}
		});
	};

	const renderDisabledPreview = (item) =>
	{
		const types = CollectionUtil.getColumnDisabledTypes();
		
		return `Quando: ${types.find(t => t.value === item.when).label}`;
	};

	const renderDisabled = (item, id, for_, column) => 
	{
		const renderDisabledTypes = () =>
			CollectionUtil.getColumnDisabledTypes(for_)
				.map((opt, index) => 
					<option key={index} value={opt.value}>{opt.label}</option>);

		const base = `disabled.${for_}[${id}]`;

		let compo = null;
		if(item.when === 'USER')
		{
			compo = (
				<Field label="Operador/Usuário">
					<InputGroup>
						<InputGroupAddon addonType="prepend" className="flex-grow-0">
							<UserFilterOpSelect
								required
								name={`${base}.user.op`}
								op={(item.user && item.user.op) || ''}
								className="rf-col-cell-filter-op ignore" 
								onChange={(e) => handleChange(e)}
								isModal />
						</InputGroupAddon>
						<Input 
							disabled={!item.user || !item.user.op}
							required
							name={`${base}.user.email`}
							value={(item.user && item.user.email) || ''}
							className="flex-grow-1"
							onChange={handleChange} />
					</InputGroup>
				</Field>
			);
		}
		else if(item.when === 'VALUE')
		{
			const scalarType = getColumnScalarType(column);

			compo = (
				<Field label="Operador/Valor">
					<InputGroup>
						<InputGroupAddon addonType="prepend" className="flex-grow-0">
							<FilterOpSelect
								name={`${base}.value.data.op`}
								op={item.value.data.op || ''}
								scalarType={scalarType}
								className="rf-col-cell-filter-op ignore" 
								onChange={(e) => handleChange(e)}
								isModal />
						</InputGroupAddon>
						<FilterValueInput 
							disabled={!item.value.data.op}
							required={!!item.value.data.op}
							name={`${base}.value.data.values`}
							filter={item.value}
							scalarType={scalarType}
							className="flex-grow-1"
							onChange={(e) => handleChange(e)} />
					</InputGroup>
				</Field>
			);
		}

		return (
			<>
				<Field label="Quando">
					<Input 
						required
						type="select" 
						name={`${base}.when`}
						value={item.when} 
						onChange={handleChange}>
						{renderDisabledTypes()}
					</Input>
				</Field>
				{compo}
			</>);
	};

	const getDependsPrototype = () =>
	{
		return {
			type: '',
			value: {
				data: {
					op: '',
					values: ['','']
				},
				column: {
					name: '',
				},
				flow: {
					name: ''
				}
			}
		};
	};

	const renderDependsPreview = (dep) =>
	{
		const schema = store.collection.schemaObj;
		const columns = CollectionUtil.transformColumns(schema, schema.columns);

		const findFlowName = (name) => 
		{
			const flow = name? 
				schema.flows[name]:
				null;
			return flow && flow.label? flow.label.value: name;
		};

		const findField = (name) =>
			columns.find(col => col.name === name) || {};
		
		const findOp = (op) =>
			CollectionUtil.getFilterOps().find(oper => oper.value === op) || {};

		const getValue = (values) =>
			values[0];

		if(dep.type === 'flow')
		{
			return (
				`Fluxo "${findFlowName(dep.value.flow.name) || ''}" em execução`
			);
		}
		else
		{
			const value = getValue(dep.value.data.values || []);
			return (
				`Campo "${(dep.value.column && findField(dep.value.column.name).label) || ''}" ${findOp(dep.value.data.op).symbol || ''} ${value? '"' + value + '"': ''}`
			);
		}
	};

	const renderDepends = (dep, id) =>
	{
		const schema = store.collection.schemaObj;
		const columns = CollectionUtil.transformColumns(schema, schema.columns);

		const renderDependsTypes = () =>
			CollectionUtil.getColumnDependsTypes()
				.map((opt, index) => <option key={index} value={opt.value}>{opt.label}</option>);

		const renderDependsColumns = () =>
			[{name: '', label: ''}].concat(columns)
				.map((col, index) => <option key={index} value={col.name}>{col.label}</option>);
		
		const renderDependsFlows = () =>
			[['', {label: ''}]].concat(Object.entries(schema.flows || {}))
				.map(([name, flow], index) => <option key={index} value={name}>{flow.label? flow.label.value: name}</option>);

		let compo = null;

		const base = `depends[${id}]`;

		if(dep.type === 'column')
		{
			const scalarType = getColumnScalarType(dep.column);

			compo = (
				<>
					<InputGroupAddon addonType="prepend" className="flex-grow-0">
						<Input 
							type="select" 
							name={`${base}.value`}
							value={dep.value.column.name || ''}
							onChange={handleDependsColumnChange}>
							{renderDependsColumns()}
						</Input>
					</InputGroupAddon>
					<InputGroupAddon addonType="prepend" className="flex-grow-0">
						<FilterOpSelect
							disabled={!dep.value.column.name}
							required={!!dep.value.column.name}
							name={`${base}.value.data.op`}
							op={dep.value.data.op || ''}
							scalarType={scalarType}
							className="rf-col-cell-filter-op ignore" 
							onChange={(e) => handleChange(e)}
							isModal />
					</InputGroupAddon>
					<FilterValueInput 
						disabled={!dep.value.data.op}
						required={!!dep.value.data.op}
						name={`${base}.value.data.values`}
						filter={dep.value}
						scalarType={scalarType}
						className="flex-grow-1"
						onChange={(e) => handleChange(e)} />
				</>
			);
		}
		else if(dep.type === 'flow')
		{
			compo = (
				<Input 
					type="select" 
					name={`${base}.value`}
					value={dep.value.flow.name || ''}
					onChange={handleDependsFlowChange}>
					{renderDependsFlows()}
				</Input>);
		}

		return (
			<Field label={`Tipo${dep.type === 'flow'? '/Fluxo': dep.type === 'column'? '/Campo/Valor': ''}`}>
				<InputGroup className="d-flex">
					<Input 
						name={`${base}.type`}
						value={dep.type}
						type="select"
						onChange={handleChange}>
						{renderDependsTypes()}
					</Input>
					{compo}
				</InputGroup>
			</Field>
		);
	};

	const updateDepends = (depends) =>
	{
		handleChange({
			target: {
				name: 'depends',
				value: depends
			}
		});
	};

	const addMethod = () =>
	{
		const methods = props.column.methods || [];

		const method = {
			script: ''
		};
		
		methods.push(method);

		handleChange({
			target: {
				name: 'methods',
				value: methods
			}
		});
		
		return method;
	};
	
	const delMethod = (item, index) =>
	{
		const methods = props.column.methods
			.filter((c, i) => i !== index);

		handleChange({
			target: {
				name: 'methods',
				value: methods
			}
		});
	};

	const renderMethod = ({item, index, parent, col, isPreview}) => 
	{
		const renderMethodNames = () =>
			CollectionUtil.getClassMethodNames()
				.filter(opt => opt.value === item.name || !parent.methods.find(meth => meth.name === opt.value))
				.map((opt, index) => <option key={index} value={opt.value}>{opt.label}</option>);

		if(isPreview)
		{
			switch(col.name)
			{
			default:
				return item[col.name];
			}
		}

		switch(col.name)
		{
		case 'name':
			return (
				<Input 
					required
					type="select" 
					name={`methods[${index}].name`}
					value={item.name} 
					onChange={handleChange}>
					{renderMethodNames()}
				</Input>);
		
		case 'script':
			return (
				<Input 
					required
					type="textarea" 
					rows="4"
					name={`methods[${index}].script`}
					value={item.script} 
					onChange={handleChange}>
				</Input>);
		}
	};

	const getColumnScalarType = (col) => 
		(col && (col.type !== 'array'? col.type : col.subtype)) || 'string';

	const renderTypes = (isSubtype = false) =>
		CollectionUtil.getColumnTypes(isSubtype)
			.map((opt, index) => <option key={index} value={opt.value}>{opt.label}</option>);

	const renderCompos = (forType) =>
		CollectionUtil.getColumnComponents(forType)
			.map((opt, index) => <option key={index} value={opt.value}>{opt.label}</option>);

	const renderClassOptions = (schema) =>
		[['', {label: ''}]].concat(Object.entries(schema.classes || {}))
			.map(([key, klass], index) => <option key={index} value={key}>{klass.label}</option>);

	const renderRefOptions = (references) =>
		[['', {label: ''}]].concat(Object.entries(references || {}))
			.map(([key, ref], index) => <option key={index} value={key}>{ref.name}</option>);

	const renderRefDisplayOptions = (references, ref) =>
		[['', {label: ''}]].concat(Object.entries(ref && references && references[ref]? references[ref].schemaObj.columns: {}))
			.map(([key, col], index) => <option key={index} value={key}>{col.label || key}</option>);
	
	const renderDefaultTypes = () =>
		Object.entries(CollectionUtil.getColumnDefaultTypes())
			.map(([key, opt], index) => <option key={index} value={key}>{opt.name}</option>);
	
	const renderDefaultFunctionNames = () =>
		CollectionUtil.getColumnDefaultFunctionNames()
			.map((opt, index) => <option key={index} value={opt.value}>{opt.label}</option>);

	const renderHiddenTypes = () =>
		CollectionUtil.getColumnHiddenTypes()
			.map((opt, index) => <option key={index} value={opt.value}>{opt.label}</option>);

	const renderSubType = (column) => 
	{
		switch(column.type)
		{
		case 'array':
			return (
				<Input 
					required
					type="select" 
					name={'subtype'}
					value={column.subtype || ''} 
					onChange={handleChange}>
					{renderTypes(true)}
				</Input>);

		default:	
			return (
				<Input 
					disabled
					type="select" 
					value="" 
					readOnly/>);
		}			
	};

	const renderAttribs = (idPrefix) =>
	{
		return (
			<>
				<Row>
					<Col xs="12" md="6">
						<CustomInput 
							disabled={type === 'object'}
							id={`${idPrefix}sortable`}
							type="switch" 
							label="Permitir filtrar e ordenar"
							checked={column.sortable? true: false}
							onChange={handleChange} />
					</Col>
					<Col xs="12" md="6">
						<CustomInput 
							disabled={type === 'object' || column.nullable}
							id={`${idPrefix}unique`}
							type="switch" 
							label="Permitir apenas valores únicos"
							checked={column.unique? true: false}
							onChange={handleChange} />
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<CustomInput 
							disabled={!!column.default.type || column.unique}
							id={`${idPrefix}nullable`}
							type="switch" 
							label="Permitir valores nulos"
							checked={column.nullable? true: false}
							onChange={handleChange} />
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<CustomInput 
							disabled={type !== 'number' || column.positional}
							id={`${idPrefix}auto`}
							type="switch" 
							label="Incrementar automaticamente"
							checked={column.auto? true: false}
							onChange={handleChange} />
					</Col>
					<Col xs="12" md="6">
						<CustomInput 
							disabled={type !== 'number' || column.auto}
							id={`${idPrefix}positional`}
							type="switch" 
							label="Utilizar na ordenação"
							checked={column.positional? true: false}
							onChange={handleChange} />						
					</Col>
				</Row>
			</>);
	};

	const renderDefault = (column) =>
	{
		const disabled = column.nullable || column.auto || column.positional;
		let compo = null;

		switch(column.default.type || 'value')
		{
		case 'value':
		{
			let compoType = 'text';
			switch(column.type)
			{
			case 'number':
			case 'decimal':
				compoType = 'number';
				break;
			case 'bool':
				compoType = 'checkbox';
				break;
			case 'array':
				compoType = 'chips';
				break;
			}
			
			switch(compoType)
			{
			case 'chips':
				compo = 
					<Chips 
						disabled={disabled}
						name={'default.value'}
						value={column.default.value?
							column.default.value.constructor === Array? 
								column.default.value:
								[]:
							[]} 
						onChange={handleChange} />;
				break;
			
			default:
				compo = 
					<Input 
						disabled={disabled}
						type={compoType} 
						name={'default.value'}
						value={column.default.value || ''} 
						onChange={handleChange}>
					</Input>;
				break;
			}
			break;
		}

		case 'function':
			compo = 
				<Input 
					disabled={disabled}
					type="select"
					name={'default.value'}
					value={column.default.value || ''} 
					onChange={handleChange}>
					{renderDefaultFunctionNames()}
				</Input>;
			break;
		
		case 'script':
			compo = 
				<Input 
					disabled={disabled}
					type="text"
					name={'default.value'}
					value={column.default.value || ''} 
					onChange={handleChange}>
				</Input>;
			break;
		}

		return (
			<InputGroup>
				<InputGroupAddon addonType="prepend">
					<Input 
						disabled={disabled}
						type="select"
						name={'default.type'}
						value={column.default.type} 
						onChange={e => handleChange(e, 'default.value')}>
						{renderDefaultTypes()}
					</Input>
				</InputGroupAddon>
				{compo}
			</InputGroup>
		);
	};

	const renderRef = (column) =>
	{
		const disabled = column.auto || column.positional || column.type !== 'string';

		return (
			<InputGroup>
				<Input 
					disabled={disabled}
					type="select" 
					name={'ref.name'}
					value={column.ref && column.ref.name || ''} 
					onChange={handleChange}>
					{renderRefOptions(store.references)}
				</Input>
				<Input 
					required={column.ref && !!column.ref.name}
					disabled={disabled || !column.ref || !column.ref.name}
					type="select" 
					name={'ref.display'}
					value={column.ref && column.ref.display || ''} 
					onChange={handleChange}>
					{renderRefDisplayOptions(store.references, column.ref? column.ref.name: null)}
				</Input>
			</InputGroup>);
	};		

	const renderHidden = (column) =>
	{
		let compo = null;

		const hidden = column.hidden || {};
		if(hidden.type === 'USER')
		{
			compo = (
				<>
					<InputGroupAddon addonType="prepend" className="flex-grow-0">
						<UserFilterOpSelect
							required
							name={'hidden.user.op'}
							op={(hidden.user && hidden.user.op) || ''}
							className="rf-col-cell-filter-op ignore" 
							onChange={(e) => handleChange(e)}
							isModal />
					</InputGroupAddon>
					<Input 
						disabled={!hidden.user || !hidden.user.op}
						required
						name={'hidden.user.email'}
						value={(hidden.user && hidden.user.email) || ''}
						className="flex-grow-1"
						onChange={handleChange} />
				</>
			);
		}

		return (
			<InputGroup className="d-flex">
				<Input 
					name={'hidden'}
					value={hidden.type || ''}
					type="select"
					onChange={handleHiddenTypeChange}>
					{renderHiddenTypes()}
				</Input>
				{compo}
			</InputGroup>
		);
	};

	const createCondition = (section, genProto, updateCondition) =>
	{
		updateCondition(LogicalTree.unpack(
			LogicalTree.createPackedNode('and', genProto())));
	};

	const renderEmptyCondition = (section, genProto, updateCondition) =>
	{
		return (
			<div>
				<span>{section === 'depends'? 'Nenhuma': 'Nunca'}</span>&nbsp;
				<Button
					onClick={() => createCondition(section, genProto, updateCondition)}>
					Criar condição
				</Button>
			</div>
		);
	};

	const schema = store.collection.schemaObj;
	
	const {column} = props;
	
	const type = getColumnScalarType(column);

	return (
		<div>
			<Field label="Id">
				<Input 
					required
					readOnly
					disabled
					type="text" 
					value={column.name}/>
			</Field>
			<Field label="Nome">
				<Input 
					required
					type="text" 
					name={'label'}
					value={column.label} 
					onChange={handleChange}>
				</Input>					
			</Field>
			<Field label="Tipo">
				<Input 
					required
					type="select" 
					name={'type'}
					value={column.type || ''} 
					onChange={handleChange}>
					{renderTypes(false)}
				</Input>					
			</Field>
			<Field label="Subtipo">
				{renderSubType(column)}
			</Field>
			<Field label="Classe">
				<Input 
					disabled={type !== 'object'}
					required={type === 'object'}
					type="select" 
					name={'class'}
					value={column.class || ''} 
					onChange={handleChange}>
					{renderClassOptions(schema)}
				</Input>					
			</Field>
			<Field label="Componente">
				<Input 
					type="select" 
					name={'component'}
					value={column.component || ''} 
					onChange={handleChange}>
					{renderCompos(column.type)}
				</Input>					
			</Field>
			<Field label="Máscara">
				<Input 
					disabled={!['string', 'number', 'decimal'].includes(type)}
					type="text" 
					name={'mask'}
					value={column.mask || ''} 
					onChange={handleChange}>
				</Input>					
			</Field>
			<Field label="Opções">
				<Chips 
					disabled={type !== 'enumeration'}
					required={type === 'enumeration'}
					name={'options'}
					value={column.options || []} 
					onChange={handleChange} />					
				<hr />
			</Field>
			<Field label="Atributos">
				{renderAttribs(props.idPrefix || '')}
			</Field>
			<Field label="Padrão (Tipo/Valor)">
				{renderDefault(column)}
				<hr />
			</Field>
			<Field label="Dependência">
				<LogicalTree
					tree={column.depends}
					disabled={column.auto || column.positional}
					updateTree={updateDepends}
					renderPreview={renderDependsPreview}
					renderNode={renderDepends}
					renderEmpty={() => renderEmptyCondition('depends', getDependsPrototype, updateDepends)}
					getNodePrototype={getDependsPrototype}
					onChange={e => props.onChange({target:{...e.target, name: `depends${e.target.name}`}})}
					className="modal-zindex"
				/>
			</Field>
			<Field label="Esconder">
				{renderHidden(column)}
			</Field>				
			<Field label="Desabilitar">
				<Card>
					<Label>Na criação</Label> 
					<LogicalTree
						tree={column.disabled.insert}
						updateTree={(dis) => updateDisabled(dis, 'insert')}
						renderPreview={renderDisabledPreview}
						renderNode={(node, id) => renderDisabled(node, id, 'insert', column)}
						renderEmpty={() => renderEmptyCondition(
							'disabled.insert', 
							() => getDisabledPrototype(column), 
							(dis) => updateDisabled(dis, 'insert')
						)}
						getNodePrototype={() => getDisabledPrototype(column)}
						onChange={e => props.onChange({target:{...e.target, name: `disabled.insert${e.target.name}`}})}
						className="modal-zindex"
					/>
					<br />						
					<Label>Na edição</Label>
					<LogicalTree
						tree={column.disabled.update}
						updateTree={(dis) => updateDisabled(dis, 'update')}
						renderPreview={renderDisabledPreview}
						renderNode={(node, id) => renderDisabled(node, id, 'update', column)}
						renderEmpty={() => renderEmptyCondition(
							'disabled.update', 
							() => getDisabledPrototype(column), 
							(dis) => updateDisabled(dis, 'update')
						)}
						getNodePrototype={() => getDisabledPrototype(column)}
						onChange={e => props.onChange({target:{...e.target, name: `disabled.update${e.target.name}`}})}
						className="modal-zindex"
					/>
				</Card>
			</Field>
			<Field label="Referência (Coleção/Campo exibido)">
				{renderRef(column)}
				<hr />
			</Field>
			<Field label="Métodos">
				<ExtensibleList 
					columns={methodsColumns}
					items={column.methods || []} 
					alwaysMobile
					totalPreviewCols={1}
					renderHeader={({col}) => col.label}
					renderItem={(args) => renderMethod({...args, parent: column})}
					addItem={addMethod}
					deleteItem={delMethod}
					extraAddButtonText="método" />					
			</Field>
		</div>);
});

FieldForm.propTypes = 
{
	store: PropTypes.object.isRequired,
	column: PropTypes.object,
	onChange: PropTypes.func,
	idPrefix: PropTypes.string,
};

export default FieldForm;