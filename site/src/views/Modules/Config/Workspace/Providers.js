import React, {useCallback, useContext, useEffect, useRef, useState} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, InputGroup, InputGroupAddon, Button} from 'reactstrap';
import BaseList from '../../../../components/BaseList';
import CollectionUtil from '../../../../libs/CollectionUtil';
import Misc from '../../../../libs/Misc';
import ObjUtil from '../../../../libs/ObjUtil';
import ObjForm from '../../../../components/Form/ObjForm';
import {WorkspaceContext} from '../../../../contexts/WorkspaceContext';

const NEW_ITEM_ID = 'empty';

const getItemTemplate = () =>
{
	return {
		id: null,
		schemaId: null,
		fields: {},
		schema: null,
		schemaObj: {}
	};
};

const Providers = (props) =>
{
	const workspace = useContext(WorkspaceContext);
	
	const [providers, setProviders] = useState([]);
	const [refs, setRefs] = useState(new Map());

	const baseList = useRef();

	useEffect(() =>
	{
		(async () =>
		{
			const res = await workspace.loadObjSchemas('providers');
			if(!res.errors)
			{
				setProviders(res.data);
			}
		})();
	}, []);

	const loadAndSetRefs = async (refsToload, currentRefs) =>
	{
		const refs = await ObjUtil.loadRefs(refsToload, currentRefs, workspace);
		setRefs(refs);
		return refs;
	};

	const transformForEditing = async (item, refsRef) =>
	{
		const schema = JSON.parse(item.schema);

		if(schema.refs)
		{
			refsRef.refs = await loadAndSetRefs(schema.refs, refsRef.refs);
		}
		
		return {
			...item,
			schemaObj: schema,
			createdAt: Misc.stringToDate(item.createdAt),
			updatedAt: Misc.stringToDate(item.updatedAt),
		};
	};

	const transformForSubmitting = useCallback((item) =>
	{
		return {
			...item,
			schema: undefined,
			schemaObj: undefined,
		};
	}, []);

	const handleLoadProviders = (method, {page, size, sortBy, ascending, filters}) =>
	{
		return workspace.loadObjs('providers', page, size, sortBy, ascending, filters);
	};

	const handleDelete = (item) =>
	{

	};

	const handleValidate = (item, errors) =>
	{
		if(errors.length > 0)
		{
			props.container.showMessage(errors, 'error');
			return false;
		}

		return true;
	};

	const handleSubmit = useCallback(async (item) =>
	{
		const res = item.id?
			await workspace.updateObj('providers', item.id, transformForSubmitting(item)):
			await workspace.createObj('providers', transformForSubmitting(item));
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return null;
		}

		baseList.current.reload();

		return await transformForEditing(res.data, {refs});
	}, [refs]);

	const handleChangeProvider = useCallback(async (e, item, onChange) =>
	{
		const schemaId = e.target.value;
		const provider = schemaId?
			providers.find(prov => prov.id === schemaId):
			{};

		const schema = provider.schemaObj || {};

		const fields = Object.keys(provider.fields || {})
			.filter(field => !field.hidden)
			.reduce((obj, key) => 
			{
				obj[key] = null; 
				return obj;
			}, 
			{});

		if(schema.refs)
		{
			loadAndSetRefs(schema.refs, refs);
		}

		onChange({
			target: {
				name: null,
				value: {
					id: null,
					fields: fields,
					schemaId: schemaId,
					schema: provider.schema || '',
					schemaObj: schema,
				},
			}
		});
	}, [providers, refs]);

	const renderProvidersOptions = (isAdding) =>
	{
		return (
			[{value: '', title: ''}].concat(providers || []).map((provider, index) => 
				isAdding && provider.name === CollectionUtil.INTERNAL_STORAGE_PROVIDER_NAME?
					undefined:
					<option value={provider.id} key={index}>{provider.title}</option>
			)
		);
	};

	const renderTop = (item, onChange) =>
	{
		const adding = !item.id;

		return (
			<>
				{!adding && 
					<Row>
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
					</Row>
				}
				<Row>
					<Col xs="12">
						<Label className="base-form-label">Provider</Label>
						<InputGroup>
							<InputGroupAddon addonType="prepend">
								<div className="base-form-prepend-icon" style={{width: 24}}>
									{getIcon(item)}
								</div>
							</InputGroupAddon>
							<Input 
								disabled={!adding} 
								required 
								type="select" 
								value={item.schemaId || ''} 
								onChange={(e) => handleChangeProvider(e, item, onChange)}>
								{renderProvidersOptions(adding)}
							</Input>
						</InputGroup>
					</Col>
				</Row>
			</>
		);
	};

	const renderBottom = (item) =>
	{
		const internal = item.schemaObj.name === CollectionUtil.INTERNAL_STORAGE_PROVIDER_NAME;
		const adding = !item.id;

		return (
			<>
				{!adding && <>
					<Row>
						<Col xs="12" md="6" lg="6">
							<Label className="base-form-label">Created at</Label>
							<Input 
								disabled={true} 
								type="text" 
								value={Misc.dateToString(item.createdAt) || ''} 
								readOnly />
						</Col>
						<Col xs="12" md="6" lg="6">
							<Label className="base-form-label">Created by</Label>
							<Input 
								disabled={true} 
								type="email" 
								value={item.createdBy? item.createdBy.email: ''} 
								readOnly />
						</Col>
					</Row>
					<Row>
						<Col xs="12" md="6" lg="6">
							<Label className="base-form-label">Updated at</Label>
							<Input 
								disabled={true} 
								type="text" 
								value={Misc.dateToString(item.updatedAt) || ''} 
								readOnly />
						</Col>
						<Col xs="12" md="6" lg="6">
							<Label className="base-form-label">Updated by</Label>
							<Input 
								disabled={true} 
								type="email" 
								value={item.updatedBy? item.updatedBy.email: ''} 
								readOnly />
						</Col>
					</Row>
				</>}
				{!internal && !adding &&
					<>
						<Row>
							<Col xs="12">
								<hr />
							</Col>
						</Row>
						<Row>
							<Col xs="4" md="2">
								Actions:
							</Col>
							<Col xs="12" md="2">
								<Button 
									color="danger" 
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

	const renderActions = (item) =>
	{
		return (
			item.schemaObj.name !== CollectionUtil.INTERNAL_STORAGE_PROVIDER_NAME &&
			<>
				<Button 
					size="xs" outline className="base-list-button" 
					onClick={() => handleDelete(item)} 
					title="Delete">
					<i className="base-list-button-add-icon la la-trash"></i>
				</Button>
			</>
		);
	};

	const getIcon = (item) =>
	{
		return CollectionUtil.getObjectIcon(item.schemaObj.icon || 'wallet');
	};

	const getHeader = () =>
	{
		return [
			{name: 'name', title: 'Provider', size: {xs: 6, md:6}, klass: ''},
			{name: null, title: 'Creator', size: {md:3}, klass: 'd-none d-md-block'},
			{name: 'createdAt', title: 'Date', size: {md:2}, klass: 'd-none d-md-block'},
		];
	};

	const renderItem = (item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="6" md="6">
						<span className="rf-list-icon">{getIcon(item)}</span> {item.fields.name || item.schemaObj.title}
					</Col>
					<Col md="3" className="d-none d-md-block">
						{item.createdBy? item.createdBy.nick: 'admin@robotikflow'}
					</Col>
					<Col md="2" className="d-none d-md-block">
						{Misc.dateToString(item.createdAt)}
					</Col>
				</>
			);
		}

		const key = item.id || NEW_ITEM_ID;

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<ObjForm
							key={key}
							obj={item}
							refs={refs}
							disabled={item.schemaObj.name === CollectionUtil.INTERNAL_STORAGE_PROVIDER_NAME}
							onRenderBegin={renderTop} 
							onRenderEnd={renderBottom}
							onValidate={handleValidate} 
							onSubmit={handleSubmit}>
						</ObjForm>
					</Col>
				</Row>
			</Col>
		);		
	};

	const handleItemsLoaded = useCallback(async (items) =>
	{
		const refsRef = {refs};
		const res = [];

		for(const item of items)
		{
			res.push(await transformForEditing(item, refsRef));
		}

		return res;
	}, [refs]);

	return (
		<div className="rf-page-contents">
			<div className="rf-page-header pt-2">
				<div>
					<div className="rf-page-title-icon">
						<i className="la la-box" />
					</div>
					<div className="rf-page-title-other">
						<div className="row">
							<div className="col-auto rf-page-title-container">
								<div className="rf-page-title">
									Config / Workspace / Providers
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>The complete list of workspace&apos;s providers</small>
							</div>
						</div>
					</div>
				</div>
			</div>

			<BaseList
				ref={baseList}
				api={props.api}
				history={props.history}
				container={props.container}
				controller={handleLoadProviders}
				updateHistory={false}
				sortBy='createdAt' 
				itemTemplate={getItemTemplate()}
				getHeader={getHeader}
				renderItem={renderItem}
				onLoad={handleItemsLoaded}
				renderActions={renderActions}
			/>
		</div>
	);
};

Providers.propTypes = 
{
	api: PropTypes.object.isRequired,
	history: PropTypes.object,
	container: PropTypes.object.isRequired
};

export default Providers;