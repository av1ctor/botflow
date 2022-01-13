import React, {useCallback, useContext, useEffect, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import SidebarResizeable from '../../../../components/Bar/SidebarResizeable';
import ObjUtil from '../../../../libs/ObjUtil';
import { transformIntegrationForEditing } from '../Integrations/IntegrationForm';
import {WorkspaceContext} from '../../../../contexts/WorkspaceContext';
import './Overriders';
import Modeler from './Modeler';

const FlowsPanel = observer(props =>
{
	const store = props.store;
	const workspace = useContext(WorkspaceContext);
	
	const [automations, setAutomations] = useState(null);
	const [integrations, setIntegrations] = useState(null);
	const [triggers, setTriggers] = useState(null);
	const [activities, setActivities] = useState(null);
	const [refs, setRefs] = useState(new Map());

	useEffect(() =>
	{
		(async () =>
		{
			if(props.visible)
			{
				Promise.all([
					loadTriggers(),
					loadActivities()
				]).then(res =>
				{
					const triggers = res[0];
					setTriggers(triggers);
					const activities = res[1];
					setActivities(activities);

					Promise.all([
						loadAutomations(), 
						loadIntegrations(triggers, activities), 
					]).then(res =>
					{
						setAutomations(res[0]);
						setIntegrations(res[1]);
					});
				});
			}
		})();
	}, [store.collection, props.visible]);
	
	const loadAutomations = async () =>
	{
		//FIXME: don't reload them unless they were updated (the WS must listen to automation changes)
		const res = await store.loadAutomations();
		return !res.errors?
			res.data:
			[];
	};

	const transformIntegration = useCallback((item, triggers, activities) =>
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
	}, [refs]);

	const transformIntegrations = useCallback(async (items, triggers, activities) =>
	{
		const transf = items.map(item => transformIntegration(item, triggers, activities));

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
	}, [refs]);

	const loadIntegrations = async (triggers, activities) =>
	{
		//FIXME: don't reload them unless they were updated (the WS must listen to integration changes)
		const res = await store.loadIntegrations();
		return !res.errors?
			transformIntegrations(res.data, triggers, activities):
			[];
	};

	const loadTriggers = async () =>
	{
		const res = await workspace.loadObjSchemas('triggers');
		return !res.errors?
			res.data:
			[];
	};

	const loadActivities = async () =>
	{
		const res = await workspace.loadObjSchemas('activities');
		return !res.errors?
			res.data:
			[];
	};

	const loadRefs = useCallback(async (refsToload, currentRefs) =>
	{
		setRefs(await ObjUtil.loadRefs(refsToload, currentRefs, workspace));
	}, []);

	const onResize = (event) =>
	{
		//setResizeEvent(Object.assign({}, event));
	};

	const handleReloadIntegrations = useCallback(async () =>
	{
		setIntegrations(await loadIntegrations(triggers, activities));
	}, [triggers, activities]);

	const container = useRef({});
	Object.assign(container.current, props.container, {	
		reload: handleReloadIntegrations,
	});
	
	if(!props.visible)
	{
		return null;
	}

	return (
		<SidebarResizeable 
			visible={props.visible} 
			position="right" 
			onHide={props.onHide}
			onResize={onResize} 
			fullScreen={props.fullScreen}>
			<div className="w-100 h-100">
				<FocusLock 
					disabled={!props.visible} 
					returnFocus 
					className="rf-flex-100">
					<h4><i className="la la-project-diagram" /> Fluxos</h4>
					{activities && 
						<Modeler
							api={props.api}
							container={container.current}	
							store={props.store}
							fullScreen={props.fullScreen}
							visible={props.visible}
							automations={automations} 
							integrations={integrations}
							triggers={triggers} 
							activities={activities} 
							refs={refs}
							loadRefs={loadRefs}
						/>
					}
				</FocusLock>
			</div>
		</SidebarResizeable>
	);
});

FlowsPanel.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default FlowsPanel;

