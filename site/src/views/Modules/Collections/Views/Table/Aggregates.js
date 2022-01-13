import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Slider from '../../../../../components/Slider/Slider';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import Misc from '../../../../../libs/Misc';

const Aggregates = props =>
{
	const store = props.store;
	
	const calcAggregate = (agg, values) =>
	{
		switch (agg) 
		{
		case 'cnt':
			return {
				label: 'Qtd',
				value: values.length
			};
		case 'avg':
			return {
				label: 'Méd',
				value: values.length > 0 ?
					Math.round(values.reduce((acc, value) => acc + value, 0) / values.length * 100) / 100 :
					0.0,
				doFormatting: true
			};
		case 'sum':
			return {
				label: 'Soma',
				value: values.length > 0 ?
					Math.round(values.reduce((acc, value) => acc + value, 0) * 100) / 100 :
					0.0,
				doFormatting: true
			};
		case 'min':
			return {
				label: 'Mín',
				value: values.length > 0 ?
					values.reduce((acc, value) => value < acc ? value : acc, values[0]) :
					0,
				doFormatting: true
			};
		case 'max':
			return {
				label: 'Máx',
				value: values.length > 0 ?
					values.reduce((acc, value) => value > acc ? value : acc, values[0]) :
					0,
				doFormatting: true
			};
		default:
			return;
		}
	};

	const collection = store.collection; 
	const references = store.references;
	const {view, rows, style, group} = props;
	
	const schema = collection.schemaObj;
	const filteredItems = Misc.mapValues(rows)
		.filter(row => row.group && row.group.name === group.name)
		.map(row => row.item);
	const sorted = CollectionUtil.getFieldsNamesSorted(view.fields);

	let left = 0;
	const baseStyle = {
		height: style.height + 'px', 
		lineHeight: style.height + 'px'
	};

	const renderColumn = (field, column, name, index) => 
	{
		if(props.container.isColumnHidden(column, field))
		{
			return null;
		}
			
		const width = field.width || CollectionUtil.DEFAULT_WIDTH;
		const frozen = index === 0 || field.frozen;
		const colStyle = {
			...baseStyle,
			width: width
		};

		if(frozen)
		{
			colStyle.left = left;
			left += width;
		}

		const vstyle = view.style || {};
		const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;

		const classes = classNames(
			'rf-col-cell rf-col-cell-group-agg', 
			{
				'frozen': frozen,
				[color]: frozen && color,
			});

		const aggs = [];
		if(view.groupBy.aggregates[name])
		{
			const values = filteredItems.map(item => Misc.getFieldValue(item, name)).filter(value => !!value);
			view.groupBy.aggregates[name].forEach((agg, i) => 
			{
				const res = calcAggregate(agg, values);
				const value = !res.doFormatting? 
					res.value:
					CollectionUtil.getValueFormatted(res.value, column, null);
				
				aggs.push(
					<div key={i}>
						{res.label? `${res.label}: `: null}
						{value}
					</div>);
			});
		}
		
		return (
			<Slider
				key={index}
				className={classes} 
				style={colStyle}>
				{aggs}
			</Slider>
		);
	};

	const vstyle = view.style || {};
	const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;
	const border = vstyle.border || {};
	
	return (
		<div style={style}>
			<div className={classNames(
				'rf-col-row rf-col-row-item',
				{
					[color]: color,
					[border.color]: border.color,
					[border.style]: border.style,
					'rounded': border.rounded,
				}
			)}>
				<div 
					key={-1} 
					className="rf-col-cell order"
					style={baseStyle}/>
				{sorted.map((name, index) => 
					renderColumn(
						view.fields[name], 
						CollectionUtil.getColumn(schema, name, references), 
						name, 
						index))}
			</div>
		</div>
	);
};

Aggregates.propTypes = {
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
	view: PropTypes.object,
	rows: PropTypes.any,
	group: PropTypes.object,
	style: PropTypes.object,
};
export default Aggregates;