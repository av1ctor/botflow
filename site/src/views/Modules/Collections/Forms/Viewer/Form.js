import React, { useCallback, useContext, useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import Misc from '../../../../../libs/Misc';
import DomUtil from '../../../../../libs/DomUtil';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import CollectionLogic from '../../../../../business/CollectionLogic';
import Steps from './Steps';
import {SessionContext} from '../../../../../contexts/SessionContext';

const genInputValueFromRef = (ref) =>
{
	const fields = {}; 
	ref.preview.columns.forEach(name => 
	{
		fields[name] = '';
	});

	return {[ref.key]: '', ...fields};
};

const isFieldDisabled = (field, value, step, form, user) =>
{
	switch(form.use)
	{
	case 'create':
		return CollectionUtil.isDisabledForInsert(field, user);

	case 'edit':
		return step === 0? 
			false:
			CollectionUtil.isDisabledForUpdate(field, value, user);

	case 'view':
		return true;
	
	default:
		return false;
	}
};

const transformFieldForEditing = (elm, step, form, schema, references, user, disabled) =>
{
	const field = CollectionUtil.getColumn(schema, elm.name, references) || {};

	const type = field.type || 'string';
	const compo = field.component || '';

	const ref = field.ref && schema.refs[field.ref.name];

	const value = ref? 
		genInputValueFromRef(ref):
		elm.default_?
			elm.default_.value || '':
			'';

	const res = {
		label: elm.label || CollectionUtil.getColumnLabel(schema, elm.name, references),
		value: value,
		disabled: disabled || elm.disabled || isFieldDisabled(field, value, step, form, user), 
		ref: ref,
		field: field
	};

	if(ref)
	{
		res.type = 'auto';
	}
	else
	{
		switch(compo)
		{
		case 'select':
		case 'grid':
			res.type = 'select';
			res.options = CollectionUtil.buildListForColumn(field);
			break;
		case 'rating':
			res.type = 'number';
			break;
		case 'textarea':
			res.type = 'textarea';
			break;
		case 'bool':
			res.type = 'checkbox';
			break;
		default:
			switch(type)
			{
			case 'date':
				res.type = 'text';
				break;
			case 'enumeration':
				res.type = 'select';
				res.options = CollectionUtil.buildListForColumn(field);
				break;
			case 'array':
				res.type = 'text';
				break;
			case 'bool':
				res.type = 'checkbox';
				break;
			case 'object':
				res.type = 'text';
				res.disabled = true;
				break;
			case 'number':
				res.type = 'number';
				break;
			default:
				res.type = 'text';
				break;
			}
		}		
	}

	return res;
};

const findFields = (elements) =>
{
	let res = elements
		.filter(elm => elm.type === 'field');

	for(const elm of elements)
	{
		if(elm.elements)
		{
			res = res.concat(findFields(elm.elements));
		}
	}

	return res;
};

export const transformStepsForEditing = (form, schema, references, user, disabled = false) =>
{
	const res = [];
	
	let i = 0;
	for(const step of form.steps)
	{
		if(step.elements)
		{
			const fields = findFields(step.elements);
			
			res.push(fields
				.filter(elm => !!elm.name)
				.reduce(
					(obj, elm) => ({
						...obj, 
						[elm.name]: transformFieldForEditing(elm, i, form, schema, references, user, disabled)
					}), 
					{}
				));
		}
		++i;
	}

	return res;
};

export const transformStepsForSubmitting = (steps, form) =>
{
	const res = {};

	steps.forEach((fields, step) => 
		Object.keys(fields).forEach(name =>
		{
			if(!form.use === 'edit' || step > 0)
			{
				const field = steps[step][name];
				const value = !field.ref? 
					field.value: 
					field.value[field.ref.key];

				if(value !== null && value !== '')
				{
					res[name] = value;
				}
			}
		})
	);
	
	return res;
};

export const transformElementsForEditing = (elements) =>
{
	return elements.map(elm => ({
		...elm,
		default_: elm.default_? 
			elm.default_.indexOf('${') >= 0?
				{func: Misc.compileExpr(Misc.stringToExpr(elm.default_))}:
				{value: elm.default_}:
			undefined,
		elements: elm.elements?
			transformElementsForEditing(elm.elements):
			undefined
	}));
};

const transformSchemaForEditing = (schema) =>
{
	return {
		...schema,
		forms: (schema.forms || []).map(form => ({
			...form,
			steps: form.steps.map(step => ({
				...step,
				elements: step.elements?
					transformElementsForEditing(step.elements):
					undefined
			}))
		}))
	};
};

export const transformRefSuggestions = (schema) =>
{
	const res = {};

	Object.entries(schema.columns).forEach(([name, column]) =>
	{
		if(column.ref)
		{
			res[name] = [];
		}
	});

	return res;
};

const Form = (props) =>
{
	const session = useContext(SessionContext);

	const [schema, setSchema] = useState(null);
	const [references, setReferences] = useState([]);
	const [errors, setErrors] = useState(null);
	const [steps, setSteps] = useState(null);
	const [step, setStep] = useState(0);
	const [id, setId] = useState(null);
	const [refSuggestions, setRefSuggestions] = useState({});

	const formElement = useRef();

	const loadForm = async () =>
	{
		const res = await props.api.get(
			`public/collections/${props.collectionId}/forms/${props.formId}`);
		setErrors(res.errors);
		if(!res.errors)
		{
			const schema = transformSchemaForEditing(res.data.schema);
			const refs = await CollectionLogic.loadReferences(schema, null, false);
			const steps = transformStepsForEditing(
				schema.forms[0] || {}, schema, refs, session.user, props.disabled);
			setId(null);
			setStep(0);
			setSteps(steps);
			setSchema(schema);
			setReferences(refs);
			setRefSuggestions(transformRefSuggestions(schema));
		}
	};
	
	useEffect(() => 
	{
		loadForm();		
	}, [props.formId]);

	const handleNextStep = useCallback((e) =>
	{
		e.preventDefault();

		const form = schema.forms[0];

		if(form.use !== 'view')
		{
			const errors = DomUtil.validateForm(formElement.current);
			if(errors.length > 0)
			{
				return;
			}
		}

		setStep(step => 
		{
			if(step < form.steps.length-1)
			{
				return step + 1;
			}

			return step;
		});
	}, [schema]);

	const handlePrevStep = useCallback((e) =>
	{
		e.preventDefault();
		
		setStep(step => 
		{
			if(step > 0)
			{
				setStep(step - 1);
			}

			return step;
		});
	}, [schema]);

	const handleChange = useCallback(e =>
	{
		const name = e.target.name || e.target.id;
		const value = e.target.type !== 'checkbox' ? 
			e.target.value : 
			e.target.checked;

		setSteps(steps =>
		{
			const to = Array.from(steps);

			to[step][name].value = value;

			return to;
		});
	}, [steps, step]);

	const handleSubmit = useCallback(async (e) =>
	{
		e.preventDefault();

		const errors = DomUtil.validateForm(formElement.current);
		if(errors.length > 0)
		{
			return;
		}

		const form = schema.forms[0];

		const res = form.use !== 'edit'? 
			await props.api.post(
				`collections/${props.collectionId}/items`, 
				{
					vars: transformStepsForSubmitting(steps, form)
				}):
			await props.api.patch(
				`collections/${props.collectionId}/items/${id}`, 
				{
					vars: transformStepsForSubmitting(steps, form)
				});
		
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		props.container.showMessage(
			form.use !== 'edit'?
				'Item added to the collection!':
				'Item updated!', 
			'success');
		
		setSteps(transformStepsForEditing(schema.forms[0], schema, references, session.user, props.disabled));
		setRefSuggestions(transformRefSuggestions(schema));
		setStep(0);
		setId(null);
	}, [schema, references, session.user, steps, id]);

	const handleSearch = useCallback(async (e) =>
	{
		e.preventDefault();

		const errors = DomUtil.validateForm(formElement.current);
		if(errors.length > 0)
		{
			return;
		}

		const filters = [];
		Object.keys(steps[0]).forEach(name =>
		{
			const field = steps[0][name];
			const value = !field.ref? 
				field.value: 
				field.value[field.ref.key];

			if(value !== null && value !== '')
			{
				filters.push({
					name: name,
					op: (field.field.type || 'string') === 'string'? 
						'like': 
						'eq',
					value: value
				});
			}
		});
	
		const filtersStr = encodeURIComponent(JSON.stringify(filters));
		
		const res = await props.api.get(
			`collections/${props.collectionId}/items?page=0&size=1&filters=${filtersStr}`);

		if(res.errors)
		{
			props.container.showMessage(res.errors, 'errors');
			return;
		}

		if(res.data.length === 0)
		{
			props.container.showMessage('Item not found', 'error');
			return;
		}

		const item = res.data[0];

		setSteps(steps =>
		{
			const to = Array.from(steps);

			for(const step of to)
			{
				Object.entries(step).forEach(([name, field]) => 
				{
					if(name in item)
					{
						field.value = item[name];
					}
				});
			}

			return to;
		});

		setId(item._id);

		if(steps.length > 1)
		{
			setStep(1);
		}
	}, [schema, steps]);

	const handleRefFilter = useCallback(async (e, name, ref) =>
	{
		const filters = [];
		ref.filters.forEach(filter =>
		{
			filters.push({
				name: filter.column,
				op: filter.op || 'like',
				value: e.query
			});
		});
		
		const filtersStr = encodeURIComponent(JSON.stringify(filters));
		
		const res = await props.api.get(
			`collections/${ref.collection}/items?page=0&size=20&sort=${ref.filters[0].column},asc&filters=${filtersStr}`);

		if(!res.errors)
		{
			setRefSuggestions(autoSuggestions =>
			{
				const to = Object.assign({}, autoSuggestions);

				to[name] = res.data;
				
				return to;
			});
		}
	}, []);

	const renderErrors = (errors) =>
	{
		return errors.map((err, index) => 
			<div key={index}>{JSON.stringify(err)}</div>);
	};

	const form = schema && schema.forms && schema.forms.length > 0? 
		schema.forms[0]:
		null;
	
	return (
		<form 
			ref={formElement}>
			{form && 
				<Steps 
					container={props.container}	
					form={form} 
					steps={steps}
					step={step}
					refs={schema.refs}
					refSuggestions={refSuggestions}
					onChange={handleChange}
					onRefFilter={handleRefFilter}
					onPrevStep={handlePrevStep}
					onNextStep={handleNextStep}
					onSearch={handleSearch}
					onSubmit={handleSubmit}
				/>
			}
			{renderErrors(errors || [])}
		</form>
	);
};

Form.propTypes = {
	api: PropTypes.object,
	container: PropTypes.object,
	collectionId: PropTypes.string.isRequired,
	formId: PropTypes.string.isRequired,
	disabled: PropTypes.bool
};

export default Form;