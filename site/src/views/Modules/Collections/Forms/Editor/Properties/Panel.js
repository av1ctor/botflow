import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../../../components/Modal/ModalScrollable';
import General from './General';
import Misc from '../../../../../../libs/Misc';
import {transformForSubmitting} from '../Panel';

const lut = {
	'general': General,
}; 

const Panel = observer(props =>
{
	const store = props.store;
	const collection = store.collection;

	const [isLoading, setIsLoading] = useState(false);
	const [errors, setErrors] = useState([]);
	const [form, setForm] = useState(props.form? Object.assign({}, props.form): null);
	
	useEffect(() =>
	{
		if(props.visible)
		{
			setForm(props.form? Object.assign({}, props.form): null);
		}
		
	}, [props.visible, props.form]);

	const handleChange = useCallback((e) =>
	{
		const name = e.target.name || e.target.id;
		const value = e.target.type !== 'checkbox'? 
			e.target.value: 
			e.target.checked;

		setForm(form =>
		{
			const to = Object.assign({}, form);

			Misc.setFieldValue(to, name, value);

			return to;
		});
	}, []);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		const errors = [];

		try 
		{
			setIsLoading(true);
			
			const schema = collection.schemaObj;
			const from = schema.forms?
				schema.forms.find(f => f.id === form.id) || {}:
				{};
			const to = transformForSubmitting(form);

			let changeCnt = 0;

			if(!Misc.compareObjects(from, to))
			{
				++changeCnt;
			}
			
			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await (to.id? 
					store.updateForm(props.index, to):
					store.createForm(to, props.index));
				if(res.errors)
				{
					errors.push(...res.errors);
				}
				else
				{
					props.container.showMessage(`Form ${to.id? 'updated': 'created'}!`, 'success');
				}
			}

			return false;
		} 
		finally 
		{
			setIsLoading(false);
			setErrors(errors);
		}
	}, [form]);	

	if(!props.visible || !form)
	{
		return null;
	}

	const Compo = lut[props.compo];

	return (
		<ModalScrollable 
			isOpen={props.visible}
			onHide={props.onHide}
			lockFocus
			icon={props.icon} 
			title={`Form "${form.name}" > Props > ${props.title}`} 
		>
			<Compo
				store={props.store}
				form={form}
				isLoading={isLoading}
				onChange={handleChange}
				onSubmit={handleSubmit}
			/>
		</ModalScrollable>
	);
});

Panel.propTypes = 
{
	container: PropTypes.object,
	store: PropTypes.object,
	visible: PropTypes.bool,
	form: PropTypes.object,
	index: PropTypes.number,
	onHide: PropTypes.func.isRequired,
};

export default Panel;