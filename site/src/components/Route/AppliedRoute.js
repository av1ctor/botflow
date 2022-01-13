import React from 'react';
import { Route } from 'react-router-dom';
import PropTypes from 'prop-types';

const AppliedRoute = ({ component: C, props, ...rest }) =>
{
	return (
		<Route {...rest}
			render={props2 => 
				<C 
					{...props2}
					{...props} 
				/>
			}
		/>
	);
};

AppliedRoute.propTypes = {
	component: PropTypes.any,
	props: PropTypes.object,
};

export default AppliedRoute;