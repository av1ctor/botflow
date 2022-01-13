import BpmnElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory';
import {DEFAULT_LABEL_SIZE} from 'bpmn-js/lib/util/LabelUtil';

class CustomElementFactory extends BpmnElementFactory
{
	constructor(bpmnFactory, moddle) 
	{
		super(bpmnFactory, moddle);
	
		this.moddle = moddle;
		this.bpmnFactory = bpmnFactory;
	}

	create(elementType, attrs) 
	{
		const type = attrs.type;
	
		if (elementType === 'label') 
		{
			return this.baseCreate(elementType, Object.assign({type: 'label'}, DEFAULT_LABEL_SIZE, attrs));
		}
	
		if (!/^rf:/.test(type)) 
		{
			return this.createBpmnElement(elementType, attrs);
		}
	
		if (!attrs.businessObject) 
		{
			attrs.businessObject = {
				type: type
			};

			if (attrs.id) 
			{
				Object.assign(attrs.businessObject, {id: attrs.id});
			}
		}

		// add width and height if shape
		Object.assign(attrs, this._getCustomElementSize(type));
		
		// we mimic the ModdleElement API to allow interoperability with
		// other components, i.e. the Modeler and Properties Panel
	
		if (!('$model' in attrs.businessObject)) 
		{
			Object.defineProperty(attrs.businessObject, '$model', {value: this.moddle});
		}
	
		if (!('$instanceOf' in attrs.businessObject)) 
		{
			// ensures we can use ModelUtil#is for type checks
			Object.defineProperty(attrs.businessObject, '$instanceOf', {
				value: function(type) {
					return this.type === type;
				}
			});
		}

		if (!('get' in attrs.businessObject)) 
		{
			Object.defineProperty(attrs.businessObject, 'get', {
				value: function(key) {
					return this[key];
				}
			});
		}

		if (!('set' in attrs.businessObject)) 
		{
			Object.defineProperty(attrs.businessObject, 'set', {
				value: function(key, value) {
					return this[key] = value;
				}
			});
		}

		// END minic ModdleElement API

		return this.baseCreate(elementType, attrs);
	}

	_getCustomElementSize(type) 
	{
		const shapes = {
			__default: { width: 50, height: 120 },
			'rf:integrationEvent': { width: 50, height: 120 },
		};
	
		return shapes[type] || shapes.__default;
	}
}

CustomElementFactory.$inject = [
	'bpmnFactory',
	'moddle'
];

export default {
	__init__: [ 'customElementFactory' ],
	customElementFactory: [ 'type', CustomElementFactory ]
};