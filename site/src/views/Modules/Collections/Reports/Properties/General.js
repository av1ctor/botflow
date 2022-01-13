import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button} from 'reactstrap';
import Misc from '../../../../../libs/Misc.js';

const General = observer(props =>
{
	const store = props.store;
	
	const [isLoading, setIsLoading] = useState(false);
	const [errors, setErrors] = useState([]);
	const [report, setReport] = useState(() => Misc.cloneObject(props.report));

	useEffect(() =>
	{
		setReport(Misc.cloneObject(props.report));
	}, [props.report]);

	const handleChange = useCallback((event) =>
	{
		const key = event.target.name;
		const value = event.target.type !== 'checkbox'? 
			event.target.value: 
			event.target.checked;

		setReport(report =>
		{
			const to = Object.assign({}, report);
	
			Misc.setFieldValue(to, key, value);
	
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
			
			const schema = props.schema;
			const from = schema.reports[props.index];
			const to = report;

			let changeCnt = 0;

			if(!to.name)
			{
				errors.push('Nome do relatório deve ser definido');
			}
			else if(from.name !== to.name)
			{
				if(schema.reports.find(v => v.name.toLowerCase() === to.name.toLowerCase()))
				{
					errors.push('Já existe um relatório com o mesmo name na coleção');
					return false;
				}
				++changeCnt;
			}
			
			if(!to.description)
			{
				errors.push('Descrição do relatório deve ser definida');
			}
			else if(from.description !== to.description)
			{
				++changeCnt;
			}
	
			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await store.updateReport(props.index, to);
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
	}, [report]);

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
					<Label className="base-form-label">Nome</Label>
					<Input 
						required 
						name="name" 
						type="text" 
						value={report.name || ''} 
						onChange={handleChange} />
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Descrição</Label>
					<Input 
						required 
						name="description" 
						type="textarea" 
						rows="4" 
						value={report.description || ''} 
						onChange={handleChange} />
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

General.propTypes = 
{
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	schema: PropTypes.object,
	report: PropTypes.object,
	index: PropTypes.number,
};

export default General;