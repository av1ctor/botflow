import Misc from '../../../../../../../libs/Misc';

export default class ElementHelper
{
	static buildEmpty(md = 6, type = 'empty')
	{
		return {
			type: type,
			id: Misc.genId(),
			widths: {
				xs: 12,
				md: md
			},
			height: {value: 4, unit: 'rem'},
		};
	}

	static buildBlockProps(md)
	{
		return {
			elements: [this.buildEmpty(md)],
		};
	}
	
	static buildBlock(md = 12)
	{
		return {
			type: 'block',
			id: Misc.genId(),
			...this.buildBlockProps(md)
		};
	}

	static buildTextProps()
	{
		return {
			text: '',
			justify: 'center',
			align: 'center',
		};
	}

	static buildText(md = 6)
	{
		return {
			...this.buildEmpty(md, 'text'),
			...this.buildTextProps(md)
		};
	}

	static buildFormProps()
	{
		return {
			collection: '',
			form: '',
			height: {value: 30, unit: 'rem'},
		};
	}
	
	static buildForm()
	{
		return {
			...this.buildEmpty(12, 'form'),
			...this.buildFormProps()
		};
	}

	static buildElementProps(type)
	{
		switch(type)
		{
		case 'block':
			return this.buildBlockProps();
		case 'text':
			return this.buildTextProps();
		case 'form':
			return this.buildFormProps();
		default:
			return {};
		}
	}

	static buildBlockOrEmptyElement(parent, md = 12)
	{
		return !parent || parent.type !== 'block'? 
			this.buildBlock(md):
			this.buildEmpty(md);
	}

	static addElement(node, index, proto, parent)
	{
		const recalc = (elements) =>
		{
			if(proto.type !== 'block')
			{
				elements[index].widths.md = Math.ceil(elements[index].widths.md / 2);
			}
		};
		
		if(node === parent)
		{
			const elements =  Misc.addToArray(node.elements, index+1, [proto]);
			return elements;
		}

		for(let i = 0; i < node.elements.length; i++)
		{
			const elm = node.elements[i];
			if(elm === parent)
			{
				elm.elements = Misc.addToArray(elm.elements, index+1, [proto]);
				recalc(elm.elements);
				break;
			}
			else
			{
				if(elm.elements)
				{
					elm.elements = this.addElement(elm, index, proto, node);
				}
			}
		}

		return Array.from(node.elements);
	}

	static updateElements(main, nodes, values)
	{
		for(let i = 0; i < main.elements.length; i++)
		{
			const elm = main.elements[i];
			const index = nodes.indexOf(elm);
			if(index >= 0)
			{
				main.elements[i] = values[index];
			}
			else
			{
				if(elm.elements)
				{
					elm.elements = this.updateElements(elm, nodes, values);
				}
			}
		}

		return Array.from(main.elements);
	}	

	static delElement(node, index, parent)
	{
		const recalc = (elements) =>
		{
			if(index > 0)
			{
				elements[index-1].widths.md += elements[index].widths.md;
			}
			else if(elements.length > 1)
			{
				elements[1].widths.md += elements[0].widths.md;
			}

		};
		
		if(node === parent)
		{
			const elements = Misc.deleteFromArray(node.elements, index, 1);

			return elements.length === 0?
				[ElementHelper.buildBlockOrEmptyElement(parent, 12)]:
				elements;
		}

		for(let i = 0; i < node.elements.length; i++)
		{
			const elm = node.elements[i];
			if(elm === parent)
			{
				recalc(elm.elements);
				
				elm.elements = Misc.deleteFromArray(elm.elements, index, 1);
				if(elm.elements.length === 0)
				{
					node.elements = Misc.deleteFromArray(node.elements, i, 1);
					if(node.elements.length === 0)
					{
						node.elements = [ElementHelper.buildBlockOrEmptyElement(node, 12)];
					}
				}

				break;
			}
			else
			{
				if(elm.elements)
				{
					elm.elements = this.delElement(elm, index, node);
				}
			}
		}

		return Array.from(node.elements);
	}

	static moveElement(node, oldIndex, newIndex, parent)
	{
		if(node === parent)
		{
			return Misc.moveArray(node.elements, oldIndex, newIndex);
		}

		for(let i = 0; i < node.elements.length; i++)
		{
			const element = node.elements[i];
			if(element === parent)
			{
				element.elements = Misc.moveArray(element.elements, oldIndex, newIndex);
				break;
			}
			else
			{
				if(element.elements)
				{
					element.elements = this.moveElement(element, oldIndex, newIndex, node);
				}
			}
		}

		return Array.from(node.elements);
	}

	static findElementOnTreeById(elements, id)
	{
		const res = elements.find(e => e.id === id);
		if(res)
		{
			return res;
		}

		for(let i = 0; i < elements.length; i++)
		{
			const elm = elements[i];
			if(elm.elements)
			{
				const res = this.findElementOnTreeById(elm.elements, id);
				if(res)
				{
					return res;
				}
			}
		}

		return null;
	}

	static findElementById(id, elements)
	{
		if(id === null)
		{
			return null;
		}
		
		return this.findElementOnTreeById(elements, id);
	}
}