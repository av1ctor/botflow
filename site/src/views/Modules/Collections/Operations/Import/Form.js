import React, {useCallback, useContext, useEffect, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import DomUtil from '../../../../../libs/DomUtil';
import Steps from '../../Forms/Viewer/Steps';
import { 
	transformRefSuggestions, 
	transformStepsForEditing, 
	transformStepsForSubmitting, 
	transformElementsForEditing 
} from '../../Forms/Viewer/Form';
import { SessionContext } from '../../../../../contexts/SessionContext';

const transformFormForEditing = (form) =>
{
	return {
		...form,
		steps: form.steps.map(step => ({
			...step,
			elements: step.elements?
				transformElementsForEditing(step.elements):
				undefined
		}))
	};
};

const Form = observer(props =>
{
	const session = useContext(SessionContext);
	const store = props.store;

	const [form, setForm] = useState(null);
	const [steps, setSteps] = useState(null);
	const [step, setStep] = useState(0);
	const [refSuggestions, setRefSuggestions] = useState({});

	const formElement = useRef();

	const loadForm = (schema) =>
	{
		const form =  transformFormForEditing(
			(schema.forms || []).find(f => f.use === 'create') || {steps: []});
		setForm(form);
		setSteps(
			transformStepsForEditing(
				form, schema, store.references, session.user));
		setStep(0);
		setRefSuggestions(transformRefSuggestions(schema));
	};
	
	useEffect(() =>
	{
		loadForm(store.collection.schemaObj);
	}, []);

	useEffect(() =>
	{
		loadForm(store.collection.schemaObj);
	}, [store.collection]);

	const handleNextStep = useCallback((e) =>
	{
		e.preventDefault();

		const errors = DomUtil.validateForm(formElement.current);
		if(errors.length > 0)
		{
			return;
		}

		setStep(step => 
		{
			if(step < form.steps.length-1)
			{
				return step + 1;
			}

			return step;
		});
	}, [form]);

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
	}, [form]);

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

		const schema = store.collection.schemaObj;

		const res = await props.api.post(
			`collections/${store.collection.id}/items`, 
			{
				vars: transformStepsForSubmitting(steps, form)
			});
		
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		props.container.showMessage('Item added to the collection!', 'success');
		
		setSteps(transformStepsForEditing(form, schema, store.references, session.user));
		setRefSuggestions(transformRefSuggestions(schema));
		setStep(0);
	}, [store.collection, store.references, session.user, steps]);

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

	const collection = store.collection;
	if(!collection)
	{
		return null;
	}

	return (
		<form ref={formElement} >
			{form && 
				<Steps 
					container={props.container}
					form={form}
					steps={steps}
					step={step}
					refs={store.references}
					refSuggestions={refSuggestions}
					onPrevStep={handlePrevStep}
					onNextStep={handleNextStep}
					onChange={handleChange}
					onRefFilter={handleRefFilter}
					onSubmit={handleSubmit}
				/>
			}
		</form>
	);
});

Form.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
};

export default Form;