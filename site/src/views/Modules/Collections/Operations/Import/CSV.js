import React, {useCallback, useContext, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Button, Label, Input} from 'reactstrap';
import { SessionContext } from '../../../../../contexts/SessionContext';

const CSV = observer(props =>
{
	const session = useContext(SessionContext);
	const store = props.store;

	const [form, setForm] = useState({
		csv: '',
		replace: true
	});

	const handleOnChange = (event) =>
	{
		const to = Object.assign({}, form);

		const value = event.target.type !== 'checkbox' ? 
			event.target.value : 
			event.target.checked;

		to[event.target.name] = value;

		setForm(to);
	};

	const handleSubmit = useCallback(async () =>
	{
		const res = await store.createItemsFromCSV(form.csv, form.replace, session.user);
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		props.container.showMessage('Todos os itens foram adicionados à coleção!', 'success');
	}, []);

	return (
		<div className="w-100 h-100">
			<Row className="mt-2">
				<Col sm="12">
					<Label className="base-form-label">
						Dados em formato CSV <small>(cabeçalho na primeira linha, separador: vírgula, delimitador: aspas duplas, uma linha para cada registro)</small>
					</Label>
					<Input 
						required 
						name="csv" 
						type="textarea" 
						rows="16" 
						value={form.csv} 
						onChange={handleOnChange} 
					/>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Button 
						color="primary" 
						className="mt-2" 
						type="submit"
						disabled={form.csv.length === 0} 
						onClick={handleSubmit}>
						Enviar
					</Button>
				</Col>
			</Row>
		</div>
	);
});

CSV.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
};

export default CSV;
