import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, Button, InputGroup, InputGroupAddon} from 'reactstrap';
import SimpleCollapse from '../../../../../components/Collapse/SimpleCollapse';
import Misc from '../../../../../libs/Misc.js';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import ExtensibleList from '../../../../../components/List/ExtensibleList';

const idsColumns = [
	{width: 4, label: 'Nome/Tipo', name: 'label'},
	{width: 2, label: 'Campo', name: 'column'},
	{width: 4, label: 'Valor', name: 'apply'}
];

const aggsColumns = [
	{width: 4, label: 'Nome/Tipo', name: 'label'},
	{width: 2, label: 'Operador', name: 'op'},
	{width: 4, label: 'Valor', name: 'apply'}
];

const transformType = (apply) =>
{
	if(!apply)
		return '';

	return apply.column? 
		'column': 
		(apply.function? 
			'function':
			(apply.script? 
				'script':
				'value'));
};

const transformApply = (apply) =>
{
	if(!apply)
		return '';

	return apply.column || apply.function || apply.script || apply.value;
};

const transformIds = (ids) =>
{
	return ids.map(id => ({
		...id,
		applyType: transformType(id.apply), 
		apply: transformApply(id.apply)
	}));
};

const transformAggregates = (aggs) =>
{
	return aggs.map(agg => ({
		...agg,
		applyType: transformType(agg.apply), 
		apply: transformApply(agg.apply)
	}));
};

const transformIdsAndAggregates = (columns) =>
{
	columns = Object.entries(columns).map(([k, col]) => ({name: k, ...col}));

	return [
		transformIds(columns.filter(col => !col.op)),
		transformAggregates(columns.filter(col => !!col.op))
	];
};

const transformReport = (report, clone = true) =>
{
	const res = report?
		(clone? Misc.cloneObject(report): report):
		{};

	const [ids, aggregates] = transformIdsAndAggregates(res.columns || {});
	res.ids = ids; 
	res.aggregates = aggregates;

	return res;
};

const Columns = observer(props =>
{
	const store = props.store;
	
	const [isLoading, setIsLoading] = useState(false);
	const [errors, setErrors] = useState([]);
	const [report, setReport] = useState(() => transformReport(props.report));

	useEffect(() =>
	{
		setReport(transformReport(props.report));
	}, [props.report]);

	const handleIdsChange = useCallback((event) =>
	{
		const to = Object.assign({}, report);
		to.ids = Array.from(to.ids);
		updateReport(event, to);
	}, [report]);

	const handleAggregatesChange = useCallback((event) =>
	{
		const to = Object.assign({}, report);
		to.aggregates = Array.from(to.aggregates);
		updateReport(event, to);
	}, [report]);

	const updateReport = (event, report) =>
	{
		const key = event.target.name;
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;

		Misc.setFieldValue(report, key, value);

		setReport(report);
	};

	const handleSubmit = async (event) =>
	{
		event.preventDefault();

		const transformName = (label) => CollectionUtil.labelToKey(label);
		const checkName = (name, cols, index) => !cols.find((c, i) => c.name === name && i !== index);
		const errors = [];

		try 
		{
			setIsLoading(true);
			
			const schema = Misc.cloneObject(props.schema);
			const from = schema.reports[props.index];
			const [fromIds, fromAggs] = transformIdsAndAggregates(from.columns || {});

			const toIds = report.ids || [];
			const toAggs = report.aggregates || [];

			let changeCnt = 0;
			if(!Misc.compareArrays(fromIds, toIds) || 
				!Misc.compareArrays(fromAggs, toAggs))
			{
				from.columns = {};
				for(let i = 0; i < toIds.length; i++)
				{
					const id = toIds[i];
					let {name, applyType, apply, ...clean} = id;
					if(!id.label)
					{
						errors.push(`Ids[${i}]: name deve ser definido`);
					}
					else if(!id.type)
					{
						errors.push(`Ids[${i}]: type deve ser definido`);
					}
					else if(!id.column)
					{
						errors.push(`Ids[${i}]: campo deve ser definido`);
					}
					else
					{
						name = transformName(id.label);
						id.name = name;
						if(!checkName(name, toIds, i) || !checkName(name, toAggs, -1))
						{
							errors.push(`Ids[${i}]: name duplicado: ${id.label}`);
						}

						from.columns[name] = {
							...clean, 
							apply: applyType? 
								{[applyType]: apply}: 
								null
						};
					}
				}
				
				for(let i = 0; i < toAggs.length; i++)
				{
					const agg = toAggs[i];
					let {name, applyType, apply, ...clean} = agg;
					if(!agg.label)
					{
						errors.push(`Agregações[${i}]: name deve ser definido`);
					}
					else if(!agg.type)
					{
						errors.push(`Agregações[${i}]: type deve ser definido`);
					}
					else if(!agg.op)
					{
						errors.push(`Agregações[${i}]: operador deve ser definido`);
					}
					else
					{
						name = transformName(agg.label);
						agg.name = name;
						if(!checkName(name, toAggs, i) || !checkName(name, toIds, -1))
						{
							errors.push(`Agregações[${i}]: name duplicado: ${agg.label}`);
						}
						from.columns[name] = {...clean, apply: applyType? {[applyType]: apply}: null};
					}
				}

				++changeCnt;
			}
			
			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await store.updateReport(props.index, from);
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

	const onAddIds = (items) =>
	{
		return items.length < 10;
	};

	const addIdsItem = (items) =>
	{
		const res = {
			name: '',
			label: '', 
			column: '',
			type: '', 
			applyType: '',
			apply: ''
		};
		
		setReport(report =>
		{
			const to = Object.assign({}, report);
			
			to.ids = Array.from(to.ids);
			
			to.ids.push(res);
			
			return to;
		});

		return res;
	};
	
	const delIdsItem = (item, index) =>
	{
		setReport(report =>
		{
			const to = Object.assign({}, report);
			to.ids = to.ids.filter((c, i) => i !== index);
			return to;
		});
	};

	const onAddAggregates = (items) =>
	{
		return items.length < 10;
	};

	const addAggregatesItem = (items) =>
	{
		const res = {
			name: '',
			label: '',
			op: '',
			type: '',
			applyType: '',
			apply: ''
		};

		setReport(report =>
		{
			const to = Object.assign({}, report);
			to.aggregates = Array.from(to.aggregates);
			to.aggregates.push(res);
			
			return to;
		});

		return res;
	};
	
	const delAggregatesItem = (item, index) =>
	{
		setReport(report =>
		{
			const to = Object.assign({}, report);
			to.aggregates = to.aggregates.filter((c, i) => i !== index);
			return to;
		});
	};

	const renderIdsHeader = ({col}) =>
	{
		return (col.label);
	};

	const renderIdsItemTypeOptions = () =>
	{
		return Object.entries(CollectionUtil.getReportGroupIdsValueTypes())
			.map(([key, type], index) => 
				<option value={key} key={index}>{type.name}</option>);
	};

	const filterIdsColumnOptions = (ids, index) =>
	{
		return props.columns.filter(col => !ids.find((c, i) => i !== index && c.column === col.name));
	};
	
	const renderIdsColumnOptions = (ids, index) =>
	{
		return [{value: '', label: ''}].concat(filterIdsColumnOptions(ids, index))
			.map((col, i) => 
				<option value={col.name} key={i}>{col.label}</option>);
	};
	
	const renderIdsItem = ({item, index, col, isPreview}) => 
	{
		if(isPreview)
		{
			const value = item[col.name] || '';
			switch(col.name)
			{
			case 'column':
				return value? 
					props.columns.find(c => c.name === value).label:
					'';
			default:
				return value;
			}
		}
		
		switch(col.name)
		{
		case 'label':
			return (						
				<InputGroup>
					<Input 
						required
						type="text" 
						name={`ids[${index}].label`}
						value={item.label} 
						onChange={handleIdsChange} />
					<InputGroupAddon addonType="append">
						<Input 
							disabled={!item.label}
							required
							type="select" 
							name={`ids[${index}].type`}
							value={item.type || ''} 
							onChange={handleIdsChange}>
							{CollectionUtil.getFilterTypes()
								.map((t, index) => <option key={index} value={t.value}>{t.label}</option>)}
						</Input>
					</InputGroupAddon>
				</InputGroup>);

		case 'column':
			return (
				<Input 
					disabled={!item.label} 
					required
					type="select" 
					name={`ids[${index}].column`}
					value={item.column} 
					onChange={handleIdsChange}>
					{renderIdsColumnOptions(report.ids, index)}
				</Input>);
		
		case 'apply':
			return (
				<InputGroup>
					<InputGroupAddon addonType="prepend">
						<Input 
							disabled={!item.column} 
							type="select" 
							name={`ids[${index}].applyType`}
							value={item.applyType} 
							onChange={handleIdsChange}>
							{renderIdsItemTypeOptions()}
						</Input>
					</InputGroupAddon>
					{(item.applyType !== 'function') &&
					<Input 
						disabled={!item.applyType} 
						type="text" 
						name={`ids[${index}].apply`}
						value={item.apply} 
						onChange={handleIdsChange} />
					}
					{item.applyType === 'function' &&
						<Input 
							disabled={!item.applyType}
							type="select" 
							name={`ids[${index}].apply`}
							value={item.apply || ''} 
							onChange={handleIdsChange}>
							{CollectionUtil.getReportGroupFunctionNames()
								.map((obj, index) => <option key={index} value={obj.value}>{obj.label}</option>)}
						</Input>
					}
				</InputGroup>);
		}
	};
	
	const renderAggregatesHeader = ({col}) =>
	{
		return (col.label);
	};

	const renderAggregatesOpOptions = () =>
	{
		return Object.entries(CollectionUtil.getReportGroupAggregatesOps())
			.map(([key, op], index) => 
				<option value={key} key={index}>{op.name}</option>);
	};

	const renderAggregatesItemTypeOptions = () =>
	{
		return Object.entries(CollectionUtil.getReportGroupAggregatesValueTypes())
			.map(([key, type], index) => 
				<option value={key} key={index}>{type.name}</option>);
	};

	const renderAggregatesItem = ({item, index, col, isPreview}) => 
	{
		if(isPreview)
		{
			const value = item[col.name] || '';
			switch(col.name)
			{
			case 'op':
				return CollectionUtil.getReportGroupAggregatesOps()[value].name;
			default:
				return value;
			}
		}
		
		switch(col.name)
		{
		case 'label':
			return (			
				<InputGroup>
					<Input 
						required
						type="text" 
						name={`aggregates[${index}].label`}
						value={item.label} 
						onChange={handleAggregatesChange} />
					<InputGroupAddon addonType="append">
						<Input 
							disabled={!item.label}
							required
							type="select" 
							name={`aggregates[${index}].type`}
							value={item.type || ''} 
							onChange={handleAggregatesChange}>
							{CollectionUtil.getFilterTypes()
								.map((t, index) => <option key={index} value={t.value}>{t.label}</option>)}
						</Input>
					</InputGroupAddon>
				</InputGroup>);
		
		case 'op':
			return (
				<Input 
					disabled={!item.label} 
					required
					type="select" 
					name={`aggregates[${index}].op`}
					value={item.op} 
					onChange={handleAggregatesChange}>
					{renderAggregatesOpOptions()}
				</Input>);
		
		case 'apply':
			return (
				<InputGroup>
					<InputGroupAddon addonType="prepend">
						<Input 
							disabled={!item.op || item.op === 'count'} 
							type="select" 
							name={`aggregates[${index}].applyType`}
							value={item.applyType} 
							onChange={handleAggregatesChange}>
							{renderAggregatesItemTypeOptions()}
						</Input>
					</InputGroupAddon>
					{(item.applyType !== 'function' && item.applyType !== 'column') &&
					<Input 
						disabled={!item.applyType || item.op === 'count'} 
						type="text" 
						name={`aggregates[${index}].apply`}
						value={item.apply} 
						onChange={handleAggregatesChange} />
					}
					{item.applyType === 'function' &&
						<Input 
							disabled={!item.applyType || item.op === 'count'}	
							type="select" 
							name={`aggregates[${index}].apply`}
							value={item.apply || ''} 
							onChange={handleAggregatesChange}>
							{CollectionUtil.getReportGroupFunctionNames()
								.map((obj, index) => <option key={index} value={obj.value}>{obj.label}</option>)}
						</Input>
					}
					{item.applyType === 'column' &&
						<Input 
							disabled={!item.applyType || item.op === 'count'}	
							type="select" 
							name={`aggregates[${index}].apply`}
							value={item.apply || ''} 
							onChange={handleAggregatesChange}>
							{props.columns
								.map((col, index) => <option key={index} value={col.name}>{col.label}</option>)}
						</Input>
					}
				</InputGroup>);
		}
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
			<Row className="mb-3">
				<Col sm="12">
					<SimpleCollapse isOpen={true} title="Ids">
						<ExtensibleList 
							columns={idsColumns}
							items={report.ids} 
							renderHeader={renderIdsHeader}
							renderItem={renderIdsItem}
							onAddItem={onAddIds}
							addItem={addIdsItem}
							deleteItem={delIdsItem} />
					</SimpleCollapse>
				</Col>
			</Row>
			<Row className="mb-3">
				<Col sm="12">
					<SimpleCollapse isOpen={true} title="Agregações">
						<ExtensibleList
							columns={aggsColumns}
							items={report.aggregates} 
							renderHeader={renderAggregatesHeader}
							renderItem={renderAggregatesItem}
							onAddItem={onAddAggregates}
							addItem={addAggregatesItem}
							deleteItem={delAggregatesItem} />
					</SimpleCollapse>
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

Columns.propTypes = 
{
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	schema: PropTypes.object,
	report: PropTypes.object,
	index: PropTypes.number,
};

export default Columns;