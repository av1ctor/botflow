import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Button} from 'reactstrap';
import Misc from '../libs/Misc';
import DomUtil from '../libs/DomUtil';

class BaseForm extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		history: PropTypes.object,
		parent: PropTypes.object,
		controller: PropTypes.oneOfType([PropTypes.string, PropTypes.func]),
		enabled: PropTypes.bool,
		editable: PropTypes.bool,
		item: PropTypes.object.isRequired,
		onLoad: PropTypes.func,
		render: PropTypes.func,
		onUpdated: PropTypes.func,
		validate: PropTypes.func,
		onSubmit: PropTypes.func,
		onSubmitted: PropTypes.func,
	};

	static defaultProps = 
	{
		enabled: true,
		editable: true,
		onLoad: (item) => item,
		validate: () => true,
		onSubmit: (item) => item,
		onSubmitted: () => null,
	};

	constructor(props)
	{
		super(props);

		this.state = {
			item: Misc.cloneObject(this.props.item),
			errors: [],
			isLoading: false
		};

		this.form = null;
	}

	componentDidUpdate(prevProps)
	{
		const {item} = this.props;
		if(item !== prevProps.item)
		{
			this.setState({
				item: Misc.cloneObject(item),
				errors: [],
			});
		}
	}

	updateItem = (fields) =>
	{
		let item = Object.assign({}, this.state.item);
		Object.assign(item, Misc.cloneObject(fields));
		this.setState({
			item: item
		});
	}

	handleChange = (event, forcedName = null) =>
	{
		const item = Object.assign({}, this.state.item);

		let value = null;
		if(event.target.type !== 'select-multiple')
		{
			value = event.target.type !== 'checkbox' ? 
				event.target.value : 
				event.target.checked;
		}
		else
		{
			value = Array.apply(null, event.target.options)
				.filter(option => option.selected)
				.map(option => option.value);
		}

		const fieldName = forcedName || event.target.name || event.target.id;

		if(fieldName.constructor !== Array)
		{
			Misc.setFieldValue(item, fieldName, value);
		}
		else
		{
			fieldName.forEach((name, index) =>
				Misc.setFieldValue(item, name, value[index])
			);
		}

		this.setState({
			item: item
		});

	}

	handleItemCreated = (item) =>
	{
		this.setState({
			item: Object.assign({}, this.props.item)
		});

		if(this.props.parent && this.props.parent.loadPage)
		{
			this.props.parent.loadPage(0);		
		}
	}

	handleItemUpdated = (item) =>
	{
		this.setState({
			item: (this.props.onUpdated || this.props.parent.updateItem)(
				this.props.item, 
				this.props.onLoad(item, true))
		});		
	}

	_insertOrUpdate(item)
	{
		if(this.props.controller.constructor === String)
		{
			return this.props.api.request(
				item.id? 'PATCH': 'POST', 
				`${this.props.controller}${item.id? '/' + item.id: ''}`, 
				this.props.onSubmit(item));
		}
		else
		{
			return this.props.controller(item.id? 'PATCH': 'POST', {
				id: item.id,
				data: this.props.onSubmit(item)
			});
		}
	}

	handleSubmit = async (event) =>
	{
		event.preventDefault();

		const item = this.state.item;

		const errors = [];
		if(!this.validateForm(errors))
		{
			this.setState({ errors: errors });
			return;
		}

		this.setState({ isLoading: true });

		try
		{
			const res = await this._insertOrUpdate(item);
			
			if(res.errors)
			{
				this.setState({ errors: res.errors });
				return;
			}

			this.setState({ errors: [] });

			this.props.parent.showMessage(item.id? 'Alterações salvas!': 'Item criado!', 'success');

			if(!item.id)
			{
				this.handleItemCreated(res.data);
			}
			else
			{
				this.handleItemUpdated(res.data);
			}

			this.props.onSubmitted(res.data);
		}
		finally
		{
			this.setState({ isLoading: false });
		}
	}

	renderErrors()
	{
		return this.state.errors && this.state.errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	}

	validateForm = (errors) =>
	{
		if(!this.form)
		{
			return true;
		}
		
		if(!this.props.validate(this.state.item, errors))
		{
			return false;
		}

		Array.prototype.push.apply(errors, DomUtil.validateForm(this.form));

		return errors.length === 0;
	}

	render()
	{
		return (
			<form onSubmit={this.handleSubmit} ref={el => this.form = el}>
				{this.props.render(this.state.item || {}, this.handleChange)}
				{this.props.editable &&
				<Row>
					<Col md="9">
						{this.renderErrors()}
					</Col>
					<Col md="3">
						{this.props.controller &&
							<Button
								className="float-right mt-2"
								color="primary"
								type="submit"
								disabled={!this.props.enabled || this.state.isLoading}>
								{this.props.item.id? 'Update': 'Create'}
							</Button>
						}
					</Col>
				</Row>}
			
			</form>
		);
	}

}

export default BaseForm;