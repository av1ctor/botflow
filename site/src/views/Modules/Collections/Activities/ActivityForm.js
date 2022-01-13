import React, {useCallback} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, InputGroup, InputGroupAddon} from 'reactstrap';
import CollectionUtil from '../../../../libs/CollectionUtil';
import ObjForm, {
	transformObjForEditing, 
	transformObjForSubmitting, 
	buildObjFields} from '../../../../components/Form/ObjForm';

export const genEmptyActivity = () =>
{
	return {
		id: null,
		activity: {
			schemaId: null,
			schema: null,
			schemaObj: {},
		}
	};
};

export const transformActivityForEditing = (item, activities) =>
{
	const activity = activities.find(act => act.id === item.schemaId);
	
	return transformObjForEditing({
		...item,
		schemaObj: activity?
			JSON.parse(activity.schema || ''):
			{}
	});
};

export const transformActivityForSubmitting = (item) =>
{
	return transformObjForSubmitting(item);
};

const ActivityForm = (props) =>
{
	const getIcon = (activity) =>
	{
		return CollectionUtil.getObjectIcon(activity.schemaObj.icon || 'running');
	};

	const handleChange = useCallback((e) =>
	{
		props.onChange({
			target: {
				name: `activities[${props.index}].activity.${e.target.name || e.target.id}`,
				value: e.target.value
			}
		});
	}, []);

	const handleChangeActivity = useCallback(async (e, item) =>
	{
		const schemaId = e.target.value;
		const activity = schemaId?
			props.activities.find(act => act.id === schemaId):
			{};

		const schema = activity.schemaObj || {};

		const fields = buildObjFields(activity.fields);

		if(schema.refs)
		{
			props.loadRefs(schema.refs, props.refs);
		}

		props.onChange({
			target: {
				name: `activities[${props.index}]`,
				value: {
					...item,
					activity: transformObjForEditing({
						id: null,
						fields: fields,
						schemaId: schemaId,
						schema: activity.schema || '',
						schemaObj: schema,
					})
				},
			}
		});
	}, [props.activities, props.refs]);

	const renderActivitiesOptions = () =>
	{
		return (
			[{value: '', title: ''}].concat(props.activities || []).map((act, index) => 
				<option value={act.id} key={index}>{act.title}</option>
			)
		);
	};

	const item = props.activity;

	return (
		<>
			{item.id &&
				<Row>
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">Id</Label>
						<Input 
							type="text" 
							value={item.id? item.id: ''} 
							disabled={true} 
							required 
							readOnly />
					</Col>
				</Row>
			}
			<Row>
				<Col xs="12">
					<Label className="base-form-label">Activity</Label>
					<InputGroup>
						<InputGroupAddon addonType="prepend">
							<div className="base-form-prepend-icon" style={{width: 24}}>
								{getIcon(item.activity)}
							</div>
						</InputGroupAddon>
						<Input 
							type="select" 
							value={item.activity.schemaId || ''} 
							disabled={!!item.id} 
							required 
							onChange={(e) => handleChangeActivity(e, item)}>
							{renderActivitiesOptions()}
						</Input>
					</InputGroup>
				</Col>
			</Row>			
			{item.activity.schemaId && 
				<ObjForm
					obj={item.activity}
					refs={props.refs}
					isMobile={props.isMobile}
					onChange={handleChange}
					getFieldOptions={props.getFieldOptions}
					getFunctionOptions={props.getFunctionOptions}>
				</ObjForm>
			}
		</>
	);

};

ActivityForm.propTypes = 
{
	isMobile: PropTypes.bool,
	activities: PropTypes.array.isRequired,
	refs: PropTypes.instanceOf(Map).isRequired,
	activity: PropTypes.object.isRequired, 
	index: PropTypes.number.isRequired, 
	onChange: PropTypes.func.isRequired, 
	loadRefs: PropTypes.func.isRequired, 
	getFieldOptions: PropTypes.func.isRequired,
	getFunctionOptions: PropTypes.func.isRequired,
};

export default ActivityForm;