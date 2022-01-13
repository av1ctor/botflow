import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Button, Badge} from 'reactstrap';
import CollapseUnmounted from '../Collapse/CollapseUnmounted';

import './ListSelectable.css';

export default class ExtensibleListCollapsed extends PureComponent
{
	static propTypes = 
	{
		className: PropTypes.any,
		items: PropTypes.array.isRequired,
		useIndex: PropTypes.bool,
		startOpened: PropTypes.bool,
		onChange: PropTypes.func,
		renderPreview: PropTypes.func.isRequired,
		renderHeader: PropTypes.func,
		renderActions: PropTypes.func,
		renderEmpty: PropTypes.func,
		onAddItem: PropTypes.func,
		addItem: PropTypes.func.isRequired,
		onDeleteItem: PropTypes.func,
		deleteItem: PropTypes.func.isRequired,
		extraAddButtonText: PropTypes.string,
		children: PropTypes.func.isRequired,
	};

	static defaultProps = 
	{
		useIndex: false,
		startOpened: false,
		renderEmpty: () => null,
		onAddItem: () => true,
		onDeleteItem: ({item, index, callback}) => callback({item, index}),
	}
	
	constructor(props)
	{
		super(props);

		this.state = {
			expanded: {
				wkmap: new WeakMap(),
				map: new Map()
			}
		};
	}

	componentDidMount()
	{
		if(this.props.startOpened)
		{
			this.props.items.forEach((item, index) => 
				this.setExpanded(item, index, true)
			);
		}
	}

	isExpanded(item, index)
	{
		const {expanded} = this.state;

		if(!this.props.useIndex)
		{
			return !!expanded.wkmap.get(item);
		}

		return expanded.map.has(index)?
			expanded.map.get(index):
			false;
	}

	delExpanded(item, index)
	{
		if(!this.props.useIndex)
		{
			this.state.expanded.wkmap.delete(item);
		}

		this.state.expanded.map.delete(index);
	}

	setExpanded(item, index, value)
	{
		const expanded = Object.assign({}, this.state.expanded);
		if(this.props.useIndex)
		{
			expanded.map.set(index, value);
		}
		else
		{
			expanded.wkmap.set(item, value);
		}

		this.setState({
			expanded: expanded
		});
	}

	handleToggleItem(event, item, index)
	{
		event && event.stopPropagation();

		const is = this.isExpanded(item, index);
		
		this.setExpanded(item, index, !is);
	}

	addItem = (event, items) =>
	{
		event.stopPropagation();

		const item = this.props.addItem(items);
		
		this.setExpanded(item, items.length, true);
	}

	delItem = (event, item, index) =>
	{
		event.stopPropagation();

		this.props.onDeleteItem({
			item, 
			index, 
			callback: () => 
			{
				this.delExpanded(item, index);
				this.props.deleteItem(item, index);
			}
		});
	}

	renderAddButton(items)
	{
		return (
			<div className="rf-listbox-button-bar">
				<Button 
					disabled={!this.props.onAddItem(items)}
					size="xs" 
					outline 
					className="base-list-button" 
					color="primary"
					title="Add" 
					onClick={(e) => this.addItem(e, items)}>
					<i className="la la-plus-circle" /> 
					<span className="base-list-button-add">{' '}
						Add {this.props.extraAddButtonText}
					</span>
				</Button>
			</div>
		);
	}

	render()
	{
		const {items, renderHeader, renderPreview, children} = this.props;

		return (
			<div className="rf-listbox-container">
				<div className="container-fluid rf-listbox-list">
					{renderHeader?
						<div className="row rf-listbox-header">
							<div className="col-auto rf-listbox-num no-border">
								#
							</div>
							<div className="col-auto">
								{renderHeader()}
							</div>
							<div className="col-auto rf-listbox-buttons no-border">
							</div>
						</div>:
						null}

					{!items || items.length === 0?
						this.props.renderEmpty():
						items.map((item, index) => 
						{
							const isExpanded = this.isExpanded(item, index);
							return (
								<React.Fragment key={index}>
									<div 
										className={`row rf-listbox-item-preview ${isExpanded? 'expanded': 'colapsed'}`}
										onClick={(e) => this.handleToggleItem(e, item, index)}>
										<div className="col-auto rf-listbox-num no-border">
											<Badge 
												color="secondary" 
												className="rf-listbox-num-badge">
												{1 + index}
											</Badge>
										</div>
										<div className="col-auto">
											{renderPreview({item, index})}
										</div>
										<div className="col-auto rf-listbox-buttons no-border">
											{this.props.renderActions && this.props.renderActions({item, index: index})}
											<Button
												size="xs" 
												outline
												className="rf-listbox-button" 
												title="Remover" 
												onClick={(e) => this.delItem(e, item, index)}>
												<i className="rf-listbox-button-icon la la-trash"></i>
											</Button>
											<Button 
												size="xs" 
												outline 
												className="rf-listbox-button" 
												title={isExpanded? 'Fechar': 'Expandir'}
												onClick={(e) => this.handleToggleItem(e, item, index)}>
												<i className={`rf-listbox-button-icon la la-caret-${isExpanded? 'up': 'down'}`}></i>
											</Button>
										</div>
									</div>
									<CollapseUnmounted isOpen={isExpanded}>
										<div className={`row rf-listbox-item ${isExpanded? 'expanded': 'colapsed'}`}>
											<div className="col-12 no-border">
												<div className="row">
													<div className="col-12">
														{children({item, index}, this.props.onChange)}
													</div>
												</div>
											</div>
										</div>
									</CollapseUnmounted>
								</React.Fragment>
							);
						})
					}
					<div className="row rf-listbox-item">
						<div className="col-12 no-border">
							{this.renderAddButton(items)}
						</div>
					</div>
				</div>
			</div>);
	}
}