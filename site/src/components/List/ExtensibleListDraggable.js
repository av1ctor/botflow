import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Button, Badge} from 'reactstrap';
import {sortableContainer, sortableElement, sortableHandle} from 'react-sortable-hoc';
import CollapseUnmounted from '../CollapseUnmounted';
import Misc from '../../libs/Misc';

import './ListSelectable.css';

const DragHandle = sortableHandle(({index_}) => 
	<Badge color="primary" className="mr-1" style={{alignSelf: 'center', cursor: 'grab'}}>
		{index_}
	</Badge>);

const SortableContainer = sortableContainer(({children}) => 
{
	return (
		<div className="rf-listbox-container">
			<div className="container-fluid rf-listbox-list">
				{children}
			</div>
		</div>);
});

export default class ExtensibleListDraggable extends PureComponent
{
	static propTypes = 
	{
		className: PropTypes.any,
		columns: PropTypes.array.isRequired,
		items: PropTypes.array.isRequired,
		onChange: PropTypes.func.isRequired,
		onSort: PropTypes.func.isRequired,
		renderItem: PropTypes.func.isRequired,
		renderHeader: PropTypes.func,
		renderActions: PropTypes.func,
		canAdd: PropTypes.func,
		addItem: PropTypes.func.isRequired,
		delItem: PropTypes.func.isRequired,
	};
	
	constructor(props)
	{
		super(props);

		this.state = {
			isMobile: this.isMobile(),
			expanded: {
				wkmap: new WeakMap()
			}
		};
	}

	handleSort = ({oldIndex, newIndex}) => 
	{
		this.props.onSort(Misc.moveArray(this.props.items, oldIndex, newIndex));
	};

	componentDidMount()
	{
		this.resizeListener = (event) => this.handleResize(event);
		window.addEventListener('resize', this.resizeListener);
	}

	componentWillUnmount()
	{
		if(this.resizeListener)
		{
			window.removeEventListener('resize', this.resizeListener);
			this.resizeListener = null;
		}
	}

	handleResize = (event) =>
	{
		this.setState({
			isMobile: this.isMobile()
		});
	}

	isMobile() 
	{
		return !window.matchMedia('(min-width: 1024px)').matches;
	}

	isExpanded(item)
	{
		return !!this.state.expanded.wkmap.get(item);
	}

	handleToggleItem(event, item)
	{
		event && event.stopPropagation();

		const expanded = Object.assign({}, this.state.expanded);
		expanded.wkmap.set(item, expanded.wkmap.get(item)? false: true);

		this.setState({
			expanded: expanded
		});
	}

	addItem(items)
	{
		const item = this.props.addItem(items);
		this.handleToggleItem(null, item);
	}

	renderAddButton(items)
	{
		return (
			<Button 
				disabled={this.props.canAdd? !this.props.canAdd(items): false}
				size="xs" 
				outline 
				className="base-list-button" 
				block
				color="secondary" 
				title="Add" 
				onClick={() => this.addItem(items)}>
				<i className="la la-plus-circle" /> <span className="base-list-button-add">Add</span>
			</Button>
		);
	}

	getColWidth = (index) => index === 0? '5': '4';
	
	SortableItemMobile = sortableElement(({item, index_}) => 
	{
		const {columns} = this.props;

		const isExpanded = this.isExpanded(item);
		return (
			<React.Fragment key={index_}>
				<div 
					className={`row rf-listbox-item-preview ${isExpanded? 'expanded': 'colapsed'}`}
					onClick={(e) => this.handleToggleItem(e, item)}>
					<div className="col-auto rf-listbox-num no-border">
						<DragHandle index_={index_} />
					</div>
					{columns.filter((col, index) => index <= 1).map((col, colIndex) => 
					{
						return (
							<div key={colIndex} className={`col-${this.getColWidth(colIndex)} ${colIndex === 0? 'no-border': ''}`}>
								{this.props.renderItem({item, index: index_, col, isPreview: true}, this.props.onChange)}
							</div>
						);
					})}
					<div className="col-auto rf-listbox-buttons no-border">
						{this.props.renderActions && this.props.renderActions({item, index: index_})}
						<Button
							size="xs" outline
							className="rf-listbox-button" 
							onClick={() => this.props.delItem(item, index_)}>
							<i className="rf-listbox-button-icon la la-trash"></i>
						</Button>
						<Button 
							size="xs" outline 
							className="rf-listbox-button" 
							onClick={(e) => this.handleToggleItem(e, item)}>
							<i className={`rf-listbox-button-icon la la-caret-${isExpanded? 'up': 'down'}`}></i>
						</Button>
					</div>
				</div>
				<CollapseUnmounted isOpen={isExpanded}>
					<div className={`row rf-listbox-item ${isExpanded? 'expanded': 'colapsed'}`}>
						<div className="col-12">
							{columns.map((col, colIndex) => 
							{
								return (
									<div key={colIndex} className="row">
										<div className="col-12">
											<div className="pt-2">{col.label}</div>
											{this.props.renderItem({item, index: index_, col, isPreview: false}, this.props.onChange)}
										</div>
									</div>
								);
							})}
						</div>
					</div>
				</CollapseUnmounted>
			</React.Fragment>
		);
	});
	
	renderMobile()
	{
		const {columns, items, className, renderHeader} = this.props;
		const Item = this.SortableItemMobile;

		return (
			<SortableContainer onSortEnd={this.handleSort} useDragHandle helperClass={className}>
				{renderHeader?
					<div className="row rf-listbox-header">
						<div className="col-auto rf-listbox-num no-border">
						</div>
						{columns.filter((col, index) => index <= 1).map((col, index) => 
						{
							return (
								<div key={index} className={`col-${this.getColWidth(index)} ${index === 0? 'no-border': ''}`}>
									{renderHeader({col, index})}
								</div>
							);
							
						})}
						<div className="col-auto rf-listbox-buttons no-border">
						</div>
					</div>:
					null}
				
				{items.map((item, index) => 
					<Item 
						key={`item-${index}`} 
						item={item} 
						index={index} 
						index_={index} />)}

				<div className="row rf-listbox-item">
					<div className="col-12">
						{this.renderAddButton(items)}
					</div>
				</div>
			</SortableContainer>);
	}

	SortableItemDesktop = sortableElement(({item, index_}) => 
	{
		const {columns} = this.props;

		return (
			<div key={index_} className="row rf-listbox-item">
				<div className="col-auto rf-listbox-num no-border">
					<DragHandle index={index_} />
				</div>
				{columns.map((col, colIndex) => 
				{
					return (
						<div key={colIndex} className={`col-${col.width} ${colIndex === 0? 'no-border': ''}`}>
							{this.props.renderItem(
								{
									item, 
									index: index_, 
									col, 
									colIndex, 
									isPreview: false
								}, 
								this.props.onChange)}
						</div>
					);
				})}
				<div className="col-auto rf-listbox-buttons no-border">
					{this.props.renderActions && this.props.renderActions({item, index: index_})}
					<Button
						size="xs" outline
						className="rf-listbox-button" 
						onClick={() => this.props.delItem(item, index_)}>
						<i className="rf-listbox-button-icon la la-trash"></i>
					</Button>
				</div>
			</div>
		);
	});

	renderDesktop()
	{
		const {columns, items, className, renderHeader} = this.props;
		const Item = this.SortableItemDesktop;

		return (
			<SortableContainer onSortEnd={this.handleSort} useDragHandle helperClass={className}>
				{renderHeader?
					<div className="row rf-listbox-header">
						<div className="col-auto rf-listbox-num no-border">
						</div>
						{columns.map((col, index) => 
						{
							return (
								<div key={index} className={`col-${col.width} ${index === 0? 'no-border': ''}`}>
									{renderHeader({col, index})}
								</div>
							);
							
						})}
						<div className="col-auto rf-listbox-buttons no-border">
						</div>
					</div>:
					null}

				{items.map((item, index) => 
					<Item 
						key={`item-${index}`} 
						item={item} 
						index={index} 
						index_={index} />)}

				<div className="row rf-listbox-item">
					<div className="col-12">
						{this.renderAddButton(items)}
					</div>
				</div>
			</SortableContainer>);
	}

	render()
	{
		return (this.state.isMobile? this.renderMobile(): this.renderDesktop());
	}
}