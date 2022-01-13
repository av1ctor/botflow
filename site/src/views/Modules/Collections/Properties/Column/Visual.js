import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button, FormGroup, InputGroup, InputGroupAddon, CustomInput} from 'reactstrap';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import Misc from '../../../../../libs/Misc';
import FilterOpSelect from '../../../../../components/Common/Collection/View/FilterOpSelect';
import FilterValueInput from '../../../../../components/Common/Collection/View/FilterValueInput';

const transformForEditing = (props, store) =>
{
	const schema = store.collection.schemaObj;
	const view = store.views.get(props.viewId);

	return {
		field: {...props.field},
		sort: transformSortForEditing(schema, view, props.name),
		frozen: transformFrozenForEditing(view, props.name, props.field),
		filter: transformFilterForEditing(schema, view, props.name, props.column),
	};
};

const transformSortForEditing = (schema, view, name) =>
{
	const hasIndex = CollectionUtil.isColumnIndexed(schema, name);

	if(view.sort && view.sort[name])
	{
		const sort = view.sort[name];
		return {
			selected: true,
			disabled: !hasIndex,
			data: {dir: sort.dir || 'desc'}
		};
	}
	else
	{
		return {selected: false, disabled: !hasIndex, data: {dir: ''}};
	}
};

const transformFrozenForEditing = (view, name, field) =>
{
	const frozenCnt = CollectionUtil.calcFrozenFields(view.fields);
	const sorted = CollectionUtil.getFieldsNamesSorted(view.fields);
	const index = sorted.indexOf(name);
	const frozeable = field.frozen || (index <= frozenCnt);

	return {disabled: !frozeable || index === 0};
};

const transformFilterForEditing = (schema, view, name, column) =>
{
	const hasIndex = CollectionUtil
		.isColumnIndexed(schema, name);

	if(view.filters && view.filters[name])
	{
		const f = view.filters[name];
		
		const filter = 
		{
			column: column,
			type: column.type || 'string',
			selected: true,
			disabled: !hasIndex,
			data: {
				op: f.op || 'eq'
			}
		};

		filter.data.values = FilterValueInput
			.toInput(filter.type, filter.data.op, f.value);
		
		return filter;
	}
	else
	{
		return {
			column: column,
			type: column.type || 'string',
			selected: false, 
			disabled: !hasIndex, 
			data: {
				op: 'eq', 
				values: ['', '']
			}
		};
	}
};

const Visual = observer(props =>
{
	const store = props.store;

	const [isLoading, setIsLoading] = useState(false);
	const [form, setForm] = useState(() => transformForEditing(props, store));

	useEffect(() =>
	{
		setForm(transformForEditing(props, store));
	}, [props.column, props.field, props.name]);

	const handleChange = useCallback((event, name = null) =>
	{
		const to = Object.assign({}, form);

		const key = name || event.target.name || event.target.id;
	
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;
		
		Misc.setFieldValue(to, key, value);

		setForm(to);
	}, [form]);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setIsLoading(true);
			
			const schema = Misc.cloneObject(store.collection.schemaObj);
			const view = store.getViewOnSchemaById(props.viewId);

			let changeCnt = 0;

			const from = props.field;
			const to = form.field;

			// width
			{
				if(from.width !== to.width)
				{
					view.fields[props.name].width = to.width|0;
					++changeCnt;
				}
			}

			//sort
			{
				const from = transformSortForEditing(schema, view, props.name);
				const to = form.sort;
				if(!Misc.compareObjects(from, to, false))
				{
					if(to.selected)
					{
						const data = to.data;
						if(data.dir === '')
						{
							props.container.showMessage('Direção deve ser preenchida em Ordenar', 'error');
							return;
						}
						
						if(!view.sort)
						{
							view.sort = {};
						}
						view.sort[props.name] = data;
					}
					else
					{
						if(view.sort)
						{
							delete view.sort[props.name];
						}
					}
					++changeCnt;
				}
			}

			// frozen
			if(!form.frozen.disabled)
			{
				if(from.frozen !== undefined || to.frozen)
				{
					if(from.frozen !== to.frozen)
					{
						view.fields[props.name].frozen = to.frozen;
						++changeCnt;
					}
				}
			}

			//filter
			{
				const to = form.filter;
				const from = transformFilterForEditing(schema, view, props.name, props.column);
				if(!Misc.compareObjects(from, to, false))
				{
					if(to.selected)
					{
						const data = to.data;
						if(data.op === '' || data.values[0] === '')
						{
							props.container.showMessage('Operador e valor devem ser preenchidos em Filtrar', 'error');
							return;
						}
						
						if(!view.filters)
						{
							view.filters = {};
						}

						view.filters[props.name] = {
							op: data.op,
							value: FilterValueInput.fromInput(
								to.type, to.column.subtype, to.data.op, to.data.values)
						};
					}
					else
					{
						if(view.filters)
						{
							delete view.filters[props.name];
						}
					}
					++changeCnt;
				}
			}

			if(changeCnt > 0)
			{
				const res = store.updateView(props.viewId, view);
				if(res.errors)
				{
					props.container.showMessage(res.errors, 'error');
				}
				else
				{
					props.container.showMessage('Atualização realizada no campo!', 'success');
				}
			}

			return false;
		} 
		finally 
		{
			setIsLoading(false);
		}
	}, [form]);

	if(!props.column)
	{
		return null;
	}

	const scalarType = (
		form.filter.type !== 'array'? 
			form.filter.type : 
			form.filter.column.subtype) 
		|| 'string';

	return (
		<form>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Largura</Label>
					<Input 
						required 
						disabled={props.isProp}
						type="number" 
						name="field.width"
						value={form.field.width || 60} 
						onChange={handleChange} />
				</Col>
			</Row>
			
			<Row className="mt-3">
				<Col sm="12">
					<FormGroup check inline>
						<CustomInput
							type="switch"
							id="sort.selected"
							label="Ordenar"
							checked={form.sort.selected}
							onChange={handleChange}
							disabled={form.sort.disabled || props.isProp}/>
					</FormGroup>
					<Input 
						type="select" 
						name="sort.data.dir"
						value={form.sort.data.dir}
						onChange={handleChange}
						disabled={!form.sort.selected || props.isProp}>
						<option value=""></option>
						<option value="asc">Crescente</option>
						<option value="desc">Decrescente</option>
					</Input>
				</Col>
			</Row>

			<Row className="mt-3">
				<Col sm="12">
					<FormGroup check inline>
						<CustomInput
							type="switch"
							id="field.frozen"
							label="Congelar"
							checked={form.field.frozen? true: false}
							onChange={handleChange}
							disabled={form.frozen.disabled || props.isProp} />
					</FormGroup>
				</Col>
			</Row>				

			<Row className="mt-3">
				<Col sm="12">
					<FormGroup check inline>
						<CustomInput
							type="switch"
							id="filter.selected"
							label="Filtrar"
							checked={form.filter.selected}
							onChange={handleChange}
							disabled={form.filter.disabled || props.isProp}/>
					</FormGroup>
					<InputGroup className="d-flex">
						<InputGroupAddon addonType="prepend" className="flex-grow-0">
							<FilterOpSelect
								name="filter.data.op"
								op={form.filter.data.op}
								scalarType={scalarType}
								className="rf-col-cell-filter-op ignore" 
								onChange={handleChange}
								disabled={!form.filter.selected || props.isProp}
								isModal />
						</InputGroupAddon>
						<FilterValueInput 
							name=""
							filter={form.filter}
							scalarType={scalarType}
							disabled={!form.filter.selected || props.isProp}
							className="flex-grow-1"
							onChange={(e, field) =>  handleChange(e, `filter.data.${field}`)} />
					</InputGroup>
				</Col>
			</Row>

			<Row>
				<Col sm="12">
					<Button
						color="primary"
						disabled={isLoading || props.isProp}
						type="submit"
						onClick={handleSubmit}
						className="mt-4"
					>Atualizar</Button>
				</Col>
			</Row>
		</form>
	);
});

Visual.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	viewId: PropTypes.string.isRequired,
	name: PropTypes.string,
	column: PropTypes.object,
	field: PropTypes.object,
	isProp: PropTypes.bool,
};

export default Visual;
