import React, {useEffect, useRef} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input} from 'reactstrap';
import BaseList from '../../../../components/BaseList';
import BaseForm from '../../../../components/BaseForm';
import ChartPanel from '../../../../components/Panel/ChartPanel';
import InputMask from '../../../../components/Input/InputMask';
import Misc from '../../../../libs/Misc';

const MAX_COLS = 4;

const buildSeconds = () =>
{
	const res = [];
	for(let i = 0; i < 60; i++)
	{
		res.push(i);
	}
	return res;
};

const buildMinutes = () =>
{
	const res = [];
	for(let i = 0; i < 60; i++)
	{
		res.push(i);
	}
	return res;
};

const buildHours = () =>
{
	const res = [];
	for(let i = 0; i < 24; i++)
	{
		res.push(i);
	}
	return res;
};

const buildDays = () =>
{
	const res = [];
	for(let i = 1; i < 32; i++)
	{
		res.push(i);
	}
	return res;
};

const buildMonths = (getWhat = 2) =>
{
	const res = [
		{id: 1, name: 'Janeiro'}, {id: 2, name: 'Fevereiro'}, {id: 3, name: 'Março'}, 
		{id: 4, name: 'Abril'}, {id: 5, name: 'Maio'}, {id: 6, name: 'Junho'},
		{id: 7, name: 'Julho'}, {id: 8, name: 'Agosto'}, {id: 9, name: 'Setembro'}, 
		{id: 10, name: 'Outubro'}, {id: 11, name: 'Novembro'}, {id: 12, name: 'Dezembro'}
	];
	
	return getWhat === 2? 
		res:
		(getWhat === 0?
			res.map(m => m.id):
			res.map(m => m.name));
};

const buildYears = () =>
{
	const year = new Date().getFullYear();
	const res = [];
	for(let i = 0; i < 21; i++)
	{
		res.push(year - i);
	}
	return res;
};

const buildFilterData = (type, extra) =>
{
	switch(type)
	{
	case 'month':
		return buildMonths(extra);
	case 'year':
		return buildYears(extra);
	case 'day':
		return buildDays(extra);
	case 'hour':
		return buildHours(extra);
	case 'minute':
		return buildMinutes(extra);
	case 'second':
		return buildSeconds(extra);
	default:
		return null;
	}
};

const buildFilterDefault = (type) =>
{
	switch(type)
	{
	case 'year':
		return new Date().getFullYear();
	case 'month':
		return 1 + new Date().getMonth();
	case 'day':
		return new Date().getDate();
	case 'hour':
		return new Date().getHours();
	case 'minute':
		return new Date().getMinutes();
	case 'second':
		return new Date().getSeconds();
	default:
		return null;
	}
};

const Report = observer(props =>
{
	const store = props.store;
	
	const baseList = useRef();

	useEffect(() =>
	{
		baseList.current && baseList.current.reload();
	},[props.report]);

	const getItemValue = (item, fieldName) =>
	{
		return Misc.getFieldValue(item, fieldName);
	};

	const format = (fmt, args) =>
	{
		return fmt.replace(/{(\d+)}/g, (match, number) =>
		{ 
			return args[number] !== undefined
				? args[number]
				: match;
		});
	};

	const renderFields = (item) =>
	{
		return (
			<Row>
				{
					Object.entries(props.report.columns)
						.map(([name, col], index) =>
						{
							const value = item[name];
							return (
								<Col key={index} sm="12" md="6" lg="4">
									<Label className="base-form-label">{col.label || name}</Label>
									<Input 
										disabled
										name={name}
										type="text"
										value={col.type === 'number'? Misc.round(value): value || ''}
										readOnly />
								</Col>);
						})
				}
			</Row>);
	};

	const calcColSize = () =>
	{
		return Math.floor(12 / Math.min(MAX_COLS, 
			Object.keys(
				props.report.columns
			).length));
	};

	const renderItem = (item, isFirstRow) =>
	{
		if(!props.report)
		{
			return null;
		}
		
		if(isFirstRow)
		{
			var size = calcColSize();
			return Object.entries(props.report.columns)
				.filter((e, index) => index < MAX_COLS)
				.map(([name, column], index) => 
					<Col key={index} xs={size}>
						{column.mask || column.type? 
							InputMask.format(getItemValue(item, name), column.mask || column.type): 
							getItemValue(item, name)
						}
					</Col>);
		}

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							api={props.api}
							history={null}
							parent={baseList.current}
							controller={null}
							item={item} 
							enabled={false}
							editable={false}
							render={renderFields} 
							validate={null} 
						/>
					</Col>
				</Row>
			</Col>
		);		
	};

	const getFilters = () =>
	{
		const {report} = props;

		if(!report || !report.filter || !report.filter.form)
		{
			return [];
		}

		return Object.entries(report.filter.form.fields).map(([k,f]) => 
		{
			return {
				name: k, 
				title: f.label, 
				type: f.type || 'text', 
				format: f.mask,
				default: f.default || buildFilterDefault(f.type),
				data: f.data || buildFilterData(f.type, 2)
			};
		});
	};

	const getHeader = () =>
	{
		const {report} = props;
		if(!report)
		{
			return [];
		}
		
		var size = calcColSize();

		return Object.entries(report.columns)
			.filter((e,i) => i < MAX_COLS)
			.map(([k,c]) => {
				return {
					name: c.sortable? k: null, 
					title: c.label, 
					size: {xs:size}, 
					klass: ''
				};
			});
	};

	const getRangeForAxisX = (axis, getValues = true) =>
	{
		if(axis.range)
		{
			const res = [];
			for(let i = axis.range.from; i <= axis.range.to; i++)
			{
				res.push(i);
			}

			return res;
		}

		const {columns} = props.report;

		const col = Object.keys(columns).find(k => k === axis.column);
		if(col)
		{
			return buildFilterData(columns[col].type, getValues? 1: 0);
		}

		return null;
	};

	const getColValuesFromAxisX = (chart) =>
	{
		const axis = chart.x;

		return getRangeForAxisX(axis, true) || baseList.current.getColValues(axis.column);
	}

	const getColValuesFromAxisY = (chart, col) =>
	{
		const values = baseList.current.getColValues(col);
		
		const xAxis = chart.x;
		const xRange = getRangeForAxisX(xAxis, false);
		if(!xRange)
		{
			return values;
		}

		const res = [];
		const xValues = baseList.current.getColValues(xAxis.column);
		xRange.forEach(i => 
		{
			const j = xValues.indexOf(i);
			if(j >= 0)
			{
				res.push(values[j]);
			}
			else
			{
				res.push(0);
			}
		});

		return res;
	};

	const handleListUpdated = () =>
	{

	};

	const renderPanels = () =>
	{
		const {chart, columns} = props.report;

		const getColLabel = (name) => (columns[name] && columns[name].label) || name;

		return chart && 
			<ChartPanel 
				type={chart.type}
				labels={() => getColValuesFromAxisX(chart)}
				datasets={() => chart.y.columns.map(col => ({label: getColLabel(col), data: getColValuesFromAxisY(chart, col)}))} 
				width='100%' 
				height={400}
				isOpen />;
	};

	if(!props.visible)
	{
		return null;
	}

	return (
		<div className="rf-col-reports-tab-container w-100">
			<div className="text-center p-2">
				<small>{props.report.description}</small><br/>
			</div>

			<BaseList
				ref={baseList}
				{...props}				
				title="Report"
				controller={`collections/${store.collection.id}/reports/${props.report.name}`}
				updateHistory={false}
				sortBy={null}
				inline={true}
				getFilters={getFilters}
				getHeader={getHeader}
				renderItem={renderItem}
				renderPanels={renderPanels}
			/>
		</div>
	);
});

Report.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	report: PropTypes.object.isRequired,
};

export default Report;