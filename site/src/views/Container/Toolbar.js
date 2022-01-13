import React, { useCallback } from 'react';
import PropTypes from 'prop-types';

const Toolbar = (props) =>
{
	const handleLogout = useCallback(() =>
	{
		props.session.logoutUser();
	}, []);
	
	return (
		<div className="rf-menu-toolbar">
			<div 
				className="rf-menu-toolbar-button"
				title="Logout"
				onClick={handleLogout}>
				<i className="la la-sign-out-alt" />
			</div>
		</div>
	);
};

Toolbar.propTypes = {
	session: PropTypes.object,
	container: PropTypes.object,
};

export default Toolbar;