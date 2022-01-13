import React, {useCallback, useContext, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import Steps from '../../Forms/Viewer/Steps';
import { transformStepsForEditing } from '../../Forms/Viewer/Form';
import { SessionContext } from '../../../../../contexts/SessionContext';

const fillValues = (steps, item) =>
{
	steps.forEach(step =>
		Object.entries(step).forEach(([name, field]) => 
		{
			if(name in item)
			{
				field.value = item[name];
			}
		})
	);

	return steps;
};

const View = observer(props =>
{
	const session = useContext(SessionContext);
	const store = props.store;

	const [form, setForm] = useState(null);
	const [steps, setSteps] = useState(null);
	const [step, setStep] = useState(0);

	useEffect(() =>
	{
		if(props.visible && props.item)
		{
			const schema = store.collection.schemaObj;
			const form = (schema.forms || []).find(f => f.use === 'view') || {steps: []};
			setForm(form);
			setSteps(
				fillValues(
					transformStepsForEditing(
						form, schema, store.references, session.user), props.item));
			setStep(0);
		}
	}, [props.visible, props.item]);

	const handleNextStep = useCallback((e) =>
	{
		e.preventDefault();

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

	if(!props.visible)
	{
		return null;
	}

	const collection = store.collection;
	if(!collection || !form)
	{
		return null;
	}

	return (
		<Steps 
			container={props.container}
			form={form}
			steps={steps}
			step={step}
			refs={store.references}
			onPrevStep={handlePrevStep}
			onNextStep={handleNextStep}
		/>
	);
});

View.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	item: PropTypes.object.isRequired,
};

export default View;

