import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Button} from 'reactstrap';
import Misc from '../../../../libs/Misc';
import FieldsList, {transformForEditing} from './FieldsList';
import {transformForSubmitting} from './FieldsForm';

const FieldsPanel = observer(props =>
{
	const store = props.store;
	
	const [fields, setFields] = useState(() => 
		transformForEditing(
			store.collection.schemaObj, 
			store.collection.schemaObj.columns,
			store.references));
	const [errors, setErrors] = useState([]);
	const [loading, setLoading]	= useState(false);
			
	useEffect(() =>
	{
		if(props.visible)
		{
			setFields(
				transformForEditing(
					store.collection.schemaObj, 
					store.collection.schemaObj.columns,
					store.references)
			);
		}
	}, [store.collection, store.references]);

	const handleChange = useCallback((event, relatedInputToCleanup = null) =>
	{
		const key = event.target.name || event.target.id;
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;

		setFields(fields =>
		{
			const to = Array.from(fields);
		
			Misc.setFieldValue(to, key, value);

			if(relatedInputToCleanup)
			{
				Misc.setFieldValue(to, relatedInputToCleanup, null);
			}
			
			return to;
		});
	}, []);

	const handleCreate = useCallback((item) =>
	{
		setFields(fields => 
		{
			const to = Array.from(fields);
			to.push(item);
			return to;
		});
	}, []);

	const handleDelete = useCallback((item, index) =>
	{
		setFields(fields => 
			fields.filter((c, i) => i !== index));
	}, []);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setLoading(true);
			
			const from = transformForEditing(
				store.collection.schemaObj, 
				store.collection.schemaObj.columns, 
				props.references);
			const to = fields;
			const columns = {};
			
			const errors = [];
			let changeCnt = 0;

			if(!Misc.compareArrays(from, to))
			{
				++changeCnt;

				if(to.length === 0)
				{
					errors.push('Ao menos um campo deve ser definido');
				}

				for(let i = 0; i < to.length; i++)
				{
					const {name, column} = transformForSubmitting(to[i], errors);
					if(errors.length > 0)
					{
						errors.forEach((err, index) => 
							errors[index] = `Campos[${1+i}].${err}`);
					}
					else
					{
						columns[name] = column;	
					}
				}
			}

			setErrors(errors);
			if(changeCnt > 0 && errors.length === 0)
			{
				const res = store.updateFields(columns);
				setErrors(res.errors || []);
				if(!res.errors)
				{
					props.container.showMessage('Atualização de campos realizada!', 'success');
				}
			}
		} 
		finally 
		{
			setLoading(false);
		}

		return false;
	}, [fields]);

	const renderErrors = () =>
	{
		return errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	};

	if(!props.visible)
	{
		return null;
	}

	return (
		<form>
			<Row>
				<Col sm="12">
					<FieldsList
						{...props}
						columns={fields}
						onChange={handleChange}
						onCreate={handleCreate}
						onDelete={handleDelete} />
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
						disabled={loading}
						type="submit"
						onClick={handleSubmit}
						className="mt-4">
							Atualizar
					</Button>
				</Col>
			</Row>
		</form>
	);
});

FieldsPanel.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
};

export default FieldsPanel;

