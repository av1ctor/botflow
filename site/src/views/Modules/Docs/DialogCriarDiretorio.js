import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Dialog} from 'primereact/dialog';
import {Button} from 'primereact/button';
import {InputText} from 'primereact/inputtext';
import {InputGroup, InputGroupAddon, InputGroupText, Container} from 'reactstrap';
import FocusLock from 'react-focus-lock';

export default class DialogCriarDiretorio extends PureComponent
{
	static propTypes = {
		visible: PropTypes.bool,
		node: PropTypes.object,
		onConfirmed: PropTypes.func,
		onCancelled: PropTypes.func
	};

	constructor(props)
	{
		super(props);

		this.state = {
			name: ''
		};

		this.oldFocus = null;
	}

	handleChange = (event) =>
	{
		this.setState({
			[event.target.id]: event.target.type !== 'checkbox'? event.target.value: event.target.checked
		});
	}
	
	render()
	{
		const footer = (
			<div>
				<Button label="Salvar" icon="pi pi-check" tabIndex={2} onClick={() => this.props.onConfirmed(this.props.node, this.state.name)} className="p-button-danger" />
				<Button label="Cancelar" icon="pi pi-times" tabIndex={3} onClick={() => this.props.onCancelled()} className="p-button-secondary" />
			</div>
		);

		return (
			<FocusLock disabled={!this.props.visible} returnFocus>
				<Dialog 
					modal
					visible={this.props.visible}
					draggable
					responsive
					resizable
					maximizable
					closable={true}
					dismissableMask
					header={<span><span className="pi pi-folder"></span><b>Criar diretório</b></span>} 
					footer={footer} 
					onHide={() => this.props.onCancelled()} >
					<Container>
						<form onSubmit={(event) => {event.preventDefault(); this.props.onConfirmed(this.props.node, this.state.name);}}>
							<InputGroup className="mb-3">
								<InputGroupAddon addonType="prepend">
									<InputGroupText>
										<span>Nome</span>
									</InputGroupText>
								</InputGroupAddon>
								<InputText id="name" tabIndex={1} value={this.state.name} placeholder="Nome do diretório" onChange={this.handleChange} />
							</InputGroup>
						</form>
					</Container>
				</Dialog>
			</FocusLock>
		);
	}
}