import {PureComponent} from 'react';
import PropTypes from 'prop-types';
import 'inputmask/lib/extensions/inputmask.numeric.extensions';
import Inputmask from 'inputmask/lib/extensions/inputmask.date.extensions';
import dayjs from 'dayjs';
import Misc from '../../libs/Misc';

Inputmask.extendDefaults({
	'autoUnmask': true
});

export default class InputMask extends PureComponent
{
	static propTypes = 
	{
		children: PropTypes.any.isRequired,
		mask: PropTypes.string.isRequired
	};

	constructor(props)
	{
		super(props);

		this.compo = this.transformCompo(props.mask);
		this.inputRef = null;

	}

	componentDidMount()
	{
		this.doMasking();
	}

	componentDidUpdate(prevProps)
	{
		const {mask} = this.props;
		if(mask !== prevProps.mask)
		{
			this.compo = this.transformCompo(mask);
			this.doMasking();
		}
	}

	transformCompo(mask)
	{
		if(!mask)
		{
			return null;
		}
			
		switch(mask)
		{
		case 'array':
			return null;
		}

		return new Inputmask(InputMask.buildMaskOptions(mask));
	}

	doMasking()
	{
		if(this.compo && this.inputRef) 
		{
			this.compo.mask(this.inputRef);
		}
	}

	static buildMaskOptions(mask)
	{
		let options = {};

		options.groupSeparator = '.';
		options.radixPoint = ',';

		if(mask.indexOf('money:') === 0 || mask === 'money')
		{
			options.alias = 'currency';
			const format = mask.length > 6? 
				mask.substring(6): 
				'';
			switch(format)
			{
			case 'USD':
				options.prefix = '$ ';
				break;
			case 'BRL':
				options.prefix = 'R$ ';
				break;
			default:
				options.prefix = format;
				break;
			}
		}
		else if(mask.indexOf('date:') === 0 || mask === 'date')
		{
			options.alias = 'datetime';
			const format = mask.length > 5? 
				mask.substring(5): 
				'';
			switch(format)
			{
			case 'notime':
				options.inputFormat = 'dd/mm/yyyy';
				break;
			case 'nosecs':
				options.inputFormat = 'dd/mm/yyyy HH:MM';
				break;
			default:
				options.prefix = format || 'dd/mm/yyyy HH:MM:ss';
				break;
			}
		}		
		else
		{
			switch(mask)
			{
			case 'string':
			case 'bool':
			case 'number':
			case 'decimal':
			case 'enumeration':
			case 'rating':
				break;
			default:
				options.mask = mask.replace(/#/g, '9');
			}
		}

		return options;
	}

	static monthNames = [
		'Janeiro', 'Fevereiro', 'Março', 
		'Abril', 'Maio', 'Junho',
		'Julho', 'Agosto', 'Setembro', 
		'Outubro', 'Novembro', 'Dezembro'
	];

	static format(value, mask)
	{
		if(!value)
		{
			return null;
		}

		switch(mask)
		{
		case 'array':
			return value.join(', ');
		case 'date':
			return dayjs(value).format('DD/MM/YYYY HH:mm:ss');
		case 'month':
			return this.monthNames[value];
		case 'second':
		case 'minute':
		case 'hour':
		case 'day':
		case 'year':
		case 'text':
			return value;
		case 'number':
			return Misc.round(value);
		case 'bool':
			return value? true: false;
		}
		
		return Inputmask.format(value.constructor !== String? value.toString(): value, this.buildMaskOptions(mask));
	}	

	setInputRef = (ref) =>
	{
		this.inputRef = ref;
	}

	render()
	{
		return this.props.children({setInputRef: this.setInputRef});
	}
}