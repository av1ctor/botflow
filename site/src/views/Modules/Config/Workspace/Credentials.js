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

const Credentials = (props) =>
{
	const workspace = useContext(WorkspaceContext);
	
	const [credentials, setCredentials] = useState([]);
	const [refs, setRefs] = useState(new Map());

	const baseList = useRef();

	useEffect(() =>
	{
		(async () =>
		{
			const res = await workspace.loadObjSchemas('credentials');
			if(!res.errors)
			{
				setCredentials(res.data);
			}
		})();

		const messageListener = window.addEventListener('message', handleCrossWindowMessage);
		return () => window.removeEventListener('message', messageListener);
	}, []);

	const loadAndSetRefs = async (refsToload, currentRefs) =>
	{
		setRefs(await ObjUtil.loadRefs(refsToload, currentRefs, workspace));
	};

	const transformForEditing = useCallback((item) =>
	{
		const schema = JSON.parse(item.schema);

		if(schema.refs)
		{
			loadAndSetRefs(schema.refs, refs);
		}
		
		return {
			...item,
			schemaObj: schema,
			createdAt: Misc.stringToDate(item.createdAt),
			updatedAt: Misc.stringToDate(item.updatedAt),
		};
	}, [refs]);

	const transformForSubmitting = (item) =>
	{
		return {
			...item,
			schema: undefined,
			schemaObj: undefined,
		};
	};

	const handleLoadCredentials = (method, {page, size, sortBy, ascending, filters}) =>
	{
		return workspace.loadObjs('credentials', page, size, sortBy, ascending, filters);
	};

	const handleCrossWindowMessage = useCallback((ev) =>
	{
		/*if(ev.origin !== '')
		{
			return;
		}*/

		if(ev.data === null || ev.data.constructor !== Object)
		{
			return;
		}

		const msg = ev.data;
		if(msg.sender !== 'ROBOTIKFLOW')
		{
			return;
		}

		if(!msg.errors)
		{
			props.container.showMessage('Authorization granted!', 'success');
			/*if(!forms.current.has(msg.data.id))
			{
				forms.current.get(NEW_ITEM_ID).handleItemCreated(msg.data);
			}
			else
			{
				forms.current.get(msg.data.id).handleItemUpdated(msg.data);
			}*/			
		}
		else	
		{
			props.container.showMessage(msg.errors, 'error');
		}
	}, []);

	const getWindowOptions = () =>
	{
		const [width, height] = [500, 500];
		const windowOptions = 
		{
			width,
			height,
			left: Math.floor(window.screen.width / 2 - width / 2),
			top: Math.floor(window.screen.height / 2 - height / 2)
		};
	
		return Object.entries(windowOptions)
			.map(([key, value]) => `${key}=${value}`)
			.join(',');
	};
	
	const handleAuthorizeOAuth2 = async (item) =>
	{
		const data = 
		{
			apiBasePath: props.api.getPath(), 
			authToken: props.api.getToken(), 
			id: item.id,
		};

		const res = await props.api.get(`config/credentials/${item.id}/oauth2/url`);
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}
		
		const win = window.open(res.data.url, encodeURI(JSON.stringify(data)), getWindowOptions());
		if(win)
		{
			win.focus();
		}
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
			await workspace.updateObj('credentials', item.id, transformForSubmitting(item)):
			await workspace.createObj('credentials', transformForSubmitting(item));
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return null;
		}

		baseList.current.reload();

		return transformForEditing(res.data);
	}, []);

	const handleChangeCredential = useCallback((e, item, onChange) =>
	{
		const schemaId = e.target.value;
		const credential = schemaId?
			credentials.find(prov => prov.id === schemaId):
			{};

		const schema = credential.schemaObj || {};

		const fields = Object.keys(credential.fields || {})
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
					schema: credential.schema || '',
					schemaObj: schema,
				},
			}
		});
	}, [credentials, refs]);

	const renderCredentialsOptions = () =>
	{
		return (
			[{value: '', title: ''}].concat(credentials || []).map((credential, index) => 
				<option value={credential.id} key={index}>{credential.title}</option>
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
						<Label className="base-form-label">Credential</Label>
						<InputGroup>
							<InputGroupAddon addonType="prepend">
								<div className="base-form-prepend-icon" style={{width: 24}}>
									{getIcon(item)}
								</div>
							</InputGroupAddon>
							<Input 
								disabled={!adding} 
								required 
								name="type" 
								type="select" 
								value={item.schemaId || ''} 
								onChange={(e) => handleChangeCredential(e, item, onChange)}>
								{renderCredentialsOptions(adding)}
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
							{item.schemaObj.mode === 'oauth2' &&
								<Col xs="12" md="2">
									<Button 
										color="primary" 
										block 
										className="m-1" 
										onClick={() => handleAuthorizeOAuth2(item)}>
										{item.fields.accessToken? 'Reauthorize': 'Authorize'}
									</Button>
								</Col>							
							}
						</Row>
					</>
				}
			</>
		);
	};

	const renderActions = (item) =>
	{
		return (
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

	const renderItem = (item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="12" md="6">
						<span className="rf-list-icon">{getIcon(item)}</span>&nbsp;
						<span>{item.schemaObj.title}</span>
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

	const getHeader = () =>
	{
		return [
			{name: 'name', title: 'Credential', size: {xs: 12, md:6}, klass: ''},
			{name: null, title: 'Creator', size: {md:3}, klass: 'd-none d-md-block'},
			{name: 'createdAt', title: 'Date', size: {md:2}, klass: 'd-none d-md-block'},
		];
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
						<i className="la la-box" />
					</div>
					<div className="rf-page-title-other">
						<div className="row">
							<div className="col-auto rf-page-title-container">
								<div className="rf-page-title">
									Config / Workspace / Credentials
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>The complete list of workspace&apos;s credentials</small>
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
				controller={handleLoadCredentials}
				updateHistory={false}
				sortBy='createdAt' 
				itemTemplate={getItemTemplate()}
				getHeader={getHeader}
				onLoad={handleItemsLoaded}
				renderItem={renderItem}
				renderActions={renderActions}
			/>
		</div>
	);
};

Credentials.propTypes = 
{
	api: PropTypes.object.isRequired,
	history: PropTypes.object,
	container: PropTypes.object.isRequired
};

export default Credentials;