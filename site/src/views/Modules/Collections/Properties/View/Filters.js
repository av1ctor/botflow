import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Button, InputGroup, InputGroupAddon} from 'reactstrap';
import Misc from '../../../../../libs/Misc.js';
import ListSelectable from '../../../../../components/List/ListSelectable';
import SimpleCollapse from '../../../../../components/Collapse/SimpleCollapse';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import FilterValueInput from '../../../../../components/Common/Collection/View/FilterValueInput';
import FilterOpSelect from '../../../../../components/Common/Collection/View/FilterOpSelect';

const sortByLabel = (items) =>
{
	return items.sort((a, b) => 
	{
		const l = a.label.toLowerCase();
		const r = b.label.toLowerCase();
		return l === r? 0: l < r? -1: 1;
	});
};

const Filters = observer(props =>
{
	const store = props.store;
	
	const [isLoading, setIsLoading] = useState(false);
	const [filters, setFilters] = useState([]);

	useEffect(() =>
	{
		const view = store.getViewOnSchemaById(props.viewId);
		setFilters(transformFilters(store.collection.schemaObj, store.references, view));
	}, [store.views.get(props.viewId)]);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setIsLoading(true);
			
			const to = Misc.cloneObject(store.getViewOnSchemaById(props.viewId));

			let changeCnt = 0;
			
			//filters
			{
				const transformed = {};
				for(let i = 0; i < filters.length; i++)
				{
					const item = filters[i];
					if(item.selected)
					{
						if(item.data.op === '' || item.data.value === '')
						{
							props.container.showMessage(
								`Operador e valor devem ser preenchidos em '${item.label}'`, 'error');
							return;
						}

						transformed[item.value] = {
							op: item.data.op, 
							value: FilterValueInput.fromInput(
								item.type, item.column.subtype, item.data.op, item.data.values)
						};
					}
				}

				if(!Misc.compareObjects(to.filters, transformed))
				{
					to.filters = transformed;
					++changeCnt;
				}
			}
			
			if(changeCnt > 0)
			{
				const res = await store.updateView(props.viewId, to);
				if(res.errors)
				{
					props.container.showMessage(res.errors, 'error');
				}
				else
				{
					props.container.showMessage('Atualização de view realizada!', 'success');
				}
			}

			return false;
		} 
		finally 
		{
			setIsLoading(false);
		}
	}, [filters]);

	const transformFilters = (schema, refs, view) =>
	{
		const items = [];
		if(view.filters)
		{
			Object.entries(view.filters).forEach(([name, f]) => 
			{
				const column = CollectionUtil.getColumn(schema, name);
				const filter = 
				{
					label: CollectionUtil.getColumnLabel(schema, name, refs), 
					value: name, 
					column: column,
					type: column.type || 'string',
					selected: true, 
					data: {op: f.op || 'eq'}
				};

				filter.data.values = FilterValueInput.toInput(filter.type, filter.data.op, f.value);

				items.push(filter);
			});
		}

		Object.entries(schema.columns).forEach(([key, col]) => 
		{
			const isObject = col.type === 'object';
			const fields = !isObject? 
				[key]: 
				Object.keys(CollectionUtil.getClassByName(schema, col.class).props).map(name => key + '.' + name);
			
			fields.forEach(name => 
			{
				if(!items.find(o => o.value === name) && CollectionUtil.isColumnIndexed(schema, name))
				{
					const column = CollectionUtil.getColumn(schema, name);
					items.push({
						label: column.label || name, 
						value: name,
						column: column,
						type: column.type || 'string', 
						selected: false, 
						data: {op: 'eq', values: ['', '']}
					});
				}
			});
		});

		return sortByLabel(items);
	};

	const handleFilters = useCallback((event, index, field) => 
	{
		const value = event.target.type !== 'checkbox'? 
			(event.target.type === 'number'? 
				event.target.value|0: 
				event.target.value): 
			event.target.checked;

		setFilters(filters =>
		{
			const to = Array.from(filters);

			Misc.setFieldValue(to[index].data, field, value);
			
			return to;
		});
	}, []);

	const renderFiltersHeader = () =>
	{
		return (
			<>
				<div className="col-6 col-sm-8">Campo</div>
				<div className="col-6 col-sm-4 pl-1">Valor</div>
			</>
		);
	};
	
	const renderFiltersItem = (item, index) => 
	{
		const scalarType = (item.type !== 'array'? item.type : item.column.subtype) || 'string';
		return (
			<>
				<div className="col-6 col-sm-8">{item.label}</div>
				<div className="col-6 col-sm-4 pl-1 rf-col-cell-filter">
					<InputGroup className="d-flex">
						<InputGroupAddon addonType="prepend">
							<FilterOpSelect
								op={item.data.op}
								scalarType={scalarType}
								className="rf-col-cell-filter-op ignore" 
								onChange={(e) => handleFilters(e, index, 'op')}
								disabled={!item.selected}
								isModal />
						</InputGroupAddon>
						<FilterValueInput 
							filter={item}
							scalarType={scalarType}
							disabled={!item.selected}
							className="rf-col-cell-filter-input-container"
							onChange={(e, field) =>  handleFilters(e, index, field)} />
					</InputGroup>
				</div>
			</>
		);
	};

	return (
		<>
			<Row>
				<Col sm="12">
					<SimpleCollapse isOpen={true} title="Filtros">
						<ListSelectable 
							items={filters} 
							onChange={(items) => setFilters(items)} 
							renderItem={renderFiltersItem}
							renderHeader={renderFiltersHeader}
							multiple />
					</SimpleCollapse>
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
		</>
	);
});

Filters.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	viewId: PropTypes.string.isRequired,
};

export default Filters;