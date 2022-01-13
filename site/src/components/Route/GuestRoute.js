import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

function querystring(name, url = window.location.href)
{
	name = name.replace(/[[]]/g, '\\$&');

	const regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)', 'i');
	const results = regex.exec(url);

	if (!results)
	{
		return null;
	}
	if (!results[2])
	{
		return '';
	}

	return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

const GuestRoute = ({isAuthenticated, component: Component, props, ...rest}) =>
{
	const redirect = querystring('redirect');
	return (
		<Route
			{...rest}
			render = {
				props2 =>
					!isAuthenticated? 
						<Component 
							{...props2} 
							{...props} 
						/>: 
						<Redirect
							to={redirect === '' || redirect === null ? '/' : redirect}
						/>
			}
		/>
	);
};

GuestRoute.propTypes = {
	isAuthenticated: PropTypes.bool.isRequired,
	component: PropTypes.any,
	props: PropTypes.object,
};

export default GuestRoute;