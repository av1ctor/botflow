import BaseRenderer from 'diagram-js/lib/draw/BaseRenderer';
import {append as svgAppend, attr as svgAttr, create as svgCreate} from 'tiny-svg';
import {getCirclePath} from 'bpmn-js/lib/draw/BpmnRenderUtil';
import {is} from 'bpmn-js/lib/util/ModelUtil';

const HIGH_PRIORITY = 1500;
export const INTEGRATIONS_WIDTH = 36;
export const INTEGRATIONS_HEIGHT = 36;

class CustomRenderer extends BaseRenderer
{
	constructor(eventBus, bpmnRenderer) 
	{
		super(eventBus, HIGH_PRIORITY);
	
		this.bpmnRenderer = bpmnRenderer;
	}

	setIntegrations(integrations)
	{
		this.integrations = integrations;
	}
	
	canRender(element) 
	{
		if(!is(element, 'bpmn:StartEvent') || element.labelTarget)
		{
			return false;
		}
		
		return this.integrations && this.integrations.length > 0;
	}

	static _drawCircle(parentNode, width, height, strokeColor) 
	{
		const cx = width / 2;
		const cy = height / 2;

		const shape = svgCreate('circle');
		svgAttr(shape, {
			cx: cx,
			cy: cy,
			r: Math.round((width + height) / 4),
			stroke: strokeColor || '#000',
			strokeWidth: 2,
			fill: '#fff'
		});

		svgAppend(parentNode, shape);

		return shape;
	}

	static _prependTo(newNode, parentNode, siblingNode) 
	{
		parentNode.insertBefore(newNode, siblingNode || parentNode.firstChild);
	}

	drawShape(parentNode, element) 
	{
		const rect = CustomRenderer._drawCircle(
			parentNode, INTEGRATIONS_WIDTH, INTEGRATIONS_HEIGHT, '#52B415');

		return rect;
	}
	
	getShapePath(shape) 
	{
		return getCirclePath({
			...shape,
			width: INTEGRATIONS_WIDTH, 
			height: INTEGRATIONS_HEIGHT
		});
	}	
}

CustomRenderer.$inject = [ 'eventBus', 'bpmnRenderer' ];

export default {
	__init__: [ 'customRenderer' ],
	customRenderer: [ 'type', CustomRenderer ]
};