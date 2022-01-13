import React, {useCallback, useContext, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import {InputGroup, InputGroupAddon} from 'reactstrap';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import Misc from '../../../../../libs/Misc';
import FilterValueInput from '../../../../../components/Common/Collection/View/FilterValueInput';
import FilterOpSelect from '../../../../../components/Common/Collection/View/FilterOpSelect';
import { SessionContext } from '../../../../../contexts/SessionContext';

const getOpForType = type =>
{
	switch(type || 'string')
	{
	case 'string':
		return 'like';
	case 'date':
		return 'gte';
	default:
		return 'eq';
	}
};

const createDebouncer = (filters, onSubmit) => {
	const res = {
		handler: null,
		options: {}
	};
	res.handler = Misc.debounce(() => onSubmit(filters), 1000, res.options);
	return res;
};

const transformFiltersForEditing = (props, store, onSubmit) =>
{
	const filters = [];
	const schema = store.collection.schemaObj;
	const references = store.references;
	const sorted = CollectionUtil.getFieldsNamesSorted(props.fields);

	sorted.forEach(name =>
	{
		const column = CollectionUtil.getColumn(schema, name, references);
		const field = props.fields[name];
		if(!props.container.isColumnHidden(column, field))
		{
			const filter = props.filters.find(filter => filter.name === name);
			if(filter)
			{
				const type = column.type || 'string';
				const f = 
				{
					name: filter.name,
					type: type,
					column: column,
					data: {
						op: filter.op || getOpForType(type),
					},
					disabled: false
				};

				f.data.values = 
					FilterValueInput.toInput(f.type, f.data.op, filter.value);

				f.debounce = createDebouncer(filters, onSubmit);
				filters.push(f);
			}
			else
			{
				if(CollectionUtil.isColumnIndexed(schema, name, references))
				{
					const vf = props.view.filters? 
						props.view.filters[name]: 
						undefined;
					const type = column.type || 'string';
					const f = 
					{
						name: name, 
						type: type,
						column: column,
						data: {
							op: vf? 
								(vf.op || 'eq'): 
								getOpForType(type)
						},
						disabled: vf? true: false
					};

					f.data.values = vf? 
						FilterValueInput.toInput(f.type, f.data.op, vf.value):
						['', ''];

					f.debounce = createDebouncer(filters, onSubmit);
					filters.push(f);
				}
			}
		}
	});

	return filters;
};

const transformFiltersForSubmitting = (filters) =>
{
	return filters.filter(f => !f.disabled && f.data.op !== '' && (f.data.values[0] !== '' || (f.data.op === 'isnull' || f.data.op === 'notnull')));
};

const Filters = observer(props =>
{
	const session = useContext(SessionContext);
	const store = props.store;

	const handleSubmit = useCallback((filters, event = null) =>
	{
		if(event)
		{
			event.preventDefault();
		}
		
		const transformed = transformFiltersForSubmitting(filters)
			.map(f => ({
				name: f.name, 
				op: f.data.op, 
				value: FilterValueInput.fromInput(f.type, f.column.subtype, f.data.op, f.data.values)
			}));

		if(!Misc.compareArrays(props.filters, transformed))
		{
			props.container.filter(transformed);
		}
	}, []);

	const [filters, setFilters] = useState(() => transformFiltersForEditing(props, store, handleSubmit));

	useEffect(() =>
	{
		setFilters(transformFiltersForEditing(props, store, handleSubmit));
	}, [props.filters]);

	const getFilterByName = (filters, name) =>
	{
		return filters.find(filter => filter.name === name);
	};
	
	const handleChange = useCallback((event, name, field) =>
	{
		const value = event.target.type !== 'checkbox'? 
			(event.target.type === 'number'? 
				event.target.value: 
				event.target.value): 
			event.target.checked;

		setFilters(filters =>
		{
			const to = Array.from(filters);

			const filter = getFilterByName(to, name);

			Misc.setFieldValue(filter.data, field, value);

			return to;
		});
	}, []);

	const handleClean = useCallback(() =>
	{
		setFilters(filters =>
		{
			const to = Array.from(filters);

			to.forEach(filter => {
				filter.data.op = getOpForType(filter.type);
				filter.data.values[0] = filter.data.values[1] = '';
			});

			handleSubmit(to);

			return to;
		});
	}, []);

	const collection = store.collection;
	const references = store.references;
	const {view, fields} = props;
	const sorted = CollectionUtil
		.getVisibleFieldsNamesSorted(collection.schemaObj, fields, session.user, references);

	let left = 0;

	const renderFilterInput = (filter) =>
	{
		const scalarType = (filter.type !== 'array'? filter.type : filter.column.subtype) || 'string';
		return (
			<>
				<InputGroup className="d-flex">
					<InputGroupAddon addonType="prepend">
						<FilterOpSelect
							name={`${filter.name}.op`}
							op={filter.data.op}
							scalarType={scalarType}
							className="rf-col-cell-filter-op" 
							onChange={(e) => handleChange(e, filter.name, 'op')}
							disabled={filter.disabled} />
					</InputGroupAddon>
					<FilterValueInput 
						name={filter.name}
						filter={filter}
						scalarType={scalarType}
						disabled={filter.disabled}
						className="rf-col-cell-filter-input-container"
						onChange={(e, field) => {handleChange(e, filter.name, field); filter.debounce.handler();}} />
				</InputGroup>
			</>);
	};

	const renderFilterField = (field, name, index) =>
	{
		const filter = getFilterByName(filters, name);
		
		const frozen = index === 0 || field.frozen;
		const width = field.width || CollectionUtil.DEFAULT_WIDTH;

		const style = {
			width: width, 
			height: props.height + 'px', 
			lineHeight: props.height + 'px'
		};

		if(frozen)
		{
			style.left = left;
			left += width;
		}

		return (
			<div 
				key={index}
				className={classNames(
					'rf-col-cell rf-col-cell-group',
					{
						'frozen': frozen,
						[color]: frozen && color,
					}
				)}
				style={style}>
				<div className="rf-col-cell-filter">
					{filter?
						renderFilterInput(filter):
						null}
				</div>
			</div>
		);
	};

	const colStyle = {
		height: props.height + 'px', 
		lineHeight: props.height + 'px'
	};

	const isDirty = transformFiltersForSubmitting(filters).length > 0;
	const vstyle = view.style || {};
	const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;
	const border = vstyle.border || {};

	return (
		<div className={classNames(
			'rf-col-row rf-col-row-filters',
			{
				[color]: color,
				[border.color]: border.color,
				[border.style]: border.style,
				'rounded': border.rounded,
			}
		)}>
			<form onSubmit={e => handleSubmit(filters, e)}>
				<div 
					key={-1} 
					className="rf-col-cell order"
					style={colStyle}
				>
					<div 
						className={`rf-col-filter-clean-icon ${isDirty? 'dirty': ''}`}
						onClick={() => isDirty? handleClean(): null}
						title="Limpar filtros"
					>
						<i className="la la-broom" />
					</div>							
				</div>
				{sorted.map((name, index) => 
					renderFilterField(fields[name], name, index))
				}
				<input 
					type="submit" 
					style={{visibility: 'hidden', position: 'absolute', left: '-9999px', width: '1px', height: '1px'}} 
				/>
			</form>
		</div>
	);
});

Filters.propTypes = {
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
	view: PropTypes.object,
};

export default Filters;