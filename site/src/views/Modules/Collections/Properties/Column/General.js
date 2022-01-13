import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import FieldsForm, {transformForEditing, transformForSubmitting} from '../../Properties/FieldsForm';
import Misc from '../../../../../libs/Misc';
import { Row, Col, Button } from 'reactstrap';

const General = observer(props =>
{
	const store = props.store;
	
	const [isLoading, setIsLoading] = useState(false);
	const [errors, setErrors] = useState([]);
	const [column, setColumn] = useState(() => transformForEditing(
		store.collection.schemaObj, props.name, props.column, store.references));

	useEffect(() =>
	{
		setColumn(transformForEditing(
			store.collection.schemaObj, props.name, props.column, store.references));
	}, [store.collection, props.name, props.column]);

	const handleChange = useCallback((event, relatedInputToCleanup = null) =>
	{
		const key = event.target.name || event.target.id;
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;
		
		setColumn(column =>
		{
			const to = Object.assign({}, column);
		
			Misc.setFieldValue(to, key, value);
	
			if(relatedInputToCleanup)
			{
				Misc.setFieldValue(to, relatedInputToCleanup, null);
			}
			
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
			
			const schema = store.collection.schemaObj;
			const from = transformForEditing(
				schema, props.name, props.column, store.references);
			const to = column;
			
			if(!Misc.compareObjects(from, to))
			{
				const {column} = transformForSubmitting(to, errors);
				if(errors.length === 0)
				{
					const hasNameChanged = from.label !== to.label;
					
					const res = await store.updateField(props.name, column);
					if(res.errors)
					{
						res.errors.forEach(err => errors.push(err));
					}
					else
					{
						props.container.showMessage('Atualização realizada no campo!', 'success');

						if(hasNameChanged)
						{
							props.onNameChanged(res.data.key);
						}
					}
				}
			}
		}
		finally 
		{
			setIsLoading(false);
			setErrors(errors);
		}		
	}, [column]);

	const renderErrors = () =>
	{
		return errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	};

	if(!column)
	{
		return null;
	}

	return (
		<form>
			<Row>
				<Col sm="12">
					<FieldsForm
						store={store}
						column={column}
						onChange={handleChange}
					/>
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
						className="mt-4">
							Atualizar
					</Button>
				</Col>
			</Row>
		</form>
	);
});

General.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	viewId: PropTypes.string.isRequired,
	name: PropTypes.string,
	column: PropTypes.object,
	field: PropTypes.object,
	isProp: PropTypes.bool,
	onNameChanged: PropTypes.func,
};

export default General;