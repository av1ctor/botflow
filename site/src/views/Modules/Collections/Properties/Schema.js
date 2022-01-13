import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Button} from 'reactstrap';
import JsonEditor from '../../../../components/Editor/JsonEditor';

const transformForEditing = (collection) =>
{
	return collection? 
		(' ' + JSON.stringify(collection.schemaObj, null, 2)).slice(1):
		null;
};

const Schema = observer((props) =>
{
	const store = props.store;
	
	const [schema, setSchema] = useState(() => transformForEditing(store.collection));
	const [loading, setLoading]	= useState(false);

	useEffect(() =>
	{
		setSchema(transformForEditing(store.collection));
	}, [store.collection]);

	const handleChange = (event) =>
	{
		const to = (' ' + event.target.value).slice(1);

		setSchema(to);
	};

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setLoading(true);
			
			const fromSchema = transformForEditing(store.collection);
			const toSchema = schema;

			let changeCnt = 0;
			if(fromSchema !== toSchema)
			{
				++changeCnt;
			}
			
			if(changeCnt > 0)
			{
				const res = await store.updateSchema(JSON.parse(toSchema), {rebuild: true});
				if(res.errors)
				{
					props.container.showMessage(res.errors, 'error');
				}
				else
				{
					props.container.showMessage('Atualização de schema realizada!', 'success');
				}
			}
		} 
		finally 
		{
			setLoading(false);
		}

		return false;
	}, [schema]);

	if(!schema || !props.visible)
	{
		return null;
	}

	return (
		<>
			<Row>
				<Col sm="12">
					<Label className="base-form-label"><b>Schema</b></Label>
					<JsonEditor 
						name="schema" 
						value={schema || ''} 
						handleOnChange={handleChange} />
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Button
						color="primary"
						disabled={loading}
						type="submit"
						onClick={handleSubmit}
						className="mt-4"
					>Atualizar</Button>
				</Col>
			</Row>
		</>
	);
});

Schema.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
};

export default Schema;