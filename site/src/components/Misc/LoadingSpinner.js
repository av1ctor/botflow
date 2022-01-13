import React from 'react';
import PropTypes from 'prop-types';

import './LoadingSpinner.css';

const LoadingSpinner = ({visible}) =>
{
	const klass = visible? 
		'cssload-container-visible': 
		'cssload-container-hidden';

	return (
		<div className={`cssload-container ${klass}`} >
			<div className="cssload-loading">
				<i></i><i></i><i></i><i></i>
			</div>
		</div>			
	);
};

LoadingSpinner.propTypes = 
{
	visible: PropTypes.bool.isRequired
};

export default LoadingSpinner;