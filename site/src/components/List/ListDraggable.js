import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {sortableContainer, sortableElement, sortableHandle} from 'react-sortable-hoc';
import Misc from '../../libs/Misc';

import './ListSelectable.css';

const DragHandle = sortableHandle(() => 
	<div className="drag-handle"/>);

const SortableContainer = sortableContainer(({children}) => 
{
	return (
		<div className="rf-listbox-container">
			<div className="container-fluid rf-listbox-list">
				{children}
			</div>
		</div>);
});

export default class ListDraggable extends PureComponent
{
	static propTypes = 
	{
		className: PropTypes.any,
		items: PropTypes.array.isRequired,
		onChange: PropTypes.func.isRequired,
		onClick: PropTypes.func,
		renderItem: PropTypes.func.isRequired,
		renderHeader: PropTypes.func
	};
	
	constructor(props)
	{
		super(props);
	}

	SortableItem = sortableElement(({item, index_}) => 
	{
		let elm = null;
		
		return (
			<div
				key={index_} 
				ref={el => elm = el}
				className={`row rf-listbox-item pointer ${item.selected? 'selected': ''}`} 
				onClick={(e) => this.props.onClick? this.props.onClick(index_, e, elm): null}>
				<div className="flex-grow-0 pl-3"><DragHandle /></div>
				{this.props.renderItem(item, index_, this.props.onChange)}
			</div>);
	});

	handleSort = ({oldIndex, newIndex}) => 
	{
		this.props.onChange(Misc.moveArray(this.props.items, oldIndex, newIndex));
	};

	render()
	{
		const {items, className, renderHeader} = this.props;
		const Item = this.SortableItem;

		return (
			<SortableContainer 
				onSortEnd={this.handleSort} 
				useDragHandle 
				helperClass={className}>
				{renderHeader?
					<div className="row rf-listbox-header">
						<div className="flex-grow-0 pl-3" style={{marginRight: '16px'}}></div>
						{renderHeader()}
					</div>:
					null}
				{items.map((item, index) => (
					<Item 
						key={`item-${index}`} 
						index={index}
						index_={index} 
						item={item} />
				))}
			</SortableContainer>);		
	}
}