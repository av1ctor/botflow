import React from 'react';
import PropTypes from 'prop-types';
import {Input, InputGroup, InputGroupAddon, Button} from 'reactstrap';
import FilterOpSelect from '../../../../components/Common/Collection/View/FilterOpSelect';
import LogicalTree from '../../../../components/Tree/LogicalTree';
import FilterValueInput from '../../../../components/Common/Collection/View/FilterValueInput';
import CollectionUtil from '../../../../libs/CollectionUtil';

// eslint-disable-next-line react/prop-types
const Field = ({children, label}) =>
	<div className="row">
		<div className="col-12">
			<div className="pt-2">{label}</div>
			{children}
		</div>
	</div>;

const getConditionPrototype = () =>
{
	return {
		field: {},
		name: '',
		type: '',
		data: {
			op: '',
			values: ['', '']
		}
	};
};

export const transformConditionForEditing = (store, cond) =>
{
	if(!cond)
	{
		return getConditionPrototype();
	}

	const fld = cond.field;
	const field = CollectionUtil.getColumn(store.collection.schemaObj, fld.name, null);
	return {
		name: fld.name,
		type: fld.type,
		field: field,
		data: {
			op: fld.op || 'eq',
			values: FilterValueInput.toInput(
				field.type, 
				fld.op, 
				fld.value)
		}
	};
};

const isUop = (op) =>
{
	return op === 'exists' || op === 'notexists' ||
		op === 'isnull' || op === 'notnull';
};

const Condition = (props) =>
{
	const store = props.store;
	
	const renderConditionPreview = (cond) =>
	{
		const findOp = (op) =>
			CollectionUtil.getFilterOps(null, true).find(oper => oper.value === op) || {};

		const getValue = (data) =>
			data.values[0];

		const value = getValue(cond.data);
		
		return (
			`Campo "${cond.field.label || ''}" ${findOp(cond.data.op).symbol || ''} ${value? '"' + value + '"': ''}`
		);
	};

	const renderColumnUpdatedToConditionOptions = () =>
	{
		return Object.entries(CollectionUtil.getAutomationColumnConditionTypes())
			.map(([key, type], index) => 
				<option value={key} key={index}>{type.name}</option>);
	};

	const getColumnScalarType = (col) => 
	{
		return (col && (col.type !== 'array'? col.type : col.subtype)) || 'string';
	};

	const handleChangeColumn = (event, onChange) =>
	{
		const base = event.target.name;
		const name = event.target.value;
		
		onChange({
			target: {
				name: [
					`${base}.name`,
					`${base}.field`
				],
				value: [
					name,
					CollectionUtil.getColumn(
						store.collection.schemaObj, name, null)
				]
			}
		});
	};

	const renderColumnOptions = (columns) =>
	{
		return (
			[{value: '', label: ''}]
				.concat(columns
					/*.filter(col => col.type === 'string' || col.subtype === 'string')*/)
				.map((col, index) => 
					<option value={col.name} key={index}>{col.label}</option>)
		);
	};
	
	const renderCondition = (cond, id, onChange) =>
	{
		const base = `condition[${id}]`;

		const field = cond.field;
		const op = cond.data.op;
		const _isUop = isUop(op);
		const scalarType = !field.ref? 
			getColumnScalarType(field):
			'id';
		
		return (
			<>
				<Field label="Field">
					<Input 
						required 
						name={`${base}`}
						type="select" 
						value={cond.name || ''} 
						onChange={e => handleChangeColumn(e, onChange)}>
						{renderColumnOptions(props.columns)}
					</Input>							
				</Field>
				<Field label="Op/Type/Value">
					<InputGroup>
						<InputGroupAddon addonType="prepend" className="flex-grow-0">
							<FilterOpSelect
								required
								name={`${base}.data.op`}
								op={op || ''}
								scalarType={scalarType}
								className="rf-col-cell-filter-op ignore" 
								onChange={(e) => onChange(e)}
								isModal
								allowExists />
							<Input 
								required={!_isUop}
								disabled={!op || _isUop}
								name={`${base}.type`} 
								type="select" 
								value={cond.type || ''} 
								onChange={onChange}>
								{renderColumnUpdatedToConditionOptions()}
							</Input>
						</InputGroupAddon>
						<FilterValueInput 
							disabled={!op || !cond.type || _isUop}
							required={!!op && !_isUop}
							name={`${base}.data.values`}
							filter={cond}
							column={field}
							type={cond.type === 'VALUE'? field.type: 'string'}
							scalarType={cond.type === 'VALUE'? scalarType: 'string'}
							className="flex-grow-1"
							onChange={(e) => onChange(e)} />
					</InputGroup>
				</Field>
			</>
		);
	};

	const updateCondition = (cond, onChange) =>
	{
		onChange({
			target: {
				name: 'condition',
				value: cond
			}
		});
	};

	const createCondition = (onChange) =>
	{
		onChange({
			target: {
				name: 'condition',
				value: LogicalTree.unpack(
					LogicalTree.createPackedNode('and', getConditionPrototype()))
			}
		});
	};

	const updateConditionProps = (e, onChange) =>
	{
		const {name, ...rest} = e.target;

		onChange({
			target: {
				...rest, 
				name: `condition${name}`
			}
		});
	};

	const renderEmptyCondition = (onChange) =>
	{
		return (
			<div>
				<span>Nenhuma</span>&nbsp;
				<Button
					onClick={() => createCondition(onChange)}>
					Criar condição
				</Button>
			</div>
		);
	};
	
	return (
		<LogicalTree
			tree={props.condition}
			updateTree={(cond) => updateCondition(cond, props.onChange)}
			renderPreview={renderConditionPreview}
			renderNode={(node, id) => renderCondition(node, id, props.onChange)}
			renderEmpty={() => renderEmptyCondition(props.onChange)}
			getNodePrototype={getConditionPrototype}
			onChange={(e) => updateConditionProps(e, props.onChange)}
			className="modal-zindex"
		/>
	);
};

Condition.propTypes = 
{
	store: PropTypes.object,
	columns: PropTypes.array.isRequired,
	condition: PropTypes.any,
	onChange: PropTypes.func.isRequired,
};

export default Condition;
