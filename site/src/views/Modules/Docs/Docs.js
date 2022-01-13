import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {withRouter} from 'react-router-dom';
import {SizeMe} from 'react-sizeme';
import {HotKeys} from 'react-hotkeys';
import Navegador from './Navegador';
import IoManager from './IoManager';
import DocumentoDetalhe from './DocumentoDetalhe';

import './Docs.css';

class Arquivos extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object,
		history: PropTypes.object,
		isMobile: PropTypes.bool,
		container: PropTypes.object
	};

	constructor(props)
	{
		super(props);

		this.severityToSummaryMap = {
			'info': 'Informação',
			'success': 'Sucesso!',
			'error': 'Erro :/',
		};

		this.keyMap = {
			'change': 'shift+tab'
		};

		this.keyHandlers = {
			'change': (event) => {
			},
		};

		this.isMobile = props.isMobile;
	}

	async handleOnFileUploaded(parent, file)
	{
		await this.navegador.handleOnFileUploaded(parent, file);
	}

	async handleOnFileDeleted(file)
	{
		await this.navegador.handleOnFileDeleted(file);
	}

	async handleOnFileCopied(source, destine, newFileData)
	{
		await this.navegador.handleOnFileCopied(source, destine, newFileData);
	}

	async handleOnFileMoved(source, destine, newFileData)
	{
		await this.navegador.handleOnFileMoved(source, destine, newFileData);
	}

	async handleOnFolderCreated(parent, folder)
	{
		await this.navegador.handleOnFolderCreated(parent, folder);
	}

	async handleOnFileRenamed(file, name)
	{
		await this.navegador.handleOnFileRenamed(file, name);
	}

	handleOnShowDetails(file)
	{
		this.documentoDetalhe.show(file);
	}

	showMessage(msg, severity = 'info')
	{
		this.props.container.showMessage(msg, severity);
	}

	render()
	{
		return (
			<HotKeys keyMap={this.keyMap} handlers={this.keyHandlers} focused="true">
				<div className="box-file-root">
					<SizeMe>
						{({ size }) => 
							<div className="box-file-main" style={{height: size.height}}>
								<div tabIndex={-1}>
									<Navegador
										ref={el => this.navegador = el}
										container={this}
										api={this.props.api}
										history={this.props.history}
										ioManager={this.ioManager}
										height={size.height}
										isDesktop={size.width >= 1024} />
								</div>
							</div>
						}
					</SizeMe>

					<IoManager 
						ref={(el) => this.ioManager = el} 
						container={this}
						api={this.props.api} />

					<div className="box-file-details-container">
						<DocumentoDetalhe
							ref={(el) => this.documentoDetalhe = el} 
							container={this}
							api={this.props.api}
							fullScreen={this.isMobile} />
					</div>
				</div>
			</HotKeys>
		);
	}
}

export default withRouter(Arquivos);