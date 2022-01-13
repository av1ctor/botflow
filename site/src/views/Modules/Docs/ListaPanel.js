import React, {PureComponent} from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import {ContextMenu} from 'primereact/contextmenu';
import {DataScroller} from 'primereact/datascroller';
import {HotKeys} from 'react-hotkeys';
import {ScrollPanel} from 'primereact/components/scrollpanel/ScrollPanel';
import {OverlayPanel} from 'primereact/overlaypanel';
import {SizeMe} from 'react-sizeme';
import mime from 'mime-types';
import Hammer from 'react-hammerjs';
import {ListaItemDetalhe, ListaHeaderDetalhe} from './ListaItemDetalhe';
import {ListaItemIcone, ListaHeaderIcone} from './ListaItemIcone';
import {ListaItemIconeBig, ListaHeaderIconeBig} from './ListaItemIconeBig';
import DocumentoDropZone from './DocumentoDropZone';

import 'file-icon-vectors/dist/file-icon-square-o.min.css';

export default class ListaPanel extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		history: PropTypes.object,
		navegador: PropTypes.object,
		nodesToCopy: PropTypes.array,
		listingMode: PropTypes.string,
		isDesktop: PropTypes.bool
	};

	constructor(props)
	{
		super(props);

		this.state = {
			workspace: this.props.api.getWorkspace(),
			files: [],
			currentFile: null,
			selectedFiles: new Map(),
			searchQuery: '',
			searchAt: '',
			sortBy: 'name',
			ascending: true
		};

		this.noMoreFiles = false;
		this.currentPage = 0;
		this.loadingFiles = false;
		this.lastestSearchAt = Date.now();
		this.SEARCH_DELAY = 500;

		this.filesIndexList = [];

		this.keyMap = {
			'prev'		: ['up', 'left'],
			'prev_keep'	: ['mod+up', 'mod+left'],
			'prev_sel'	: ['shift+up', 'shift+left'],
			'next'		: ['down', 'right'],
			'next_keep'	: ['mod+down', 'mod+right'],
			'next_sel'	: ['shift+down', 'shift+right'],
			'first'		: 'home',
			'first_keep': 'mod+home',
			'first_sel'	: 'shift+home',
			'last'		: 'end',
			'last_keep'	: 'mod+end',
			'last_sel'	: 'shift+end',
			'open'		: 'enter',
			'goBack'	: 'backspace',
			'delete'	: 'del',
			'rename'	: 'f2',
			'copy'		: 'mod+c',
			'cut'		: 'mod+x',
			'paste'		: 'mod+v',
			'select'	: ['mod+space', 'shift+space'],
			'deselect'	: 'esc'
		};

		this.keyHandlers = {
			'prev'		: (event) => {event.preventDefault(); this.changeCurrentFile('prev');},
			'prev_keep'	: (event) => {event.preventDefault(); this.changeCurrentFile('prev', false, true);},
			'prev_sel'	: (event) => {event.preventDefault(); this.sequenciallySelectFiles('prev');},
			'next'		: (event) => {event.preventDefault(); this.changeCurrentFile('next');},
			'next_keep'	: (event) => {event.preventDefault(); this.changeCurrentFile('next', false, true);},
			'next_sel'	: (event) => {event.preventDefault(); this.sequenciallySelectFiles('next');},
			'first'		: () => this.changeCurrentFile('first'),
			'first_keep': () => this.changeCurrentFile('first', false, true),
			'first_sel'	: () => this.sequenciallySelectFiles('first'),
			'last'		: () => this.changeCurrentFile('last'),
			'last_keep'	: () => this.changeCurrentFile('last', false, true),
			'last_sel'	: () => this.sequenciallySelectFiles('last'),
			'goBack'	: (event) => {
				event.preventDefault();
				if(!this.state.currentFile.parent || !this.state.currentFile.parent.parent) 
				{
					return;
				}

				this.handleOnOpenFile(this.state.currentFile.parent.parent);
			},
			'open'		: () => this.handleOnOpenFile(this.state.currentFile),
			'delete'	: () => this.handleOnDeleteFile(),
			'rename'	: () => this.handleOnRenameFile(),
			'copy'		: () => this.handleOnCopyFile(),
			'cut'		: () => this.handleOnCutFile(),
			'paste'		: () => this.handleOnPasteFile(this.props.navegador.getCurrentFolder()),
			'select'	: () => this.handleOnSelectFile(true),
			'deselect'	: () => this.changeCurrentFile('current')
		};

		this.backFolder = {
			name: '..',
			type: 'DIRETORIO',
			owner: {},
			isSystem: true
		};

		this.handleLazyLoad = this.handleLazyLoad.bind(this);
		this.handleOnDropFile = this.handleOnDropFile.bind(this);
		this.handleOnContextMenu = this.handleOnContextMenu.bind(this);
		this.handleOnFileClicked = this.handleOnFileClicked.bind(this);
		this.handleOnOpenFile = this.handleOnOpenFile.bind(this);
		this.handleOnDeleteFile = this.handleOnDeleteFile.bind(this);
		this.handleOnRenameFile = this.handleOnRenameFile.bind(this);
		this.handleOnFileRenamed = this.handleOnFileRenamed.bind(this);
		this.handleCreateFolder = this.handleCreateFolder.bind(this);
		this.handleOnFolderCreated = this.handleOnFolderCreated.bind(this);
		this.handleOnMoveFile = this.handleOnMoveFile.bind(this);
		this.handleOnSelectFile = this.handleOnSelectFile.bind(this);
		this.handleOnSortChanged = this.handleOnSortChanged.bind(this);
		this.handleOnDownloadFiles = this.handleOnDownloadFiles.bind(this);
		this.changeCurrentFile = this.changeCurrentFile.bind(this);
		this.deleteFiles = this.deleteFiles.bind(this);
		this.filesTemplate = this.filesTemplate.bind(this);
	}

	setStateAsync = async (state) =>
	{
		return new Promise(resolve => this.setState(state, resolve));
	}

	getFirstFile() 
	{
		return this.state.files.length > 0 && this.state.files[0];
	}

	getLastFile() 
	{
		return this.state.files.length > 0 && this.state.files[this.state.files.length-1];
	}

	sortNodes(nodes)
	{
		let tipoToValor = (type) => type === 'DIRETORIO'? 0: 1;
	
		return nodes.sort(
			(a,b) => {
				if(a.type !== b.type)
					return tipoToValor(a.type) - tipoToValor(b.type);
				
				return a.name.toLowerCase() < b.name.toLowerCase()? -1: (a.name.toLowerCase() > b.name.toLowerCase()? 1: 0);
			});
	}

	addNodes(parent, nodes)
	{
		let newFiles = this.transformFiles(parent, nodes);
		
		let files = this.sortNodes(this.state.files.concat(this.filterFiles(newFiles, this.state.searchQuery, this.state.searchAt)));
		
		this.setState({
			files: this.reindexFiles(files)
		});
	}

	async delNode(node)
	{
		await this.setStateAsync({
			files: this.reindexFiles(this.state.files.filter(f => f.id !== node.id))
		});
	}

	updateParent(parent)
	{
		let files = this.state.files.map((n) =>
		{
			n.parent = parent;
			return n;
		});

		this.setState({
			files: files
		});
	}

	buildParentTree(node)
	{
		let list = [node];
		let parent = node.parent;
		while(parent)
		{
			list[list.length] = parent;
			parent = parent.parent;
		}
		return list;
	}

	getFiles()
	{
		return this.state.files;
	}

	transformFiles(parent, nodes)
	{
		nodes.forEach((n) =>
		{
			n.parent = parent;
			n.icon = this.getIcon(n);
			n.createdAt = n.createdAt? new Date(n.createdAt).toLocaleString(): '';
			n.updatedAt = n.updatedAt? new Date(n.updatedAt).toLocaleString(): '';
			n.size = n.type === 'DIRETORIO'? '': this.formatBytes(n.size, 0);
			n.mimeType = n.type === 'DIRETORIO'? 'Diretório': (mime.lookup(n.name) || '');
			n.highlightText = n.highlightText && n.highlightText.trim()
				.replace(new RegExp('\\n\\n', 'g'), '\n')
				.replace(new RegExp('\\n', 'g'), '<br/>')
				.replace(new RegExp('<em>', 'g'), '<b><u>')
				.replace(new RegExp('</em>', 'g'), '</u></b>')
				.substring(0, 1024);
			n.origin = 'LIST';
			n.isSystem = n.isSystem? n.isSystem: false;
			return n;
		});		

		return nodes;
	}

	getIcon(node) 
	{
		return 'fiv-sqo fiv-icon-' + (node.type !== 'DIRETORIO' ? node.name.substr(node.name.lastIndexOf('.') + 1).toLowerCase() : 'folder');
	}

	formatBytes(bytes, decimals = 2) 
	{
		if(!bytes || bytes === 0)
		{
			return '0 B';
		}
		
		var k = 1024;
		var sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
		var i = Math.floor(Math.log(bytes) / Math.log(k));
		
		return parseFloat((bytes / Math.pow(k, i)).toFixed(decimals)) + ' ' + sizes[i];
	}	

	reindexFiles(nodes)
	{
		nodes.forEach((f, index) => 
		{
			f.index =  index+1;
			f.prev = index > 0? nodes[index-1]: null;
			f.next = index+1 < nodes.length? nodes[index+1]: null;
		});

		return nodes;
	}

	async loadAndTransformFiles(folder, page, size, options = {})
	{
		let query = options.query || this.state.searchQuery;
		let contentSearch = (options.searchAt || this.state.searchAt) === 'content';
		let sortBy = options.sortBy || this.state.sortBy;
		let ascending = options.ascending || this.state.ascending;
		
		let res = await this.props.api.get(
			`docs/${folder.id}/filhos?any=1&page=${page}&size=${size}${sortBy?'&sort='+sortBy+','+(ascending?'asc':'desc'):''}${query?'&filters='+query:''}${contentSearch?'&contentSearch=true':''}`);
		
		let files = res.errors === null? res.data: [];
		
		if(options.addBackFolder)
		{
			if(folder.parent)
			{
				this.backFolder.id = folder.parent.id;
				files.unshift(this.backFolder);
			}
		}
		
		return this.transformFiles(folder, files);
	}

	async loadFiles(treeNode, force = false, options = {}) 
	{
		let currentFolder = this.props.navegador.getCurrentFolder();
		if (!currentFolder || currentFolder.id !== treeNode.id || force) 
		{
			options.addBackFolder = !this.props.isDesktop;

			let files = await this.loadAndTransformFiles(treeNode, 0, this.getTotalVisibleItems(), options);
			files = this.reindexFiles(files);
			
			this.noMoreFiles = false;
			this.currentPage = 0;

			this.setState({
				files: files,
				selectedFiles: new Map()
			});

			let currentFile = files && files.length > 0? files[0]: null;
			if(!options.query)
			{
				this.setCurrentFileSelection(currentFile);
			}
			else
			{
				this.clearSelectedFiles();
				this.setState({
					currentFile: currentFile
				});
			}

			if(this.props.navegador.getCurrentFolder().id !== treeNode.id)
			{
				this.props.navegador.setCurrentFolder(treeNode);
			}
		}
	}

	getTotalVisibleItems() 
	{
		let cnt = 0;
		
		switch(this.props.listingMode)	
		{
		case 'details':
			cnt = 5;
			break;
		case 'icons':
			cnt = 1;
			break;
		case 'icons-big':
			cnt = 2;
			break;
		default:
			break;
		}

		return this.props.navegador.PAGE_SIZE * cnt;
	}

	async loadMoreFiles(page)
	{
		let currentFolder = this.props.navegador.getCurrentFolder();
		if(currentFolder === null || this.noMoreFiles || page === 0 )
		{
			return;
		}

		if(this.loadingFiles)
			return;

		this.loadingFiles = true;
		++this.currentPage;
		
		try
		{
			let moreFiles = await this.loadAndTransformFiles(currentFolder, this.currentPage, this.getTotalVisibleItems());

			if(moreFiles && moreFiles.length > 0)
			{
				let files = this.state.files.concat(moreFiles);
				this.setState({
					files: this.reindexFiles(files)
				});

				this.noMoreFiles = false;
			}
			else
			{			
				// evitar ficar tentando carregar filhos que não existem (o DataScroller fica chamando sem parar quando vem zerado)
				this.noMoreFiles = true;
			}
		}
		finally
		{
			this.loadingFiles = false;
		}
	}

	async deleteFiles(nodes)
	{
		await this.props.navegador.deleteFiles(nodes);
	}

	async handleOnFileDeleted(node)
	{
		if(this.props.navegador.getCurrentFolder().id === node.parent.id)
		{
			// mover foco
			await this.setCurrentFile(node.prev || this.getFirstFile());

			// remover nó da lista
			await this.delNode(node);

			if(this.state.files.length < this.getTotalVisibleItems())
			{
				await this.loadMoreFiles(1);
			}

			// se for um diretório, recarregar árvore
			if(node.type === 'DIRETORIO')
			{
				let treeNode = this.props.navegador.findNodeById(this.props.navegador.getFolders(), node.id);
				await this.props.navegador.loadFolders({...treeNode.parent}, true);
			}
		}
	}

	async handleOnDeleteFile()
	{
		let docs = '';
		this.state.selectedFiles.forEach(async node =>
		{
			docs += `${docs.length > 0? ', "': '"'}${node.name}"`;
		});
		
		await this.props.navegador.showUserConfirmation(
			this.deleteFiles, 
			new Map(this.state.selectedFiles), 
			`Você tem certeza que deseja excluir o(s) documentos ${docs}?`);
	}

	async handleOnFileRenamed(node, name)
	{
		if(this.state.files.length < this.getTotalVisibleItems())
		{
			let files = this.reindexFiles(this.sortNodes(this.state.files.map(f => {
				if(f.id === node.id)
				{
					f.name = name;
					f.icon = this.getIcon(f);
				}
				return {...f};
			})));

			this.setState({
				files: files
			});

			if(node.type === 'DIRETORIO')
			{
				let treeNode = this.props.navegador.findNodeById(this.props.navegador.getFolders(), node.parent.id);
				await this.props.navegador.loadFolders({...treeNode}, true);
			}
		}
		else
		{
			if(node.type === 'DIRETORIO')
			{
				await this.reloadFilesAndFolders(node.parent.id);
			}
			else
			{
				if(node.parent.id === this.props.navegador.getCurrentFolder().id)
				{
					await this.loadFiles(node.parent, true);
				}
			}
		}
	}

	async handleOnRenameFile()
	{
		let node = this.state.currentFile;

		if(node.isSystem)
		{
			return;
		}

		this.props.navegador.showRenameDialog(node);
	}

	async handleOnMoveFile(source, destine)
	{
		await this.props.navegador.moveFiles(source, destine);
	}

	async handleOnFolderCreated(parent, node)
	{
		await this.reloadFilesAndFolders(parent.id);
	}
	
	async reloadFilesAndFolders(id) 
	{
		let treeNode = this.props.navegador.findNodeById(this.props.navegador.getFolders(), id);
		treeNode = await this.props.navegador.loadFolders({...treeNode}, true);
		if(id === this.props.navegador.getCurrentFolder().id)
		{
			await this.loadFiles(treeNode, true);
		}
	}

	async handleCreateFolder()
	{
		let parent = this.props.navegador.getCurrentFolder();
		
		this.props.navegador.showCreateFolderDialog(parent);
	}

	async handleLazyLoad(event)
	{
		await this.loadMoreFiles(event.first);
	}	

	handleOnContextMenu(event, node)
	{
		if(node.isSystem)
		{
			return;
		}
		
		this.setState({
			currentFile: node
		}); 
		this.listContextMenu.show(event);
	}

	async handleOnFileClicked(node, shiftKey = false, ctrlKey = false)
	{
		if(!shiftKey)
		{
			if(ctrlKey)
			{
				if(this.getSelectedFiles().filter(f => f.id === node.id).length > 0)
				{
					this.deselectFile(node);
					return;
				}
			}
			this.setCurrentFileSelection(node, true, ctrlKey);
		}
		else
		{
			this.selectFilesFromTopToNode(node, ctrlKey);
		}
	}
		
	async handleOnOpenFile(node)
	{
		if(node.type === 'DIRETORIO')
		{
			// carregar todos os filhos dos ancestrais primeiro, já que estamos navegando pela lista 
			// de documentos e a árvore de diretórios usa lazy loading
			let folders = this.props.navegador.getFolders();
			let parent = node.parent && this.props.navegador.findNodeById(folders, node.parent.id);
			let parentList = parent? this.buildParentTree(parent): [];
			for(let i = parentList.length-1; i >= 0; i--)
			{
				await this.props.navegador.loadFolders(parentList[i]);
			}
			// carregar os diretórios filhos deste nó, para construir a árvore de diretórios
			let treeNode = this.props.navegador.findNodeById(folders, node.id);
			treeNode = await this.props.navegador.loadFolders({...treeNode});
			// carregar todos os filhos deste nó, para construir a lista de documentos
			await this.loadFiles(treeNode);

			this.changeCurrentFile('first');
		}
		else
		{
			this.props.navegador.handleOnShowDetails(node);
		}
	}

	async handleOnDropFile(files)
	{
		let currentFolder = this.props.navegador.getCurrentFolder();

		this.props.navegador.uploadFiles(currentFolder, files);
	}

	async handleOnFileUploaded(parent, file)
	{
		if(this.props.navegador.getCurrentFolder().id === parent.id)
		{
			if(this.state.files.length + 1 < this.getTotalVisibleItems())
			{
				this.addNodes(parent, [file]);
				this.changeCurrentFile('first');
			}
			else
			{
				await this.loadFiles(parent, true);
			}
		}
	}

	async handleOnCopyFile()
	{
		await this.props.navegador.setNodesToCopy(Array.from(this.state.selectedFiles.values()), false);
	}

	async handleOnCutFile()
	{
		await this.props.navegador.setNodesToCopy(Array.from(this.state.selectedFiles.values()), true);
	}

	async handleOnPasteFile(destine)
	{
		await this.props.navegador.copyOrMoveFiles(destine);
	}

	handleOnFocus()
	{
		this.changeCurrentFile('current');
	}
	
	handleOnBlur()
	{
	}

	getNodeByDirection(dir)
	{
		switch(dir)
		{
		case 'next':
			return this.state.currentFile.next;
		case 'prev':
			return this.state.currentFile.prev;
		case 'first':
			return this.getFirstFile();
		case 'last':
			return this.getLastFile();
		case 'current':
			return this.state.currentFile;
		default:
			return null;
		}
	}

	changeCurrentFile(dir, select = true, preserve = false)
	{
		var node = this.getNodeByDirection(dir);
		
		if(node)
		{
			this.setCurrentFileSelection(node, select, preserve);
		}
	}

	async setCurrentFileSelection(node, select = true, preserve = false) 
	{
		if (select) 
		{
			await this.selectFile(node, preserve);
		}
		else 
		{
			if (!preserve) 
			{
				await this.clearSelectedFiles();
			}
		}

		await this.setCurrentFile(node);
	}

	async setCurrentFile(node) 
	{
		if (node !== this.state.currentFile) 
		{
			await this.setStateAsync({
				currentFile: node
			});
		}
		
		if(node && node.ref.current)
		{
			node.ref.current.focus();
		}
	}

	getSelectedFiles(asArray = true)
	{
		return asArray? Array.from(this.state.selectedFiles.values()): this.state.selectedFiles.values();
	}

	async selectFile(node, preserve = true)
	{
		var files = new Map(preserve? this.state.selectedFiles: null);

		if(node && !node.isSystem)
		{
			files.set(node.id, node);
		}
		
		await this.setStateAsync({
			selectedFiles: files
		}); 
	}

	async deselectFile(node)
	{
		var files = new Map(this.state.selectedFiles);

		files.delete(node.id);
		
		await this.setStateAsync({
			selectedFiles: files
		}); 
	}

	async clearSelectedFiles()
	{
		await this.setStateAsync({
			selectedFiles: new Map()
		}); 
	}

	selectFilesFromTopToNode(node, ctrlKey) 
	{
		var top = this.state.selectedFiles.size === 0? 
			this.state.files.find(f => !f.isSystem): 
			(this.state.files.find(f => this.state.selectedFiles.has(f.id)) || node);
		
		if(!top)
		{
			return;
		}
		
		if (top.index === node.index) 
		{
			this.setCurrentFileSelection(node, true, ctrlKey);
		}
		else 
		{
			if (top.index > node.index) 
			{
				let tmp = top;
				top = node;
				node = tmp;
			}
			
			let selectedFiles = new Map();
			let file = top;
			do 
			{
				if(!file.isSystem)
				{
					selectedFiles.set(file.id, file);
				}
				file = file.next;
			} while (file.id !== node.id);
			
			if(!node.isSystem)
			{
				selectedFiles.set(node.id, node);
			}
			
			this.setState({
				selectedFiles: selectedFiles
			});
			
			this.setCurrentFile(node);
		}
	}

	sequenciallySelectFiles(dir)
	{
		let node = this.getNodeByDirection(dir);
		if(node)
		{
			this.selectFilesFromTopToNode(node, false);
		}
	}

	handleOnSelectFile(preserve = false)
	{
		var node = this.state.currentFile;

		if(node.isSystem)
		{
			return;
		}
		
		if(this.state.selectedFiles.has(node.id))
		{
			this.deselectFile(node);
		}
		else
		{
			this.selectFile(node, preserve);
		}
	}

	buildItemContextMenu()
	{
		return [
			{
				label: 'Copiar',
				icon: 'pi pi-clone',
				command: async () => 
				{
					await this.handleOnCopyFile();
				}
			},
			{
				label: 'Recortar',
				icon: 'pi pi-times',
				command: async () => 
				{
					await this.handleOnCutFile();
				}
			},
			{
				label: 'Colar',
				icon: 'pi pi-check-circle',
				disabled: this.props.nodesToCopy.length === 0,
				command: async () => 
				{
					await this.handleOnPasteFile(this.state.currentFile);
				}
			},
			{
				label: 'Renomear',
				icon: 'pi pi-pencil',
				command: async () => 
				{
					await this.handleOnRenameFile();
				}
			},
			{
				separator: true
			},
			{
				label: 'Baixar',
				icon: 'pi pi-cloud-download',
				command: async () => 
				{
					await this.handleOnDownloadFiles();
				}
			},
			{
				separator: true
			},
			{
				label: 'Excluir',
				icon: 'pi pi-trash',
				command: async () => 
				{
					await this.handleOnDeleteFile();
				}
			},
			{
				separator: true
			},
			{
				label: 'Propriedades',
				icon: 'pi pi-briefcase',
				command: async () => 
				{
					await this.props.navegador.handleOnShowDetails(this.state.currentFile);
				}
			}
		];
	}

	buildContextMenu()
	{
		return [
			{
				label: 'Colar',
				icon: 'pi pi-check-circle',
				disabled: this.props.nodesToCopy.length === 0,
				command: async () => 
				{
					await this.handleOnPasteFile(this.props.navegador.getCurrentFolder());
				}
			},
			{
				separator: true
			},
			{
				label: 'Criar diretório',
				icon: 'pi pi-folder',
				command: async () => 
				{
					await this.handleCreateFolder();
				}
			}
		];
	}

	filterFiles(files, searchQuery = null, searchAt = null)
	{
		searchQuery = searchQuery || this.state.searchQuery;

		searchAt = searchAt || this.state.searchAt;

		if(!searchQuery || searchAt === 'content')
		{
			return files;
		}
		
		let filterLC = searchQuery.toLowerCase();
		
		return files.filter(f => f.name.toLowerCase().indexOf(filterLC) !== -1);
	}

	async handleOnSearchQueryChanged(query, searchAt)
	{
		await this.setStateAsync({
			searchQuery: query,
			searchAt: searchAt
		});

		if(Date.now() >= this.lastestSearchAt + this.SEARCH_DELAY)
		{
			this.lastestSearchAt = Date.now();
			setTimeout(() => 
			{
				this.loadFiles(this.props.navegador.getCurrentFolder(), true, {query: this.state.searchQuery, searchAt: this.state.searchAt});	
			}, this.SEARCH_DELAY*2);
		}
	}

	async handleOnSortChanged(sortBy, ascending)
	{
		await this.setStateAsync({
			sortBy: sortBy,
			ascending: ascending
		});

		this.loadFiles(this.props.navegador.getCurrentFolder(), true, {sortBy, ascending});
	}

	handleOnResize()
	{
		this.scrollPanel.moveBar();
	}

	async handleOnDownloadFiles()
	{
		await this.props.navegador.downloadFiles(this.state.selectedFiles);
	}

	filesTemplate(node) 
	{
		if(!node)
			return;

		let ref = React.createRef();
		node.ref = ref;

		const item = (node) =>
		{
			switch(this.props.listingMode)
			{
			case 'details':
				return <ListaItemDetalhe node={node} selected={this.state.selectedFiles.has(node.id)} container={this} />;
			case 'icons':
				return <ListaItemIcone node={node} selected={this.state.selectedFiles.has(node.id)} container={this} />;
			case 'icons-big':
				return <ListaItemIconeBig node={node} selected={this.state.selectedFiles.has(node.id)} container={this} />;
			default:
				break;
			}
		};

		return (
			<>
				<div
					ref={ref}
					tabIndex={node.index}
					onContextMenu={(event) => this.handleOnContextMenu(event, node)}>
					<Hammer
						onTap={(event) => this.handleOnFileClicked(node, event.srcEvent.shiftKey, event.srcEvent.ctrlKey)}
						onDoubleTap={() => this.handleOnOpenFile(node)}
						onPress={(event) => this.handleOnContextMenu(event.srcEvent, node)}>
						<div>
							{item(node)}
						</div>
					</Hammer>
				</div>
				{this.props.listingMode === 'icons'? <hr />: null}
			</>
		);
	}

	buildFolderNode(id, ids, name, names)
	{
		return {
			type: 'DIRETORIO',
			icon: 'pi pi-fw pi-folder-open',
			key: id,
			id: id,
			label: name,
			name: name,
			leaf: false,
			defaultExpanded: true,
			parent: ids.length > 0? this.buildFolderNode(ids.pop(), ids, names? names.pop(): null, names): null,
			origin: 'TREE'
		};

	}
	
	getFolderFromQuery(defaultFolder = null)
	{
		let values = this.props.navegador.getRouterSearchValues();

		let ids = values['idsPath']? values['idsPath'].split('/').slice(1): null;
		let id = ids? ids.pop(): null;

		let names = values['namesPath']? values['namesPath'].value.split('/').slice(1): null;
		let name = names? names.pop(): null;
		
		return id? this.buildFolderNode(id, ids, name, names): defaultFolder;
	}

	showOverlay(event, html)
	{
		this.overlayContent.innerHTML = html;
		this.overlay.show(event.target);
	}

	hideOverlay()
	{
		this.overlay.hide();
	}
	
	async componentDidMount()
	{
		this._isMounted = true;
		
		// carregar todos documentos filhos da raiz para montar a lista de documentos
		let options = {
			addBackFolder: !this.props.isDesktop
		};

		let files = await this.loadAndTransformFiles(this.getFolderFromQuery(this.props.navegador.getRoot()), 0, this.getTotalVisibleItems(), options);
		
		if(this._isMounted)
		{
			if(files.length > 0)
			{
				this.setState({
					files: this.reindexFiles(files),
					currentFile: files[0]
				});
			}

			this.configDataPanelWithScrollPanel();
		}
	}

	configDataPanelWithScrollPanel() 
	{
		// maneira hackish de integrar o ScrollPanel como DataPanel
		this.scrollFunction = () => 
		{
			const elm = this.contentElement;
			if ((elm.scrollTop >= ((elm.scrollHeight * 0.9) - (elm.clientHeight)))) 
			{
				this.dataScroller.load();
			}
		};
		
		// eslint-disable-next-line
		this.contentElement = ReactDOM.findDOMNode(this.scrollPanel.content);
		this.contentElement.addEventListener('scroll', this.scrollFunction);
	}

	componentWillUnmount() 
	{
		this._isMounted = false;
		
		if (this.scrollFunction) 
		{
			this.contentElement.removeEventListener('scroll', this.scrollFunction);
			this.scrollFunction = null;
		}	
	}

	render()
	{
		const calcHeight = (height) => Math.floor(height)-(this.props.listingMode === 'details'? 24: 2);
		
		const header = () => 
		{
			switch(this.props.listingMode) 
			{
			case 'details':
				return <ListaHeaderDetalhe sortBy={this.state.sortBy} onSortChanged={this.handleOnSortChanged} />;
			case 'icons':
				return <ListaHeaderIcone sortBy={this.state.sortBy} onSortChanged={this.handleOnSortChanged} />;
			case 'icons-big':
				return <ListaHeaderIconeBig sortBy={this.state.sortBy} onSortChanged={this.handleOnSortChanged} />;
			default:
				return null;
			}
		};

		return (
			<div style={{height:'100%'}} onContextMenu={e => this.listBlankSpaceContextMenu.show(e)}>
				<ContextMenu 
					model={this.buildItemContextMenu()} 
					ref={el => this.listContextMenu = el} />

				<ContextMenu 
					model={this.buildContextMenu()} 
					ref={el => this.listBlankSpaceContextMenu = el} />

				<OverlayPanel ref={el => this.overlay = el}>
					<div ref={el => this.overlayContent = el} />
				</OverlayPanel>


				<DocumentoDropZone node={this.props.navegador.getCurrentFolder} onDropAccepted={this.handleOnMoveFile} height="100%" className="box-file-list-content">
					<SizeMe monitorHeight monitorWidth={false}>
						{ ({size}) =>
							<HotKeys keyMap={this.keyMap} handlers={this.keyHandlers} className="full-height">
								{header()}
								<ScrollPanel ref={el => this.scrollPanel = el} style={{height: calcHeight(size.height)}}>
									<DataScroller
										ref={el => {this.dataScroller = el;}} 
										value={this.state.files} 
										onLazyLoad={this.handleLazyLoad}
										lazy
										itemTemplate={this.filesTemplate} 
										rows={this.getTotalVisibleItems()} 
										inline={true} 
										scrollHeight={calcHeight(size.height)} />
								</ScrollPanel>
							</HotKeys>
						}
					</SizeMe>
				</DocumentoDropZone>

			</div>
		);
	}
}

