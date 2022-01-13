
export default class DomUtil 
{
	static validateForm(form)
	{
		const errors = [];

		for(var i = 0; i < form.elements.length; i++)
		{
			let err = null;
			
			const el = form.elements[i];
			el.setCustomValidity('');

			const name = el.getAttribute('field-name') || el.getAttribute('name') || el.getAttribute('id');
			if(name)
			{
				let type = el.getAttribute('field-type') || el.getAttribute('type');
				const value = el.value;
				if(type !== null && type !== undefined)
				{
					//var isArray = false;
					if(type.startsWith('array:'))
					{
						//isArray = true;
						type = type.substring(6);
					}
					
					switch(type.toLowerCase())
					{
					case 'datetime':
						break;
					case 'email':
						break;
					case 'short':
					case 'integer':
					case 'long':
						break;
					case 'number':
					case 'float':
					case 'double':
						break;
					case 'text':
					case 'textarea':
					case 'string':
					case 'password':
					case 'checkbox':
					case 'boolean':
						break;
					default:
						err = `Unknown type: ${type}`;
					}
				}

				var required = el.getAttribute('required');
				if(required !== null && required !== 'false')
				{
					if(value === null || value === undefined || value === '')
					{
						err = 'Obrigatory field';
					}
				}

				if(err)
				{
					el.setCustomValidity(err);
					errors.push(`${name || ''}: ${err}`);
				}
				else
				{
					el.setCustomValidity('');
				}

				el.reportValidity();
			}
		}		

		return errors;
	}

	static loadExternalScript(url, props = {})
	{
		const script = document.createElement('script');
		script.src = url;
		script.type = 'text/javascript';
		script.async = true;
		Object.entries(props)
			.forEach(([key, value]) => 
				script[key] = value);
		document.body.appendChild(script);		
	}

	static getZindex(elm)
	{
		while(elm && elm instanceof HTMLElement)
		{
			const zIndex = getComputedStyle(elm).getPropertyValue('z-index');
			if(!isNaN(zIndex))
			{
				return zIndex;
			}

			elm = elm.parentElement;
		}

		return 0;
	}

	static getWidth(elm)
	{
		while(elm && elm instanceof HTMLElement)
		{
			if(elm.clientWidth)
			{
				return elm.clientWidth;
			}

			elm = elm.parentElement;
		}

		return 0;
	}

	static getHeight(elm)
	{
		while(elm && elm instanceof HTMLElement)
		{
			if(elm.clientHeight)
			{
				return elm.clientHeight;
			}

			elm = elm.parentElement;
		}

		return 0;
	}

	static getDimension(element)
	{
		const width = this.getWidth(element);
		const height = this.getHeight(element);

		return {
			width, 
			height
		};
	}

	static getPosition(element)
	{
		const rect = element.getBoundingClientRect();
		return {
			left: rect.left + window.scrollX,
			top: rect.top + window.scrollY
		};
	}
}