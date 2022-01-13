import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import ListDraggable from './ListDraggable';

export default class ListSelectableDraggable extends PureComponent
{
	static propTypes = 
	{
		className: PropTypes.any,
		items: PropTypes.array.isRequired,
		onChange: PropTypes.func.isRequired,
		renderItem: PropTypes.func.isRequired,
		renderHeader: PropTypes.func,
		multiple: PropTypes.bool
	};
	
	static defaultProps = {
		multiple: true
	};

	constructor(props)
	{
		super(props);

		this.toggleItem = this.toggleItem.bind(this);
	}

	toggleItem(index, event, elm)
	{
		switch(event.target.tagName)
		{
		case 'LABEL':
		case 'INPUT':
		case 'SELECT':
			return;
		default:
			{
				let target = event.target;
				if(!elm.contains(target))
				{
					return;
				}
				
				while(target !== elm)
				{
					if(target.classList && target.classList.contains('ignore'))
					{
						return;
					}
					target = target.parentNode;
				}
			}
			break;
		}
			
		const {items, multiple} = this.props;

		this.props.onChange(items.map((item, j) => 
			j === index? 
				{...item, selected: !item.selected}: 
				(multiple? item: {...item, selected: false})
		));
	}

	render()
	{
		return (
			<ListDraggable {...this.props} onClick={this.toggleItem} />
		);
	}
}