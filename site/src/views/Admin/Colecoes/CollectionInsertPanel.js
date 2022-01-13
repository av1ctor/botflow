import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import {Col, Row, Button, Label, Input} from 'reactstrap';
import SidebarResizeable from '../../../components/SidebarResizeable';

export default class CollectionInsertPanel extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		showMessage: PropTypes.func,
		fullScreen: PropTypes.bool
	};

	constructor(props)
	{
		super(props);

		this.emptyForm= {
			csv: '',
			substituirSeExistir: true
		};

		this.state = {
			visible: false,
			collection: null,
			form: Object.assign({}, this.emptyForm)
		};

		this.show = this.show.bind(this);
		this.handleOnChange = this.handleOnChange.bind(this);
		this.handleSubmit = this.handleSubmit.bind(this);
	}

	show(collection)
	{
		this.setState({
			collection: collection,
			visible: true,
			form: Object.assign({}, this.emptyForm)
		});
	}

	handleOnChange(event)
	{
		let form = Object.assign({}, this.state.form);

		let value = event.target.type !== 'checkbox' ? event.target.value : event.target.checked;

		form[event.target.name] = value;

		this.setState({
			form: form
		});
	}

	async handleSubmit()
	{
		var res = await this.props.api.post(`collections/${this.state.collection.id}/items`, this.state.form);

		if(res.errors !== null)
		{
			this.props.showMessage(res.errors, 'error');
			return;
		}

		this.props.showMessage('Todos os itens foram adicionados à coleção!', 'success');

		this.setState({
			form: Object.assign({}, this.emptyForm)
		});
	}

	render()
	{
		let collection = this.state.collection;

		return (
			<SidebarResizeable visible={this.state.visible} position="right" onHide={() => this.setState({visible: false})} fullScreen={this.props.fullScreen}>
				<div className="w-100 h-100">
					<FocusLock disabled={!this.state.visible} returnFocus className="w-100 h-100">
						<Row>
							<Col sm="12" className="text-center">
								Inserir itens na coleção <b>{collection && collection.name}</b>
							</Col>
						</Row>
						<Row>
							<Col sm="12">
								<Label className="base-form-label">Dados em formato CSV <small>(cabeçalho na primeira linha, separador: vírgula, delimitador: aspas duplas, uma linha para cada registro)</small></Label>
								<Input required name="csv" type="textarea" rows="16" value={this.state.form.csv} onChange={this.handleOnChange} />
							</Col>
						</Row>
						<Row>
							<Col sm="12">
								<Button color="primary" className="mt-4 float-right" type="submit" onClick={this.handleSubmit}>Enviar</Button>
							</Col>
						</Row>
					</FocusLock>
				</div>
			</SidebarResizeable>
		);
	}
}