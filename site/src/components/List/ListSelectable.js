import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';

import './ListSelectable.css';

export default class ListSelectable extends PureComponent
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
		const {items, renderHeader} = this.props;

		return (
			<div className="rf-listbox-container">
				<div className="container-fluid rf-listbox-list">
					{renderHeader?
						<div className="row rf-listbox-header">
							{renderHeader()}
						</div>:
						null}
					{items.map((item, index) => 
					{
						let elm = null;
						return (
							<div 
								key={index} 
								ref={el => elm = el}
								className={`row rf-listbox-item pointer ${item.selected? 'selected': ''}`} 
								onClick={(e) => this.toggleItem(index, e, elm)}>
								{this.props.renderItem(item, index)}
							</div>
						);
					}
					)}
				</div>
			</div>);
	}
}