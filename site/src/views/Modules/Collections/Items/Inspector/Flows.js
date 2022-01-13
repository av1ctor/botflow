import React, {useEffect, useRef} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import BpmnJS from 'bpmn-js/dist/bpmn-navigated-viewer.development';
import minimapModule from 'diagram-js-minimap';
import BpmnConverter from '../../../../../libs/BpmnConverter/BpmnConverter';

const Flows = observer(props =>
{
	const store = props.store;
	
	const viewer = useRef(null);
	const converter = useRef(new BpmnConverter());

	useEffect(() =>
	{
		const flows = store.collection.schemaObj.flows || {};
		show(converter.current.toBpmn(flows, null));
	}, [store.collection]);

	const onLoad = (err) =>
	{
		const {item} = props;

		if(err)
		{
			props.container.showMessage(err, 'error');
			return;
		}

		const schemaflows = Object.entries(store.collection.schemaObj.flows || {});

		const flows = item._meta && item._meta.flows? 
			(Object.keys(item._meta.flows).length > 0?
				Object.keys(item._meta.flows):
				schemaflows
					.filter(([, flow]) => flow.type === 'END')
					.map(([key,]) => key)):
			schemaflows
				.filter(([, flow]) => flow.type === 'START')
				.map(([key,]) => key);
		
		const canvas = viewer.current.get('canvas');

		if(flows)
		{
			flows.forEach(flow => 
				canvas.addMarker(flow, 'highlight')
			);
		}

		//canvas.zoom('fit-viewport');
	};

	const show = (bpmn) =>
	{
		if(viewer.current === null)
		{
			viewer.current = new BpmnJS({
				container: '#diagramCanvas',
				additionalModules: [
					minimapModule
				],
				moddleExtensions: {
					rf: converter.current.getExtensions()
				}
			});
		}
		
		viewer.current.importXML(bpmn, onLoad);
	};

	return(
		<div 
			id="diagramCanvas" 
			className="w-100 h-100">
		</div>
	);
});

Flows.propTypes = 
{
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	item: PropTypes.object.isRequired,
};

export default Flows;