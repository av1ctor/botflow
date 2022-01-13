import Misc from '../../../../../../../libs/Misc';

export default class ElementHelper
{
	static buildEmpty()
	{
		return {
			type: 'empty',
			id: Misc.genId(),
		};
	}

	static buildBlockProps(options = {md: 3, align: 'center'}, elements = null)
	{
		return {
			widths: {
				xs: options.md,
				md: options.md
			},
			align: options.align,
			elements: elements?
				elements:
				[this.buildEmpty()]
		};
	}

	static buildBlock(options = {md: 3, align: 'center'}, elements = null)
	{
		return {
			type: 'block',
			id: Misc.genId(),
			...this.buildBlockProps(options, elements)
		};
	}

	static buildButtonProps()
	{
		return {
			icon: '',
			title: '',
			onClick: {action: '', target: ''},
		};
	}

	static buildButton()
	{
		return {
			type: 'button',
			id: Misc.genId(),
			...this.buildButtonProps()
		};
	}

	static buildMenuProps()
	{
		return {
			elements: [],			
		};
	}

	static buildMenu()
	{
		return {
			type: 'menu',
			id: Misc.genId(),
			...this.buildMenuProps()
		};
	}

	static buildElementProps(type)
	{
		switch(type)
		{
		case 'block':
			return this.buildBlockProps();
		case 'button':
			return this.buildButtonProps();
		case 'menu':
			return this.buildMenuProps();
		default:
			return {};
		}
	}

	static buildBlockOrEmptyElement(parent, md = 3)
	{
		return !parent || parent.type !== 'block'? 
			this.buildBlock(md):
			this.buildEmpty();
	}

	static addElement(node, index, proto, parent)
	{
		const recalc = (elements) =>
		{
			const widths = elements[index].widths;
			if(widths)
			{
				widths.md = Math.ceil(widths.md / 2);
				widths.xs = Math.ceil(widths.xs / 2);
			}

		};
		
		if(node === parent)
		{
			const elements = Misc.addToArray(node.elements, index+1, [proto]);
			recalc(elements);
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
				const widths = elements[index-1].widths;
				if(widths)
				{
					widths.md += elements[index].widths.md;
					widths.xs += elements[index].widths.xs;
				}
			}
			else if(elements.length > 1)
			{
				const widths = elements[1].widths;
				if(widths)
				{
					widths.md += elements[0].widths.md;
					widths.xs += elements[0].widths.xs;
				}
			}
		};
		
		if(node === parent)
		{
			recalc(node.elements);

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