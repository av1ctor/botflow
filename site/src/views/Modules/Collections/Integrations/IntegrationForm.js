import React, {useCallback, useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, InputGroup, InputGroupAddon, Button} from 'reactstrap';
import CollectionUtil from '../../../../libs/CollectionUtil';
import Misc from '../../../../libs/Misc';
import Logs from '../Properties/Logs';
import SimpleCollapse from '../../../../components/Collapse/SimpleCollapse';
import ObjForm, {transformObjForEditing, transformObjForSubmitting, getObjOutFields} from '../../../../components/Form/ObjForm';
import {transformActivityForEditing} from '../Activities/ActivityForm';
import ActivitiesList from '../Activities/ActivitiesList';
import InputMask from '../../../../components/Input/InputMask';

export const transformIntegrationForEditing = (item, triggers, activities) =>
{
	const trigger = triggers.find(trig => trig.id === item.trigger.schemaId);
	
	return {
		...item,
		start: Misc.dateToString(Misc.stringToDate(item.start)),
		execMode: item.minOfDay === null?
			'rate':
			'time',
		hour: ((item.minOfDay||0) / 60)|0,
		minute: ((item.minOfDay||0) % 60)|0,		
		trigger: transformObjForEditing({
			...item.trigger,
			options: trigger.options,
			schemaObj: trigger?
				JSON.parse(trigger.schema || ''):
				{}
		}),
		createdAt: Misc.stringToDate(item.createdAt),
		updatedAt: Misc.stringToDate(item.updatedAt),
		activities: item.activities.map(act => ({
			...act,
			activity: transformActivityForEditing(act.activity, activities)
		}))
	};
};

const transformForSubmitting = (item) =>
{
	return {
		...item,
		start: item.start?
			Misc.dateToISO(Misc.stringToDate(item.start, 'DD/MM/YYYY HH:mm')):
			null,
		execMode: undefined,
		hour: undefined,
		minute: undefined,
		freq: item.execMode === 'rate'?
			item.freq:
			null,
		minOfDay: item.execMode === 'time'?
			(item.hour|0) * 60 + (item.minute|0):
			null,
		trigger: transformObjForSubmitting(item.trigger),
		createdAt: undefined,
		updatedAt: undefined,
		activities: item.activities
			.filter(act => !!act.activity.schemaId)
			.map(act => ({
				...act,
				activity: transformObjForSubmitting(act.activity)
			})) 
	};
};

const IntegrationForm = (props) =>
{
	const store = props.store;

	const [item, setItem] = useState(() => Misc.cloneObject(props.item));

	useEffect(() =>
	{
		setItem(Misc.cloneObject(props.item));
	}, [props.item]);
	
	const getIcon = (trigger) =>
	{
		return CollectionUtil.getObjectIcon(trigger.schemaObj.icon || 'fighter-jet');
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

	const handleChangeTrigger = useCallback((e) =>
	{
		handleChange({
			target: {
				name: `trigger.${e.target.name || e.target.id}`,
				value: e.target.value,
				checked: e.target.checked,
				type: e.target.type,
			}
		});
	}, []);

	const handleDelete = useCallback((item) =>
	{

	}, []);

	const validate = (item, errors) =>
	{
		if(!item.trigger.schemaId)
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
				`collections/${store.collection.id}/integrations/${item.id}`, 
				transf):
			await props.api.post(
				`collections/${store.collection.id}/integrations`, 
				transf);
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return null;
		}

		props.container.showMessage('Integration updated!', 'success');

		props.container.reload();

		return transformIntegrationForEditing(res.data, props.triggers, props.activities);
	}, [item, props.triggers, props.activities]);

	const handleSwitchTrigger = useCallback(async (schemaId, item, onChange) =>
	{
		const trigger = schemaId?
			props.triggers.find(trig => trig.id === schemaId):
			{};

		const schema = trigger.schemaObj || {};

		const fields = Object.keys(trigger.fields || {})
			.filter(field => !field.hidden)
			.reduce((obj, key) => 
			{
				obj[key] = null; 
				return obj;
			}, 
			{});

		if(schema.refs)
		{
			props.loadRefs(schema.refs, props.refs);
		}

		onChange({
			target: {
				name: null,
				value: transformIntegrationForEditing({
					...item,
					trigger: {
						schemaId: schemaId,
						fields: fields,
					},
					activities: []
				},
				props.triggers,
				props.activities)
			}
		});
	}, [props.triggers, props.activities, props.refs]);

	const handleGetFieldOptions = useCallback((item, name) =>
	{
		switch(name)
		{
		case 'collection':
			return Object.entries(props.store.collection.schemaObj.columns)
				.filter(column => !column.auto)
				.map(([key, column]) => ({id: key, ...column}));

		case 'in':
		{
			return getObjOutFields(item.trigger, 'sync', props.refs);
		}

		default:
			return [];
		}
	}, [props.refs]);

	const handleGetFunctionOptions = useCallback(() =>
	{
		return [];
	}, []);

	const renderTriggersOptions = () =>
	{
		return (
			[{value: '', title: ''}].concat(props.triggers || []).map((trigger, index) => 
				<option value={trigger.id} key={index}>{trigger.title}</option>
			)
		);
	};
	
	const hasTrigger = !!item.trigger.schemaId;

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
				<Col xs="12">
					<Label className="base-form-label">Trigger</Label>
					<InputGroup>
						<InputGroupAddon addonType="prepend">
							<div className="base-form-prepend-icon" style={{width: 24}}>
								{getIcon(item.trigger)}
							</div>
						</InputGroupAddon>
						<Input 
							disabled={!!item.id} 
							required 
							type="select" 
							value={item.trigger.schemaId || ''} 
							onChange={(e) => handleSwitchTrigger(e.target.value, item, handleChange)}>
							{renderTriggersOptions()}
						</Input>
					</InputGroup>
				</Col>
			</Row>

			{hasTrigger &&
				<>
					<Row>
						<Col sm="12" md="6">
							<Label className="base-form-label">Active</Label>
							<Input 
								className="base-form-checkbox"
								name="active"
								type="checkbox"
								checked={!!item.active}
								onChange={handleChange}
							/>								
						</Col>
						<Col sm="12" md="6">
							<Label className="base-form-label">Start at</Label>
							<InputMask mask="date:nosecs">
								{({setInputRef}) =>
									<Input 
										innerRef={setInputRef}
										name="start" 
										type="text" 
										value={item.start || ''} 
										onChange={handleChange}/>
								}
							</InputMask>
						</Col>
					</Row>
				</>
			}	

			{hasTrigger && ((item.trigger.options & CollectionUtil.TRIGGER_OPTIONS_ALLOW_FREQ) !== 0) &&
				<>
					<Row>
						<Col sm="12" md="6">
							<Label className="base-form-label">Execution method</Label>
							<Input 
								required 
								name="execMode" 
								type="select" 
								value={item.execMode || ''} 
								onChange={handleChange}>
								<option value=""></option>
								<option value="rate">Frequency</option>
								<option value="time">Time of day</option>
							</Input>
						</Col>
						{
							item.execMode === 'rate' && 
							<Col sm="12" md="6">
								<Label className="base-form-label">Frequency</Label>
								<Input 
									required 
									name="freq" 
									type="select" 
									value={item.freq || 0} 
									onChange={handleChange}>
									{CollectionUtil.getIntegrationRates().map((opt, index) => 
										<option key={index} value={opt.value}>{opt.label}</option>)}
								</Input>
							</Col>
						}
						{
							item.execMode === 'time' && 
							<Col sm="12" md="6">
								<Label className="base-form-label">Time of day</Label>
								<InputGroup>
									<Input 
										required 
										name="hour" 
										type="number" 
										min="0"
										max="23"
										value={item.hour !== null? `00${item.hour}`.slice(-2): ''}
										onChange={handleChange}>
									</Input>:
									<Input 
										required 
										name="minute" 
										type="number" 
										min="0"
										max="59"
										value={item.minute !== null? `00${item.minute}`.slice(-2): ''}
										onChange={handleChange}>
									</Input>
								</InputGroup>
							</Col>
						}					
					</Row>
				</>			
			}

			<ObjForm
				obj={item.trigger}
				refs={props.refs}
				onChange={handleChangeTrigger}>
			</ObjForm>

			{hasTrigger && 
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Activities</Label>
						<ActivitiesList
							store={props.store}
							isMobile={props.isMobile}
							element={item}
							activities={props.activities}
							refs={props.refs}
							onChange={handleChange}
							loadRefs={props.loadRefs}
							// note: must be props.item because 'item' is cloned with JSON.### and the pre-compilation is lost
							getFieldOptions={(name) => handleGetFieldOptions(props.item, name)} 
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
						<Col xs="12">
							Actions:
						</Col>
						<Col xs="12" md="6">
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

IntegrationForm.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
	triggers: PropTypes.array.isRequired,
	activities: PropTypes.array.isRequired,
	refs: PropTypes.instanceOf(Map).isRequired,
	item: PropTypes.object.isRequired,
	isMobile: PropTypes.bool,
	loadRefs: PropTypes.func.isRequired,
};

export default IntegrationForm;
