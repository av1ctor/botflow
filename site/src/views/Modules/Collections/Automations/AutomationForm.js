import React, {useCallback, useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button} from 'reactstrap';
import CollectionUtil from '../../../../libs/CollectionUtil';
import Misc from '../../../../libs/Misc';
import Logs from '../Properties/Logs';
import SimpleCollapse from '../../../../components/Collapse/SimpleCollapse';
import {transformActivityForEditing, transformActivityForSubmitting} from '../Activities/ActivityForm';
import ActivitiesList from '../Activities/ActivitiesList';
import LogicalTree from '../../../../components/Tree/LogicalTree';
import FilterValueInput from '../../../../components/Common/Collection/View/FilterValueInput';
import Condition, { transformConditionForEditing } from './Condition';

export const transformAutomationForEditing = (item, store, activities) =>
{
	const schema = item.schema?
		JSON.parse(item.schema):
		{
			condition: null
		};

	const condition = LogicalTree.unpack(
		schema.condition, (cond) => transformConditionForEditing(store, cond));
	
	return {
		...item,
		condition: condition,
		createdAt: Misc.stringToDate(item.createdAt),
		updatedAt: Misc.stringToDate(item.updatedAt),
		activities: item.activities.map(act => ({
			...act,
			activity: transformActivityForEditing(act.activity, activities)
		}))
	};
};

const isUop = (op) =>
{
	return op === 'exists' || op === 'notexists' ||
		op === 'isnull' || op === 'notnull';
};

const transformConditionForSubmitting = (cond) =>
{
	if(!cond || !cond.field || !cond.data.op)
	{
		return undefined;
	}

	const field = cond.field;
	const op = cond.data.op;
	const _isUop = isUop(op);

	return {
		type: 'FIELD',
		field: {
			name: cond.name,
			op: op,
			type: !_isUop?
				cond.type:
				undefined,
			value: !_isUop?
				FilterValueInput.fromInput(
					field.type, 
					field.subtype, 
					op, 
					cond.data.values):
				undefined
		}
	};
};

const transformForSubmitting = (item) =>
{
	const schema = {
		condition: 
			LogicalTree.pack(item.condition, transformConditionForSubmitting)
	};

	return {
		...item,
		schema: item.trigger !== 'FIELD_UPDATED_TO'?
			undefined:
			JSON.stringify(schema),
		createdAt: undefined,
		updatedAt: undefined,
		activities: item.activities
			.filter(act => !!act.activity.schemaId)
			.map(act => ({
				...act,
				activity: transformActivityForSubmitting(act.activity)
			})) 
	};
};

const AutomationForm = (props) =>
{
	const store = props.store;

	const [item, setItem] = useState(() => Misc.cloneObject(props.item));

	useEffect(() =>
	{
		setItem(Misc.cloneObject(props.item));
	}, [props.item]);
	
	const getIcon = (trigger) =>
	{
		return CollectionUtil.getObjectIcon('fighter-jet');
	};
	
	const handleChange = useCallback((event) =>
	{
		const key = event.target.name || event.target.id;

		const value = event.target.type !== 'select-multiple'?
			(event.target.type !== 'checkbox'? 
				event.target.value:
				event.target.checked):
			Array.apply(null, event.target.options)
				.filter(option => option.selected)
				.map(option => option.value);

		if(!key)
		{
			setItem(value);
		}
		else
		{
			setItem(state => 
			{
				const obj = Object.assign({}, state);

				if(key.constructor !== Array)
				{
					Misc.setFieldValue(obj, key, value);
				}
				else
				{
					key.forEach((name, index) =>
						Misc.setFieldValue(obj, name, value[index])
					);
				}

				return obj;
			});
		}

	}, []);

	const handleDelete = useCallback((item) =>
	{

	}, []);

	const validate = (item, errors) =>
	{
		if(!item.type)
		{
			errors.push('Type not selected');
		}
		
		if(!item.trigger)
		{
			errors.push('Trigger not selected');
		}
		
		if(!item.activities || item.activities.length === 0)
		{
			errors.push('At least one activity must be selected');
		}
		else
		{
			item.activities.forEach(act =>
			{
				if(item.activities.find(other => other.activity.schemaId === act.activity.schemaId && other !== act))
				{
					errors.push('At most one kind of activity must be selected');
				}
			});
		}

		if(errors.length > 0)
		{
			props.container.showMessage(errors, 'error');
			return false;
		}

		return true;
	};

	const handleSubmit = useCallback(async (e) =>
	{
		e.preventDefault();

		if(!validate(item, []))
		{
			return false;
		}
		
		const transf = transformForSubmitting(item);
		
		const res = item.id?
			await props.api.patch(
				`collections/${store.collection.id}/automations/${item.id}`, 
				transf):
			await props.api.post(
				`collections/${store.collection.id}/automations`, 
				transf);
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return null;
		}

		props.container.showMessage('Automation updated!', 'success');

		props.container.reload();

		return transformAutomationForEditing(res.data, props.store, props.activities);
	}, [item, props.activities]);

	const handleSwitchType = useCallback(async (type, item, onChange) =>
	{
		onChange({
			target: {
				name: null,
				value: transformAutomationForEditing({
					...item,
					type: type,
					trigger: '',
					activities: []
				},
				props.store,
				props.activities)
			}
		});
	}, [props.activities, props.refs]);

	const handleGetFieldOptions = useCallback((item, name) =>
	{
		switch(name)
		{
		case 'collection':
			return Object.entries(props.store.collection.schemaObj.columns)
				.filter(column => !column.auto)
				.map(([key, column]) => ({id: key, ...column}));

		default:
			return [];
		}
	}, []);

	const handleGetFunctionOptions = useCallback(() =>
	{
		return [];
	}, []);

	const renderTypeOptions = () =>
	{
		return Object.entries(CollectionUtil.getAutomationTypes())
			.map(([key, type], index) => 
				<option value={key} key={index}>{type.name}</option>);
	};

	const renderTriggerOptions = (type) =>
	{
		return Object.entries(CollectionUtil.getAutomationTriggers())
			.filter(([, trig]) => trig.type === 'ANY' || trig.type === type)
			.map(([key, trig], index) => 
				<option value={key} key={index}>{trig.name}</option>);
	};
	
	const hasTrigger = !!item.trigger;

	let compo = null;
	if(hasTrigger)
	{
		switch(item.trigger)
		{
		case 'FIELD_UPDATED_TO':
			compo = 
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Condition</Label>
						<Condition
							store={props.store}
							columns={props.columns}
							condition={item.condition}
							onChange={handleChange}
						/>
					</Col>
				</Row>;
			break;
		}
	}

	return (
		<form onSubmit={handleSubmit}>
			<Row>
				<Col sm="12" md="6" lg="4">
					<Label className="base-form-label">Id</Label>
					<Input 
						disabled={true} 
						required 
						name="" 
						type="text" 
						value={item.id? item.id: ''} 
						readOnly />
				</Col>
				<Col sm="12" md="6" lg="8">
					<Label className="base-form-label">Description</Label>
					<Input 
						required 
						name="desc" 
						type="text" 
						value={item.desc || ''} 
						onChange={handleChange} />
				</Col>
			</Row>
			
			<Row>
				<Col xs="12" md="6">
					<Label className="base-form-label">Type</Label>
					<Input 
						disabled={!!item.id} 
						required 
						type="select" 
						value={item.type || ''} 
						onChange={(e) => handleSwitchType(e.target.value, item, handleChange)}>
						{renderTypeOptions()}
					</Input>
				</Col>
				<Col xs="12" md="6">
					<Label className="base-form-label">Trigger</Label>
					<Input 
						required 
						disabled={!item.type}
						name="trigger" 
						type="select" 
						value={item.trigger || ''} 
						onChange={handleChange}>
						{renderTriggerOptions(item.type)}
					</Input>							
				</Col>
			</Row>
			
			{compo}

			{hasTrigger && 
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Activities</Label>
						<ActivitiesList
							store={props.store}
							isMobile={false}
							element={item}
							activities={props.activities}
							refs={props.refs}
							onChange={handleChange}
							loadRefs={props.loadRefs}
							getFieldOptions={(name) => handleGetFieldOptions(item, name)}
							getFunctionOptions={handleGetFunctionOptions}
						/>
					</Col>
				</Row>
			}
			
			{item.id && 
				<SimpleCollapse
					isOpen={false}
					title="Logs"
					style={{marginTop: 8}}>
					<Logs 
						api={props.api}
						container={props.container}
						store={props.store}
						visible
						height={300}
						path={`collections/${store.collection.id}/integrations/${item.id}/logs`} />
				</SimpleCollapse>
			}
			
			{item.id &&
				<>
					{item.createdAt &&
						<Row>
							<Col sm="12" md="6" lg="6">
								<Label className="base-form-label">Created at</Label>
								<Input disabled={true} required name="" var-type="datetime" type="text" value={item.createdAt || ''} readOnly />
							</Col>
							<Col sm="12" md="6" lg="6">
								<Label className="base-form-label">Created by</Label>
								<Input disabled={true} required name="" var-type="email" type="email" value={item.createdBy? item.createdBy.email: ''} readOnly />
							</Col>
						</Row>
					}
					{item.updatedAt &&
						<Row>
							<Col sm="12" md="6" lg="6">
								<Label className="base-form-label">Updated at</Label>
								<Input disabled={true} name="" var-type="datetime" type="text" value={item.updatedAt || ''} readOnly />
							</Col>
							<Col sm="12" md="6" lg="6">
								<Label className="base-form-label">Updated by</Label>
								<Input disabled={true} name="" var-type="email" type="email" value={item.updatedBy? item.updatedBy.email: ''} readOnly />
							</Col>
						</Row>
					}
					<Row>
						<Col xs="12">
							<hr />
						</Col>
					</Row>
					<Row>
						<Col xs="4" md="2">
							Actions:
						</Col>
						<Col xs="8" md="2">
							<Button 
								color="primary" 
								block 
								className="m-1"
								onClick={() => handleDelete(item)}>
								Delete
							</Button>
						</Col>
					</Row>
				</>
			}			
			<Row>
				<Col xs="12">
					<Button
						className="float-right mt-2"
						color="primary"
						type="submit">
						{item.id? 'Update': 'Create'}
					</Button>
				</Col>
			</Row>
		</form>
	);
};

AutomationForm.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
	columns: PropTypes.array.isRequired,
	activities: PropTypes.array.isRequired,
	refs: PropTypes.instanceOf(Map).isRequired,
	item: PropTypes.object.isRequired,
	loadRefs: PropTypes.func.isRequired,
};

export default AutomationForm;
