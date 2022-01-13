import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {TabView, TabPanel} from 'primereact/tabview';
import FocusLock from 'react-focus-lock';
import DocumentoDetalheWorkflows from './DocumentoDetalheWorkflows';
import SidebarResizeable from '../../../components/SidebarResizeable';

export default class DocumentoDetalhe extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		container: PropTypes.object,
		fullScreen: PropTypes.bool
	};

	constructor(props)
	{
		super(props);

		this.state = {
			visible: false,
			file: null,
			activeIndex: 0
		};

		this.show = this.show.bind(this);
	}

	show(file)
	{
		this.setState({
			file: file,
			visible: true,
			activeIndex: 0
		});
	}

	render()
	{
		if(!this.state.file)
		{
			return null;
		}

		let file = this.state.file;

		let fileTabs = () => 
			<TabView activeIndex={this.state.activeIndex} onTabChange={(e) => this.setState({activeIndex: e.index})}>
				<TabPanel header="Geral">

				</TabPanel>
							
				<TabPanel header="Prévia" contentClassName="box-file-details-preview-tab">
					<div className="box-file-details-preview-container">
						<img src={file.previewUrl}/>
					</div>
				</TabPanel>

				<TabPanel header="Segurança">
					
				</TabPanel>

				<TabPanel header="Detalhes">
					
				</TabPanel>					

				<TabPanel header="Versões anteriores">
					
				</TabPanel>					
			</TabView>;

		let dirTabs = () => 
			<TabView renderActiveOnly={false} activeIndex={this.state.activeIndex} onTabChange={(e) => this.setState({activeIndex: e.index})}>
				<TabPanel header="Geral">

				</TabPanel>
							
				<TabPanel header="Segurança">
					
				</TabPanel>				

				<TabPanel header="Detalhes">
					
				</TabPanel>					
				
				<TabPanel header="Workflows">
					<DocumentoDetalheWorkflows 
						api={this.props.api}
						container={this.props.container}
						file={this.state.file}
					/>
				</TabPanel>					
			</TabView>;

		return (
			<SidebarResizeable visible={this.state.visible} position="bottom" onHide={() => this.setState({visible: false})} fullScreen={this.props.fullScreen}>
				<div>Propriedades de <b>{file.name}</b></div>
				<div>
					<FocusLock disabled={!this.state.visible} returnFocus>
						{file.type === 'DIRETORIO'? dirTabs(): fileTabs()}
					</FocusLock>
				</div>
			</SidebarResizeable>
		);
	}
}