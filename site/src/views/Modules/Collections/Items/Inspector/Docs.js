import React, {useState, useRef, useEffect, useCallback} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, InputGroup, InputGroupAddon, Button} from 'reactstrap';
import BaseList from '../../../../../components/BaseList';
import BaseForm from '../../../../../components/BaseForm';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import Modal from '../../../../../components/Modal/Modal';
import DocPreview from '../../../../../components/DocPreview/DocPreview';
import DomUtil from '../../../../../libs/DomUtil';

import 'file-icon-vectors/dist/file-icon-square-o.min.css';
import DropFileZone from '../../../../../components/Misc/DropFileZone';
import FileHelper from '../../../../../libs/FileHelper';

const Docs = observer(props =>
{
	const store = props.store;
	
	const [modalVisible, setModalVisible] = useState(false);
	const [url, setUrl] = useState(null);
	const [provider, setProvider] = useState({});

	const fileHelper = useRef(new FileHelper(props.api));
	const baseList = useRef();

	useEffect(() =>
	{
		DomUtil.loadExternalScript('https://www.dropbox.com/static/api/2/dropins.js', {
			id: 'dropboxjs',
			// eslint-disable-next-line no-undef
			'data-app-key': process.env.REACT_APP_ROBOTIKFLOW_OAUTH2_DROPBOX_CLIENTID
		});
	}, []);

	const getHeader = () =>
	{
		return [
			{name: null, title: 'Nome', size: {xs:12, md:8}, klass: ''},
			{name: 'createdAt', title: 'Created at', size: {md:4}, klass: 'd-none d-md-block'},
		];
	};

	const getIcon = (item) =>
	{
		return 'fiv-sqo fiv-icon-' + 
			(item.type !== 'DIRETORIO' ? 
				item.name.substr(item.name.lastIndexOf('.') + 1).toLowerCase() : 
				'folder');
	};

	const renderStorageOptions = () =>
	{
		return (
			Object.entries(CollectionUtil.getStorageProviders()).map(([key, provider], index) => 
				<option value={key} key={index}>{provider.name}</option>
			)
		);
	};

	const buildParentPath = (collection, item) =>
	{
		return {
			path: `cols/${collection.id}/items/${item._id}`
		};
	};

	const uploadFiles = (files) =>
	{
		const {item} = props;
		const collection = store.collection;
		
		for(let i = 0; i < files.length; i++)
		{
			const file = files[i];
			if(file)
			{
				fileHelper.current.upload(buildParentPath(collection, item), file, collection.provider)
					.then(async doc => 
					{
						props.container.showMessage('Upload finalizado!', 'success');
						const res = props.api.post(`collections/${collection.id}/items/${item._id}/docs/${doc.id}`);
						if(res.errors)
						{
							props.container.showMessage(res.errors, 'error');	
							return;
						}
						baseList.current.closeCreateForm();
						baseList.current.reload();
					})
					.catch(errors => 
					{
						props.container.showMessage(errors, 'error');
					});
			}
		}
	};

	const handleOnFilesDropped = async (fileObjs) =>
	{
		const files = await fileHelper.current.getDropped(null, fileObjs);
		if(files)
		{
			uploadFiles(files.map(file => file.file));
		}
	};

	const renderUpload = () =>
	{
		return (
			<DropFileZone
				onDropAccepted={(files) => handleOnFilesDropped(files)}>
				<Row>
					<Col sm="12">
						<Label className="base-form-label">Arquivo</Label>
						<Input
							type="file"
							name="file"
							onChange={(event) => uploadFiles(event.target.files)}
						/>
						<div className="rf-col-docs-drophere">
							<h3><i className="la la-cloud-upload" /></h3> Arraste e solte seu arquivo aqui!
						</div>
					</Col>
				</Row>
			</DropFileZone>
		);
	};

	const renderFields = (item) =>
	{
		return (
			<>
				<Row>
					<Col sm="12" md="6">
						<Label className="base-form-label">Id</Label>
						<Input 
							disabled 
							required 
							name="" 
							var-type="string" 
							type="text" 
							value={item.id || ''} 
							readOnly />
					</Col>
					<Col sm="12" md="6">
						<Label className="base-form-label">Provedor</Label>
						<InputGroup>
							<InputGroupAddon addonType="prepend">
								<div className="image-valigned-container mr-1" style={{width: 24}}>
									<img 
										className="image-valigned" 
										src={`icons/${CollectionUtil.getStorageProviderIcon(item.provider.type)}.svg`} 
										width="24" 
										height="24" />
								</div>
							</InputGroupAddon>
							<Input 
								disabled 
								required 
								name="" 
								var-type="string" 
								type="select" 
								value={item.provider.type || ''} 
								readOnly>
								{renderStorageOptions()}
							</Input>
						</InputGroup>
					</Col>
				</Row>
				<Row>
					<Col sm="12" md="6">
						<Label className="base-form-label">Nome</Label>
						<Input 
							disabled 
							required 
							name="" 
							var-type="string" 
							type="text" 
							value={item.name || ''} 
							readOnly />
					</Col>
					<Col sm="12" md="6" lg="6">
						<Label className="base-form-label">Created at</Label>
						<Input 
							disabled 
							required 
							name="" 
							var-type="datetime" 
							type="text" 
							value={item.createdAt || ''} 
							readOnly />
					</Col>
				</Row>
				{item.id &&
					<>
						<Row>
							<Col xs="12">
								<hr />
							</Col>
						</Row>
						<Row>
							<Col xs="12" md="2">
								Ações:
							</Col>
							<Col xs="12" md="2">
								<Button 
									color="primary" 
									block 
									className="m-1" 
									onClick={() => handlePreview(item)}>
									Visualizar
								</Button>
							</Col>
							<Col xs="12" md="2">
								<Button 
									color="secondary" 
									block 
									className="m-1" 
									onClick={(e) => handleDownload(e, item)}>
									Baixar
								</Button>
							</Col>
						</Row>
					</>
				}
			</>
		);
	};
		
	const renderThumb = (item) =>
	{
		if(!item.thumbUrl || item.thumbUrl.length === 0)
		{
			return (
				<span 
					className={`rf-col-doc-icon ${getIcon(item)}`}>
				</span>);
		}
		else
		{
			return (
				<img 
					style={{
						maxHeight:80, 
						maxWidth:80
					}} 
					alt="thumbnail" 
					src={item.thumbUrl} />);
		}
	};

	const renderUrl = (item) =>
	{
		return (
			<span style={{display: 'table-cell', verticalAlign: 'middle'}}>
				{!item.downloadUrl? 
					item.name: 
					<a 
						href={item.downloadUrl} 
						onClick={e => handleDownload(e, item)}>
						{item.name}
					</a>}
			</span>
		);
	};

	const renderItem = (item, isFirstRow) =>
	{
		if(isFirstRow)
		{
			return (
				<>
					<Col xs="12" md="8">
						<div style={{display: 'table'}}>
							{renderThumb(item)}
							{renderUrl(item)}
						</div>
					</Col>
					<Col md="4" className="d-none d-md-block">
						{item.createdAt}
					</Col>
				</>
			);
		}

		return (
			<Col xs="12">
				<Row>
					<Col xs="12">
						<BaseForm
							key={item.id || 'empty'} 
							api={props.api}
							history={null}
							parent={baseList.current}
							item={item}
							enabled
							render={item.id? renderFields: renderUpload} 
						/>
					</Col>
				</Row>
			</Col>
		);		
	};

	const getWindowOptions = (width = null, height = null) =>
	{
		if(!width)
		{
			width = Math.ceil(window.screen.width * 0.8);
		}

		if(!height)
		{
			height = Math.ceil(window.screen.height * 0.8);
		}

		const windowOptions = 
		{
			width,
			height,
			left: Math.floor(window.screen.width / 2 - width / 2),
			top: Math.floor(window.screen.height / 2 - height / 2),
		};
	
		return Object.entries(windowOptions)
			.map(([key, value]) => `${key}=${value}`)
			.join(',');
	};

	const showModal = useCallback((item) =>
	{
		setModalVisible(true);
		setUrl(item.previewUrl);
		setProvider(item.provider);
	}, [modalVisible]);

	const hideModal = useCallback(() =>
	{
		setModalVisible(false);
		setUrl(null);
	}, [modalVisible]);

	const handlePreview = async (item) =>
	{
		if(!item.previewUrl)
		{
			const res = await props.api.get(`docs/${item.id}/preview`);
			if(res.errors !== null)
			{
				return;
			}		

			item.previewUrl = res.data.url;
		}

		if(item.provider.name === CollectionUtil.INTERNAL_STORAGE_PROVIDER_NAME)
		{
			showModal(item);
		}
		else
		{
			window.open(item.previewUrl, '_blank', getWindowOptions());
		}

	};

	const handleDownload = async (e, item) =>
	{
		e.preventDefault(); 
		e.stopPropagation();

		let url = item.downloadUrl;
		if(!url)
		{
			const res = await props.api.get(`docs/${item.id}`);
			if(res.errors !== null)
			{
				return;
			}

			url = res.data.url;
		}

		window.open(url, '_blank');
	};

	const renderActions = (item) =>
	{
		return (
			<>
				<Button 
					size="xs" outline className="base-list-button" 
					onClick={(e) => handleDownload(e, item)} 
					title="Baixar">
					<i className="base-list-button-add-icon la la-download"></i>
				</Button>
				<Button 
					size="xs" outline className="base-list-button" 
					onClick={() => handlePreview(item)} 
					title="Visualizar">
					<i className="base-list-button-add-icon la la-eye"></i>
				</Button>
			</>
		);
	};

	const getEmpty = (collection) =>
	{
		return {
			id: null,
			name: '',
			provider: collection.provider
		};
	};

	if(!props.visible)
	{
		return null;
	}

	const collection = store.collection;
	if(!collection)
	{
		return null;
	}

	return (
		<>
			<BaseList
				ref={baseList}
				inline
				controller={`collections/${collection.id}/items/${props.item._id}/docs`}
				updateHistory={false}
				sortBy='createdAt'
				api={props.api}
				container={props.container} 
				itemTemplate={getEmpty(collection)}
				getHeader={getHeader}
				renderItem={renderItem}
				renderActions={renderActions}
			/>
			
			<Modal
				isOpen={modalVisible}
				onHide={hideModal}
				className="rf-col-doc-preview-modal">
				<DocPreview 
					url={url}
					provider={provider.type}
					width="100%"
					height="100%" />
			</Modal>
		</>
	);
});

Docs.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
	store: PropTypes.object,
	fullScreen: PropTypes.bool,
	visible: PropTypes.bool,
	item: PropTypes.object.isRequired,
};

export default Docs;

