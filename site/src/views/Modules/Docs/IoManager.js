import React, {PureComponent} from 'react';
import {ScrollPanel} from 'primereact/components/scrollpanel/ScrollPanel';
import {ProgressBar} from 'primereact/progressbar';
import PropTypes from 'prop-types';
import saveAs from 'file-saver';
import JSZip from 'jszip';
import {Upload} from 'tus-js-client';
import { v4 as uuid } from 'uuid';
import './IoManager.css';

const OP_EXPIRES_DELAY = 3*1000;

export default class IoManager extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		container: PropTypes.object
	};

	constructor(props)
	{
		super(props);

		this.state = {
			files: [],
			finished: [],
			visible: false,
			expanded: true
		};

		this.submittingFinished = false;
		// eslint-disable-next-line no-undef
		this.basePath = process.env.NODE_ENV === 'development'? 
			// eslint-disable-next-line no-undef
			process.env.REACT_APP_ROBOTIKFLOW_UPLOAD_URL.replace('{{hostname}}', window.location.hostname):
			// eslint-disable-next-line no-undef
			process.env.REACT_APP_ROBOTIKFLOW_UPLOAD_URL;
		
		this.handleOnPause = this.handleOnPause.bind(this);
		this.handleOnCancel = this.handleOnCancel.bind(this);
		this.handleOnToggle = this.handleOnToggle.bind(this);
	}

	componentDidMount()
	{
		this.intervalId = setInterval(() => 
		{
			if(!this.state.visible && this.state.files.length > 0)
			{
				this.setState({
					visible: true
				});
			}
			
			this.removeExpired(this.state.files);
			this.submitFinished();

			if(this.state.visible && this.state.files.length === 0)
			{
				this.setState({
					visible: false
				});
			}
		}, 1000);
	}

	componentWillUnmount()
	{
		clearInterval(this.intervalId);
	}

	setStateAsync = async (state) =>
	{
		return new Promise(resolve => this.setState(state, resolve));
	}

	async downloadAndZipFiles(parent, folder, zip)
	{
		let res = await this.props.api.get(`docs/${folder.id}/filhos?any=1&page=0&size=10000`);
		if(res.errors !== null)
		{
			parent.io.state = 'FAILED';
			parent.io.errors = res.errors;
			return false;
		}
		
		zip = zip.folder(folder.name);
		parent.io.totalFiles += res.data.length;
		for(var i = 0; i < res.data.length; i++)
		{
			if(parent.io.state === 'CANCELLED')
			{
				return false;
			}
			
			let f = res.data[i];
			if(f.type === 'DIRETORIO')
			{
				if(!await this.downloadAndZipFiles(parent, f, zip))
				{
					return false;
				}
			}
			else
			{
				let res = await this.props.api.get(`docs/${f.id}`);

				parent = this.getFileNode(parent);
				if(res.errors === null)
				{
					let data = await fetch(res.data.url);
					if(data.ok)
					{
						++parent.io.filesCompleted;
						parent.io.completed = parent.io.filesCompleted / parent.io.totalFiles;
						this.setFileNode(parent, false);
						
						let blob = await data.blob();
						zip.file(f.name, blob);
					}
					else
					{
						parent.io.state = 'FAILED';
						parent.io.errors = [data.statusText];
						return false;
					}
				}
				else
				{
					parent.io.state = 'FAILED';
					parent.io.errors = res.errors;
					return false;
				}
			}
		}

		return true;
	}

	async downloadFolder(folder)
	{
		let zip = new JSZip();
		folder.io.filesCompleted = 0;
		folder.io.totalFiles = 0;
		var res = await this.downloadAndZipFiles(folder, folder.node, zip);

		folder = this.getFileNode(folder);
		if(res)
		{
			if(!folder.io.errors)
			{
				folder.io.state = 'FINISHED';
				folder.io.completed = 1;
				folder.io.expiresAt = Date.now() + OP_EXPIRES_DELAY;
				
				let content = await zip.generateAsync({type:'blob'});
				saveAs(content, folder.node.name + '.zip');
			}
		}
		
		this.downloadNext(this.setFileNode(folder, false));
	}	

	async download(file)
	{
		if(file.node.type === 'DIRETORIO')
		{
			await this.downloadFolder(file);
		}
		else
		{
			let res = await this.props.api.get(`docs/${file.node.id}`);
			if(res.errors !== null)
			{
				file.io.state = 'FAILED';
				file.io.errors = res.errors;
				this.downloadNext(this.setFileNode(file, false));
				return;
			}

			file.io.xhr = new XMLHttpRequest();
			file.io.xhr.open('GET', res.data.url);
			file.io.xhr.responseType = 'blob';
			file.io.xhr.onload = () =>
			{
				file = this.getFileNode(file);
				if(file && file.io.state !== 'CANCELLED')
				{
					saveAs(file.io.xhr.response, file.node.name);
					file.io.state = 'FINISHED';
					file.io.completed = 1;
					file.io.expiresAt = Date.now() + OP_EXPIRES_DELAY;
					this.downloadNext(this.setFileNode(file, false));
				}
			};

			file.io.xhr.onerror = () => 
			{
				file = this.getFileNode(file);
				file.io.state = 'FAILED';
				file.io.errors = file.io.xhr.statusText;
			};

			file.io.xhr.onprogress = (event) =>
			{
				file = this.getFileNode(file);
				file.io.completed = event.loaded / event.total;
				this.setFileNode(file, false);
			};
			
			file.io.xhr.send();
		}
	}

	async upload(parent, file, fileObj)
	{
		const this_ = this;
		
		file.io.upload = new Upload(fileObj, {
			endpoint: `${this.basePath}docs/upload`,
			// eslint-disable-next-line no-undef
			chunkSize: process.env.REACT_APP_ROBOTIKFLOW_UPLOAD_CHUNKSIZE,
			resume: false,
			retryDelays: [0, 3000, 5000, 10000, 20000],
			headers: {
				Authorization: 'Bearer ' + this.props.api.token
			},
			metadata: {
				parentId: parent.id,
				providerId: null,
				name: fileObj.name,
				size: fileObj.size,
				type: fileObj.type,
				updatedAt: fileObj.lastModified
			},
			onError: (error) =>
			{
				file = this_.getFileNode(file);
				file.io.errors = [error];
				file.io.state = 'FAILED';
				this_.uploadOrCreateDirNext(this_.setFileNode(file, false));
			},
			onProgress: (bytesUploaded, bytesTotal) =>
			{
				file = this_.getFileNode(file);
				file.io.completed = bytesUploaded / bytesTotal;
				this_.setFileNode(file, false);
			},
			onSuccess: async () =>
			{
				const data = {
					name: file.io.upload.file.name,
					size: file.io.upload.file.size,
					type: file.io.upload.file.type,
					updatedAt: file.io.upload.file.lastModified,
					uploadUrl: file.io.upload.url,
					providerId: null,
					parentId: parent.id
				};

				file = this_.getFileNode(file);
				if(file && file.io.state !== 'CANCELLED')
				{
					let res = await this_.props.api.requestURL('POST', `${this.basePath}docs`, data, {});
					if(res.errors === null)
					{
						file.io.state = 'FINISHED';
						file.io.completed = 1;
						file.io.expiresAt = Date.now() + OP_EXPIRES_DELAY;

						this_.addFinished(file, {parent: parent, data: res.data});						
					}
					else
					{
						file.io.state = 'FAILED';
						file.io.errors = res.errors;
					}
				
					this_.uploadOrCreateDirNext(this_.setFileNode(file, false));
				}
			}
		});
	
		file.io.upload.start();		
	}

	async delete(file)
	{
		let res = await this.props.api.del(`docs/${file.node.id}`);
		
		file = this.getFileNode(file);
		if(res.errors !== null)
		{
			file.io.state = 'FAILED';
			file.io.errors = res.errors;
		}
		else
		{
			file.io.state = 'FINISHED';
			file.io.completed = 1;
			file.io.expiresAt = Date.now() + OP_EXPIRES_DELAY;
			this.addFinished(file);
		}

		this.deleteNext(this.setFileNode(file, false));
	}

	async copy(file, destine)
	{
		let res = await this.props.api.patch(`docs/${file.node.id}`, {
			type: 'COPIAR',
			destino: destine.id
		});

		file = this.getFileNode(file);
		if(res.errors !== null)
		{
			file.io.state = 'FAILED';
			file.io.errors = res.errors;
		}		
		else
		{
			file.io.state = 'FINISHED';
			file.io.completed = 1;
			file.io.expiresAt = Date.now() + OP_EXPIRES_DELAY;
			this.addFinished(file, {destine: destine, data: res.data});
		}

		this.copyOrMoveNext(this.setFileNode(file, false));
	}

	async move(file, destine)
	{
		let res = await this.props.api.patch(`docs/${file.node.id}`, {
			type: 'MOVER',
			destino: destine.id
		});

		file = this.getFileNode(file);
		if(res.errors !== null)
		{
			file.io.state = 'FAILED';
			file.io.errors = res.errors;
		}		
		else
		{
			file.io.state = 'FINISHED';
			file.io.completed = 1;
			file.io.expiresAt = Date.now() + OP_EXPIRES_DELAY;
			this.addFinished(file, {destine: destine, data: res.data});
		}

		this.copyOrMoveNext(this.setFileNode(file, false));
	}

	async createDir(file)
	{
		let res = await this.props.api.patch(`docs/${file.io.parent.id}`, {
			type: 'CRIAR_DIRETORIO',
			diretorio: file.io.fileObj.name
		});

		file = this.getFileNode(file);
		if(res.errors !== null)
		{
			file.io.state = 'FAILED';
			file.io.errors = res.errors;
		}		
		else
		{
			file.io.state = 'FINISHED';
			file.io.completed = 1;
			file.io.expiresAt = Date.now() + OP_EXPIRES_DELAY;
			// patch
			Object.assign(file.node, res.data);
			this.addFinished(file, {parent: file.io.parent, data: res.data});
		}

		this.uploadOrCreateDirNext(this.setFileNode(file, false));
	}

	async rename(file, newName)
	{
		let res = await this.props.api.patch(`docs/${file.node.id}`, {
			type: 'RENOMEAR',
			name: newName
		});

		file = this.getFileNode(file);
		if(res.errors !== null)
		{
			file.io.state = 'FAILED';
			file.io.errors = res.errors;
		}		
		else
		{
			file.io.state = 'FINISHED';
			file.io.completed = 1;
			file.io.expiresAt = Date.now() + OP_EXPIRES_DELAY;
			this.addFinished(file, {parent: file.io.parent, data: res.data});
		}

		this.otherOpNext(this.setFileNode(file, false));
	}

	async doUpdate(files)
	{
		// upload
		files = await this.uploadOrCreateDirNext(files);

		// download
		files = await this.downloadNext(files);

		// delete
		files = await this.deleteNext(files);

		// copy or move
		files = await this.copyOrMoveNext(files);

		// other
		files = await this.otherOpNext(files);

		// remove expired
		this.removeExpired(files);
	}

	async uploadOrCreateDirNext(files) 
	{
		let uploadingOrCreating = files.find(f => f.io.state === 'UPLOADING' || f.io.state === 'CREATING');
		if (!uploadingOrCreating) 
		{
			let file = files.find(f => (f.io.operation === 'UPLOAD' || f.io.operation === 'CREATE_DIR') && f.io.state === 'QUEUED');
			if (file) 
			{
				if(file.io.operation === 'UPLOAD')
				{
					file.io.state = 'UPLOADING';
					if (file.io.upload) 
					{
						files = this.setFileNode(file, false);
						file.io.upload.start();
					}
					else 
					{
						file.io.completed = 0;
						files = this.setFileNode(file, false);
						this.upload(file.io.parent, file, file.io.fileObj);
					}
				}
				else
				{
					file.io.state = 'CREATING';
					file.io.completed = 0;
					files = this.setFileNode(file, false);
					this.createDir(file);
				}
			}
		}

		return files;
	}

	async downloadNext(files) 
	{
		let downloading = files.find(f => f.io.state === 'DOWNLOADING');
		if (!downloading) 
		{
			let file = files.find(f => f.io.operation === 'DOWNLOAD' && f.io.state === 'QUEUED');
			if (file) 
			{
				file.io.state = 'DOWNLOADING';
				file.io.completed = 0;
				files = this.setFileNode(file, false);
				if(file.io.xhr)
				{
					file.io.xhr.send();
				}
				else
				{
					this.download(file);
				}
				
			}
		}

		return files;
	}

	async copyOrMoveNext(files)
	{
		let copyingOrMoving = files.filter(f => f.io.state === 'COPYING' || f.io.state === 'MOVING');
		if (copyingOrMoving.length <= 2) 
		{
			let file = files.find(f => (f.io.operation === 'COPY' || f.io.operation === 'MOVE') && f.io.state === 'QUEUED');
			if (file) 
			{
				let isCopy = file.io.operation === 'COPY';
				file.io.state = isCopy ? 'COPYING' : 'MOVING';
				file.io.completed = 0;
				files = this.setFileNode(file, false);
				if (isCopy) 
				{
					this.copy(file, file.io.destine);
				}
				else 
				{
					this.move(file, file.io.destine);
				}
			}
		}

		return files;
	}

	async deleteNext(files)
	{
		let deleting = files.filter(f => f.io.state === 'DELETING');
		if(deleting.length <= 2)
		{
			let file = files.find(f => f.io.operation === 'DELETE' && f.io.state === 'QUEUED');
			if(file)
			{
				file.io.state = 'DELETING';
				file.io.completed = 0;
				files = this.setFileNode(file, false);
				this.delete(file);
			}
		}
		
		return files;
	}

	async otherOpNext(files)
	{
		let renaming = files.filter(f => f.io.state === 'RENAMING');
		if (renaming.length <= 2)
		{
			let file = files.find(f => f.io.operation === 'RENAME' && f.io.state === 'QUEUED');
			if (file) 
			{
				file.io.state = 'RENAMING';
				file.io.completed = 0;
				files = this.setFileNode(file, false);
				this.rename(file, file.io.fileObj.name);
			}
		}

		return files;
	}

	removeExpired(files) 
	{
		let expired = files.filter(f => (f.io.state === 'CANCELLED' || f.io.state === 'FINISHED') && Date.now() >= f.io.expiresAt).map(f => f.io.id);
		if (expired.length > 0) 
		{
			this.delFileNodes(expired, false);
		}
	}

	getFileNode(file)
	{
		let res = this.state.files.find(f => f.io.id === file.io.id);
		return res? res: null;
	}

	setFileNode(file, checkStates = true)
	{
		let files = Array.from(this.state.files);

		for(let i = 0; i < files.length; i++)
		{
			if(files[i].io.id === file.io.id)
			{
				files[i] = {...file};
				break;
			}
		}

		this.setState({
			files: files
		});
		
		if(checkStates)
		{
			this.doUpdate(files);
		}

		return files;
	}

	async delFileNodes(idList, checkStates = true)
	{
		let files = this.state.files.filter(f => !idList.includes(f.io.id));

		await this.setStateAsync({
			files: files
		});

		if(checkStates)
		{
			this.doUpdate(files);
		}
	}

	createFileNode(node, operation)
	{
		let file = {
			node: node,
			io: {
				id: uuid(),
				operation: operation,
				state: 'QUEUED',
				completed: 0
			}
		};
		
		return file;
	}

	addFinished(file, extra)
	{
		let finished = Array.from(this.state.finished);

		finished.push({
			file: file, 
			extra: extra
		});

		this.setState({
			finished: finished
		});
	}

	submitFinished()
	{
		try
		{
			if(this.submittingFinished)
			{
				return;
			}

			this.submittingFinished = true;
			
			let toDelete = [];

			for(let i = 0; i < this.state.finished.length; i++)
			{
				let fin = this.state.finished[i];

				switch(fin.file.io.operation)
				{
				case 'UPLOAD':
					this.props.container.handleOnFileUploaded(fin.extra.parent, fin.extra.data);
					break;
				case 'DELETE':
					this.props.container.handleOnFileDeleted(fin.file.node);
					break;
				case 'COPY':
					this.props.container.handleOnFileCopied(fin.file.node, fin.extra.destine, fin.extra.data);
					break;
				case 'MOVE':
					this.props.container.handleOnFileMoved(fin.file.node, fin.extra.destine, fin.extra.data);
					break;
				case 'CREATE_DIR':
					this.props.container.handleOnFolderCreated(fin.extra.parent, fin.extra.data);
					break;
				case 'RENAME':
					this.props.container.handleOnFileRenamed(fin.file.node, fin.file.io.fileObj.name);
					break;
				default:
					break;
				}

				toDelete.push(fin.file.io.id);
			}

			let finished = this.state.finished.filter(fin => !toDelete.includes(fin.file.io.id));

			this.setState({
				finished: finished
			});

			this.submittingFinished = false;
		}
		catch
		{
			this.submittingFinished = false;
		}
	}

	async addToDownload(nodes)
	{
		let files = Array.from(this.state.files);

		nodes.forEach(node =>
		{
			let file = this.createFileNode(node, 'DOWNLOAD');

			files.push(file);
		});

		await this.setStateAsync({
			files: files
		});

		this.doUpdate(files);
	}

	addToUploadAsEntry(parent, entry)
	{
		let this_ = this;
		return new Promise((resolve, reject) =>
		{
			let reading = 0;
			const files = [];		

			readEntry(parent, entry);

			function readEntry(parent, entry)
			{
				if (entry.isFile) 
				{
					reading++;
					entry.file(fileObj => 
					{
						reading--;
						let file = this_.fileObjToFileNode(parent, fileObj, 'UPLOAD');
						files.push(file);

						if (reading === 0) 
						{
							resolve(files);
						}
					});
				} 
				else if (entry.isDirectory) 
				{
					let fileObj = {
						name: entry.name
					};
	
					let folder = this_.fileObjToFileNode(parent, fileObj, 'CREATE_DIR');
					files.push(folder);

					readReaderContent(folder.node, entry.createReader());
				}
			}

			function readReaderContent(parent, reader) 
			{
				reading++;
	
				reader.readEntries(function (entries) 
				{
					reading--;
					for (const entry of entries) 
					{
						readEntry(parent, entry);
					}
	
					if (reading === 0) 
					{
						resolve(files);
					}
				});
			}			
		});
	}

	addToUploadAsFilesAndDirectories(parent, entry)
	{
		let this_ = this;
		return new Promise((resolve, reject) =>
		{
			let reading = 0;
			const files = [];		

			readReaderContent(parent, entry);

			function readEntry(parent, fileObj)
			{
				if (!fileObj.getFilesAndDirectories) 
				{
					reading++;
					if(fileObj.name)
					{
						reading--;
						let file = this_.fileObjToFileNode(parent, fileObj, 'UPLOAD');
						files.push(file);

						if (reading === 0) 
						{
							resolve(files);
						}
					}
				} 
				else
				{
					let folderObj = {
						name: fileObj.name
					};

					let folder = this_.fileObjToFileNode(parent, folderObj, 'CREATE_DIR');
					files.push(folder);

					readReaderContent(folder.node, fileObj);
				}
			}

			function readReaderContent(parent, entry) 
			{
				reading++;
	
				entry.getFilesAndDirectories(function (entries) 
				{
					reading--;
					for (const e of entries) 
					{
						readEntry(parent, e);
					}
	
					if (reading === 0) 
					{
						resolve(files);
					}
				});
			}			
		});
	}
	
	async addToUpload(parent, fileObjs)
	{
		let files = Array.from(this.state.files);

		if(fileObjs.getFilesAndDirectories)
		{
			let newFiles = await this.addToUploadAsFilesAndDirectories(parent, fileObjs);
			files.push(...newFiles);
		}
		else
		{
			if(fileObjs.length === 0)
			{
				return;
			}
			
			if (typeof fileObjs[0].webkitGetAsEntry === 'function') 
			{
				// NOTA: é necessário ler toda a array fileObjs primeiro, ou ela vai ser apagada pelo browser
				//		 dentro de addToUploadAsEntry() por algum motivo
				let entries = [];
				for(let i = 0; i < fileObjs.length; i++)
				{
					if (fileObjs[i].kind === 'file') 
					{
						entries.push(fileObjs[i].webkitGetAsEntry());
					}
				}
	
				for(let i = 0; i < entries.length; i++)
				{
					let newFiles = await this.addToUploadAsEntry(parent, entries[i]);
					files.push(...newFiles);
				}
			}
			else
			{
				fileObjs.forEach(fileObj =>
				{
					let file = this.fileObjToFileNode(parent, fileObj, 'UPLOAD');
					files.push(file);
				});
			}
		}
		

		await this.setStateAsync({
			files: files
		});

		this.doUpdate(files);
	}

	fileObjToFileNode(parent, fileObj, operation) 
	{
		return {
			node: 
			{
				name: fileObj.name,
			},
			io: 
			{
				id: uuid(),
				operation: operation,
				state: 'QUEUED',
				completed: 0,
				parent: parent,
				fileObj: fileObj
			},
		};
	}

	async addToDelete(nodes)
	{
		let files = Array.from(this.state.files);
		
		nodes.forEach(node =>
		{
			let file = this.createFileNode(node, 'DELETE');

			files.push(file);
		});

		await this.setStateAsync({
			files: files
		});

		this.doUpdate(files);
	}

	async addToCopy(nodes, destine)
	{
		let files = Array.from(this.state.files);
		
		nodes.forEach(node =>
		{
			let file = this.createFileNode(node, 'COPY');
			file.io.destine = destine;

			files.push(file);
		});

		await this.setStateAsync({
			files: files
		});

		this.doUpdate(files);
	}

	async addToMove(nodes, destine)
	{
		let files = Array.from(this.state.files);
		
		nodes.forEach(node =>
		{
			let file = this.createFileNode(node, 'MOVE');
			file.io.destine = destine;

			files.push(file);
		});

		await this.setStateAsync({
			files: files
		});

		this.doUpdate(files);
	}

	async addToCreateDir(parent, name)
	{
		let files = Array.from(this.state.files);
		
		let fileObj = {
			name: name
		};
		let file = this.fileObjToFileNode(parent, fileObj, 'CREATE_DIR');
		files.push(file);

		await this.setStateAsync({
			files: files
		});

		this.doUpdate(files);
	}

	async addToRename(node, name)
	{
		let files = Array.from(this.state.files);
		
		let file = this.createFileNode(node, 'RENAME');
		file.io.fileObj = {
			name: name
		};
		files.push(file);

		await this.setStateAsync({
			files: files
		});

		this.doUpdate(files);
	}

	handleOnPause(file)
	{
		switch(file.io.state)
		{
		case 'PAUSED':
			file.io.state = 'QUEUED';
			break;
		
		case 'QUEUED':
			file.io.state = 'PAUSED';
			break;

		case 'UPLOADING':
			if(file.io.upload)
			{
				file.io.state = 'PAUSED';
				file.io.upload.abort();
			}
			break;
		
		default:
			break;
		}

		this.setFileNode(file);
	}

	handleOnCancel(file)
	{
		switch(file.io.state)
		{
		case 'DOWNLOADING':
			if(file.io.xhr)
			{
				file.io.xhr.abort();
			}
			file.io.state = 'CANCELLED';
			file.io.expiresAt = Date.now() + 3*1000;
			this.setFileNode(file);
			break;
		
		case 'UPLOADING':
			if(file.io.upload)
			{
				file.io.upload.abort();
			}
			file.io.state = 'CANCELLED';
			file.io.expiresAt = Date.now() + 3*1000;
			this.setFileNode(file);
			break;

		case 'DELETING':
		case 'QUEUED':
		case 'PAUSED':
		case 'FAILED':
			file.io.state = 'CANCELLED';
			file.io.expiresAt = Date.now() + 3*1000;
			this.setFileNode(file);
			break;
		default:
			break;
		}
	}

	handleOnToggle()
	{
		this.setState({
			expanded: !this.state.expanded
		});
	}

	handleOnClick(file)
	{
		if(file.io.state === 'FAILED')
		{
			this.props.container.showMessage(file.io.errors, 'error');
		}
	}

	opToIcon = new Map([
		['UPLOAD', 'cloud-upload'], 
		['DOWNLOAD', 'cloud-download'],
		['COPY', 'clone'],
		['MOVE', 'times'],
		['DELETE', 'trash'],
		['CREATE_DIR', 'folder'],
		['RENAME', 'pencil']
	]);
	
	stateToIcon = new Map([
		['UPLOADING', 'spinner'], 
		['DOWNLOADING', 'spinner'],
		['COPYING', 'spinner'],
		['MOVING', 'spinner'],
		['DELETING', 'spinner'],
		['CREATING', 'spinner'],
		['RENAMING', 'spinner'],
		['QUEUED', 'list'],
		['PAUSED', 'minus'],
		['FAILED', 'exclamation-triangle'],
		['FINISHED', 'check'],
		['CANCELLED', 'ban'],
	]);
	
	render()
	{
		const children = () => 
		{
			return this.state.files.map(f =>
			{
				return (
					<div key={f.io.id} onClick={() => this.handleOnClick(f)} className={`io-manager-window-container-row ${f.io.errors && 'io-manager-window-container-row-error'}`}>
						<div><i className={`pi pi-${this.opToIcon.get(f.io.operation)}`}></i></div>
						<div style={{flexGrow: 3, textAlign: 'left'}}>{f.node.name.substring(0, 20)}</div>
						<div><ProgressBar value={(f.io.completed*100).toFixed(0)}/></div>
						<div><i className={`pi pi-${this.stateToIcon.get(f.io.state)}`}></i></div>
						<div>
							<span>
								<button 
									disabled={['FINISHED', 'FAILED', 'CANCELLED'].includes(f.io.state)} 
									onClick={() => this.handleOnPause(f)}>
									<i className={`pi pi-${f.io.state === 'PAUSED'? 'chevron-circle-right': 'minus-circle'}`} />
								</button>
							</span>
							<span>
								<button 
									disabled={['FINISHED', 'CANCELLED'].includes(f.io.state)} 
									onClick={() => this.handleOnCancel(f)}>
									<i className="pi pi-times-circle" />
								</button>
							</span>
						</div>
					</div>
				);
			});
		};
		
		return (
			<div className="io-manager-window" style={{display: this.state.visible? 'block': 'none'}}>
				<div className="io-manager-window-header">
					<div><i className="pi pi-cog"/></div>
					<div style={{flexGrow: 5}}>Operações {this.state.files.length > 0? `(${this.state.files.length})`: ''}</div>
					<div><button onClick={() => this.handleOnToggle()}><i className={`pi pi-${this.state.expanded? 'chevron-circle-down': 'chevron-circle-up'}`} /></button></div>
				</div>
				<div className="io-manager-window-container" style={{display: this.state.expanded? 'block': 'none'}}>
					<ScrollPanel className="full-height">
						{children()}
					</ScrollPanel>
				</div>
			</div>
		);
	}
}