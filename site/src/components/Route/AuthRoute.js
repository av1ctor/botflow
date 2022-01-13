import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

const AuthRoute = ({isAuthenticated, component: Component, props, param, ...rest}) =>
{
	return (
		<Route
			{...rest}
			render={
				routeProps =>
					isAuthenticated? 
						<Component 
							key={param && routeProps.match && routeProps.match.params? 
								routeProps.match.params[param]: 
								undefined} 
							{...routeProps} 
							{...props} 
						/>: 
						<Redirect 
							to={`/auth/login?redirect=${routeProps.location.pathname}${routeProps.location.search}`} 
						/>
			}
		/>
	);
};

AuthRoute.propTypes = {
	isAuthenticated: PropTypes.bool.isRequired,
	component: PropTypes.any,
	props: PropTypes.object,
	param: PropTypes.string,
};

export default AuthRoute;