import CollectionUtil from '../CollectionUtil';
import XmlParser from '../XmlParser';
import activitiesExtension from './Extensions/Activities';
//import CustomRenderer from './Custom/CustomRenderer';
//import CustomElementFactory from './Custom/CustomElementFactory';

import './Custom/styles.css';

const xmlHeader = '<?xml version="1.0" encoding="UTF-8"?>';
const defaultCollabName = 'Collaboration_1';
const defaultProcessName = 'Process_1';
const typeToTagLut = new Map([
	['XOR', 'bpmn:exclusiveGateway'],
	['USER', 'bpmn:userTask'],
	['SERVICE', 'bpmn:serviceTask'],
	['PARALLEL', 'bpmn:parallelGateway'],
	['END', 'bpmn:endEvent'],
	['START', 'bpmn:startEvent'],
	['PART', 'bpmn:participant'],
]);

const tagToTypeLut = new Map([
	['exclusiveGateway', 'XOR'],
	['userTask', 'USER'],
	['task', 'USER'],
	['serviceTask', 'SERVICE'],
	['parallelGateway', 'PARALLEL'],
	['endEvent', 'END'],
	['startEvent', 'START'],
	['participant', 'PART'],
]);

export default class BpmnConverter
{
	constructor()
	{
		this.parser = new XmlParser();
	}

	getExtensions()
	{
		return {
			name: 'RfExtensions',
			uri: 'https://botflow.com/schemas/bpmn/extensions',
			prefix: 'rf',
			xml: {	
				'tagAlias': 'lowerCase'
			},
			types: [
				activitiesExtension,
			],
			emumerations: [],
			associations: []
		};
	}

	getCustomModules()
	{
		return [
			//CustomRenderer,
			//CustomElementFactory
		];
	}
	
	_collectParticipants(flows)
	{
		const res = new Map();

		Object.entries(flows)
			.filter(([, node]) => node.type === 'PART')
			.forEach(([key, node]) => res.set(key, node));

		return res;
	}

	_collectNodes(flows)
	{
		const res = new Map();

		Object.entries(flows)
			.filter(([, node]) => node.type !== 'PART')
			.forEach(([key, node]) => res.set(key, node));

		return res;
	}

	_boundsToBpmn(bounds = null)
	{
		return bounds? 
			{
				'@_x': bounds.x,
				'@_y': bounds.y,
				'@_width': bounds.width,
				'@_height': bounds.height,
			}: 
			null;
	}

	_waypointsToBpmn(edges)
	{
		return edges?
			edges.map(edge => ({
				'@_x': edge.x,
				'@_y': edge.y,
			})):
			null;
	}

	_seqToBpmn(srcKey, srcId, src, dstKey, seq, flows, processes, messages, edges)
	{
		const dst = flows[dstKey];
		const dstId = dstKey;
		const seqId = `Flow_${srcKey}_${dstKey}`;
		const cond = seq.condition? 
			JSON.stringify(seq.condition): 
			null;

		if(!src.part || src.part === dst.part)
		{
			const seqs = processes.get(src.part || defaultProcessName)['bpmn:sequenceFlow'];
			seqs.push({
				'@_id': seqId,
				'@_name': (seq && seq.label)? (seq.label.value || seq.label): undefined,
				'@_sourceRef': srcId,
				'@_targetRef': dstId,
				'conditionExpression': cond? 
					{
						'@_type': 'bpmn:tFormalExpression',
						'#text': cond
					}:
					undefined
			});
		}
		else
		{
			messages.push({
				'@_id': seqId,
				'@_name': (seq && seq.label)? (seq.label.value || seq.label): undefined,
				'@_sourceRef': srcId,
				'@_targetRef': dstId,
			});
		}

		if(seq)
		{
			const waypoints = this._waypointsToBpmn(seq.edges);
			if(waypoints)
			{
				const edge = {
					'@_id': `${seqId}_di`,
					'@_bpmnElement': seqId,
					'di:waypoint': waypoints,
				};
			
				edges.push(edge);

				if(seq.label && seq.label.bounds)
				{
					const bounds = this._boundsToBpmn(seq.label.bounds);
					if(bounds)
					{
						edge['bpmndi:BPMNLabel'] = {
							'dc:Bounds': bounds
						};
					}
				}		
			}
		}
	}
	
	toBpmn(flows)
	{
		if(!flows)
		{
			return null;
		}

		const definitions = {
			'@_xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
			'@_xmlns:bpmn': 'http://www.omg.org/spec/BPMN/20100524/MODEL',
			'@_xmlns:bpmndi': 'http://www.omg.org/spec/BPMN/20100524/DI',
			'@_xmlns:dc': 'http://www.omg.org/spec/DD/20100524/DC',
			'@_xmlns:di': 'http://www.omg.org/spec/DD/20100524/DI',
			'@_id': 'Definitions_1',
			'@_targetNamespace': 'http://bpmn.io/schema/bpmn',
			'@_exporter': 'bpmn-js (https://demo.bpmn.io)',
			'@_exporterVersion': '6.5.1',
		};

		const json = {
			'bpmn:definitions': definitions
		};

		const parts = this._collectParticipants(flows);
		const nodes = this._collectNodes(flows);
		
		const participants = [];
		const messages = [];
		const processes = new Map();
		const shapes = [];
		const edges = [];

		const getLabel = (label) => 
			label && (label.value !== null && label.value !== undefined? label.value: label);

		if(parts.size > 0)
		{
			definitions['bpmn:collaboration'] = {
				'@_id': defaultCollabName,
				'bpmn:participant': participants,
				'bpmn:messageFlow': messages,
			};

			for(let [key, part] of parts.entries())
			{
				const nodeId = `Participant_${key}`;
				participants.push({
					'@_id': nodeId,
					'@_name': getLabel(part.label),
					'@_processRef': `Process_${key}`
				});

				processes.set(key, {
					'@_id': `Process_${key}`,
					'@_isExecutable': 'false',
				});

				const bounds = this._boundsToBpmn(part.bounds);
				if(bounds)
				{
					const shape = {
						'@_id': `${nodeId}_di`,
						'@_bpmnElement': nodeId,
						'@_isHorizontal': 'true',
						'dc:Bounds': bounds
					};
	
					shapes.push(shape);
				}
			}
		}
		else
		{
			processes.set(defaultProcessName, {
				'@_id': defaultProcessName,
				'@_isExecutable': 'false',	
			});
		}

		// emit 
		for(let [nodeKey, node] of nodes)
		{
			const inps = node.in || [];
			const outs = Object.entries(node.out || {});
			const nodeId = nodeKey;

			const proc = processes.get(node.part? 
				node.part: 
				defaultProcessName);

			const tagName = typeToTagLut.get(node.type);

			const tags = proc[tagName]? 
				proc[tagName]: 
				proc[tagName] = [];

			const extensions = [];

			if(node.activities)
			{
				extensions.push({
					'rf:activities': [
						{
							'#text': JSON.stringify(node.activities)
						}
					]
				});
			}

			tags.push({
				'@_id': nodeId,
				'@_name': getLabel(node.label),
				'bpmn:incoming': inps.map((k) => `Flow_${k}_${nodeKey}`),
				'bpmn:outgoing': outs.map(([k]) => `Flow_${nodeKey}_${k}`),
				'bpmn:extensionElements': extensions.length > 0?
					extensions:
					undefined
			});

			const bounds = this._boundsToBpmn(node.bounds);
			if(bounds)
			{
				const shape = {
					'@_id': `${nodeId}_di`,
					'@_bpmnElement': nodeId,
					'dc:Bounds': bounds
				};

				shapes.push(shape);

				if(node.label && node.label.bounds)
				{
					const bounds = this._boundsToBpmn(node.label.bounds);
					if(bounds)
					{
						shape['bpmndi:BPMNLabel'] = {
							'dc:Bounds': bounds
						};
					}
				}
			}
		}

		// the sequenceFlow's must be the last emitted
		for(let [, proc] of processes)	
		{
			proc['bpmn:sequenceFlow'] = [];
		} 

		for(let [nodeKey, node] of nodes)
		{
			const outs = Object.entries(node.out || {});
			const nodeId = nodeKey;

			outs.forEach(([seqKey, seq]) => {
				this._seqToBpmn(nodeKey, nodeId, node, seqKey, seq, flows, processes, messages, edges);
			});			
		}		

		definitions['bpmn:process'] = Array.from(processes).map(([, v])=> v);

		definitions['bpmndi:BPMNDiagram'] = {
			'@_id': 'BPMNDiagram_1',
			'bpmndi:BPMNPlane': {
				'@_id': 'BPMNPlane_1',
				'@_bpmnElement': parts.size > 0? 
					defaultCollabName: 
					defaultProcessName,
				'bpmndi:BPMNShape': shapes,
				'bpmndi:BPMNEdge': edges,
			}
		};

		const xml = this.parser.toXml(json);

		return xmlHeader + xml;
	}

	fromBpmn(xml)
	{
		const bpmn = this.parser.fromXml(xml);

		const flows = {};
		const errors = [];

		const transformShapes = (shapes) => {
			const res = {};
			shapes.forEach(shape => res[shape['@_bpmnElement']] = {...shape});
			return res;
		};

		const transformEdges = (edges) => {
			const res = {};
			edges.forEach(edge => res[edge['@_bpmnElement']] = {...edge});
			return res;
		};

		const definitions = bpmn['definitions'][0];
		const collaboration = definitions['collaboration']? definitions['collaboration'][0]: null;
		const plane = definitions['BPMNDiagram'][0]['BPMNPlane'][0];
		const shapes = transformShapes(plane['BPMNShape']);
		const edges = transformEdges(plane['BPMNEdge']);

		const participantsLut = new Map();
		const nodesLut = new Map();
		const seqsLut = new Map();

		const boundsToObj = (bounds) => ({
			x: bounds['@_x'],
			y: bounds['@_y'],
			width: bounds['@_width'],
			height: bounds['@_height'],
		});

		const edgesToObj = (edge, label) => ({
			edges: edge.waypoint.map(way => ({
				x: way['@_x'],
				y: way['@_y']
			})),
			label: edge['BPMNLabel']?
				{
					value: label,
					bounds: boundsToObj(edge['BPMNLabel'][0]['Bounds'][0])
				}: 
				undefined			
		});

		const tagToFlow = (node, type, process) => 
		{
			const extensions = node['extensionElements'] || [];
			const activitiesExtension = extensions.find(ext => ext.activities !== null);
			
			const flow = {
				part: collaboration? 
					participantsLut.get(process['@_id']): 
					undefined,
				type: type,
				label: {
					value: node['@_name'] || ''
				},
				in: [],
				out: {},
				activities: activitiesExtension?
					JSON.parse(activitiesExtension.activities):
					undefined
			};

			const shape = shapes[node['@_id']];
			if(shape)
			{
				if(shape['Bounds'])
				{
					flow.bounds = boundsToObj(shape['Bounds'][0]);
				}
				if(shape['BPMNLabel'])
				{
					flow.label.bounds = boundsToObj(shape['BPMNLabel'][0]['Bounds'][0]);
				}
			}

			return flow;
		};
		
		const messages = new Map();
		if(collaboration)
		{
			const participants = collaboration['participant'];
			if(participants)
			{
				participants.forEach(part => 
				{
					const name = CollectionUtil.labelToKey(part['@_name']);
					if(!name)
					{
						errors.push(`Participante ${part['@_id']} não possui name definido`);
					}
					else
					{
						if(flows[name])
						{
							errors.push(`Detectado participante com name duplicado: ${part['@_name']}`);
						}

						participantsLut.set(part['@_processRef'], name);
						flows[name] = {
							type: 'PART',
							label: {
								value: part['@_name'] || ''
							},
							bounds: boundsToObj(shapes[part['@_id']]['Bounds'][0])
						};
					}
				});
			}

			const messageFlows = collaboration['messageFlow'];
			if(messageFlows)
			{
				messageFlows.forEach(message =>
				{
					messages.set(message['@_id'], message);
				});
			}
		}

		const processes = definitions['process'];

		processes.forEach(process =>
		{
			const nodes = process['sequenceFlow'];
			if(nodes)
			{
				nodes.forEach(node => seqsLut.set(node['@_id'], node));
			}
		});
	
		processes.forEach(process =>
		{
			for(let [tag, type] of tagToTypeLut)
			{
				const elements = process[tag];
				if(elements)
				{
					elements.forEach(element => 
					{
						const name = CollectionUtil.labelToKey(element['@_name']);
						if(!name)
						{
							errors.push(`Elemento ${element['@_id']} não possui name definido`);
						}
						else
						{
							if(flows[name])
							{
								errors.push(`Detectado elemento com name duplicado: ${element['@_name']}`);
							}
	
							const flow = tagToFlow(element, type, process);
							flows[name] = flow;
							nodesLut.set(element['@_id'], {node: element, flow, name});
						}
					});
				}
			}
		});

		if(errors.length === 0)
		{
			for(let [, {node, flow}] of nodesLut)
			{
				let inps = node['incoming'];
				if(inps)
				{
					inps = inps.constructor === String? [inps]: inps;
					inps.forEach(inp => 
					{
						const seq = seqsLut.get(inp);
						const src = nodesLut.get(seq['@_sourceRef']);
						if(src)
						{
							const {name} = src;
							flow.in.push(name);
						}
					});
				}

				let outs = node['outgoing'];
				if(outs)
				{
					outs = outs.constructor === String? [outs]: outs;
					outs.forEach(out => 
					{
						const seq = seqsLut.get(out);
						const dst = nodesLut.get(seq['@_targetRef']);
						if(dst)
						{
							const {name} = dst;
							const edge = edges[seq['@_id']];
							const o = flow.out[name] = edgesToObj(edge, seq['@_name']);
							if(seq['conditionExpression'])
							{
								const cond = seq['conditionExpression'][0];
								try
								{
									o.condition = cond['false']?
										JSON.parse(cond['false']):
										undefined;
								}
								catch
								{
									errors.push('Condição inválida');
									o.condition = undefined;
								}
							}
						}
					});
				}
			}

			for(let [id, message] of messages)
			{
				const {flow: src, name: srcName} = nodesLut.get(message['@_sourceRef']);
				const {flow: dst, name: dstName} = nodesLut.get(message['@_targetRef']);
				const edge = edges[id];

				dst.in[srcName] = [];
				src.out[dstName] = edgesToObj(edge, message['@_name']);
			}
		}

		return {flows, errors};
	}

	_genOverlayHtml(klass, length, text, icon)
	{
		return `<div 
			class="diagram-note ${klass}">
			${length} <span class="diagram-text">${text}</span><i class="diagram-icon la la-${icon}" />
		</div>`;
	}

	addOverlays(modeler, flows, integrations)
	{
		if(!flows)
		{
			return;
		}
		
		const overlays = modeler.get('overlays');
		
		for(let [id, node] of Object.entries(flows))
		{
			switch(node.type)
			{
			case 'START':
				if(integrations && integrations.length > 0)
				{
					overlays.add(id, 'note', {
						position: {
							bottom: 0,
							right: 0
						},
						html: this._genOverlayHtml(
							'integrations', integrations.length, 'integrations', 'chain') 
					});		
				}
				break;

			case 'SERVICE':
				if(node.activities && node.activities.length > 0)
				{
					overlays.add(id, 'note', {
						position: {
							bottom: 0,
							right: 0
						},
						html: this._genOverlayHtml(
							'activities', node.activities.length, 'activities', 'running') 
					});		
				}
				break;
			}

			const outs = Object.entries(node.out || {});
			if(outs.length > 0)
			{
				for(let [key, out] of outs)
				{
					if(out.condition)
					{
						const conditions = out.condition.and || out.condition.or;
						if(conditions && conditions.length > 0)
						{
							const seqId = `Flow_${id}_${key}`;
							overlays.add(seqId, 'note', {
								position: {
									top: 0,
									left: 0
								},
								html: this._genOverlayHtml(
									'conditions', conditions.length, 'conditions', 'random') 
							});		
						}
					}
				}
			}
		}
	}

	setIntegrations(modeler, integrations)
	{
		//modeler.get('customRenderer').setIntegrations(integrations);
	}
}