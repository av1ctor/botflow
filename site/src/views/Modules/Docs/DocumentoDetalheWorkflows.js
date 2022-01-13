import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Button, Label, Container} from 'reactstrap';
import {AutoComplete} from 'primereact/autocomplete';

export default class DocumentoDetalheWorkflows extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		container: PropTypes.object,
		file: PropTypes.object
	};
	
	constructor(props)
	{
		super(props);

		this.state = {
			workflows: new Map(),
			workflowSuggestions: null
		};

		this.tipoOperacaoEnum = [
			{op: 'CRIADO', title: 'Documento Criado'},
			{op: 'ATUALIZADO', title: 'Documento Atualizado'},
			{op: 'MOVIDO', title: 'Documento Movido'},
			{op: 'REMOVIDO', title: 'Documento Excluído'}
		];

		this.handleWorkflowsChanged = this.handleWorkflowsChanged.bind(this);
		this.handleSuggestWorkflows = this.handleSuggestWorkflows.bind(this);
		this.handleSubmit = this.handleSubmit.bind(this);
	}

	async componentDidMount()
	{
		if(this.props.file)
		{
			let res = await this.props.api.get(`docs/${this.props.file.id}/workflows`);

			if(res.errors !== null)
			{
				this.props.container.showMessage(res.errors, 'error');
				return;
			}

			this.transformWorkflows(res.data);
		}
	}

	transformWorkflows(data) 
	{
		let workflows = new Map();
		this.tipoOperacaoEnum.forEach(top => 
		{
			workflows.set(top.op, data.filter(w => w.operacao === top.op));
		});

		this.setState({
			workflows: workflows
		});
	}

	handleWorkflowsChanged(workflowsForOp, op)
	{
		var workflowsWithouDups = workflowsForOp.filter((f, i) => !workflowsForOp.find((ff, ii) => ff.id === f.id && ii > i));

		var workflows = new Map(this.state.workflows);

		workflows.set(op, workflowsWithouDups);

		this.setState({
			workflows: workflows
		});
	}

	async handleSubmit()
	{
		let workflows = [];
		this.tipoOperacaoEnum.forEach(top => 
		{
			for(let workflow of this.state.workflows.get(top.op))
			{
				workflows.push({operacao: top.op, ...workflow});
			}
		});

		let res = await this.props.api.patch(`docs/${this.props.file.id}/workflows`, workflows);

		if(res.errors !== null)
		{
			this.props.container.showMessage(res.errors, 'error');
			return;
		}

		this.props.container.showMessage('Workflows atribuídos', 'success');
		
		this.transformWorkflows(res.data);
	}

	async handleSuggestWorkflows(query)
	{
		if(!query)
		{
			this.setState({
				workflowSuggestions: null
			});
			return;
		}

		let res = await this.props.api.get(`workflows?page=0&size=10&sort=name,asc&filters="name":"${query}"`);

		if(res.errors !== null)
		{
			return;
		}

		this.setState({
			workflowSuggestions: res.data
		});
	}

	render()
	{
		let file = this.props.file;
		if(!file || file.type !== 'DIRETORIO' || this.state === null || this.state.workflows === null)
		{
			return null;
		}

		let renderFields = () => this.tipoOperacaoEnum.map((top, index) =>
		{
			let workflows = this.state.workflows.get(top.op);
			
			return (
				<Row key={index} className="mb-2">
					<Col sm="12" md="6" lg="4">
						<Label className="base-form-label">{top.title}</Label>
						<span className="p-fluid">
							<AutoComplete 
								value={workflows}
								field="name" 
								multiple
								onChange={(e) => this.handleWorkflowsChanged(e.value, top.op)}
								suggestions={this.state.workflowSuggestions} 
								completeMethod={(e) => this.handleSuggestWorkflows(e.query)} />
						</span>
					</Col>
				</Row>
			);
		});
		
		return (
			<Container>
				{renderFields()}
				<Row>
					<Col sm="12" md="6" lg="4">
						<Button color="primary" onClick={this.handleSubmit}>Salvar</Button>
					</Col>
				</Row>
			</Container>
		);
	}
}