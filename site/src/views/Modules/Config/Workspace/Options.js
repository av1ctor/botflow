import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';

export default class Options extends PureComponent
{
	static propTypes = 
	{
		api: PropTypes.object.isRequired,
		history: PropTypes.object,
		container: PropTypes.object.isRequired
	};

	constructor(props)
	{
		super(props);
	}

	componentDidMount()
	{
	}

	render()
	{
		return (
			<div className="rf-page-contents">
				<div className="rf-page-header pt-2">
					<div>
						<div className="rf-page-title-icon">
							<i className="la la-money-bill" />
						</div>
						<div className="rf-page-title-other">
							<div className="row">
								<div className="col-auto rf-page-title-container">
									<div className="rf-page-title">
										Configurações / Área de trabalho / Opções
									</div>
								</div>
							</div>
							<div className="row">
								<div className="col">
									<small>Opções desta área de trabalho</small>
								</div>
							</div>
						</div>
					</div>
				</div>

			</div>
		);
	}
}
