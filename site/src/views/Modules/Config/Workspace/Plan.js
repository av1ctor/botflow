import React from 'react';
import PropTypes from 'prop-types';

const Plan = (props) =>
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
									Configurações / Área de trabalho / Plano
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>Opções do plano desta área de trabalho</small>
							</div>
						</div>
					</div>
				</div>
			</div>

		</div>
	);
};

Plan.propTypes = 
{
	api: PropTypes.object.isRequired,
	history: PropTypes.object,
	container: PropTypes.object.isRequired
};

export default Plan;
