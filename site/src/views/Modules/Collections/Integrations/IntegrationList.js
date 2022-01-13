import React, {useCallback, useContext, useEffect, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Button} from 'reactstrap';
import BaseList from '../../../../components/BaseList';
import CollectionUtil from '../../../../libs/CollectionUtil';
import ObjUtil from '../../../../libs/ObjUtil';
import IntegrationForm, {transformIntegrationForEditing} from './IntegrationForm';
import {genEmptyActivity} from '../Activities/ActivityForm';
import {WorkspaceContext} from '../../../../contexts/WorkspaceContext';

const getItemTemplate = () =>
{
	return {
		id: null,
		active: true,
		trigger: {
			schemaId: null,
			schema: null,
			schemaObj: {},
		},
		activities: [genEmptyActivity()]
	};
};

const IntegrationList = observer(props =>
{
	const store = props.store;
	const workspace = useContext(WorkspaceContext);
	
	const [triggers, setTriggers] = useState(null);
	const [activities, setActivities] = useState(null);
	const [refs, setRefs] = useState(new Map());

	const baseList = useRef();

	useEffect(() =>
	{
		(async () =>
		{
			Promise.all([
				workspace.loadObjSchemas('triggers'), 
				workspace.loadObjSchemas('activities')
			]).then((res) =>
			{
				setTriggers(!res[0].errors?
					res[0].data:
					[]);
				setActivities(!res[1].errors?
					res[1].data:
					[]);
			});
		})();
	}, []);

	const loadRefs = async (refsToload, currentRefs) =>
	{
		const res = await ObjUtil.loadRefs(refsToload, currentRefs, workspace);
		setRefs(res);
	};
	
	const getIcon = (trigger) =>
	{
		return CollectionUtil.getObjectIcon(trigger.icon || 'fighter-jet');
	};

	const getHeader = () =>
	{
		return [
			{name: null, title: 'Trigger', size: {xs: 6, md:5}, klass: ''},
			{name: null, title: 'User', size: {md:4}, klass: 'd-none d-md-block'},
			{name: 'createdAt', title: 'Date', size: {md:3}, klass: 'd-none d-md-block'},
		];
	};

	const renderItem = useCallback((item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="6" md="5">
						{getIcon(item.trigger)}
						&nbsp;
						{item.trigger.schemaObj.title}
					</Col>
					<Col md="4" className="d-none d-md-block">
						{item.createdBy.email}
					</Col>
					<Col md="3" className="d-none d-md-block">
						{item.createdAt.toDateString()}
					</Col>
				</>
			);
		}

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<IntegrationForm
							{...props}
							container={baseList.current}
							triggers={triggers}
							activities={activities}
							refs={refs}
							item={item}
							loadRefs={loadRefs}
						/>
					</Col>
				</Row>
			</Col>
		);		
	}, [triggers, activities, refs]);

	const renderActions = (item) =>
	{
		return (
			<>
				<Button 
					size="xs" outline className="base-list-button" 
					onClick={null} 
					title="Remover">
					<i className="base-list-button-add-icon la la-trash"></i>
				</Button>
			</>
		);
	};

	const transformItemForEditing = useCallback((item) =>
	{
		const transf = transformIntegrationForEditing(item, triggers, activities);
		const refsToLoad = [];

		if(transf.trigger.schemaObj && transf.trigger.schemaObj.refs)
		{
			refsToLoad.push(transf.trigger.schemaObj.refs);
		}

		transf.activities.forEach(act =>
		{
			if(act.activity.schemaObj && act.activity.schemaObj.refs)
			{
				refsToLoad.push(act.activity.schemaObj.refs);
			}			
		});

		return [transf, refsToLoad];
	}, [triggers, activities, refs]);

	const handleItemsLoaded = useCallback(async (items) =>
	{
		const transf = items.map(item => transformItemForEditing(item));

		const refsToLoad = transf
			.reduce((res, pair) => res.concat(pair[1]), []);

		let refsLoaded = refs;
		for(let i = 0; i < refsToLoad.length; i++)
		{
			refsLoaded = await ObjUtil
				.loadRefs(refsToLoad[i], refsLoaded, workspace);
		}

		setRefs(refsLoaded);

		return transf.map(pair => pair[0]);
	}, [triggers, activities, refs]);

	const handleReloadList = () =>
	{
		return baseList.current.reload();
	};
	
	const container = useRef({});
	Object.assign(container.current, props.container, {	
		reload: handleReloadList,
	});

	if(!triggers || !activities)
	{
		return null;
	}

	return (
		<BaseList
			ref={baseList}
			inline
			controller={`collections/${store.collection.id}/integrations`}
			updateHistory={false}
			sortBy='createdAt'
			api={props.api}
			container={container.current}
			itemTemplate={getItemTemplate()}
			getHeader={getHeader}
			onLoad={handleItemsLoaded}
			renderItem={renderItem}
			renderActions={renderActions}
		/>
	);
});

IntegrationList.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
};

export default IntegrationList;
