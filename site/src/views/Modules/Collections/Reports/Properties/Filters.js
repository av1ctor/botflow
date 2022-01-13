import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, Button, InputGroup, InputGroupAddon} from 'reactstrap';
import SimpleCollapse from '../../../../../components/Collapse/SimpleCollapse';
import Misc from '../../../../../libs/Misc.js';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import ExtensibleList from '../../../../../components/List/ExtensibleList';

const formColumns = [
	{width: 2, label: 'Tipo', name: 'metatype'},
	{width: 2, label: 'Campo ou Nome/Tipo', name: 'name'},
	{width: 2, label: 'Operador', name: 'op'},
	{width: 2, label: 'Padrão', name: 'default'},
	{width: 2, label: 'Min/Max', name: 'minMax'},
];

const fieldsColumns = [
	{width: 3, label: 'Campo', name: 'name'},
	{width: 2, label: 'Operador', name: 'op'},
	{width: 5, label: 'Valor', name: 'values'},
];

const transformType = (field) =>
{
	const getType = (value) => value.script? 'script': 'value';
	
	if(field.op !== 'between')
	{
		if(!field.value)
			return '';
		else
			return getType(field.value);
	}
	else
	{
		return getType(field.from) || getType(field.to);
	}
};

const transformValue = (field) =>
{
	const getValue = (value) => value.script || value.value;
	
	if(field.op !== 'between')
	{
		if(!field.value)
			return ['', ''];
		else
			return [getValue(field.value), ''];
	}
	else
	{
		return [getValue(field.from), getValue(field.to)];
	}
};

const transformFields = (fields) =>
{
	return fields? 
		Object.entries(fields)
			.map(([k, field]) => ({
				name: k,
				...field,
				type: transformType(field), 
				values: transformValue(field)
			})):
		[];
};

const transformFormFields = (fields) =>
{
	return fields? 
		Object.entries(fields)
			.map(([k, field]) => ({
				name: field.template? field.label: k, 
				...field, 
				metatype: field.template? 'template': 'column'
			})):
		[];
};

const transformReport = (report, clone = true) =>
{
	const res = report? 
		(clone? Misc.cloneObject(report): report):
		{};

	if(!res.filter)
	{
		res.filter = {
			fields: {}, 
			form: {
				fields: {}
			}
		};
	}

	if(!res.filter.form)
	{
		res.filter.form = {
			fields: {}
		};
	}

	res.filter.form.fields = transformFormFields(res.filter.form.fields);
	res.filter.fields = transformFields(res.filter.fields);

	return res;
};

const Filters = observer(props =>
{
	const store = props.store;
	
	const [isLoading, setIsLoading] = useState(false);
	const [errors, setErrors] = useState([]);
	const [report, setReport] = useState(() => transformReport(props.report));

	useEffect(() =>
	{
		setReport(transformReport(props.report));
	}, [props.report]);

	const handleFieldsChange = useCallback((event) =>
	{
		const to = Object.assign({}, report);
		to.filter.fields = Array.from(to.filter.fields);
		updateReport(event, to);
	}, [report]);

	const handleFormFieldsChange = useCallback((event) =>
	{
		const to = Object.assign({}, report);
		to.filter.form.fields = Array.from(to.filter.form.fields);
		updateReport(event, to);
	}, [report]);

	const updateReport = (event, report) =>
	{
		const name = event.target.name;
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;

		Misc.setFieldValue(report, name, value);

		setReport(report);
	};

	const handleSubmit = async (event) =>
	{
		event.preventDefault();

		const transformName = (name) => CollectionUtil.labelToKey(name);
		const checkName = (name, cols, index) => !cols.find((c, i) => c.name === name && i !== index);
		const errors = [];

		try 
		{
			setIsLoading(true);
			
			const schema = Misc.cloneObject(props.schema);
			const from = transformReport(schema.reports[props.index], false);
			const fromFields = from.filter.fields;
			const fromFormFields = from.filter.form.fields;

			const toFields = report.filter.fields || [];
			const toFormFields = report.filter.form.fields || [];

			let changeCnt = 0;
			if(!Misc.compareArrays(fromFields, toFields) || 
				!Misc.compareArrays(fromFormFields, toFormFields))
			{
				from.filter.fields = {};
				for(let i = 0; i < toFields.length; i++)
				{
					const field = toFields[i];
					let {name, type, values, ...clean} = field;
					if(!name)
					{
						errors.push(`Pesquisa[${i}]: campo deve ser definido`);
					}
					else if(!field.op)
					{
						errors.push(`Pesquisa[${i}]: operador deve ser definido`);
					}
					else
					{
						if(field.op !== 'between')
						{
							from.filter.fields[name] = {
								...clean, 
								value: type? {[type]: values[0]}: null
							};
						}
						else

						{
							from.filter.fields[name] = {
								...clean, 
								from: {[type]: values[0]}, 
								to: {[type]: values[1]}
							};
						}
					}
				}

				from.filter.form.fields = {};
				for(let i = 0; i < toFormFields.length; i++)
				{
					const field = toFormFields[i];
					let {metatype, name, type, op, ...clean} = field;
					const isTemplate = field.metatype === 'template';
					if(!metatype)
					{
						errors.push(`Formulário[${i}]: type deve ser definido`);
					}
					else if(!name)
					{
						errors.push(`Formulário[${i}]: campo/name deve ser definido`);
					}
					else if(!isTemplate && !op)
					{
						errors.push(`Formulário[${i}]: operador deve ser definido`);
					}
					else
					{
						const label = !isTemplate?
							CollectionUtil.getColumnLabel(schema, name, null):
							name;
						if(isTemplate)
						{
							name = transformName(name);
							if(!checkName(name, toFormFields, i))
							{
								errors.push(`Formulário[${i}]: name duplicado: ${label}`);
							}
						}

						from.filter.form.fields[name] = {
							...clean,
							type: type !== ''? type: undefined,
							op: op !== ''? op: undefined,
							label, 
							template: isTemplate
						};
					}
				}

				++changeCnt;
			}

			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await store.updateReport(props.index, from);
				if(res.errors)
				{
					errors.push(...res.errors);
				}
				else
				{
					props.container.showMessage('Atualização realizada no relatório!', 'success');
				}
			}

			return false;
		} 
		finally 
		{
			setIsLoading(false);
			setErrors(errors);
		}
	};

	const onAddFields = (items) =>
	{
		return items.length < 10;
	};

	const addFieldsItem = (items) =>
	{
		const res = {
			name: '',
			op: '',
			type: '', 
			values: ['', '']
		};

		setReport(report =>
		{
			const to = Object.assign({}, report);
			to.filter.fields = Array.from(to.filter.fields);
			to.filter.fields.push(res);
			
			return to;
		});
		
		return res;
	};
	
	const delFieldsItem = (item, index) =>
	{
		setReport(report =>
		{
			const to = Object.assign({}, report);
			to.filter.fields = to.filter.fields.filter((c, i) => i !== index);
			return to;
		});
	};

	const onAddFormFields = (items) =>
	{
		return items.length < 10;
	};

	const addFormFieldsItem = (items) =>
	{
		const res = {
			name: '',
			label: '',
			type: '', 
			default: '',
			template: false,
			metatype: ''
		};

		setReport(report =>
		{
			const to = Object.assign({}, report);
			to.filter.form.fields = Array.from(to.filter.form.fields);
			to.filter.form.fields.push(res);
			
			return to;
		});

		return res;
	};
	
	const delFormFieldsItem = (item, index) =>
	{
		setReport(report =>
		{
			const to = Object.assign({}, report);
			to.filter.form.fields = to.filter.form.fields.filter((c, i) => i !== index);
			
			return to;
		});
	};

	const renderFieldsHeader = ({col}) =>
	{
		return (col.label);
	};

	const renderFieldsItemTypeOptions = () =>
	{
		return Object.entries(CollectionUtil.getReportFilterFieldValueTypes())
			.map(([key, type], index) => 
				<option value={key} key={index}>{type.name}</option>);
	};

	const filterFieldsOptions = (fields, index) =>
	{
		return props.columns; //.filter(col => !fields.find((f, i) => i !== index && f.column === col.name));
	};
	
	const renderFieldsItemColumns = (fields, index) =>
	{
		return [{name: '', label: ''}].concat(filterFieldsOptions(fields, index))
			.map((field, i) => 
				<option value={field.name} key={i}>{field.label}</option>);
	};

	const renderFieldsItemOps = (type) =>
	{
		return CollectionUtil.getFilterOps(type)
			.map((op, i) => 
				<option value={op.value} key={i}>{op.label}</option>);
	};
	
	const getColumnType = (name) => 
	{
		const col = CollectionUtil.getColumn(props.schema, name, null);
		return (col && (col.type !== 'array'? col.type : col.subtype)) || 'string';
	};

	const renderFieldsItem = ({item, index, col, isPreview}) => 
	{
		const noTypeOrValue = !item.op || item.op === 'isnull' || item.op === 'notnull';

		if(isPreview)
		{
			const value = item[col.name] || '';
			switch(col.name)
			{
			case 'name':
				return value? 
					getColumnLabel(value):
					'';
			
			case 'op':
				return CollectionUtil.getFilterOps().find(op => op.value === value).label;
			
			default:
				return value;
			}
	
		}

		switch(col.name)
		{
		case 'name':
			return (
				<Input 
					required
					type="select" 
					name={`filter.fields[${index}].name`}
					value={item.name || ''} 
					onChange={handleFieldsChange}>
					{renderFieldsItemColumns(report.filter.fields, index)}
				</Input>);

		case 'op':
			return (
				<Input 
					disabled={!item.name} 
					required
					type="select" 
					name={`filter.fields[${index}].op`}
					value={item.op || ''} 
					onChange={handleFieldsChange}>
					{renderFieldsItemOps(getColumnType(item.name))}
				</Input>);

		case 'values':
			return (
				<InputGroup>
					<InputGroupAddon addonType="prepend">
						<Input 
							disabled={noTypeOrValue} 
							type="select" 
							name={`filter.fields[${index}].type`}
							value={item.type || ''} 
							onChange={handleFieldsChange}>
							{renderFieldsItemTypeOptions()}
						</Input>
					</InputGroupAddon>
					<Input 
						disabled={noTypeOrValue} 
						type="text" 
						name={`filter.fields[${index}].values[0]`}
						value={item.values[0] || ''} 
						onChange={handleFieldsChange} />
					{item.op === 'between' &&
						<Input 
							disabled={noTypeOrValue}
							type="text" 
							name={`filter.fields[${index}].values[1]`}
							value={item.values[1] || ''} 
							onChange={handleFieldsChange}/>
					}
				</InputGroup>);
		}
	};

	const renderFormFieldsHeader = ({col}) =>
	{
		return (col.label);
	};

	const renderFormFieldsItemColumns = () =>
	{
		return [{name: '', label: ''}].concat(props.columns)
			.map((field, i) => 
				<option value={field.name} key={i}>{field.label}</option>);
	};

	const getColumnLabel = (value) =>
	{
		const col = props.columns.find(c => c.name === value);
		return col? col.label: '';
	}

	const renderFormFieldsItem = ({item, index, col, isPreview}) => 
	{
		if(isPreview)
		{
			const value = item[col.name] || '';
			switch(col.name)
			{
			case 'metatype':
				return value === 'column'? 
					'Campo': 
					'Modelo';

			case 'name':
				return item.metatype === 'column'?
					(value? 
						getColumnLabel(value):
						''):
					value;
			default:
				return value;
			}
	
		}
		
		switch(col.name)
		{
		case 'metatype':
			return (
				<Input 
					required
					type="select" 
					name={`filter.form.fields[${index}].metatype`}
					value={item.metatype} 
					onChange={handleFormFieldsChange}>
					<option value=""></option>
					<option value="column">Campo</option>
					<option value="template">Modelo</option>
				</Input>);

		case 'name':
			return (
				<InputGroup>
					<Input 
						disabled={!item.metatype}
						required
						type={item.metatype === 'column'? 'select': 'text'}
						name={`filter.form.fields[${index}].name`}
						value={item.name || ''} 
						onChange={handleFormFieldsChange}>
						{item.metatype === 'column'? renderFormFieldsItemColumns(): null}
					</Input>
					{item.metatype === 'template' &&
					<InputGroupAddon addonType="append">
						<Input 
							disabled={!item.metatype}
							required
							type="select" 
							name={`filter.form.fields[${index}].type`}
							value={item.type || ''} 
							onChange={handleFormFieldsChange}>
							{CollectionUtil.getFilterTypes()
								.map((t, index) => <option key={index} value={t.value}>{t.label}</option>)}
						</Input>
					</InputGroupAddon>
					}
				</InputGroup>);
		
		case 'op':
			return (
				<Input 
					disabled={item.metatype !== 'column' || !item.name}
					required
					type="select" 
					name={`filter.form.fields[${index}].op`}
					value={item.op || ''} 
					onChange={handleFormFieldsChange}>
					{CollectionUtil.getFilterOps(getColumnType(item.name))
						.map((t, index) => <option key={index} value={t.value}>{t.label}</option>)}
				</Input>);

		case 'default':
			return (
				<Input 
					disabled={item.metatype !== 'template' || !item.type}
					required
					type="text" 
					name={`filter.form.fields[${index}].default`}
					value={item.default || ''} 
					onChange={handleFormFieldsChange}>
				</Input>);
		
		case 'minMax':
			return (
				<InputGroup>
					<Input 
						disabled={item.metatype !== 'template' || item.type !== 'number'}
						required
						type="number" 
						name={`filter.form.fields[${index}].min`}
						value={item.min || ''} 
						onChange={handleFormFieldsChange}>
					</Input>
					<Input 
						disabled={item.metatype !== 'template' || item.type !== 'number'}
						required
						type="number" 
						name={`filter.form.fields[${index}].max`}
						value={item.max || ''} 
						onChange={handleFormFieldsChange}>
					</Input>
				</InputGroup>);
		}
	};
	
	const renderErrors = () =>
	{
		return errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	};

	if(!report)
	{
		return null;
	}

	return (
		<form>
			<Row className="mb-3">
				<Col sm="12">
					<SimpleCollapse isOpen={true} title="Formulário">
						<ExtensibleList
							columns={formColumns} 
							items={report.filter.form.fields} 
							renderHeader={renderFormFieldsHeader}
							renderItem={renderFormFieldsItem}
							onAddItem={onAddFormFields}
							addItem={addFormFieldsItem}
							deleteItem={delFormFieldsItem} />
					</SimpleCollapse>
				</Col>
			</Row>
			<Row className="mb-3">
				<Col sm="12">
					<SimpleCollapse isOpen={true} title="Pesquisa">
						<ExtensibleList 
							columns={fieldsColumns} 
							items={report.filter.fields} 
							renderHeader={renderFieldsHeader}
							renderItem={renderFieldsItem}
							onAddItem={onAddFields}
							addItem={addFieldsItem}
							deleteItem={delFieldsItem} />
					</SimpleCollapse>
				</Col>
			</Row>
			<Row>
				<Col md="12">
					{renderErrors()}
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
		</form>
	);
});

Filters.propTypes = 
{
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	schema: PropTypes.object,
	report: PropTypes.object,
	index: PropTypes.number,
};

export default Filters;