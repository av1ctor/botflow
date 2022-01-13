import React, {useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button} from 'reactstrap';
import Misc from '../../../../../libs/Misc.js';
import InputMultiSelectObject from '../../../../../components/Input/InputMultiSelectObject.js';

const transformYAxisOptions = (report) =>
{
	return report.columns? 
		Object.entries(report.columns)
			.filter(([, col]) => !!col.op)
			.map(([k, col]) => ({value: k, label: col.label}))
		:[];
};

const transformReport = (report, clone = true) =>
{
	const res = report?
		(clone? Misc.cloneObject(report): report):
		{};

	if(!res.chart)
	{
		res.chart = {
			type: '',
			x: {column: ''},
			y: {columns: []}
		};
	}

	return res;
};

const Chart = observer(props =>
{
	const store = props.store;
	
	const [yAxisOptions, setYAxisOptions] = useState(() => transformYAxisOptions(props.report));
	const [yAxisSuggestions, setYAxisSuggestions] = useState(() => transformYAxisOptions(props.report));
	const [isLoading, setIsLoading] = useState(false);
	const [errors, setErrors] = useState([]);
	const [report, setReport] = useState(() => transformReport(props.report));

	useEffect(() =>
	{
		const options = transformYAxisOptions(props.report);
		setYAxisOptions(options);
		setYAxisSuggestions(options);

		setReport(transformReport(props.report));
	}, [props.report]);

	const handleChange = (event, targetName = null, targetValue = null) =>
	{
		const key = targetName || event.target.name;

		const value = targetValue ||
			(event.target.type !== 'checkbox'? 
				event.target.value: 
				event.target.checked);
		
		setReport(report =>
		{
			const to = Object.assign({}, report);
			Misc.setFieldValue(to, key, value);
			return to;
		});
	};

	const handleYAxisChange = (event) => 
	{
		handleChange(
			event, 
			'chart.y.columns', 
			event.target.value? 
				event.target.value.filter(value => !!value).map(value => value.value):
				[]);
	};

	const handleSubmit = async (event) =>
	{
		event.preventDefault();

		const errors = [];

		try 
		{
			setIsLoading(true);
			
			const schema = props.schema;
			const from = transformReport(schema.reports[props.index], false);
			const fromChart = from.chart;
			const toChart = report.chart;

			let changeCnt = 0;
			if(!toChart.type)
			{
				errors.push('Tipo do gráfico deve ser definido');
			}
			else if(fromChart.type !== toChart.type)
			{
				++changeCnt;
			}

			if(!toChart.x.column)
			{
				errors.push('Campo do eixo X deve ser definido');
			}
			else if(fromChart.x.column !== toChart.x.column)
			{
				++changeCnt;
			}
			
			if(toChart.y.columns.length === 0)
			{
				errors.push('Campos do eixo Y devem ser definidos');
			}
			else if(!Misc.compareArrays(fromChart.y.columns, toChart.y.columns))
			{
				++changeCnt;
			}
			
			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await store.updateReport(props.index, report);
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
	};

	const renderXAxisOptions = () =>
	{
		const columns = props.report.columns? 
			Object.entries(props.report.columns)
				.filter(([, col]) => !col.op)
				.map(([k, col]) => ({value: k, label: col.label}))
			:[];

		return [{value: '', label: ''}]
			.concat(columns).map((col, index) =>
				<option key={index} value={col.value}>{col.label}</option>
			);
	};

	const renderErrors = () =>
	{
		return errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	};

	if(!report)
	{
		return null;
	}

	return (
		<form>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Tipo</Label>
					<Input 
						required 
						name="chart.type" 
						type="select" 
						value={(report.chart && report.chart.type) || ''} 
						onChange={handleChange}>
						<option value=""></option>
						<option value="line">Linha</option>
						<option value="bar">Campos</option>
						<option value="pie">Pizza</option>
						<option value="doughnut">Rosquina</option>
					</Input>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Campo do eixo X</Label>
					<Input 
						required 
						name="chart.x.column" 
						type="select" 
						value={(report.chart && report.chart.x.column) || ''} 
						onChange={handleChange}>
						{renderXAxisOptions()}
					</Input>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Campos do eixo Y</Label>
					<InputMultiSelectObject
						options={yAxisSuggestions} 
						values={((report.chart && report.chart.y.columns) || [])
							.map(value => yAxisOptions.find(opt => opt.value === value))} 
						objectKey="value"
						objectLabel="label"
						onChange={handleYAxisChange} 
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
						className="mt-4"
					>Atualizar</Button>
				</Col>
			</Row>
		</form>
	);
});

Chart.propTypes = 
{
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	schema: PropTypes.object,
	report: PropTypes.object,
	index: PropTypes.number,
};

export default Chart;