import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Tree} from 'primereact/tree';
import {ContextMenu} from 'primereact/contextmenu';
import {HotKeys} from 'react-hotkeys';
import {Scrollbars} from 'react-custom-scrollbars';
import ArvoreItem from './ArvoreItem';

export default class ArvorePanel extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		history: PropTypes.object,
		navegador: PropTypes.object,
		nodesToCopy: PropTypes.array,
		doRender: PropTypes.bool
	};

	constructor(props)
	{
		super(props);

		const repo = this.props.api.getWorkspace();

		this.state = {
			workspace: repo,
			folders: null,
			currentFolder: null,
			expandedKeys: {},
			selectedNodeKey: null,
			selectedFolderId: null,
			selectedFolder: null
		};

		this.root = {
			type: 'DIRETORIO',
			icon: 'pi pi-fw pi-folder-open',
			key: repo.idRootDoc,
			id: repo.idRootDoc,
			label: repo.name,
			name: repo.name,
			leaf: false,
			defaultExpanded: true,
			parent: null,
			origin: 'TREE'
		};

		this.keyMap = {
			'delete': 'del',
			'rename': 'f2',
			'copy'	: 'mod+c',
			'cut'	: 'mod+x',
			'paste'	: 'mod+v'
		};

		this.keyHandlers = {
			'delete': (event) => {
				this.state.selectedFolderId && this.handleOnDeleteFolder();
			},
			'rename': (event) => this.handleOnRenameFolder(),
			'copy'	: () => this.handleOnCopyFolder(),
			'cut'	: () => this.handleOnCutFolder(),
			'paste'	: () => this.handleOnPasteFolder()
		};


		this.handleOnExpand = this.handleOnExpand.bind(this);
		this.handleOnCollapse = this.handleOnCollapse.bind(this);
		this.handleOnSelect = this.handleOnSelect.bind(this);
		this.handleOnDeleteFolder = this.handleOnDeleteFolder.bind(this);
		this.handleOnRenameFolder = this.handleOnRenameFolder.bind(this);
		this.handleOnFolderRenamed = this.handleOnFolderRenamed.bind(this);
		this.handleOnCreateFolder = this.handleOnCreateFolder.bind(this);
		this.handleOnFolderCreated = this.handleOnFolderCreated.bind(this);
		this.handleOnMoveFolder = this.handleOnMoveFolder.bind(this);
		this.handleOnResize = this.handleOnResize.bind(this);
		this.deleteFolder = this.deleteFolder.bind(this);
		this.nodeTemplate = this.nodeTemplate.bind(this);
	}

	getCurrentFolder()
	{
		return this.state.currentFolder;
	}

	getFolders()
	{
		return this.state.folders;
	}

	getRoot()
	{
		return this.root;
	}

	assignNode(parent, nodes, node)
	{
		let atChildren = null;
		nodes.forEach((n, index) => 
		{
			if(n.id === node.id)
			{
				n.parent = parent;
				nodes[index] = node;
				atChildren = nodes;
				return;
			}
		});

		if(atChildren)
		{
			return atChildren;
		}

		nodes.forEach((n) => 
		{
			if(n.children && n.children.length > 0)
			{
				atChildren = this.assignNode(n, n.children, node);
				if(atChildren)
				{
					if(atChildren === n.children)
					{
						n.children = this.sortNodes(n.children);
					}
					return;
				}
			}
		});

		return atChildren;
	}

	updateNode(node, sort = false)
	{
		let clone = [...this.state.folders];
		let atChildren = this.assignNode(null, clone, node);
		if(sort && atChildren === clone)
		{
			clone = this.sortNodes(clone);
		}
		this.setState({
			folders: clone
		});
	}

	fixUpChildrenNodes(node)
	{
		if(node.children && node.children.length > 0)
		{
			node.children.forEach((n,i) =>
			{
				node.children[i].parent = node;
			});
		}
	}

	sortNodes(nodes)
	{
		return nodes.sort((a, b) => a.name.toLowerCase() < b.name.toLowerCase()? -1: (a.name.toLowerCase() > b.name.toLowerCase()? 1: 0));
	}

	async loadFolders(node, force = false) 
	{
		// carregar os diretórios filhos deste nó, se não tiverem sido carregados ainda
		if ((!node.children && !node.childrenLoaded) || force) 
		{
			let res = await this.props.api.get(`docs/${node.id}/filhos?page=0&size=0&sort=name,asc`);
			if (res.errors == null) 
			{
				// carregando pela primeira vez?
				if(!node.children)
				{
					node.children = this.transformFolders(node, res.data);
					node.childrenLoaded = true;
				}
				// já existem filhos, só atualizar
				else
				{
					let doSorting = false;
					
					// para cada diretório filho existente...
					for(let i = 0; i < node.children.length;)
					{
						let c = node.children[i];
						let n = res.data.find((e) => e.id === c.id);
						// diretório filho foi removido?
						if(!n)
						{
							node.children.splice(i, 1);
						}
						// só atualize o que for importante e mantenha o nó
						else
						{
							let clone = {...c};
							clone.name = clone.label = n.name;
							node.children[i] = clone;
							this.fixUpChildrenNodes(clone);
							if(clone.id === this.state.currentFolder)
							{
								this.props.navegador.updateFileListParent(clone);
							}
							i++;
							doSorting = true;
						}
					}
					
					// para cada diretório retornado pela API...
					res.data.forEach(n => {
						let c = node.children.find((e) => e.id === n.id);
						// novo diretório filho?
						if(!c)
						{
							node.children.push(this.transformFolder(node, {...n}));
							doSorting = true;
						}
					});

					// reordenar se necessário
					if(doSorting)
					{
						node.children = this.sortNodes(node.children);
					}
				}
			}
		}
		
		// mudar ícone para pasta aberta
		node.leaf = !node.children || node.children.length === 0;
		node.icon = 'pi pi-fw pi-folder' + (node.leaf ? '' : '-open');
		node.defaultExpanded = true;
		
		let clone = [...this.state.folders];
		this.assignNode(null, clone, node);
		this.setState({
			folders: clone
		});

		return node;
	}

	transformFolders(parent, nodes)
	{
		return nodes.map((n) => 
		{
			return this.transformFolder(parent, n);
		});
	}

	transformFolder(parent, n) 
	{
		n.parent = parent;
		n.icon = 'pi pi-fw pi-folder';
		n.key = n.id;
		n.label = n.name;
		if (n.children && n.children.length > 0) 
		{
			n.children = this.transformFolders(n, n.children);
			n.childrenLoaded = true;
		}
		else 
		{
			n.children = null;
			n.childrenLoaded = false;
		}
		n.leaf = n.type !== 'DIRETORIO';
		n.origin = 'TREE';

		return n;
	}

	buildTree(nodes)
	{
		this.root.children = this.transformFolders(this.root, nodes);

		return [this.root];
	}

	getFolderFullIdPath(folder)
	{
		var res = '';
		while(folder)
		{
			res = '/' + folder.id + res;
			folder = folder.parent;
		}

		return res;
	}

	getFolderFullNamePath(folder)
	{
		var res = '';
		while(folder)
		{
			res = '/' + folder.name + res;
			folder = folder.parent;
		}

		return res;
	}

	updateRouterLocation(folder)
	{
		let values = this.props.navegador.getRouterSearchValues(['idsPath', 'namesPath']);

		values['idsPath'] = this.getFolderFullIdPath(folder);
		//values['namesPath'] = this.getFolderFullNamePath(folder);
		
		this.props.navegador.setRouterSearchValues(values);
	}

	setCurrentFolder(folder)
	{
		// avisar ao Tree que este nó, e de todos seus ancestrais, da árvore de diretórios foi aberto
		let expandedKeys = {...this.state.expandedKeys};
		let n = folder;
		while(n)
		{
			expandedKeys[n.id] = true;
			n = n.parent;
		}
				
		this.setState({
			currentFolder: folder,
			expandedKeys: expandedKeys,
			selectedNodeKey: folder.id
		});

		this.props.navegador.buildBreadCrumb(folder);

		this.updateRouterLocation(folder);
	}

	async componentDidMount()
	{
		this._isMounted = true;

		// carregar diretórios filhos da raiz para montar a árvore de diretórios
		const res = await this.props.api.get(`docs/${this.state.workspace.idRootDoc}/filhos?page=0&size=0&sort=name,asc`);
		
		if(res.errors == null)
		{
			let folders = this.buildTree(res.data);
			let root = folders[0];
			let expandedKeys = {};
			expandedKeys[root.id] = true;
			if(this._isMounted)
			{
				this.setState({
					currentFolder: root,
					folders: folders,
					expandedKeys: expandedKeys,
					selectedNodeKey: root.id
				});

				let search = this.props.navegador.getRouterSearchValues();
				let ids = search['idsPath']? search['idsPath'].split('/').slice(1): null;
				if(ids)
				{
					for(let i = 1; i < ids.length; i++)
					{
						let child = root.children && root.children.find(c => c.id === ids[i]);
						if(!child)
						{
							break;
						}
						await this.loadFolders(child);
						expandedKeys[root.id] = true;
						root = child;
					}

					this.setState({
						currentFolder: root,
						expandedKeys: expandedKeys,
						selectedNodeKey: root.id
					});
				}

				this.props.navegador.buildBreadCrumb(root);
			}
		}
	}

	componentWillUnmount() 
	{
		this._isMounted = false;
	}

	removeById(nodes, id)
	{
		var res = nodes.filter(n => n.id !== id);
		if(res.length < nodes.length)
			return res;

		res.forEach(n => 
		{
			if(n.children)
			{
				var children = this.removeById(n.children, id);
				if(children.length < n.children.length)
				{
					n.children = children;
					return;
				}
			}
		});

		return res;
	}

	async deleteFolder(node)
	{
		await this.props.navegador.deleteFiles([node]);
	}

	async handleOnDeleteFolder()
	{
		let node = this.props.navegador.findNodeById(this.state.folders, this.state.selectedFolderId);
		this.props.navegador.showUserConfirmation(this.deleteFolder, node, `Você tem certeza que deseja excluir o diretório "${node.name}" e todo seu conteúdo?`);
	}

	async handleOnFolderDeleted(node)
	{
		let folders = this.removeById(this.state.folders, node.id);
		this.setState({
			folders: folders
		});

		// se foi removido a pasta atualmente exibida, devemos voltar um nível
		//FIXME: é necessário verificar se qualquer ascendente do nó atual foi removido
		if(this.state.currentFolder.id === node.id)
		{
			this.setState({
				currentFolder: node.parent
			});
			this.props.navegador.loadFiles(node.parent, true);
			this.props.navegador.buildBreadCrumb(node.parent);
		}
	}

	async handleOnExpand(event) 
	{
		await this.loadFolders({...event.node});
	}

	async handleOnCollapse(event) 
	{
		let node = {...event.node};
		node.icon = 'pi pi-fw pi-folder';
		this.updateNode(node);
	}

	async handleOnSelect(event)
	{
		let node = await this.loadFolders({...event.node});
		await this.props.navegador.loadFiles(node);
	}

	async handleOnFolderRenamed(node, name)
	{
		node.name = node.label = name;
		this.updateNode(node, true);
		if(this.state.currentFolder.id === node.parent.id)
		{
			await this.props.navegador.loadFiles(node.parent, true);
		}
		this.props.navegador.buildBreadCrumb(this.state.currentFolder);
	}
	
	async handleOnRenameFolder()
	{
		let node = this.props.navegador.findNodeById(this.state.folders, this.state.selectedFolderId);
		this.props.navegador.showRenameDialog(node);
	}

	async handleOnFolderCreated(parent, node)
	{
		parent = this.props.navegador.findNodeById(this.state.folders, parent.id);
		if(parent)
		{
			parent = await this.loadFolders({...parent}, true);
			if(parent.id === this.state.currentFolder.id)
			{
				await this.props.navegador.loadFiles(parent, true);
			}
		}
	}
	
	async handleOnCreateFolder()
	{
		let parent = this.props.navegador.findNodeById(this.state.folders, this.state.selectedFolderId);
		this.props.navegador.showCreateFolderDialog(parent);
	}

	async handleOnMoveFolder(source, destine)
	{
		await this.props.navegador.moveFiles(source, destine);
	}

	handleOnFocus()
	{
		/*var folder = this.state.currentFolder;
		this.setState({
			selectedNodeKey: folder.id, 
			selectedFolderId: folder.id
		});*/
	}

	handleOnBlur()
	{
	}

	async handleOnCopyFolder()
	{
		await this.props.navegador.setNodesToCopy([this.state.selectedFolder], false);
	}

	async handleOnCutFolder()
	{
		await this.props.navegador.setNodesToCopy([this.state.selectedFolder], true);
	}

	async handleOnPasteFolder()
	{
		await this.props.navegador.copyOrMoveFiles(this.state.selectedFolder);
	}

	handleOnResize()
	{
		if(this.props.doRender) 
		{
			this.scrollPanel.update();
		}
	}

	async handleOnDownloadFolder()
	{
		await this.props.navegador.downloadFiles([this.state.selectedFolder]);
	}

	handleOnShowProprieties()
	{
		this.props.navegador.handleOnShowDetails(this.state.selectedFolder);
	}

	buildContextMenu()
	{
		return [
			{
				label: 'Copiar',
				icon: 'pi pi-clone',
				command: async () => 
				{
					await this.handleOnCopyFolder();	
				}
			},
			{
				label: 'Recortar',
				icon: 'pi pi-times',
				command: async () => 
				{
					await this.handleOnCutFolder();
				}
			},
			{
				label: 'Colar',
				icon: 'pi pi-check-circle',
				disabled: this.props.nodesToCopy.length === 0,
				command: async () => 
				{
					await this.handleOnPasteFolder();
				}
			},
			{
				label: 'Renomear',
				icon: 'pi pi-pencil',
				command: async () => 
				{
					await this.handleOnRenameFolder();
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
					await this.handleOnCreateFolder();
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
					await this.handleOnDownloadFolder();
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
					await this.handleOnDeleteFolder();
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
					await this.handleOnShowProprieties();
				}
			}
		];
	}

	nodeTemplate(node)
	{
		return (
			<ArvoreItem node={node} onMoveFolder={this.handleOnMoveFolder} />
		);
	}

	render()
	{
		if(!this.props.doRender)
		{
			return null;
		}

		return (
			<>
				<ContextMenu 
					model={this.buildContextMenu()} 
					ref={el => this.treeContextMenu = el}
					onHide={() => this.setState({selectedNodeKey: null}) }/>

				<HotKeys keyMap={this.keyMap} handlers={this.keyHandlers}>
					<Scrollbars className="full-height" ref={el => this.scrollPanel = el}>
						<Tree 
							selectionMode="single"
							value={this.state.folders}
							onExpand={this.handleOnExpand} 
							onCollapse={this.handleOnCollapse} 
							onToggle={e => this.setState({expandedKeys: e.value})}
							onSelect={(event) =>
							{	
								this.setState({
									selectedFolder: event.node, 
									selectedNodeKey: event.node.id, 
									selectedFolderId: event.node.id}); 
								this.handleOnSelect(event);
							}}
							expandedKeys={this.state.expandedKeys}
							contextMenuSelectionKey={this.state.selectedNodeKey} 
							onContextMenuSelectionChange={event => this.setState({selectedNodeKey: event.value, selectedFolderId: event.value})} 
							onContextMenu={event => {this.setState({selectedFolder: event.node}); this.treeContextMenu.show(event.originalEvent);}}
							nodeTemplate={this.nodeTemplate} />
					</Scrollbars>
				</HotKeys>
			</>
		);
	}
}