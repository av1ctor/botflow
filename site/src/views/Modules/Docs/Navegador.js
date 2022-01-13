import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import Split from 'react-split';
import {Button} from 'primereact/button';
import {HotKeys} from 'react-hotkeys';
import {BreadCrumb} from 'primereact/breadcrumb';
import {InputText} from 'primereact/inputtext';
import {Dropdown} from 'primereact/dropdown';
import {Row, Col} from 'reactstrap';
import DropFileZone from '../../../components/DropFileZone';
import ArvorePanel from './ArvorePanel';
import ListaPanel from './ListaPanel';
import DialogRenomear from './DialogRenomear';
import DialogCriarDiretorio from './DialogCriarDiretorio';
import Conditional from '../../../components/Conditional';
import ConfirmDialog from '../../../components/Dialog/ConfirmDialog';

export default class Navegador extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		history: PropTypes.object,
		container: PropTypes.object,
		ioManager: PropTypes.object,
		height: PropTypes.number,
		isDesktop: PropTypes.bool
	};

	constructor(props)
	{
		super(props);

		this.state = {
			userConfirmationVisible: false,
			userConfirmationText: '',
			selectedNode: null,
			nodesToCopy: [],
			isCut: false,
			renameDialogVisible: false,
			createFolderDialogVisible: false,
			breadCrumbItems: [],
			breadCrumbHome: null,
			searchQuery: '',
			searchAt: 'name',
			listingMode: 'details'
		};

		this.keyMap = {
			'change': 'tab'
		};

		this.keyHandlers = {
			'change': (event) => {

				switch(this.currentFocus)
				{
				case this.arvorePanelParent:
					this.arvorePanel.handleOnBlur();
					this.currentFocus = this.listaPanelParent;
					break;
				case this.listaPanelParent:
					this.listaPanel.handleOnBlur();
					this.currentFocus = this.arvorePanelParent;
					break;
				default:
					this.currentFocus = this.arvorePanelParent;
					break;
				}
				
				this.currentFocus.focus();
				
				if(this.currentFocus === this.arvorePanelParent)
				{
					this.arvorePanel.handleOnFocus();
				}
				else
				{
					this.listaPanel.handleOnFocus();
					event.preventDefault();
				}
			},
		}

		this.PAGE_SIZE = 10;
		this.currentFocus = null;
		this.arvorePanel = null;
		this.listaPanel = null;

		this.listingViewButtonIcon = {
			'details': 'align-justify',
			'icons': 'table',
			'icons-big': 'image',
		};

		this.searchAtOptions = [
			{label: 'Por name', value: 'name'},
			{label: 'Por conteúdo', value: 'content'}
		];

		this.loadFolders = this.loadFolders.bind(this);
		this.getFolders = this.getFolders.bind(this);
		this.getRoot = this.getRoot.bind(this);
		this.setCurrentFolder = this.setCurrentFolder.bind(this);
		this.getCurrentFolder = this.getCurrentFolder.bind(this);
		this.loadFiles = this.loadFiles.bind(this);
		this.showMessage = this.showMessage.bind(this);
		this.showUserConfirmation = this.showUserConfirmation.bind(this);
		this.handleOnUserConfirmed = this.handleOnUserConfirmed.bind(this);
		this.buildBreadCrumb = this.buildBreadCrumb.bind(this);
		this.showRenameDialog = this.showRenameDialog.bind(this);
		this.handleOnRenameConfirmed = this.handleOnRenameConfirmed.bind(this);
		this.showCreateFolderDialog = this.showCreateFolderDialog.bind(this);
		this.handleOnCreateFolderConfirmed = this.handleOnCreateFolderConfirmed.bind(this);
		this.moveFiles = this.moveFiles.bind(this);
		this.updateFileListParent = this.updateFileListParent.bind(this);
		this.handleOnSearchQueryChanged = this.handleOnSearchQueryChanged.bind(this);
		this.handleOnChangeViewMode = this.handleOnChangeViewMode.bind(this);
		this.handleOnResize = this.handleOnResize.bind(this);
	}

	setStateAsync = async (state) =>
	{
		return new Promise(resolve => this.setState(state, resolve));
	}

	async loadFolders(node, force = false)
	{
		return await this.arvorePanel.loadFolders(node, force);
	}

	setCurrentFolder(node)
	{
		return this.arvorePanel.setCurrentFolder(node);
	}

	getCurrentFolder()
	{
		return this.arvorePanel && this.arvorePanel.getCurrentFolder();
	}

	getFolders()
	{
		return this.arvorePanel.getFolders();
	}

	getRoot()
	{
		return this.arvorePanel && this.arvorePanel.getRoot();
	}

	async loadFiles(node, force = false)
	{
		return await this.listaPanel.loadFiles(node, force);
	}

	showMessage(msg, severity = 'info')
	{
		this.props.container.showMessage(msg, severity);
	}

	showUserConfirmation(callback, data, detail = 'Você tem certeza que deseja continuar?')
	{
		this.handleUserConfirmationCtx = {callback, data};
		this.setState({
			userConfirmationText: detail,
			userConfirmationVisible: true
		});
	}

	findNodeById(nodes, id)
	{
		for(let i = 0; i < nodes.length; i++)
		{
			if(nodes[i].id === id)
			{
				return nodes[i];
			}
		}

		for(let i = 0; i < nodes.length; i++)
		{
			let n = nodes[i];
			if(n.children && n.children.length > 0)
			{
				let res = this.findNodeById(n.children, id);
				if(res)
				{
					return res;
				}
			}
		}

		return null;
	}

	async handleOnUserConfirmed()
	{
		await this.handleUserConfirmationCtx.callback(this.handleUserConfirmationCtx.data);

		this.setState({
			userConfirmationVisible: false
		});
	}

	showRenameDialog(node)
	{
		this.setState({
			renameDialogVisible: true,
			selectedNode: node
		});
	}

	async handleOnRenameConfirmed(node, name)
	{
		this.setState({
			renameDialogVisible: false
		});

		this.props.ioManager.addToRename(node, name);
	}

	async handleOnFileRenamed(node, name)
	{
		if(node.origin === 'TREE')
		{
			await this.arvorePanel.handleOnFolderRenamed(node, name);
		}
		else
		{
			await this.listaPanel.handleOnFileRenamed(node, name);
		}
	}

	showCreateFolderDialog(parent)
	{
		this.setState({
			createFolderDialogVisible: true,
			selectedNode: parent
		});
	}

	async handleOnCreateFolderConfirmed(parent, name)
	{
		this.setState({
			createFolderDialogVisible: false
		});
		
		this.props.ioManager.addToCreateDir(parent, name);
	}

	async handleOnFolderCreated(parent, folder)
	{
		this.arvorePanel.handleOnFolderCreated(parent, folder);
	}

	async handleOnFileMoved(source, destine, newFileData)
	{
		let currentFolder = this.getCurrentFolder();
		
		// se o source é da lista atual, remover
		if(currentFolder.id === source.parent.id)
		{
			this.listaPanel.delNode(source);
		}

		// se o destino for a lista atual, adicionar o source
		if(currentFolder.id === destine.id)
		{
			if(this.listaPanel.getFiles().length < this.PAGE_SIZE - 1)
			{
				this.listaPanel.addNodes(destine, [newFileData]);
			}
			else
			{
				this.loadFiles(currentFolder, true);
			}
		}
		
		// se for um diretório, recarregar árvore
		if(source.type === 'DIRETORIO')
		{
			let treeNode = this.findNodeById(this.getFolders(), source.id);
			await this.loadFolders({...treeNode.parent}, true);
		}
	
		let treeNode = this.findNodeById(this.getFolders(), destine.id);
		await this.loadFolders({...treeNode}, true);
	}

	async moveFiles(source, destine)
	{
		let nodes = null;
		if(source.origin === 'TREE')
		{
			nodes = [source];
		}
		else
		{
			nodes = this.listaPanel.getSelectedFiles();
			if(!nodes.find(n => n.id === source.id))
			{
				nodes.push(source);
			}
		}
		
		await this.props.ioManager.addToMove(nodes, destine);
	}

	async handleOnFileCopied(source, destine, newFileData)
	{
		let currentFolder = this.getCurrentFolder();
		
		// se o destino for a lista atual, adicionar o source
		if(currentFolder.id === destine.id)
		{
			if(this.listaPanel.getFiles().length < this.PAGE_SIZE - 1)
			{
				this.listaPanel.addNodes(destine, [newFileData]);
			}
			else
			{
				this.loadFiles(currentFolder, true);
			}
		}
		
		let treeNode = this.findNodeById(this.getFolders(), destine.id);
		await this.loadFolders({...treeNode}, true);		
	}
	
	async downloadFiles(files)
	{
		await this.props.ioManager.addToDownload(files);
	}

	async uploadFiles(parent, files)
	{
		await this.props.ioManager.addToUpload(parent, files);
	}

	async handleOnFileUploaded(parent, file)
	{
		this.listaPanel.handleOnFileUploaded(parent, file);
	}

	async deleteFiles(files)
	{
		await this.props.ioManager.addToDelete(files);
	}

	async handleOnFileDeleted(file)
	{
		if(file.origin === 'TREE')
		{
			this.arvorePanel.handleOnFolderDeleted(file);
		}
		else
		{
			this.listaPanel.handleOnFileDeleted(file);
		}
	}

	updateFileListParent(parent)
	{
		this.listaPanel.updateParent(parent);
	}

	async setNodesToCopy(nodes, isCut)
	{
		this.showMessage(`${nodes.length} document(s) selecionado(s) para ${isCut? 'recortar': 'copiar'}!`);
		
		await this.setStateAsync({
			nodesToCopy: nodes,
			isCut: isCut
		});
	}

	getNodesToCopy()
	{
		return this.state.nodesToCopy;
	}

	async copyOrMoveFiles(destine)
	{
		if(this.state.nodesToCopy.length === 0)
		{
			return;
		}

		try
		{
			if(this.state.isCut)
			{
				await this.props.ioManager.addToMove(this.state.nodesToCopy, destine);
			}
			else
			{
				await this.props.ioManager.addToCopy(this.state.nodesToCopy, destine);
			}
		}
		finally
		{
			await this.setStateAsync({
				nodesToCopy: [],
				isCut: false
			});
		}
	}

	buildBreadCrumb(node)
	{
		if(!this.props.isDesktop)
		{
			return;
		}
		
		var res = [];
		while(node)
		{
			res.unshift({
				label: node.name,
				node: node,
				command: (event) => {this.setCurrentFolder(event.item.node); this.loadFiles(event.item.node);}
			});
			node = node.parent;
		}
		
		this.setState({
			breadCrumbHome: {...res[0], icon: 'pi pi-fw pi-sitemap'},
			breadCrumbItems: res.slice(1)
		});
	}

	handleOnSearchQueryChanged(searchQuery, searchAt)
	{
		this.setState({
			searchQuery: searchQuery,
			searchAt: searchAt
		});

		if(this.state.searchQuery !== searchQuery || this.state.searchAt !== searchAt)
		{
			this.listaPanel.handleOnSearchQueryChanged(searchQuery, searchAt);
		}
	}

	async handleOnChangeViewMode()
	{
		let listingMode = null;
		switch(this.state.listingMode)
		{
		case 'details':
			listingMode = 'icons';
			break;
		case 'icons':
			listingMode = 'icons-big';
			break;
		case 'icons-big':
			listingMode = 'details';
			break;
		default:
			break;
		}
		
		await this.setStateAsync({
			listingMode: listingMode
		});

		this.loadFiles(this.getCurrentFolder(), true);
	}
	
	handleOnResize()
	{
		this.arvorePanel.handleOnResize(); 
		this.listaPanel.handleOnResize(); 		
	}

	handleOnShowDetails(node)
	{
		this.props.container.handleOnShowDetails(node);
	}

	getRouterSearchValues(keysToExclude = [])
	{
		let values = [];
		let matches = this.props.history.location.search? this.props.history.location.search.match(/([a-zA-Z_\-0-9]+=[^&]+)/g): null;
		if(matches)
		{
			for(let i = 0; i < matches.length; i++)
			{
				let [key, value] = matches[i].split('=');
				if(keysToExclude.indexOf(key) < 0)
				{
					values[key] = value;
				}
			}
		}

		return values;
	}

	setRouterSearchValues(search)
	{
		let keys = Object.keys(search);
		let values = Object.values(search);
		
		this.props.history.push({
			pathname: this.props.history.location.pathname,
			hash: this.props.history.location.hash,
			search: keys.map((k,i) => `${k}=${values[i]}`).join('&')
		});
	}

	render()
	{
		return (
			<>
				<div className="box-file-container">
					<HotKeys keyMap={this.keyMap} handlers={this.keyHandlers} focused="true">
						<Row className="box-file-header">
							<Col xs="8">
								<Conditional 
									condition={this.props.isDesktop}
									component={BreadCrumb}
									model={this.state.breadCrumbItems} home={this.state.breadCrumbHome} />
							</Col>
							<Col xs="4">
								<div className="p-inputgroup float-right">
									<InputText id="searchQuery" type="text" placeholder="Pequisar" value={this.state.searchQuery} 
										onChange={(e) => this.handleOnSearchQueryChanged(e.target.value, this.state.searchAt)} />
									<span className="p-inputgroup-addon">
										<Dropdown value={this.state.searchAt} options={this.searchAtOptions} 
											onChange={(e) => this.handleOnSearchQueryChanged(this.state.searchQuery, e.value)} placeholder="Local"/>
									</span>
									<div className="float-right" style={{padding:2}}>
										<Button icon={`pi pi-${this.listingViewButtonIcon[this.state.listingMode]}`} onClick={this.handleOnChangeViewMode} />
									</div>
								</div>
							</Col>
						</Row>
						<Row className="box-file">
							<Conditional 
								condition={this.props.isDesktop}
								component={Split}
								sizes={[15, 85]} 
								minSize={[200, 500]} 
								gutterSize={3} 
								onDragEnd={()=> this.handleOnResize()}>
								<div className="box-file-tree" tabIndex={-1} ref={el => this.arvorePanelParent = el} style={{display: this.props.isDesktop? 'block': 'none'}}>
									<ArvorePanel
										ref={el => this.arvorePanel = el}
										api={this.props.api}
										history={this.props.history}
										navegador={this}
										nodesToCopy={this.state.nodesToCopy}
										doRender={this.props.isDesktop} />
								</div>
								<div className="box-file-list" tabIndex={-1} ref={el => this.listaPanelParent = el}>
									<DropFileZone onDropAccepted={(files) => this.listaPanel.handleOnDropFile(files)}>
										<ListaPanel
											ref={el => this.listaPanel = el}
											api={this.props.api}
											history={this.props.history}
											navegador={this}
											listingMode={this.state.listingMode}
											nodesToCopy={this.state.nodesToCopy}
											isDesktop={this.props.isDesktop} />
									</DropFileZone>
								</div>
							</Conditional>
						</Row>
					</HotKeys>
				</div>
				
				<ConfirmDialog
					visible={this.state.userConfirmationVisible}
					text={this.state.userConfirmationText}
					onConfirmed={this.handleOnUserConfirmed}
					onHide={() => {this.setState({userConfirmationVisible: false}); }} 
				/>
				
				<DialogRenomear
					visible={this.state.renameDialogVisible}
					node={this.state.selectedNode}
					onConfirmed={this.handleOnRenameConfirmed}
					onCancelled={() => this.setState({renameDialogVisible: false})}
					key={'dr' + (this.state.selectedNode && this.state.selectedNode.id)}
				/>

				<DialogCriarDiretorio
					visible={this.state.createFolderDialogVisible}
					node={this.state.selectedNode}
					onConfirmed={this.handleOnCreateFolderConfirmed}
					onCancelled={() => this.setState({createFolderDialogVisible: false})}
					key={'dcd' + (this.state.selectedNode && this.state.selectedNode.id)} />
			</>
		);
	}
}