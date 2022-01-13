import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Button, CustomInput, Input, Label} from 'reactstrap';
import Misc from '../../../../../libs/Misc.js';
import ListSelectable from '../../../../../components/List/ListSelectable';
import ListSelectableDraggable from '../../../../../components/List/ListSelectableDraggable';
import SimpleCollapse from '../../../../../components/Collapse/SimpleCollapse';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import InputMultiSelectObject from '../../../../../components/Input/InputMultiSelectObject.js';

const aggregateOptions = [
	{label: 'Qtd', value: 'cnt'},
	{label: 'Soma', value: 'sum'},
	{label: 'Méd', value: 'avg'},
	{label: 'Mín', value: 'min'},
	{label: 'Máx', value: 'max'},
];

const sortEntryByLabel = (a, b) =>
{
	const l = (a[1].label || a[0]).toLowerCase();
	const r = (b[1].label || b[0]).toLowerCase();
	return l === r? 0: l < r? -1: 1;
};

const Visuals = observer(props =>
{
	const store = props.store;
	
	const [isLoading, setIsLoading] = useState(false);
	const [frozen, setFrozen] = useState([]);
	const [sortBy, setSortBy] = useState([]);
	const [groupBy, setGroupBy] = useState('');
	const [aggregates, setAggregates] = useState([]);

	useEffect(() =>
	{
		const view = Misc.cloneObject(store.getViewOnSchemaById(props.viewId));
		const schema = store.collection.schemaObj;

		setFrozen(transformFrozen(schema, store.references, view));
		setSortBy(transformSortBy(schema, store.references, view));
		setGroupBy(view.groupBy? view.groupBy.column: '');
		setAggregates(transformAggregates(schema, store.references, view));
	}, [store.views.get(props.viewId)]);

	const handleChange = useCallback((base, name, event) =>
	{
		const value = event.target.type !== 'checkbox' ? 
			event.target.value : 
			event.target.checked;
		
		Misc.setFieldValue(base, name, value);
	}, []);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setIsLoading(true);
			
			const to = Misc.cloneObject(store.getViewOnSchemaById(props.viewId));

			let changeCnt = 0;
			
			// frozen
			{
				const fields = Misc.cloneObject(to.fields);
				let lastIndex = -1;
				for(let i = 0; i < frozen.length; i++)
				{
					const item = frozen[i];
					const field = fields[item.value];
					if(item.selected)
					{
						if(i - lastIndex > 1)
						{
							props.container.showMessage(
								'Campos congelados devem ser contínuas e se iniciar no primeiro campo', 'error');
							return;
						}
						lastIndex = i;

						field.frozen = true;
					}
					else
					{
						delete field.frozen;
					}
				}

				if(!Misc.compareObjects(to.fields, fields, false))
				{
					to.fields = fields;
					++changeCnt;
				}
			}

			// sort
			{
				const transformed = {};
				sortBy.forEach(item => 
				{
					if(item.selected)
					{
						transformed[item.value] = {dir: item.data.asc? 'asc': 'desc'};
					}
				});
				
				if(!Misc.compareObjects(to.sort, transformed, true))
				{
					to.sort = transformed;
					++changeCnt;
				}
			}

			// groupBy
			{
				const transformed = groupBy? 
					{column: groupBy}: 
					undefined;
				const items = aggregates.filter(item => item.selected && item.data.aggs.length > 0);
				if(items.length > 0)
				{
					transformed.aggregates = {};
					items.forEach(item => 
					{
						transformed.aggregates[item.value] = item.data.aggs;
					});
				}

				if(!Misc.compareObjects(transformed, to.groupBy, false))
				{
					if(transformed && transformed.column !== '')
					{
						to.groupBy = transformed;
					}
					else
					{
						delete to.groupBy;
					}
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
	}, [frozen, sortBy, groupBy, aggregates]);

	const transformFrozen = (schema, refs, view) =>
	{
		const sorted = CollectionUtil.getFieldsNamesSorted(view.fields);
		
		return sorted.map(name => ({
			label: CollectionUtil.getColumnLabel(schema, name, refs), 
			value: name,
			selected: view.fields[name].frozen? true: false
		}));
	};

	const transformSortBy = (schema, refs, view) =>
	{
		const items = [];
		if(view.sort)
		{
			Object.entries(view.sort).forEach(([name, s]) => 
			{
				items.push({
					label: CollectionUtil.getColumnLabel(schema, name, refs), 
					value: name, 
					selected: true, 
					data: {
						asc: s.dir && s.dir === 'asc'? true: false
					}
				});
			});
		}

		const sorted = CollectionUtil.getFieldsNamesSorted(view.fields);
		
		sorted.forEach(name => 
		{
			if(!items.find(o => o.value === name) && CollectionUtil.isColumnIndexed(schema, name))
			{
				items.push({
					label: CollectionUtil.getColumnLabel(schema, name), 
					value: name, 
					selected: false, 
					data: {
						asc: false
					}
				});
			}
		});

		return items;
	};

	const transformAggregates = (schema, refs, view) =>
	{
		const items = [];

		const sorted = CollectionUtil.getFieldsNamesSorted(view.fields);
		
		const groupBy = view.groupBy;

		sorted.forEach(name => 
		{
			const hasAggregate = groupBy && groupBy.aggregates && groupBy.aggregates[name];
			items.push({
				label: CollectionUtil.getColumnLabel(schema, name, refs), 
				value: name, 
				selected: hasAggregate, 
				data: {
					aggs: hasAggregate? Array.from(groupBy.aggregates[name]): []
				}
			});
		});

		return items;
	};

	const handleSortDir = useCallback((event, index) => 
	{
		const value = event.target.checked;

		setSortBy(sort =>
		{
			const to = Array.from(sort);

			to[index].data.asc = value;
		
			return to;
		});
	}, []);

	const renderSortHeader = () =>
	{
		return (
			<>
				<div className="col-9 col-sm-10 pl-1">Campo</div>
				<div className="col-2 col-sm-1 pl-1">Asc</div>
			</>
		);
	};
	
	const renderSortItem = (item, index) => 
	{
		return (
			<>
				<div className="col-9 col-sm-10 pl-1">{item.label}</div>
				<div className="col-2 col-sm-1 pl-1">
					<small>
						<CustomInput 
							type="checkbox" 
							id={`asc_${item.value}`} 
							checked={item.data.asc}
							onChange={(e) => handleSortDir(e, index)} 
							disabled={!item.selected} />
					</small>
				</div>
			</>
		);
	};

	const handleAggregate = useCallback((event, index) => 
	{
		const value = event.target.value.map(v => v.value);
		
		setAggregates(aggregates =>
		{
			const to = Array.from(aggregates);

			to[index].data.aggs = value;

			return to;
		});
	}, []);

	const renderAggregateHeader = () =>
	{
		return (
			<>
				<div className="col-4">Campo</div>
				<div className="col-8 pl-1">Sumarização</div>
			</>
		);
	};

	const renderAggregateItem = (item, index) => 
	{
		return (
			<>
				<div className="col-4">{item.label}</div>
				<div className="col-8 pl-1" style={{overflowX: 'visible'}}>
					<InputMultiSelectObject
						options={aggregateOptions} 
						values={item.data.aggs.map(value => aggregateOptions.find(agg => agg.value === value))} 
						objectKey="value"
						objectLabel="label"
						disabled={!item.selected || groupBy === ''}
						onChange={(e) => handleAggregate(e, index)} 
					/>
				</div>
			</>
		);
	};

	const renderGroupBy = () =>
	{
		const schema = store.collection.schemaObj;
		
		const renderOptions = () => 
		{
			return [<option key="" value=""></option>].concat(
				Object.entries(schema.columns)
					.filter(([name]) => CollectionUtil.isColumnIndexed(schema, name))
					.sort(sortEntryByLabel)
					.map(([name, column]) => 
						<option key={name} value={name}>{column.label || name}</option>
					)
			);
		};

		return (
			<>
				<Label className="base-form-label">Campo:</Label>
				<Input 
					type="select" 
					value={groupBy || ''}
					onChange={(e) => setGroupBy(e.target.value)}>
					{renderOptions()}
				</Input>
			</>
		);
	};

	return (
		<>
			<Row className="mb-3">
				<Col sm="12">
					<SimpleCollapse isOpen={true} title="Ordenação">
						<ListSelectableDraggable 
							className="modal-zindex rf-over-the-modal"
							items={sortBy} 
							onChange={(items) => setSortBy(items)} 
							renderItem={renderSortItem}
							renderHeader={renderSortHeader} />
					</SimpleCollapse>
				</Col>
			</Row>
			<Row className="mb-3">
				<Col sm="12">
					<SimpleCollapse isOpen={false} title="Campos congelados">
						<ListSelectable 
							items={frozen} 
							onChange={(items) => setFrozen(items)} 
							renderItem={(item) => <div className="col">{item.label}</div>}
							renderHeader={() => <div className="col">Campo</div>}
							multiple />
					</SimpleCollapse>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<SimpleCollapse isOpen={false} title="Agrupamento e sumarização">
						{renderGroupBy()}
						<br/>
						<ListSelectable 
							items={aggregates} 
							onChange={(items) => setAggregates(items)} 
							renderItem={renderAggregateItem}
							renderHeader={renderAggregateHeader}
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

Visuals.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	viewId: PropTypes.string.isRequired,
};

export default Visuals;