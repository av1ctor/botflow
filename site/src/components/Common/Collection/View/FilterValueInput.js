import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import {Input} from 'reactstrap';
import {Chips} from 'primereact/chips';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import InputMask from '../../../Input/InputMask';
import CollectionUtil from '../../../../libs/CollectionUtil';

dayjs.extend(customParseFormat);

const DATE_FORMAT = 'DD/MM/YYYY HH:mm:ss';

export default class FilterValueInput extends PureComponent
{
	static propTypes = 
	{
		filter: PropTypes.object.isRequired,
		column: PropTypes.object,
		type: PropTypes.string,
		scalarType: PropTypes.string.isRequired,
		disabled: PropTypes.bool,
		onChange: PropTypes.func.isRequired,
		className: PropTypes.string
	};

	static stringToDate(value)
	{
		return value?
			dayjs(value.replace(/[HMs]/g, '0'), DATE_FORMAT).toDate():
			null;
	}

	static fromInput(type, subtype, op, values)
	{
		type = (type !== 'array'? type : subtype) || 'string';
		const value1 = values[0], value2 = values[1];

		let value = null;
		switch(op)
		{
		case 'between':
			value = [value1, value2 === ''? value1: value2];
			break;
		case 'in':
		case 'notin':
		{
			value = (Array.isArray(value1)? value1: (typeof value1 === 'string'? value1.split(','): [value1]))
				.map(v => typeof v === 'string'? v.trim(): v)
				.map(v => type === 'number'? 
					parseInt(v): 
					(type === 'decimal'? 
						parseFloat(v): 
						v));
			break;
		}
		case 'isnull':
		case 'notnull':
			value = null;
			break;
		default:
			value = typeof value1 === 'string'? value1.trim(): value1;
			break;
		}

		if(value === null)
		{
			return value;
		}

		switch(type)
		{
		case 'date':
			if(Array.isArray(value))
			{
				return value.map(v => this.stringToDate(v));
			}
			else
			{
				return this.stringToDate(value);
			}
		
		case 'bool':
			if(Array.isArray(value))
			{
				return value.map(v => !v || v === '0'? false: true);
			}
			else
			{
				return !value || value === '0'? false: true;
			}

		case 'number':
			if(!Array.isArray(value))
			{
				return parseInt(value);
			}
			break;

		case 'decimal':
			if(!Array.isArray(value))
			{
				return parseFloat(value);
			}
			break;
		}

		return value;
	}

	static preProcess(value, type)
	{
		if(value === null || value === undefined)
		{
			return value;
		}
		
		switch(type)
		{
		case 'date':
			if(!Array.isArray(value))
			{
				return dayjs(value).format(DATE_FORMAT);
			}
			else
			{
				return value.map(v => dayjs(v).format(DATE_FORMAT));
			}

		case 'bool':
			if(!Array.isArray(value))
			{
				return !value || value === '0'? 0: 1;
			}
			else
			{
				return value.map(v => !v || v === '0'? 0: 1);
			}
			
		default:
			return value;
		}
	}
	
	static toInput(type, op, value)
	{
		value = this.preProcess(value, type);

		switch(op)
		{
		case 'between':
			if(Array.isArray(value))
			{
				return value;
			}
			return [value, ''];

		case 'in':
		case 'notin':
			return [!Array.isArray(value)? [value]: value, ''];

		default:
			return [value, ''];
		}
	}

	static renderOneInput = (index, name, value, mask, isNumber, disabled, onChange, props) =>
	{
		if(mask)
		{
			return (
				<InputMask mask={mask}>
					{({setInputRef}) =>
						<Input 
							{...props}	
							innerRef={setInputRef}
							name={`${name}[${index}]`}
							type={isNumber? 'number': 'text'} 
							value={!disabled? value: ''} 
							title="Valor"
							onChange={(e) => onChange(e, `values[${index}]`)}
							className="flex-grow-1"
							disabled={disabled} />
					}
				</InputMask>
			);
		}
		else
		{
			return (
				<Input 
					{...props}	
					name={`${name}[${index}]`}
					type={isNumber? 'number': 'text'} 
					value={!disabled? value: ''} 
					title="Valor"
					onChange={(e) => onChange(e, `values[${index}]`)}
					className="flex-grow-1"
					disabled={disabled} />
			);
		}
	};

	static renderInput({column, type, scalarType, op, values, className, name, onChange, ...props})
	{
		const isUop = 
			op === 'isnull' || op === 'notnull' || 
			op === 'exists' || op === 'notexists';
		
		switch(type)
		{
		case 'enumeration':
			if(op !== 'in' && 'op' !== 'notin')
			{
				return (
					!isUop &&
					<Input
						{...props}
						type="select"
						name={`${name}[0]`}
						value={values[0]}
						title="Valor"
						onChange={(e) => onChange(e, 'values[0]')}
						className={className}>
						{[{name: '', value: ''}]
							.concat(CollectionUtil.buildListForColumn(column))
							.map((o, index) => <option key={index} value={o.value}>{o.name}</option>)}
					</Input>
				);
			}
			break;

		case 'bool':
			return (
				<Input
					{...props}
					type="select"
					name={`${name}[0]`}
					value={values[0]}
					title="Valor"
					onChange={(e) => onChange(e, 'values[0]')}
					className={className}>
					<option key="1" value=""></option>
					<option key="2" value={0}>Falso</option>
					<option key="3" value={1}>Verdadeiro</option>
				</Input>);
		}

		switch(op)
		{
		case 'in':
		case 'notin':
			return (
				<Chips 
					{...props}	
					name={`${name}[0]`}
					value={values[0]? (values[0].constructor === Array? values[0]: [values[0]]): []} 
					onChange={(e) => onChange(e, 'values[0]')} />
			);
		}

		const isNumber = scalarType === 'number' && !(op === 'in' || op === 'notin');

		const mask = column && !isNumber? 
			(column.mask || column.type || ''): 
			'';

		const disabled = props.disabled || isUop;

		return (
			<div className={classnames('d-flex', className)}>
				{FilterValueInput.renderOneInput(0, name, values[0], mask, isNumber, disabled, onChange, props)}
				{op === 'between' && 
					FilterValueInput.renderOneInput(1, name, values[1], mask, isNumber, disabled, onChange, props)
				}
			</div>
		);
	}

	render()
	{
		const {filter, ...rest} = this.props;
		const {column, type} = filter;
		const {op, values} = filter.data;

		return FilterValueInput.renderInput({
			...rest, 
			column: column || this.props.column,
			type: type || this.props.type,
			op: op,
			values: values
		});
	}
}
