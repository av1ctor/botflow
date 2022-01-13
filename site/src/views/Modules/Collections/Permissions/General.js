import React, {useCallback, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button} from 'reactstrap';
import {AutoComplete} from 'primereact/autocomplete';
import BaseList from '../../../../components/BaseList';
import BaseForm from '../../../../components/BaseForm';
import Misc from '../../../../libs/Misc';
import CollectionUtil from '../../../../libs/CollectionUtil';

const General = observer(props =>
{
	const store = props.store;

	const [suggestions, setSuggestions] = useState([]);
	
	const baseList = useRef();
	
	const handleDelete = useCallback((item) =>
	{

	}, []);

	const validateFields = (item, errors) =>
	{
		if(item.type === 'user')
		{
			if(!item.user || !item.user.id)
			{
				errors.push('Campo Usuário/Group deve ser preenchido');
			}
		}
		else
		{
			if(!item.group || !item.group.id)
			{
				errors.push('Campo Usuário/Group deve ser preenchido');
			}
		}

		if(!item.role)
		{
			errors.push('Campo role deve ser preenchido');
		}
		
		return errors.length === 0;
	};

	const handleUserOrGroupChanged = useCallback((type, objectOrText, onChange) =>
	{
		const isUser = type === 'user';
		
		const value = !objectOrText || typeof objectOrText === 'string'? 
			{[isUser? 'email': 'name']: objectOrText}: 
			objectOrText;
		
		onChange({
			target: {
				type: 'object',
				name: isUser? 'user': 'group',
				value: value 
			}
		});
	}, []);

	const handleSuggestUserOrGroup = useCallback(async (type, query) =>
	{
		const res = await props.api.get(type === 'user'?
			`users?filters="email":"${query}"`:
			`groups?filters="name":"${query}"`);

		if(res.errors !== null)
		{
			props.parent.showMessage(res.errors, 'error');
			return;
		}

		setSuggestions(res.data);
	}, []);

	const getHeader = () =>
	{
		return [
			{name: null, title: 'Usuário/Group', size: {xs:7, md:5}, klass: ''},
			{name: null, title: 'Role', size: {xs:5, md:4}, klass: ''},
			{name: 'createdAt', title: 'Created at', size: {md:3}, klass: 'd-none d-md-block'},
		];
	};

	const getItemTemplate = () =>
	{
		return {
			id: null,
			role: '',
			type: 'user',
			user: {id: null, email: ''},
			group: {id: null, name: ''},
			reverse: false
		};
	};

	const transformForEditing = (item) =>
	{
		const isUser = item.user && item.user.id;
		return {
			...item, 
			type: isUser? 'user': 'group',
			createdAt: Misc.stringToDate(item.createdAt),
			updatedAt: Misc.stringToDate(item.updatedAt),
		};
	};

	const transformForSubmitting = (item) =>
	{
		return {
			...item,
			group: item.type !== 'user'?
				item.group:
				undefined,
			user: item.type === 'user'?
				item.user:
				undefined
		};
	};

	const renderProfileOptions = () =>
	{
		return (
			CollectionUtil.getPermissionRoles().map((pp, index) =>
				<option key={index} value={pp.value}>{pp.label}</option>)
		);
	};

	const renderFields = (item, onChange) =>
	{
		const isUser = item.type === 'user';
		const hasId = !!item.id;
		
		return (
			<>
				{item.id &&
					<Row>
						<Col sm="12">
							<Label className="base-form-label">Id</Label>
							<Input 
								disabled={true} 
								required 
								name="" 
								var-type="string" 
								type="text" 
								value={item.id? item.id: ''} 
								readOnly 
							/>
						</Col>
					</Row>
				}
				<Row>
					<Col sm="12" md="6">
						<Label className="base-form-label">Type</Label>
						<Input 
							required
							disabled={hasId} 
							name="type" 
							var-type="string" 
							type="select" 
							value={item.type || 'user'}
							onChange={onChange}>
							<option value="user">User</option>
							<option value="group">Group</option>
						</Input>
					</Col>
					<Col sm="12" md="6">
						<Label className="base-form-label">User/Group</Label>
						<span className="p-fluid">
							<AutoComplete 
								value={isUser? item.user: item.group}
								disabled={hasId} 
								field={isUser? 'email': 'name'}
								onChange={(e) => handleUserOrGroupChanged(item.type, e.value, onChange)}
								suggestions={suggestions} 
								completeMethod={(e) => handleSuggestUserOrGroup(item.type, e.query)} />
						</span>
					</Col>
				</Row>
				<Row>
					<Col sm="12" md="6">
						<Label className="base-form-label">Role</Label>
						<Input 
							required 
							name="role" 
							var-type="string" 
							type="select" 
							value={item.role || ''} 
							onChange={onChange}>
							{renderProfileOptions()}
						</Input>
					</Col>
					<Col sm="12" md="6">
						<Label className="base-form-label">Reverse</Label>
						<Input 
							className="base-form-checkbox"
							name="reverse" 
							type="checkbox" 
							checked={item.reverse} 
							onChange={onChange} />
					</Col>
				</Row>
				{item.createdAt &&
					<Row>
						<Col sm="12" md="6" lg="6">
							<Label className="base-form-label">Created at</Label>
							<Input 
								disabled 
								type="text" 
								value={Misc.dateToString(item.createdAt) || ''} 
								readOnly />
						</Col>
						<Col sm="12" md="6" lg="6">
							<Label className="base-form-label">Created by</Label>
							<Input 
								disabled
								type="email" 
								value={item.createdBy? item.createdBy.email: ''} 
								readOnly />
						</Col>
					</Row>
				}
				{item.updatedAt &&
					<Row>
						<Col sm="12" md="6" lg="6">
							<Label className="base-form-label">Updated at</Label>
							<Input 
								disabled 
								type="text" 
								value={Misc.dateToString(item.updatedAt) || ''} 
								readOnly />
						</Col>
						<Col sm="12" md="6" lg="6">
							<Label className="base-form-label">Updated by</Label>
							<Input 
								disabled
								type="email" 
								value={item.updatedBy? item.updatedBy.email: ''} 
								readOnly />
						</Col>
					</Row>
				}
				{item.id &&
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
							<Col xs="8" md="2">
								<Button 
									color="primary" 
									block 
									className="m-1"
									onClick={() => handleDelete(item)}>
									Remover
								</Button>
							</Col>
						</Row>
					</>
				}

			</>
		);
	};

	const renderItem = (item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="7" md="5">
						{item.type === 'user'? item.user.email: item.group.name}
					</Col>
					<Col xs="5" md="4">
						{CollectionUtil.getPermissionRoleName(item.role)}
					</Col>
					<Col md="3" className="d-none d-md-block">
						{Misc.dateToString(item.createdAt)}
					</Col>
				</>
			);
		}

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={item.id? item.id: 'empty-item-form'} 
							api={props.api}
							history={null}
							parent={baseList.current}
							controller={`collections/${store.collection.id}/permissions`}
							item={item}
							enabled
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
					size="xs" 
					outline 
					className="base-list-button" 
					onClick={null} 
					title="Remover">
					<i className="base-list-button-add-icon la la-trash"></i>
				</Button>
			</>
		);
	};	

	const handleItemsLoaded = useCallback((items) =>
	{
		return items.map(item => transformForEditing(item));
	}, []);

	const collection = store.collection;

	return (
		<BaseList
			ref={baseList}
			inline
			controller={`collections/${collection.id}/permissions`}
			updateHistory={false}
			sortBy='createdAt'
			api={props.api}
			container={props.container} 
			itemTemplate={getItemTemplate()}
			getHeader={getHeader}
			onLoad={handleItemsLoaded}
			renderItem={renderItem}
			renderActions={renderActions}
		/>
	);
});

General.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
};

export default General;
