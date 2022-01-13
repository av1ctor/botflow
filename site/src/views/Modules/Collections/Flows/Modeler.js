import React, {useCallback, useEffect, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import BpmnModeler from 'bpmn-js/lib/Modeler';
import {is} from 'bpmn-js/lib/util/ModelUtil';
import {Button} from 'reactstrap';
import LogicalTree from '../../../../components/Tree/LogicalTree';
import CollectionUtil from '../../../../libs/CollectionUtil';
import BpmnConverter from '../../../../libs/BpmnConverter/BpmnConverter';
import Misc from '../../../../libs/Misc';
import {transformActivityForEditing, transformActivityForSubmitting} from '../Activities/ActivityForm';
import PropertiesPanel from './Properties';

const jsonToActivities = (providers, json) =>
{
	const parse = (json) => {
		try
		{
			return json? 
				JSON.parse(json): 
				null;
		}
		catch
		{
			return null;
		}
	};

	const activities = json? 
		parse(json):
		[];

	return activities.map(
		act => ({
			activity: {
				...transformActivityForEditing(act, providers),
				id: null
			}
		})
	);
};

const activitiesToJson = (activities) =>
{
	const transformed = activities.map(
		act => ({
			...transformActivityForSubmitting(act.activity),
			id: undefined,
		}));
	return transformed? 
		JSON.stringify(transformed):
		null;
};

const jsonToCondition = (json) =>
{
	const parse = (json) => {
		try
		{
			return json? 
				JSON.parse(json): 
				null;
		}
		catch
		{
			return null;
		}
	};

	return LogicalTree.unpack(
		parse(json), 
		(node) => ({
			...node, 
			type: node.column? 
				'COLUMN': 
				node.automation? 
					'AUTOMATION': 
					'',
			column: node.column || {name: '', op: '', value: null},
			automation: node.automation || {name: ''}
		})
	);
};

const conditionToJson = (condition) =>
{
	return JSON.stringify(
		LogicalTree.pack(
			condition, 
			(node) => {
				return {
					...node,
					type: undefined,
					column: node.type === 'COLUMN'? 
						node.column: 
						undefined,
					automation: node.type === 'AUTOMATION'? 
						node.automation: 
						undefined,
				};
			}
		)
	);
};

const Modeler = observer(props =>
{
	const store = props.store;
	
	const [, setSelectedElements] = useState([]);
	const [element, setElement] = useState(null);
	const [isSaving, setIsSaving] = useState(false);
	const [columns, setColumns] = useState([]);

	const converter = useRef(new BpmnConverter());
	const modeler = useRef(null);

	useEffect(() =>
	{
		modeler.current = new BpmnModeler({
			container: '#js-canvas', 
			width: '100%', 
			height: '100%',
			keyboard: {
				bindTo: document.body
			},
			moddleExtensions: {
				rf: converter.current.getExtensions()
			},
			additionalModules: converter.current.getCustomModules()
		});

		modeler.current.on('selection.changed', onSelectionChanged);
		modeler.current.on('element.changed', onElementChanged);
	}, []);

	useEffect(() =>
	{
		const schema = store.collection.schemaObj;

		converter.current.setIntegrations(modeler.current, props.integrations);

		const xml = converter.current.toBpmn(schema.flows);
		loadBpmn(xml);

		setColumns(CollectionUtil.transformColumns(schema, schema.columns));
	}, [store.collection, props.integrations]);

	const updateSchemaWithFlows = async (flows) =>
	{
		const res = await store.updateFlows(flows);
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return false;
		}

		props.container.showMessage('Atualização realizada!', 'success');
	};

	const onBpmnLoaded = (err) =>
	{
		if(err)
		{
			props.container.showMessage(err.message, 'error');
			return;
		}

		onResize();

		const schema = store.collection.schemaObj;
		converter.current.addOverlays(
			modeler.current, schema.flows, props.integrations);
	};

	const loadBpmn = (xml) =>
	{
		if(xml)
		{
			modeler.current.importXML(xml, onBpmnLoaded);
		}
		else
		{
			modeler.current.createDiagram(onBpmnLoaded);
		}
	};

	const onBpmnSaved = (err, xml) =>
	{
		const {container} = props;
		
		if(err)
		{
			container.showMessage(err.message, 'error');
			return;
		}

		try
		{
			setIsSaving(true);

			const {flows, errors} = converter.current.fromBpmn(xml);

			if(errors.length > 0)
			{
				container.showMessage(errors, 'error');
			}
			else
			{
				updateSchemaWithFlows(flows);
			}
		}
		finally
		{
			setIsSaving(false);
		}
	};

	const saveBpmn = useCallback(() =>
	{
		modeler.current.saveXML({ format: false }, onBpmnSaved);
	}, []);

	const uploadBpmn = useCallback((e) =>
	{
		if(!e.target.files || e.target.files.length === 0)
		{
			return;
		}

		const reader = new FileReader();

		reader.onload = (e) => loadBpmn(e.target.result);

		reader.readAsText(e.target.files[0]);
	}, []);

	const onResize = () =>
	{
		//const canvas = modeler.current.get('canvas');
		//canvas.zoom('fit-viewport');
	};

	const getExtensionElement = (bo, type) =>
	{
		if (!bo || !bo.extensionElements) 
		{
			return null;
		}

		return bo.extensionElements.values.find(
			elm => elm.$type === type);
	};

	const getConditionExpression = (bo) =>
	{
		if (!bo || !bo.conditionExpression) 
		{
			return null;
		}
		
		const ce = bo.conditionExpression;
		if(ce.$type !== 'bpmn:Expression' ||
			!ce.body ||
				ce.body.indexOf('{') !== 0)
		{
			return null;
		}
		
		return ce.body;
	};

	const transformElementForEditing = useCallback((el, current = null) =>
	{
		if(!el)
		{
			el = {
				businessObject: {}
			};
		}

		if(el.labelTarget) 
		{
			el = el.labelTarget;
		}

		const isStart = is(el, 'bpmn:StartEvent');
		const isSeq = !isStart && is(el, 'bpmn:SequenceFlow');
		const isService = !isStart && !isSeq && is(el, 'bpmn:ServiceTask');
		
		const condition = 
			isSeq && 
			getConditionExpression(el.businessObject);

		const activities = 
			isService && 
			getExtensionElement(el.businessObject, 'rf:Activities');

		return {
			element: el,
			id: el.id,
			name: el.businessObject.name,
			is: isSeq? 
				'SEQ':
				isService?
					'SERVICE':
					isStart?  
						'START':
						'',
			condition: !current || current.id !== el.id?
				isSeq?
					jsonToCondition(condition):
					null:
				current.condition,
			activities: !current || current.id !== el.id?
				isService?
					activities? jsonToActivities(props.activities, activities.body): []:
					null:
				current.activities
		};
	}, [props.activities]);

	const onSelectionChanged = useCallback((e) =>
	{
		setSelectedElements(e.newSelection);
		setElement(
			transformElementForEditing(e.newSelection[0])
		);
	}, []);

	const onElementChanged = useCallback((e) =>
	{
		setElement(element => 
			transformElementForEditing(e.element, element)
		);
	}, []);

	const createActivityOnModel = (el, modeling) =>
	{
		const moddle = modeler.current.get('moddle');
		const elm = el.element;

		const extensionElements = 
			elm.businessObject.extensionElements || 
			moddle.create('bpmn:ExtensionElements');

		const actionsExpression = moddle.create('rf:Activities', {
			body: activitiesToJson(el.activities)
		});

		extensionElements.get('values').push(
			actionsExpression
		);

		modeling.updateProperties(elm, {extensionElements});
	};

	const updateActivityOnModel = (el, modeling) =>
	{
		const elm = el.element;
		
		const {extensionElements} = elm.businessObject;
		extensionElements.values[0].body = activitiesToJson(el.activities);
		
		modeling.updateProperties(elm, {extensionElements});
	};

	const deleteActionsFromModel = (el, modeling) =>
	{
		const elm = el.element;
	
		modeling.updateProperties(elm, {extensionElements: undefined});
	};

	const createConditionOnModel = (el, modeling) =>
	{
		const moddle = modeler.current.get('moddle');
		const elm = el.element;

		const source = elm.source;
		if (source && source.businessObject.default === elm.businessObject) 
		{
			modeling.updateProperties(source, {default: undefined});
		}		
		
		const conditionExpression = moddle.create('bpmn:Expression', {
			$type: 'bpmn:tFormalExpression', 
			body: conditionToJson(el.condition)
		});

		conditionExpression.$container = elm;

		modeling.updateProperties(elm, {conditionExpression});
	};

	const updateConditionOnModel = (el, modeling) =>
	{
		const elm = el.element;
		
		const {conditionExpression} = elm.businessObject;
		conditionExpression.body = conditionToJson(el.condition);
		
		modeling.updateProperties(elm, {conditionExpression});
	};

	const deleteConditionFromModel = (el, modeling) =>
	{
		const elm = el.element;
	
		modeling.updateProperties(elm, {conditionExpression: undefined});
	};

	const toggleElementOnChange = (to = 'on') =>
	{
		if(to === 'on')
		{
			modeler.current.on('element.changed', onElementChanged);
		}
		else
		{
			modeler.current.off('element.changed', onElementChanged);
		}
	};

	const handleChange = useCallback(async (e) =>
	{
		const modeling = modeler.current.get('modeling');
		const {name, value} = e.target;
		
		switch(name)
		{
		case 'name':
			modeling.updateLabel(element.element, value);
			break;

		default:
			setElement(element =>
			{
				const el = Object.assign({}, element);
				Misc.setFieldValue(el, name, value);

				toggleElementOnChange('off');

				if(name.indexOf('activities') === 0)
				{
					if(!getExtensionElement(el.element.businessObject, 'rf:Activities'))
					{
						createActivityOnModel(el, modeling);
					}
					else
					{
						updateActivityOnModel(el, modeling);
					}
				}
				else if(name.indexOf('condition') === 0)
				{
					if(!hasConditionOnModel(el))
					{
						createConditionOnModel(el, modeling);
					}
					else
					{
						updateConditionOnModel(el, modeling);
					}
				}

				toggleElementOnChange('on');

				return el;
			});
		}
	}, [element]);

	const hasConditionOnModel = (el) =>
	{
		return el.element.businessObject &&
			el.element.businessObject.conditionExpression;
	};

	const updateCondition = useCallback(async (tree) =>
	{
		setElement(element =>
		{
			const el = Object.assign({}, element);
			el.condition = tree;

			const modeling = modeler.current.get('modeling');
	
			if(!tree)
			{
				deleteConditionFromModel(el, modeling);
			}
			else
			{
				if(!hasConditionOnModel(el))
				{
					createConditionOnModel(el, modeling);
				}
				else
				{
					updateConditionOnModel(el, modeling);
				}
			}		

			return el;
		});
	}, [element]);

	
	return (
		<div className="rf-col-modeler-content">
			<div 
				id="js-canvas" 
				className="rf-col-modeler-canvas">
			</div>
			<div 
				className={`rf-col-modeler-props ${!props.fullScreen && element && element.id? 'expanded': 'collapsed'}`}>
				<PropertiesPanel 
					{...props}
					columns={columns}
					element={element}
					loadRefs={props.loadRefs}
					onChange={handleChange}
					updateCondition={updateCondition} />
			</div>
		
			<div className="rf-col-modeler-buttons">
				<Button
					disabled={isSaving}
					onClick={saveBpmn}>
					Atualizar
				</Button>
			</div>
		</div>
	);
});

Modeler.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	automations: PropTypes.array,
	integrations: PropTypes.array,
	triggers: PropTypes.array,
	activities: PropTypes.array,
	refs: PropTypes.instanceOf(Map).isRequired,
	loadRefs: PropTypes.func.isRequired,
};

export default Modeler;

