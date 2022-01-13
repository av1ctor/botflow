import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Button, Badge} from 'reactstrap';
import CollapseUnmounted from '../Collapse/CollapseUnmounted';

import './ListSelectable.css';

export default class ExtensibleList extends PureComponent
{
	static propTypes = 
	{
		className: PropTypes.any,
		columns: PropTypes.array.isRequired,
		items: PropTypes.array.isRequired,
		alwaysMobile: PropTypes.bool,
		totalPreviewCols: PropTypes.number,
		useIndex: PropTypes.bool,
		startOpened: PropTypes.bool,
		onChange: PropTypes.func,
		renderItem: PropTypes.func.isRequired,
		renderHeader: PropTypes.func,
		renderActions: PropTypes.func,
		onAddItem: PropTypes.func,
		addItem: PropTypes.func,
		onDeleteItem: PropTypes.func,
		deleteItem: PropTypes.func,
		extraAddButtonText: PropTypes.string
	};

	static defaultProps = 
	{
		alwaysMobile: false,
		useIndex: false,
		startOpened: false,
		onAddItem: (items) => true,
		onDeleteItem: ({item, index, callback}) => callback({item, index}),
		totalPreviewCols: 2,
	}
	
	constructor(props)
	{
		super(props);

		this.state = {
			isMobile: props.alwaysMobile || this.isMobile(),
			expanded: {
				wkmap: new WeakMap(),
				map: new Map()
			}
		};
	}

	componentDidMount()
	{
		this.resizeListener = (event) => this.handleResize(event);
		window.addEventListener('resize', this.resizeListener);

		if(this.props.startOpened)
		{
			this.props.items.forEach((item, index) => 
				this.setExpanded(item, index, true)
			);
		}
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
			isMobile: this.props.alwaysMobile || this.isMobile()
		});
	}

	isMobile() 
	{
		return !window.matchMedia('(min-width: 1024px)').matches;
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
		return this.props.addItem && (
			<div className="rf-listbox-button-bar">
				<Button 
					disabled={!this.props.onAddItem(items)}
					size="xs" 
					outline 
					className="base-list-button" 
					color="primary" 
					title="Adicionar" 
					onClick={(e) => this.addItem(e, items)}>
					<i className="la la-plus-circle" /> 
					<span className="base-list-button-add">{' '}
						Adicionar {this.props.extraAddButtonText}
					</span>
				</Button>
			</div>
		);
	}

	renderMobile()
	{
		const {columns, items, renderHeader, totalPreviewCols} = this.props;

		const first = columns.findIndex(col => col.width > 0);
		const last = first + totalPreviewCols - 1;

		const getColWidth = (col) => col.width || Math.floor(9 / totalPreviewCols);

		return (
			<div className="rf-listbox-container">
				<div className="container-fluid rf-listbox-list">
					{renderHeader?
						<div className="row rf-listbox-header">
							<div className="col-auto rf-listbox-num no-border">
								#
							</div>
							{columns.filter((col, index) => index >= first && index <= last && col.width).map((col, index) => 
							{
								return (
									<div key={index} className={`col-${getColWidth(col)} ${index === 0? 'no-border': ''}`}>
										{renderHeader({col, index})}
									</div>
								);
								
							})}
							<div className="col-auto rf-listbox-buttons no-border">
							</div>
						</div>:
						null}
					
					{items.map((item, rowIndex) => 
					{
						const isExpanded = this.isExpanded(item, rowIndex);
						return (
							<React.Fragment key={rowIndex}>
								<div 
									className={`row rf-listbox-item-preview ${isExpanded? 'expanded': 'colapsed'}`}
									onClick={(e) => this.handleToggleItem(e, item, rowIndex)}>
									<div className="col-auto rf-listbox-num no-border">
										<Badge 
											color="secondary" 
											className="rf-listbox-num-badge">
											{1 + rowIndex}
										</Badge>
									</div>
									{columns.filter((col, index) => index >= first && index <= last && col.width).map((col, colIndex) => 
									{
										return (
											<div key={colIndex} className={`col-${getColWidth(col)} ${colIndex === 0? 'no-border': ''}`}>
												{this.props.renderItem({item, index: rowIndex, col, isPreview: true}, this.props.onChange)}
											</div>
										);
									})}
									<div className="col-auto rf-listbox-buttons no-border">
										{this.props.renderActions && this.props.renderActions({item, index: rowIndex})}
										{this.renderDelButton(item, rowIndex)}
										<Button 
											size="xs" 
											outline 
											className="rf-listbox-button" 
											title={isExpanded? 'Fechar': 'Expandir'}
											onClick={(e) => this.handleToggleItem(e, item, rowIndex)}>
											<i className={`rf-listbox-button-icon la la-caret-${isExpanded? 'up': 'down'}`}></i>
										</Button>
									</div>
								</div>
								<CollapseUnmounted isOpen={isExpanded}>
									<div className={`row rf-listbox-item ${isExpanded? 'expanded': 'colapsed'}`}>
										<div className="col-12 no-border">
											{columns.map((col, colIndex) => 
											{
												return (
													<div key={colIndex} className="row">
														<div className="col-12">
															<div className="pt-2">{col.label}</div>
															{this.props.renderItem({item, index: rowIndex, col, isPreview: false}, this.props.onChange)}
														</div>
													</div>
												);
											})}
										</div>
									</div>
								</CollapseUnmounted>
							</React.Fragment>
						);
					}
					)}
					<div className="row rf-listbox-item">
						<div className="col-12 no-border">
							{this.renderAddButton(items)}
						</div>
					</div>
				</div>
			</div>);
	}

	renderDelButton(item, index) 
	{
		return (
			this.props.deleteItem && 
			<Button 
				size="xs" 
				outline 
				className="rf-listbox-button" 
				title="Remover" 
				onClick={(e) => this.delItem(e, item, index)}>
				<i className="rf-listbox-button-icon la la-trash"/>
			</Button>);
	}

	renderDesktop()
	{
		const {columns, items, renderHeader} = this.props;

		return (
			<div className="rf-listbox-container">
				<div className="container-fluid rf-listbox-list">
					{renderHeader?
						<div className="row rf-listbox-header">
							<div className="col-auto rf-listbox-num no-border">
								#
							</div>
							{columns.filter(col => !!col.width).map((col, index) => 
							{
								return (
									<div 
										key={index} 
										className={`col-${col.width} ${index === 0? 'no-border': ''}`}>
										{renderHeader({col, index})}
									</div>
								);
								
							})}
							<div className="col-auto rf-listbox-buttons no-border">
							</div>
						</div>:
						null}
					{items.map((item, rowIndex) => 
					{
						return (
							<div key={rowIndex} className="row rf-listbox-item">
								<div className="col-auto rf-listbox-num no-border">
									<Badge 
										color="secondary" 
										className="rf-listbox-num-badge">
										{1 + rowIndex}
									</Badge>
								</div>
								{columns.filter(col => !!col.width).map((col, colIndex) => 
								{
									return (
										<div 
											key={colIndex} 
											className={`col-${col.width} ${colIndex === 0? 'no-border': ''}`}>
											{this.props.renderItem({item, index: rowIndex, col, colIndex, isPreview: false}, this.props.onChange)}
										</div>
									);
								})}
								<div className="col-auto rf-listbox-buttons no-border">
									{this.props.renderActions && this.props.renderActions({item, index: rowIndex})}
									{this.renderDelButton(item, rowIndex)}
								</div>
							</div>
						);
					}
					)}
					<div className="row rf-listbox-item">
						<div className="col-12 no-border">
							{this.renderAddButton(items)}
						</div>
					</div>
				</div>
			</div>);
	}

	render()
	{
		return (
			this.state.isMobile? 
				this.renderMobile(): 
				this.renderDesktop()
		);
	}
}