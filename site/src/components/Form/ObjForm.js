import React, { forwardRef, useCallback, useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Button, Label, Input, InputGroup, InputGroupAddon} from 'reactstrap';
import expr from 'expression-eval';
import memoize from 'fast-memoize';
import Misc from '../../libs/Misc';
import DomUtil from '../../libs/DomUtil';
import LogicalTree from '../../components/Tree/LogicalTree';
import ExtensibleList from '../../components/List/ExtensibleList';

import './ObjForm.css';

const fieldTypeToCompoType = {
	'string': 'text',
	'number': 'number',
	'decimal': 'text',
	'bool': 'checkbox',
	'date': 'text',
	'time': 'text',
	'datetime': 'text',
	'logtree': 'logtree',
	'object': 'list',
};

const lookupRef = (name, id, schema, refs) =>
{
	const key = JSON.stringify(schema.refs[name]);
	if(!refs.has(key))
	{
		return {};
	}

	return refs.get(key).find(ref => ref.id === id) || {};
};

const scriptingFunctions = (schema, refs) =>
{
	return {
		ref: (name, id) => lookupRef(name, id, schema, refs)
	};
};

// eslint-disable-next-line react/display-name, react/prop-types
const Form = forwardRef(({obj, disabled, isLoading, onSubmit, children}, innerRef) =>
{
	return (
		<form 
			ref={innerRef}	
			onSubmit={onSubmit}>
			{children}
			<Row>
				<Col xs="12">
					<Button
						className="float-right mt-2"
						color="primary"
						type="submit"
						disabled={disabled || isLoading}>
						{obj.id? 'Update': 'Create'}
					</Button>
				</Col>
			</Row>
		</form>
	);
});

const genDefaultFieldValue = (field) =>
{
	if(field.multiple)
	{
		return [];
	}
	
	switch(field.type)
	{
	case 'object':
		return {};

	default:
		return null;
	}
};

export const buildObjFields = (fields) =>
{
	return Object.keys(fields || {})
		.filter(field => !field.hidden)
		.reduce((obj, key) => 
		{
			obj[key] = genDefaultFieldValue(fields[key]); 
			return obj;
		}, 
		{});
};

const transformValueFromMultipleInputsForEditing = (inputs, value) =>
{
	if(!value)
	{
		return {type: '', value: null};
	}

	const input = inputs.find(inp => value[inp.type] !== undefined);

	return {
		type: input? 
			input.type: 
			'', 
		value: input? 
			value[input.type]: 
			null
	};
};

const transformValueForEditing = (value, field) =>
{
	switch(field.type)
	{
	case 'logtree':
		return LogicalTree.unpack(JSON.parse(value));
	case 'object':
		return value && !field.multiple?
			Object.entries(value).map(([key, value]) => ({
				key: key, 
				value: transformValueForEditing(
					value, field.fields['value'])
			})):
			value;
	default:
		return field.inputs?
			transformValueFromMultipleInputsForEditing(field.inputs, value):
			value;
	}
};

const compileMethod = memoize(script =>
{
	return expr.compile(script);
});

const compileMethods = (methods) =>
{
	const keys = Object.keys(methods);
	for(let i = 0; i < keys.length; i++)
	{
		const key = keys[i];
		methods[key].func = compileMethod(methods[key].script); 
	}
};

const transformSchemaForEditing = (schema) =>
{
	const clone = Misc.cloneObject(schema);
	if(clone.methods)
	{
		compileMethods(clone.methods);
	}

	if(clone.operations)
	{
		const keys = Object.keys(clone.operations);
		for(let i = 0; i < keys.length; i++)
		{
			const key = keys[i];
			if(clone.operations[key].methods)
			{
				compileMethods(clone.operations[key].methods);
			}
		}
	}
		
	return clone;
};

export const transformObjForEditing = (obj) =>
{
	const fields = obj.schemaObj.fields || {};
	const keys = Object.keys(obj.fields || {});

	return {
		...obj,
		schemaObj: transformSchemaForEditing(obj.schemaObj),
		fields: keys.reduce((res, key) => 
		{
			res[key] = transformValueForEditing(
				obj.fields[key], fields[key] || {});
			return res;
		}, {})
	};
};

const transformValueFromMultipleInputsForSubmitting = (value) =>
{
	if(!value || !value.type)
	{
		return null;
	}

	return {
		[value.type]: value.value
	};
};


const transformValueForSubmitting = (value, field) =>
{
	switch(field.type)
	{
	case 'logtree':
		return JSON.stringify(LogicalTree.pack(value));
	case 'object':
		return value && !field.multiple?
			value.reduce((obj, item) => {
				obj[item.key] = transformValueForSubmitting(
					item.value, field.fields['value']); 
				return obj;
			}, 
			{}):
			value;
	default:
		return field.inputs?
			transformValueFromMultipleInputsForSubmitting(value):
			value;
	}
};

export const transformObjForSubmitting = (obj) =>
{
	const fields = obj.schemaObj.fields;
	const keys = Object.keys(obj.fields || {});

	return {
		...obj,
		schema: undefined,
		schemaObj: undefined,
		fields: keys.reduce((res, key) => 
		{
			res[key] = transformValueForSubmitting(
				obj.fields[key], fields[key]);
			return res;
		}, {})
	};
};

export const getObjOutFields = (obj, operation, refs) =>
{
	const ops = obj.schemaObj.operations;
	if(!ops)
	{
		return [];
	}

	const out = ops[operation].out;
	if(out.fields)
	{
		return Object.entries(out.fields)
			.filter(([, field]) => !field.hidden)
			.map(([key, field]) => ({key: key, title: field.title || key}));
	}
		
	const methods = ops[operation].methods;
	if(!methods || !methods.getOutFields)
	{
		return [];
	}

	try
	{
		return methods.getOutFields.func({
			...scriptingFunctions(obj.schemaObj, refs),
			fields: obj.fields,
		});
	}
	catch(ex)
	{
		console.error(ex);
		return [];
	}
};

const ObjForm = (props) =>
{
	const [obj, setObj] = useState(() => !props.onChange? 
		transformObjForEditing(props.obj):
		null);
	const [isLoading, setIsLoading] = useState(false);
	const form = useRef();

	useEffect(() =>
	{
		if(!props.onChange)
		{
			setObj(transformObjForEditing(props.obj));
		}
	}, [props.obj]);

	const validate = () =>
	{
		return props.onValidate(obj, DomUtil.validateForm(form.current) || []);
	};

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		if(!validate())
		{
			return;
		}		

		try
		{
			setIsLoading(true);

			const res = await props.onSubmit(transformObjForSubmitting(obj));
			if(!res)
			{
				return;
			}

			setObj(transformObjForEditing(res));
		}
		finally
		{
			setIsLoading(false);
		}
	}, [obj]);

	const handleChange = useCallback((event) =>
	{
		const key = event.target.name || event.target.id;

		const value = event.target.type !== 'select-multiple'?
			(event.target.type !== 'checkbox'? 
				event.target.value:
				event.target.checked):
			Array.apply(null, event.target.options)
				.filter(option => option.selected)
				.map(option => option.value);

		if(!key)
		{
			setObj(value);
		}
		else
		{
			setObj(state => 
			{
				const obj = Object.assign({}, state);

				if(key.constructor !== Array)
				{
					Misc.setFieldValue(obj, key, value);
				}
				else
				{
					key.forEach((name, index) =>
						Misc.setFieldValue(obj, name, value[index])
					);
				}

				return obj;
			});
		}

	}, []);

	const buildFieldPrototype = useCallback((field) =>
	{
		return Object.keys(field.fields || {})
			.reduce((obj, key) => {
				obj[key] = ''; 
				return obj;
			}, 
			{});
	}, []);

	const renderSelectOptions = (input, schema) =>
	{
		if(!input)
		{
			return null;
		}
		
		if(input.options)
		{
			return [{value: '', title: ''}]
				.concat(input.options)
				.map((o, index) => 
					<option 
						key={index} 
						value={o.value}>
						{o.title}
					</option>
				);
		}
		
		if(!input.source)
		{
			return null;
		}

		const name = input.source.name.replace('.fields', '');
		const titleField = input.source.option.title;
		const valueField = input.source.option.value;
		if(schema.refs)
		{
			const key = JSON.stringify(schema.refs[name]);
			if(props.refs.has(key))
			{
				const refs = props.refs.get(key);

				return [{}]
					.concat(refs)
					.map((ref, index) => 
						<option 
							key={index} 
							value={(ref[valueField] !== undefined? 
								ref[valueField]:
								ref.id) || ''}>
							{ref[titleField] !== undefined? 
								ref[titleField]:
								ref.schemaObj? ref.schemaObj.title: ''}
						</option>
					);
			}
		}
			
		const options = props.getFieldOptions(name) || [];
		
		return [{}]
			.concat(options)
			.map((option, index) => 
				<option 
					key={index} 
					value={option[valueField] || ''}>
					{option[titleField]}
				</option>
			);
	};

	const renderFunctionInputOptions = (input) =>
	{
		if(!input)
		{
			return null;
		}
		const options = props.getFunctionOptions();
		
		return [{}]
			.concat(options)
			.map((option, index) => 
				<option 
					key={index} 
					value={option.value}>
					{option.title}
				</option>
			);
	};	

	const updateLogTree = useCallback((name, value, onChange) =>
	{
		onChange({
			target: {
				name: name,
				value: value
			}
		});
	}, []);

	const renderLogTreePreview = (node, field) =>
	{
		const fields = field.fields;
		
		const pairs = Object.keys(fields || {})
			.map(key => ({
				title: fields[key].title, 
				value: node[key]
			}));

		return pairs.map(pair => `${pair.title}=${pair.value}`).join(', ');
	};

	const renderLogTreeNode = (key, node, id, field, schema, onChange) =>
	{
		return renderFields(node, `${key}[${id}]`, field.fields, schema, onChange);
	};	

	const createLogTreeNode = (key, field, onChange) =>
	{
		onChange({
			target: {
				name: key,
				value: LogicalTree.unpack(
					LogicalTree.createPackedNode('and', buildFieldPrototype(field)))
			}
		});
	};

	const renderEmptyLogTree = (key, field, onChange) =>
	{
		return (
			<div>
				<span>None</span>&nbsp;
				<Button
					onClick={() => createLogTreeNode(key, field, onChange)}>
					Create condition
				</Button>
			</div>
		);
	};

	const renderExpListItem = ({index, item, col, isPreview}, key, field, schema, onChange) =>
	{
		const value = item[col.key];
		if(isPreview)
		{
			return value;
		}

		return renderField(
			`${key}[${index}].${col.key}`, 
			field.fields[col.key], 
			value, 
			schema, 
			onChange, 
			{containerless: true}
		);
	};

	const getExpListColumns = useCallback((field) =>
	{
		const fields = Object.entries(field.fields || {});
		const width = Math.floor(9 / (fields.length || 1));
		return fields.map(([key, field]) => ({
			key: key, 
			label: field.title || key, 
			width: field.width || width
		}));
	}, []);

	const addExpListItem = useCallback((key, value, onChange) =>
	{
		onChange({
			target: {
				name: key,
				value: value.concat([{key: '', value: null}])
			}
		});
	}, []);

	const delExpListItem = useCallback((key, value, index, onChange) =>
	{
		onChange({
			target: {
				name: key,
				value: value.filter((a, i) => i !== index)
			}
		});
	}, []);

	const renderField = (key, field, value, schema, onChange, options = {}) =>
	{
		if(!field.inputs)
		{
			return renderFieldInput(
				field.input || {}, 
				key, 
				field, 
				value, 
				schema, 
				onChange, 
				options
			);
		}
		else
		{
			return (
				<InputGroup>
					<InputGroupAddon addonType="prepend">
						<Input 
							type="select" 
							name={`${key}.type`} 
							value={value? value.type: ''} 
							onChange={onChange}>
							{[{type: '', title: ''}].concat(field.inputs).map((inp, index) => 
								<option key={index} value={inp.type}>
									{inp.title}
								</option>
							)}
						</Input>
					</InputGroupAddon>
					{renderFieldInput(
						value? field.inputs.find(inp => inp.type === value.type) || {}: {}, 
						`${key}.value`, 
						field, 
						value? value.value: null, 
						schema, 
						onChange, 
						options
					)}
				</InputGroup>
			);
		}
	};

	const renderFieldInput = (input, key, field, value, schema, onChange, options = {}) =>
	{
		let compo = null;

		const compoType = input.type?
			input.type:
			fieldTypeToCompoType[field.type] || 'text';

		switch(compoType)
		{
		case 'text':
		case 'email':
		case 'number':
		case 'password':
			compo = 
				<Input
					type={compoType}
					className={`of-input-${compoType}`}
					name={key}
					value={value || field.default || ''}
					placeholder={field.placeholder}
					required={field.required}
					disabled={field.disabled}
					title={field.desc}
					onChange={onChange}
				/>;
			break;

		case 'checkbox':
			compo = 
				<Input
					type="checkbox"
					className={`of-input-${compoType}`}
					name={key}
					checked={value || field.default || ''}
					required={field.required}
					disabled={field.disabled}
					title={field.desc}
					onChange={onChange}
				/>;
			break;

		case 'textarea':
			compo = 
				<Input
					type="textarea"
					name={key}
					value={value || field.default || ''}
					required={field.required}
					disabled={field.disabled}
					title={field.desc}
					rows={input.rows}
					onChange={onChange}
				/>;
			break;

		case 'select':
		case 'field':
			compo = 
				<Input
					type="select"
					name={key}
					value={value || field.default || ''}
					required={field.required}
					disabled={field.disabled}
					title={field.desc}
					onChange={onChange}
				>
					{renderSelectOptions(input, schema)}
				</Input>;
			break;

		case 'script':
			compo = 
				<Input
					type="text"
					name={key}
					value={value || field.default || ''}
					required={field.required}
					disabled={field.disabled}
					title={field.desc}
					onChange={onChange}
				/>;
			break;

		case 'function':
			compo = 
				<Input
					type="select"
					name={key}
					value={value || field.default || ''}
					required={field.required}
					disabled={field.disabled}
					title={field.desc}
					onChange={onChange}
				>
					{renderFunctionInputOptions(input, schema)}
				</Input>;
			break;

		case 'logtree':
			compo = 
				<LogicalTree 
					tree={value}
					updateTree={(value) => updateLogTree(key, value, onChange)}
					renderPreview={(node) => renderLogTreePreview(node, field)}
					renderNode={(node, id) => renderLogTreeNode(key, node, id, field, schema, onChange)}
					renderEmpty={() => renderEmptyLogTree(key, field, onChange)}
					getNodePrototype={() => buildFieldPrototype(field)}
					onChange={e => onChange({target:{...e.target, name: `${key}${e.target.name}`}})}
					className="modal-zindex"
				/>;
			break;
		
		case 'list':
			compo = 
				<ExtensibleList 
					alwaysMobile={props.isMobile}
					totalPreviewCols={1}
					useIndex
					columns={getExpListColumns(field)}
					items={value || []} 
					onChange={e => onChange({target:{...e.target, name: `${key}${e.target.name}`}})}
					addItem={() => addExpListItem(key, value || [], onChange)} 
					deleteItem={(item, index) => 
						delExpListItem(key, value, index, onChange)}
					renderItem={(args) => 
						renderExpListItem(args, key, field, schema, onChange) }
					renderHeader={({col}) => col.label}
				/>;
			break;
		}

		if(options.containerless)
		{
			return compo;
		}
		else
		{		
			return (
				<Col key={key} xs={field.width || 12}>
					<Label className="of-input-label" for={key}>{field.title || key}</Label>
					{compo}
				</Col>
			);
		}
	};

	const sortFields = (fields = {}) =>
	{
		return Object.entries(fields || {})
			.sort((a, b) => (a[1].index || 0) - (b[1].index || 0));
	};

	const renderFields = (values, base, fields, schema, onChange, options = {}) =>
	{
		const cols = [];

		const sorted = sortFields(fields);

		if(schema)
		{
			let cnt = 0;
			let compos = [];
			for(let i = 0; i < sorted.length; i++)
			{
				const key = sorted[i][0];
				const field = sorted[i][1];
				if(!field.hidden)
				{
					const value = values[key];
					const compo = renderField(`${base}.${key}`, field, value, schema, onChange, options);
					compos.push(compo);
					
					const width = field.width || 12;
					cnt += width;
					if(cnt >= 12)
					{
						cols.push(compos);
						compos = [];
						cnt = 0;
					}
				}
			}
		}

		if(options.containerless)
		{
			return (
				cols.map((col) => 
					col.map((compo) => 
						compo
					)
				)
			);
		}
		else
		{
			return (
				cols.map((col, index) => 
					<Row key={index}>
						{col.map((compo) => 
							compo
						)}
					</Row>
				)
			);
		}
	};

	const renderAll = (obj, onChange) =>
	{
		return (
			<>
				{props.onRenderBegin(obj, onChange)}
				{renderFields(obj.fields, 'fields', obj.schemaObj.fields, obj.schemaObj, onChange)}
				{props.onRenderEnd(obj, onChange)}
			</>			
		);
	};

	return (
		!props.onChange?
			<Form 
				ref={form}
				obj={obj}
				isLoading={isLoading}
				disabled={props.disabled}
				onSubmit={handleSubmit}>
				{renderAll(obj, handleChange)}
			</Form>:
			renderAll(props.obj, props.onChange)
	);
};

ObjForm.propTypes = {
	obj: PropTypes.object.isRequired,
	refs: PropTypes.instanceOf(Map).isRequired,
	disabled: PropTypes.bool,
	isMobile: PropTypes.bool,
	onRenderBegin: PropTypes.func,
	onRenderEnd: PropTypes.func,
	onValidate: PropTypes.func,
	onSubmit: PropTypes.func,
	onChange: PropTypes.func,
	getFieldOptions: PropTypes.func,
	getFunctionOptions: PropTypes.func,
};

ObjForm.defaultProps = 
{
	onRenderBegin: () => null,
	onRenderEnd: () => null,
	onValidate: () => true,
	onSubmit: (obj) => obj,
	getFieldOptions: () => [],
	getFunctionOptions: () => [],
};

export default ObjForm;
