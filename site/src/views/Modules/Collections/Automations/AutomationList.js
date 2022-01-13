import React, {useCallback, useContext, useEffect, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Button} from 'reactstrap';
import BaseList from '../../../../components/BaseList';
import CollectionUtil from '../../../../libs/CollectionUtil';
import ObjUtil from '../../../../libs/ObjUtil';
import AutomationForm, {transformAutomationForEditing} from './AutomationForm';
import {genEmptyActivity} from '../Activities/ActivityForm';
import {WorkspaceContext} from '../../../../contexts/WorkspaceContext';

const getItemTemplate = () =>
{
	return {
		id: null,
		active: true,
		type: '',
		trigger: '',
		activities: [genEmptyActivity()]
	};
};

const AutomationList = observer(props =>
{
	const store = props.store;
	const workspace = useContext(WorkspaceContext);
	
	const [activities, setActivities] = useState(null);
	const [refs, setRefs] = useState(new Map());

	const baseList = useRef();

	useEffect(() =>
	{
		(async () =>
		{
			const res = await workspace.loadObjSchemas('activities');
			setActivities(!res.errors?
				res.data:
				[]);		
		})();
	}, []);

	const loadRefs = useCallback(async (refsToload, currentRefs) =>
	{
		setRefs(await ObjUtil.loadRefs(refsToload, currentRefs, workspace));
	}, []);
	
	const getIcon = (trigger) =>
	{
		return CollectionUtil.getObjectIcon('cogs');
	};

	const getHeader = () =>
	{
		return [
			{name: null, title: 'Description', size: {xs: 6, md:5}, klass: ''},
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
						{item.desc}
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
						<AutomationForm
							{...props}
							container={baseList.current}
							activities={activities}
							refs={refs}
							item={item}
							loadRefs={loadRefs}
						/>
					</Col>
				</Row>
			</Col>
		);		
	}, [activities, refs]);

	const renderActions = (item) =>
	{
		return (
			<>
				<Button 
					size="xs" outline className="base-list-button" 
					onClick={null} 
					title="Delete">
					<i className="base-list-button-add-icon la la-trash"></i>
				</Button>
			</>
		);
	};

	const transformItemForEditing = useCallback((item) =>
	{
		const transf = transformAutomationForEditing(item, props.store, activities);
		const refsToLoad = [];

		transf.activities.forEach(act =>
		{
			if(act.activity.schemaObj && act.activity.schemaObj.refs)
			{
				refsToLoad.push(act.activity.schemaObj.refs);
			}			
		});

		return [transf, refsToLoad];
	}, [activities, refs]);

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
	}, [activities, refs]);

	if(!activities)
	{
		return null;
	}

	return (
		<BaseList
			ref={baseList}
			inline
			controller={`collections/${store.collection.id}/automations`}
			updateHistory={false}
			sortBy='createdAt'
			api={props.api}
			container={props.container}
			itemTemplate={getItemTemplate()}
			getHeader={getHeader}
			onLoad={handleItemsLoaded}
			renderItem={renderItem}
			renderActions={renderActions}
		/>
	);
});

AutomationList.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
};

export default AutomationList;
