import React, {useContext, useRef} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button} from 'reactstrap';
import BaseList from '../../../../components/BaseList';
import BaseForm from '../../../../components/BaseForm';
import {SessionContext} from '../../../../contexts/SessionContext';

const Workspaces = ({api, history, container}) => 
{
	const sessionStore = useContext(SessionContext);

	const baseList = useRef();
	
	const handleJoin = async (item) =>
	{
		const res = await api.patch(`workspaces/accept/${item.id}`);
		if(res.errors !== null)
		{
			container.showMessage(res.errors, 'error');
			return;
		}

		container.showMessage('Você agora é membro da área de trabalho!');
		baseList.current.updateItem(item, res.data);
	};

	const doLeave = async (item) =>
	{
		const res = await api.patch(`workspaces/leave/${item.id}`);
		if(res.errors !== null)
		{
			container.showMessage(res.errors, 'error');
			return;
		}

		container.showMessage('Você agora não é mais membro da área de trabalho!');
		baseList.current.deleteItem(item);
	};

	const handleLeave = (item) =>
	{
		container.showUserConfirmation(doLeave, item);
	};

	const handleSwitch = (item) =>
	{
		container.switchWorkspace(item.id);
	};

	const isOwner = (item) =>
	{
		return item.owner.id === sessionStore.user.id;
	};

	const renderFields = (item) =>
	{
		return (
			<>
				<Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Id</Label>
						<Input 
							disabled
							required 
							name="id" 
							type="text" 
							value={item.id || ''} 
							readOnly />
					</Col>
					<Col xs="12" md="6">
						<Label className="base-form-label">Nome</Label>
						<Input 
							disabled
							required 
							name="name" 
							type="text" 
							value={item.name || ''} 
							readOnly />
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Dono</Label>
						<Input 
							disabled
							required 
							name="owner" 
							type="text" 
							value={item.owner.nick || ''} 
							readOnly />
					</Col>
				</Row>
				<>
					<Row>
						<Col xs="12">
							<hr />
						</Col>
					</Row>
					<Row>
						<Col xs="4" md="2">
							Ações:
						</Col>
						{(item.user.acceptedAt && !isOwner(item)) &&
							<Col xs="12" md="2">
								<Button 
									color="danger" 
									className="mt-1"
									block 
									onClick={() => handleLeave(item)}
									disabled={!item.user.acceptedAt || isOwner(item)}>
									Abandonar
								</Button>
							</Col>
						}
						{(!item.user.acceptedAt && !isOwner(item)) &&
							<Col xs="12" md="2">
								<Button
									color="primary"
									className="mt-1"
									block
									onClick={() => handleJoin(item)}>
									Aceitar convite
								</Button>
							</Col>
						}
						<Col xs="12" md="2">
							<Button
								color="primary"
								className="mt-1"
								block
								onClick={() => handleSwitch(item)}
								disabled={item.id === sessionStore.workspace.id}>
								Visualizar
							</Button>
						</Col>
					</Row>
				</>				
			</>
		);
	};

	const renderItem = (item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="6" md="5">
						<span className="rf-list-icon"><i className="la la-desktop" /></span>
						&nbsp;
						<span className="rf-list-key">{item.name}</span>
					</Col>
					<Col md="4" className="d-none d-md-block">
						{item.owner.nick}
					</Col>
					<Col xs="6" md="3">
						{item.user.acceptedAt || isOwner(item)? 
							<span><i className="base-list-button-add-icon la la-check-circle"/> {isOwner(item)? 'Dono': 'Membro'}</span>: 
							<span><i className="base-list-button-add-icon la la-hourglass-half"/> Pendente</span>
						}
					</Col>
				</>
			);
		}

		const key = item.id;

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={key} 
							api={api}
							history={null}
							parent={baseList.current}
							controller='workspaces'
							item={item}
							editable={false}
							render={renderFields} 
						/>
					</Col>
				</Row>
			</Col>
		);	
	};

	const renderActions = (item) =>
	{
		return (
			<>
				<Button
					size="xs" outline className="base-list-button" 
					onClick={() => handleSwitch(item)}
					disabled={item.id === sessionStore.workspace.id} 
					title="Visualizar">
					<i className="base-list-button-add-icon la la-eye"></i>
				</Button>
				<Button
					size="xs" outline className="base-list-button" 
					onClick={() => handleLeave(item)}
					disabled={sessionStore.user.id === item.owner.id} 
					title="Abandonar">
					<i className="base-list-button-add-icon la la-trash"></i>
				</Button>
			</>
		);
	};

	const getFilters = () =>
	{
		return [
			{name: 'name', title: 'Nome', type: 'text'},
		];
	};

	const getHeader = () =>
	{
		return [
			{name: 'name', title: 'Nome', size: {xs:6, md:5}, klass: ''},
			{name: 'owner', title: 'Dono', size: {md:4}, klass: 'd-none d-md-block'},
			{name: 'state', title: 'Estado', size: {xs:6, md:3}, klass: ''},
		];
	};

	return (
		<div className="rf-page-contents">
			<div className="rf-page-header pt-2">
				<div>
					<div className="rf-page-title-icon">
						<i className="la la-desktop" />
					</div>
					<div className="rf-page-title-other">
						<div className="row">
							<div className="col-auto rf-page-title-container">
								<div className="rf-page-title">
									Configurações / Conta / Áreas de trabalho
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>Relação de áreas de trabalho do usuário</small>
							</div>
						</div>
					</div>
				</div>
			</div>

			<BaseList
				ref={baseList}
				api={api}
				history={history}
				container={container}
				title="Workspaces"
				controller="workspaces"
				sortBy='name'
				ascending={true} 
				getFilters={getFilters}
				getHeader={getHeader}
				renderItem={renderItem}
				renderActions={renderActions}
			/>
		</div>
	);
};

Workspaces.propTypes = 
{
	api: PropTypes.object.isRequired,
	history: PropTypes.object,
	container: PropTypes.object.isRequired
};

export default Workspaces;
