import React, {useCallback, useEffect, useRef, useState} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button} from 'reactstrap';
import BaseList from '../../../../components/BaseList';
import BaseForm from '../../../../components/BaseForm';
import Misc from '../../../../libs/Misc';
import CollectionUtil from '../../../../libs/CollectionUtil';
import InputMultiSelectTree from '../../../../components/Input/InputMultiSelectTree';

const NEW_ITEM_ID = 'empty';

const Users = ({api, history, container}) =>
{
	const [groups, setGroups] = useState([]);

	const baseList = useRef();
	
	useEffect(() =>
	{
		loadGroups();
	}, []);

	const loadGroups = async () =>
	{
		const res = await api.get('groups?sort=id,asc');
		if(res.errors)
		{
			container.showMessage(res.errors, 'error');
			return;
		}

		setGroups(transformGroupsForEditing(res.data));
	};

	const transformGroupsForEditing = (groups) =>
	{
		const res = [];
		
		const pushNode = (group, level) => 
		{			
			const node = {
				value: group.id,
				label: group.name,
				level: level
			};

			res.push(node);

			if(group.children)
			{
				group.children.forEach(id => 
				{
					const sub = groups.find(g => g.id === id);
					if(sub)
					{
						pushNode(sub, level + 1);
					}
				});
			}
		};

		const root = groups
			.find(group => !group.parentId);

		pushNode(root, 0);

		return res;
	};

	const handleDelete = (item) =>
	{

	};

	const validateFields = (item, errors) =>
	{
		if(!item.groups || item.groups.length === 0)
		{
			errors.push('Usuário deve pertencer ao menos a um grupo');
		}

		return errors.length === 0;
	};

	const renderRoleOptions = () =>
	{
		return (
			CollectionUtil.getUserRoles().map((role, index) => 
				<option value={role.value} key={index}>{role.label}</option>
			)
		);
	};

	const renderLangOptions = () =>
	{
		return (
			CollectionUtil.getUserLangs().map((lang, index) => 
				<option value={lang.value} key={index}>{lang.label}</option>
			)
		);
	};

	const renderFields = (item, onChange) =>
	{
		const adding = !item.id;

		return (
			<>
				{!adding && <Row>
					<Col xs="12">
						<Label className="base-form-label">Id</Label>
						<Input 
							disabled={true} 
							required 
							name="" 
							type="text" 
							value={item.id || ''} 
							readOnly />
					</Col>
				</Row>}
				<Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">E-mail</Label>
						<Input 
							disabled={!adding}
							required 
							name="email" 
							type="text" 
							value={item.email || ''} 
							onChange={onChange} />
					</Col>
					<Col xs="12" md="6">
						<Label className="base-form-label">Ativo</Label>
						<Input 
							name="active" 
							type="checkbox" 
							className="base-form-checkbox"
							checked={(item.props && item.props.active) || false} 
							onChange={onChange} />
					</Col>
				</Row>
				{!adding && <Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Apelido</Label>
						<Input 
							disabled
							name="nick" 
							type="text" 
							value={item.nick || ''} 
							readOnly />
					</Col>
					<Col xs="12" md="6">
						<Label className="base-form-label">Nome</Label>
						<Input 
							disabled
							name="name" 
							type="text" 
							value={item.name || ''} 
							readOnly />
					</Col>
				</Row>}
				<Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Role</Label>
						<Input 
							required 
							name="role" 
							type="select" 
							value={item.role || ''} 
							onChange={onChange}>
							{renderRoleOptions()}
						</Input>
					</Col>
					<Col xs="12" md="6">
						<Label className="base-form-label">Grupos</Label>
						<InputMultiSelectTree
							required
							name="groups"
							options={groups}
							values={item.groups}
							onChange={onChange}
							hasSelectAll={false}
							disableSearch
						/>						
					</Col>
				</Row>
				{!adding && <Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Idioma</Label>
						<Input 
							disabled
							name="lang" 
							type="select" 
							value={item.lang || ''} 
							readOnly>
							{renderLangOptions()}
						</Input>
					</Col>
					<Col xs="12" md="6">
						<Label className="base-form-label">Timezone</Label>
						<Input 
							disabled
							name="timezone" 
							type="text" 
							value={item.timezone || ''} 
							readOnly />
					</Col>
				</Row>}
			</>
		);
	};

	const renderItem = (item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="3" md="1">
						<span className="rf-list-icon"><i className={`ava-${item.icon}`}/></span>
					</Col>
					<Col xs="9" md="5">
						{item.email}
					</Col>
					<Col md="6" className="d-none d-md-block">
						{item.name}
					</Col>
				</>
			);
		}

		const key = item.id || NEW_ITEM_ID;

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={key} 
							api={api}
							history={null}
							parent={baseList.current}
							controller='users'
							item={item}
							onLoad={transformForEditing}
							render={renderFields} 
							validate={validateFields} 
							onSubmit={transformForSubmitting}
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
					onClick={() => handleDelete(item)} 
					title="Remover">
					<i className="base-list-button-add-icon la la-trash"></i>
				</Button>
			</>
		);
	};

	const getFilters = () =>
	{
		return [
			{name: 'id', title: 'Id', type: 'text'},
			{name: 'email', title: 'E-mail', type: 'text'},
		];
	};

	const getHeader = () =>
	{
		return [
			{name: 'icon', title: 'Avatar', size: {xs:3, md:1}, klass: ''},
			{name: 'email', title: 'Email', size: {xs:9, md:5}, klass: ''},
			{name: 'name', title: 'Name', size: {md:6}, klass: 'd-none d-md-block'},
		];
	};

	const getItemTemplate = () =>
	{
		return {
			id: null,
			email: '',
			nick: '',
			role: '',
			groups: []
		};
	};

	const transformForEditing = (item) =>
	{
		return {
			...item,
			role: (item.role && item.role.name) || '',
			groups: item.groups.map(g => ({label: g.name, value: g.id})),
			createdAt: Misc.stringToDate(item.createdAt),
			updatedAt: Misc.stringToDate(item.updatedAt),
		};
	};

	const transformForSubmitting = (item) =>
	{
		return {
			...item,
			role: {name: item.role},
			groups: item.groups.map(g => ({id: g.value})),
		};
	};

	const handleItemsLoaded = useCallback((items) =>
	{
		return items.map(item => transformForEditing(item));
	}, []);

	return (
		<div className="rf-page-contents">
			<div className="rf-page-header pt-2">
				<div>
					<div className="rf-page-title-icon">
						<i className="la la-users" />
					</div>
					<div className="rf-page-title-other">
						<div className="row">
							<div className="col-auto rf-page-title-container">
								<div className="rf-page-title">
									Config / Workspace / Users
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>The complete list of workspace&apos;s users</small>
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
				title="Users"
				controller="users"
				sortBy='name'
				ascending={true} 
				itemTemplate={getItemTemplate()}
				getFilters={getFilters}
				getHeader={getHeader}
				onLoad={handleItemsLoaded}
				renderItem={renderItem}
				renderActions={renderActions}
			/>
		</div>
	);
};

Users.propTypes = 
{
	api: PropTypes.object.isRequired,
	history: PropTypes.object,
	container: PropTypes.object.isRequired
};

export default Users;
