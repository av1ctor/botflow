import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {Button, Card, CardBody, CardHeader, Input, Label, Row, Col, Collapse, FormGroup, CustomInput} from 'reactstrap';
import Misc from '../libs/Misc';

export default class BaseListFilterForm extends Component
{
	static propTypes = 
	{
		filters: PropTypes.array, 
		updateFilters: PropTypes.func,
		maskInput: PropTypes.func
	};

	constructor(props)
	{
		super(props);

		this.state = {
			filters: this.transformFilters(props.filters),
			expanded: false,
		};

		this.maskedFields = [];
	}

	isSelect(filter, type)
	{
		return type === 'select' && filter.data;
	}

	transformFilters(filters)
	{
		const createDebouncer = (filter, type) => {
			const res = {
				handler: null,
				options: {}
			};
			res.handler = type === 'checkbox' || this.isSelect(filter, type)?
				this.handleSubmit:
				Misc.debounce(this.handleSubmit, 1000, res.options);
			return res;
		};
		
		return filters.map(filter => {
			const type = this.transformFilterType(filter.type);
			const res = {
				...filter,
				options: {
					type: type,
					debounce: createDebouncer(filter, type)
				},
			};
			return res;
		});
	}

	componentDidUpdate(oldProps)
	{
		if(oldProps.filters !== this.props.filters)
		{
			this.setState({
				filters: this.transformFilters(this.props.filters)
			});
		}

		this.maskedFields.forEach(m => this.props.maskInput(m.ref, m.format));
	}
	
	handleChange = (event) =>
	{
		const id = event.target.id.replace('filter_', '');

		const filters = Array.from(this.state.filters);

		let filter = filters.find(f => f.name === id);
		if(filter)
		{
			filter.value = event.target.type !== 'checkbox'? 
				event.target.value: 
				event.target.checked;
		}

		this.setState({
			filters: filters
		});
	}

	handleSubmit = async (filters) =>
	{
		await this.props.updateFilters(filters || this.state.filters);
	}

	handleClear = () =>
	{
		const filters = this.state.filters.map(filter => ({...filter, value: filter.default}));

		this.setState({
			filters: filters
		});

		this.handleSubmit(filters);
	}

	handleCollapse = () =>
	{
		this.setState({
			expanded: !this.state.expanded
		});
	}	

	transformFilterType(t) 
	{
		switch(t)
		{
		case 'datetime':
			return 'datetime-local';
		case 'year':
		case 'month':
		case 'day':
			return 'select';
		default:
			return t;
		}
	}

	render()
	{
		this.maskedFields = [];
		
		const Filtros = this.state.filters && this.state.filters.map((filter) =>
		{
			const type = filter.options.type;

			const inputContent = 
				this.isSelect(filter, type) && 
				[{id:'', name:''}]
					.concat(filter.data.constructor === Function? filter.data(): filter.data)
					.map((opt, index) =>
					{
						const isObj = opt.constructor === Object;
						const value = isObj? opt.id: opt;
						const label = isObj? opt.name: opt;
						return <option key={index} value={value}>{label}</option>;
					});

			const ref = type !== 'checkbox'? React.createRef(): null;
			if(ref != null && filter.format)
			{
				this.maskedFields.push({ref: ref, format: filter.format});
			}

			const id = `filter_${filter.name}`;
			return (
				<Col xs="12" md="6" lg="4" key={id}>
					<FormGroup>
						{type !== 'checkbox'?
							<>
								<Label for={id}>{filter.title}</Label>
								<Input 
									innerRef={ref}
									type={type} 
									id={id} 
									value={filter.value? filter.value: filter.default || ''} 
									onChange={(e) => {this.handleChange(e); filter.options.debounce.handler();}}>
									{inputContent? inputContent: null}
								</Input>
							</>:
							<CustomInput 
								type="checkbox" 
								id={id} 
								label={filter.title} 
								checked={filter.value !== undefined && filter.value !== null? 
									(filter.value.constructor === String? filter.value === 'true': filter.value): 
									false} 
								onChange={(e) => {this.handleChange(e); filter.options.debounce.handler();}}/>
						}
					</FormGroup>
				</Col>
			);
		});

		const expanded = this.state.expanded;

		return (
			<div className={`base-list-filter-form ${expanded? 'expanded': ''}`}>
				<Row>
					<Col 
						className="base-list-filter-form-title"
						xs="12" 
						onClick={this.handleCollapse}>
						<div>
							<strong>Filters</strong>
						</div>
						<div className="float-right">
							<Button 
								size="xs" 
								outline 
								className="base-list-button" 
								onClick={this.handleCollapse}>
								<i className={`la la-caret-${expanded? 'up': 'down'}`}></i>
							</Button>
						</div>
					</Col>
				</Row>
				<Collapse isOpen={expanded}>
					<CardBody>
						<form>
							<Row>
								{Filtros}
							</Row>
							<Row className="pt-1">
								<Col xs="12">
									<div className="float-right">
										<Button
											color="secondary"
											disabled={this.state.isLoading}
											onClick={this.handleClear}
										>Clear</Button>
										&nbsp;
										<Button
											color="primary"
											disabled={this.state.isLoading}
											onClick={() => this.handleSubmit()}
										>Search</Button>
									</div>
								</Col>
							</Row>
						</form>
					</CardBody>
				</Collapse>
			</div>
		);
	}
}