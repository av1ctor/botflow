import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {DataScroller} from 'primereact/datascroller';
import {Button, Row, Col} from 'reactstrap';
import InputMask from './Input/InputMask';
import BaseListFilterForm from './BaseListFilterForm';
import ConfirmDialog from './Dialog/ConfirmDialog';
import CollapseUnmounted from './Collapse/CollapseUnmounted';
import Drawer from './Slider/Drawer';
import DomUtil from '../libs/DomUtil';

import './BaseList.css';

export default class BaseList extends Component
{
	static propTypes = 
	{
		api: PropTypes.object,
		history: PropTypes.object,
		container: PropTypes.object,
		controller: PropTypes.oneOfType([PropTypes.string, PropTypes.func]),
		updateHistory: PropTypes.bool,
		title: PropTypes.string,
		addButtonText: PropTypes.string,
		itemTemplate: PropTypes.object,
		details: PropTypes.any,
		sortBy: PropTypes.oneOfType([PropTypes.string, PropTypes.func]),
		ascending: PropTypes.bool,
		inline: PropTypes.bool,
		mode: PropTypes.string,
		height: PropTypes.number,
		getFilters: PropTypes.func, 
		transformFilters: PropTypes.func, 
		getHeader: PropTypes.func,
		getItemId: PropTypes.func,
		onLoad: PropTypes.func,
		renderPanels: PropTypes.func,
		renderItem: PropTypes.func,
		renderActions: PropTypes.func,
	};

	static defaultProps =
	{
		updateHistory: true,
		ascending: true,
		mode: 'COLLAPSED',
		addButtonText: 'Add',
		getFilters: () => [],
		onLoad: (item) => item,
		transformFilters: (filters) => filters,
	};

	static ITEMS_PER_PAGE = 30;
	
	constructor(props)
	{
		super(props);

		this.state = {
			list: [],
			dataLoaded: false,
			filters: null,
			sortBy: this.props.sortBy,
			ascending: this.props.ascending,
			expandedItens: new Map(),
			create: {
				expanded: null,
				fields: this.props.itemTemplate
			},
			confirm: {
				visible: false,
				text: null,
			},
			dimensions: null,
			slide: null	
		};

		this.currentPage = 0;
		this.stopLoading = false;
		this.loadingItems = false;

		this.isMobile = !window.matchMedia('(min-width: 1024px)').matches;
	}

	componentDidMount()
	{
		this.loadFiltersAndChangePage();

		const dimension = DomUtil.getDimension(this.grid);

		this.setState({
			dimensions: {
				width: dimension.width,
				height: dimension.height
			},
			slide: {
				visible: false,
			}
		});
	}

	componentDidUpdate(prevProps)
	{
		let reloadCnt = 0;
		if(this.props.updateHistory)
		{
			if(this.props.history.location.search !== this.search)
			{
				if(!this.changingPage)
				{
					++reloadCnt;
				}
			}
		}

		if(prevProps.controller !== this.props.controller)
		{
			if(this.props.controller.constructor === String)
			{
				++reloadCnt;
			}
		}

		if(reloadCnt > 0)
		{
			this.reload();
		}
	}

	getDimensions = () =>
	{
		return this.state.dimensions;
	}

	onResize = (event) =>
	{
		const dimension = DomUtil.getDimension(this.grid);

		this.setState({
			dimensions: {
				width: dimension.width,
				height: dimension.height
			}
		});		
	}

	showMessage = (msg, severity = 'info', life = 5000) =>
	{
		this.props.container.showMessage && this.props.container.showMessage(msg, severity, life);
	}

	showDetails(...args)
	{
		this.details.show(...args);
	}
	
	showUserConfirmation(callback, data, detail = 'Você tem certeza que deseja continuar?')
	{
		this.userConfirmCtx = {callback, data};
		this.setState({
			confirm: {
				text: detail,
				visible: true
			}
		});
	}

	handleUserConfirmed = async () =>
	{
		await this.userConfirmCtx.callback(this.userConfirmCtx.data);

		this.setState({
			confirm: {
				visible: false
			}
		});
	}

	getSearchValues = () =>
	{
		const values = [];

		if(this.props.updateHistory)
		{
			const matches = this.props.history.location.search? 
				decodeURI(this.props.history.location.search).match(/([a-zA-Z_\-0-9]+:[^,]+)/g): 
				null;
			if(matches)
			{
				for(let i = 0; i < matches.length; i++)
				{
					const [key, value] = matches[i].split(':');
					if(key !== 'page')
					{
						values[key] = value;
					}
				}
			}
		}

		return values;
	}

	getCurrentPage()
	{
		const match = this.props.updateHistory && this.props.history.location.search? 
			this.props.history.location.search.match(/page=([0-9]+)/): 
			null;

		return match? 
			match[1]: 
			0;
	}

	getFilterValue(filter, addQuotes = true)
	{
		switch(filter.type)
		{
		case 'number':
		case 'year':
		case 'month':
		case 'day':
			return filter.value === null? 
				0: 
				(filter.value.constructor === String? 
					parseFloat(filter.value): 
					filter.value);
		default:
			return addQuotes?
				`"${filter.value}"`:
				filter.value;
		}
	}

	async _requestItems(page, size, sortBy, ascending, filters)
	{
		const {controller} = this.props;

		if(controller.constructor === String)
		{
			const hasParams = controller.indexOf('?') >= 0;
			
			const filtersStr = filters
				.filter(f => f.value && f.exclude !== true)
				.map(f => `"${f.name}":${this.getFilterValue(f, true)}`).join(',');

			return await this.props.api.get(
				`${controller}${hasParams? '&': '?'}page=${page}&size=${size}${sortBy?'&sort='+sortBy+','+(ascending?'asc':'desc'):''}${filters? '&filters=' + filtersStr: ''}`);
		}
		else
		{
			return await controller('GET', {
				page,
				size,
				sortBy,
				ascending,
				filters: filters
					.filter(f => f.value && f.exclude !== true)
					.map(f => ({name: f.name, value: this.getFilterValue(f, false)}))
			});
		}
	}

	async _loadItems(page, size, options = {})
	{
		const filters = Array.from(options.filters || this.state.filters || []);
		const sortBy = options.sortBy || 
			(this.state.sortBy? (
				this.state.sortBy.constructor === String? 
					this.state.sortBy: 
					this.state.sortBy()): 
				null);
		const ascending = options.ascending !== undefined? 
			options.ascending: 
			this.state.ascending;

		const res = await this._requestItems(
			page, size, sortBy, ascending, this.props.transformFilters(filters));
		
		return res.errors === null? 
			res.data: 
			[];
	}

	loadPage = async (page = 0, options = {}) =>
	{
		if(!this.props.controller)
		{
			return 0;
		}
		
		try
		{
			this.props.container.showSpinner && this.props.container.showSpinner();

			if(page === 0)
			{
				this.currentPage = 0;
				this.stopLoading = false;
			}

			const res = await this.props.onLoad(
				await this._loadItems(
					page, BaseList.ITEMS_PER_PAGE, options));
			
			const items = res instanceof Promise?
				await res:
				res;

			const list = page > 0? 
				this.state.list.concat(items): 
				['header'].concat(items);
				
			this.setState({
				list: list,
				dataLoaded: true,
				create: Object.assign({}, this.state.create, {
					expanded: this.state.create.expanded !== null? 
						this.state.create.expanded:
						list.length === 1
				})
			});

			if(this.props.updateHistory)
			{
				let query = items.length > 0 && page > 0? `?page=${page}`: '';
				const filters = options.filters || this.state.filters;
				query += filters && filters.find(f => f.value)
					? (query.length === 0? '?': '&') + 'filters=' + filters.filter(f => f.value).map(f => `${f.name}:${f.value}`).join(',')
					: '';

				if(this.props.history.location.search !== query)
				{
					this.props.history.push(this.props.history.location.pathname + query);
				}
			}
			
			return items.length;
		}
		finally
		{
			this.props.container.hideSpinner && this.props.container.hideSpinner();
		}
	}

	async loadMoreItems(page)
	{
		if(this.stopLoading || page === 0)
		{
			return;
		}

		if(this.loadingItems)
			return;

		this.loadingItems = true;
		++this.currentPage;

		try
		{
			const itemCount = await this.loadPage(this.currentPage);

			this.stopLoading = itemCount === 0;
		}
		finally
		{
			this.loadingItems = false;
		}
	}

	reload()
	{
		this.loadFiltersAndChangePage();
	}

	async loadFiltersAndChangePage() 
	{
		this.changingPage = true;

		try
		{
			const values = this.getSearchValues();
			
			const filters = this.props.getFilters().map((filter) => {
				return {
					...filter,
					value: values[filter.name] || filter.value || filter.default
				};
			});
			
			this.setState({
				filters: filters
			});
			
			this.loadPage(this.getCurrentPage(), {filters});
	
			this.search = this.props.history && this.props.history.location.search;
		}
		finally
		{
			this.changingPage = false;
		}
	}

	getColValues(column)
	{
		return this.state.list? 
			this.state.list.filter(i => i !== 'header').map(i => i[column]): 
			[];
	}

	handleLazyLoad = async (event) =>
	{
		await this.loadMoreItems(event.first);
	}

	handleSortBy = (sortBy) =>
	{
		const ascending = !this.state.ascending;
		
		this.setState({
			sortBy: sortBy,
			ascending: ascending
		});

		this.loadPage(0, {sortBy, ascending});
	}

	findItem(item)
	{
		return this.findItemById(this.getItemId(item));
	}

	findItemById(id)
	{
		const list = this.state.list;
		for(let i = 0; i < list.length; i++)
		{
			if(id === this.getItemId(list[i]))
			{
				return list[i];
			}
		}

		return null;
	}

	updateItem = (item, fields) =>
	{
		const id = this.getItemId(item);
		
		if(!id)
		{
			fields = Object.assign({}, this.state.create.fields, fields); 
			this.setState({
				create: Object.assign({}, this.state.create, {fields: fields})
			});
			return fields;
		}
		
		const list = this.state.list;
		const itemOnList = this.findItemById(id);
		if(itemOnList)
		{
			Object.assign(itemOnList, fields);
		}

		this.setState({
			list: Array.from(list)
		});

		return itemOnList;
	}

	deleteItem = (item) =>
	{
		const id = this.getItemId(item);

		const list = this.state.list.filter(i => this.getItemId(i) !== id);

		this.setState({
			list: list
		});
	}

	updateFilters = async (filters) =>
	{
		this.setState({
			filters: Array.from(filters)
		});

		await this.loadPage(0, {filters});
	}

	toggleCreateForm = () =>
	{
		const expanded = this.state.create.expanded? false: true;

		this.setState({
			create: Object.assign({}, this.state.create, {expanded: expanded}),
		});
	}

	showCreateForm = (fields) =>
	{
		this.setState({
			create: {
				expanded: true,
				fields: fields
			}
		});
	}

	closeCreateForm = (fields) =>
	{
		this.setState({
			create: {
				expanded: false,
				fields: this.props.itemTemplate
			}
		});
	}

	renderHeader()
	{
		const renderHeaderCol = ({size, name, title, klass}, index) =>
		{
			const selected = (this.state.sortBy? (this.state.sortBy.constructor === String? this.state.sortBy: this.state.sortBy()): '') === name;
			
			return (
				<Col key={index} {...size} onClick={() => name? this.handleSortBy(name): null} 
					className={`base-list-header-col ${selected? 'base-list-header-col-selected': ''} ${klass}`}>
					{title}
					{selected? 
						<i className={`la la-caret-${this.state.ascending? 'up': 'down'}`} />:
						null
					}
				</Col>
			);
		};

		const expanded = this.state.create.expanded;

		return (
			<>
				{this.props.itemTemplate &&
					<div className={`base-list-item add ${expanded? 'expanded': ''}`}>
						<div className={`d-flex base-list-row base-list-row-add ${expanded? 'expanded': ''}`}>
							<Button 
								size="xs" 
								outline 
								className="base-list-button" 
								color="none" 
								type="submit" 
								title={this.props.addButtonText} 
								onClick={this.toggleCreateForm}>
								<span className="base-list-button-add-title">{this.props.addButtonText}</span> <i className="base-list-button-add-icon la la-plus-square" />
							</Button>
						</div>
						<CollapseUnmounted isOpen={expanded} className="base-list-row-form">
							<div className="d-flex">
								{this.props.renderItem(this.state.create.fields, false)}
							</div>
						</CollapseUnmounted>
					</div>
				}
				<div className="d-flex base-list-row base-list-row-header">
					<Col xs="11" className="base-list-row-col-others">
						<Row className="base-list-header">
							{this.props.getHeader().map((h,i) => renderHeaderCol(h, i))}
						</Row>
					</Col>
				</div>
				{this.state.list.length === 1 &&
					<div className="base-list-item">
						<div className="d-flex base-list-row stripped">
							<Col xs="12" className="base-list-row-col-others text-center">
								Nenhum item encontrado :/
							</Col>
						</div>
					</div>
				}
			</>
		);

	}

	getItemId(item)
	{
		return item.id !== undefined? 
			item.id: 
			(this.props.getItemId? 
				this.props.getItemId(item): 
				Object.values(item).join('|')
			);
	}

	handleToggleItem = (id) =>
	{
		const expandedItens = new Map(this.state.expandedItens);
		expandedItens.set(id, !expandedItens.has(id)? true: !expandedItens.get(id));

		this.setState({
			expandedItens: expandedItens
		});
	}

	renderCollapsableItem = (item) =>
	{
		if(!item)
		{
			return;
		}

		if(item === 'header')
		{
			return this.renderHeader();
		}

		const id = this.getItemId(item);
		
		const expanded = this.state.expandedItens.get(id)? true: false;

		return (
			<div key={id} className={`base-list-item ${expanded? 'expanded': ''}`}>
				<div className={`d-flex base-list-row stripped ${expanded? 'expanded': ''}`}>
					<Col xs="11" className="base-list-row-col-others">
						<Row 
							className="base-list-row-content" 
							tabIndex="-1" 
							onClick={() => this.handleToggleItem(id)}>
							{this.props.renderItem(item, true)}
						</Row>
					</Col>
					<div className="base-list-row-col-actions">
						<Button 
							size="xs" 
							outline 
							className="base-list-button"
							color="none"
							onClick={() => this.handleToggleItem(id)}
							title={`${expanded? 'Contrair': 'Expandir'}`}>
							<i className={`base-list-button-add-icon la la-caret-${expanded? 'up': 'down'}`}></i>
						</Button>
						{this.props.renderActions && this.props.renderActions(item)}
					</div>
				</div>
				<CollapseUnmounted isOpen={expanded} className="base-list-row-form">
					<Row>
						{this.props.renderItem(item, false)}
					</Row>
				</CollapseUnmounted>
			</div>
		);
	}
	
	handleSlideItem = (item = null) =>
	{
		const {slide} = this.state;
		this.setState({
			slide: {
				...slide, 
				item: item !== null? 
					item: 
					slide.item,
				visible: !slide.visible
			}
		});
	}

	renderSlidableItem = (item) =>
	{
		if(!item)
		{
			return;
		}

		if(item === 'header')
		{
			return this.renderHeader();
		}

		const id = this.getItemId(item);
		
		return (
			<div key={id} className="base-list-item">
				<div className="d-flex base-list-row stripped">
					<Col xs="11" className="base-list-row-col-others">
						<Row 
							className="base-list-row-content" 
							tabIndex="-1" 
							onClick={() => this.handleSlideItem(item)}>
							{this.props.renderItem(item, true)}
						</Row>
					</Col>
					<div className="base-list-row-col-actions">
						<Button 
							size="xs" 
							outline 
							className="base-list-button" 
							color="none"
							onClick={() => this.handleSlideItem(item)}
							title="Abrir">
							<i className="base-list-button-add-icon la la-caret-right"></i>
						</Button>
						{this.props.renderActions && this.props.renderActions(item)}
					</div>
				</div>
			</div>
		);
	}	

	render()
	{
		const DetailsComponent = this.props.details;

		return (
			<>
				<div className="base-list-container">
					<div className="base-list-form">
						{this.state.filters && this.state.filters.length > 0 &&
							<BaseListFilterForm
								filters={this.state.filters}
								updateFilters={this.updateFilters}
								maskInput={InputMask.maskInput} />
						}
					</div>
					{this.props.renderPanels && this.state.dataLoaded && this.props.renderPanels()}
					<div 
						className="base-list-grid" 
						ref={el => this.grid = el}>
						{!!this.state.dimensions &&
							<DataScroller
								ref={el => {this.dataScroller = el;}} 
								value={this.state.list} 
								lazy
								onLazyLoad={this.handleLazyLoad}
								itemTemplate={this.props.mode === 'COLLAPSED'? 
									this.renderCollapsableItem:
									this.renderSlidableItem} 
								rows={BaseList.ITEMS_PER_PAGE} 
								inline={this.props.inline? true: false}
								scrollHeight={this.props.inline? (this.props.height || this.state.dimensions.height) - 32: undefined} />
						}
						{this.props.mode === 'SLIDED' && this.state.slide &&
							<Drawer
								visible={this.state.slide.visible}
								onReturn={this.handleSlideItem}
							>
								<Row>
									{
										// eslint-disable-next-line no-extra-boolean-cast
										!!this.state.slide.item? 
											this.props.renderItem(this.state.slide.item, false):
											null
									}
								</Row>
							</Drawer>
						}						
					</div>
				</div>

				<ConfirmDialog
					visible={this.state.confirm.visible}
					text={this.state.confirm.text}
					onConfirmed={this.handleUserConfirmed}
					onHide={() => this.setState({confirm: {visible: false}})} 
				/>

				{DetailsComponent &&
					<div className="base-list-details-container">
						<DetailsComponent
							ref={(el) => this.details = el} 
							container={this}
							api={this.props.api}
							fullScreen={this.isMobile} />
					</div>							
				}
			</>
		);
	}
}
