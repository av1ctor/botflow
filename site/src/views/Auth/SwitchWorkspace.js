import React, {useContext, useEffect, useState} from 'react';
import {useLocation} from 'react-router-dom';
import PropTypes from 'prop-types';
import Misc from '../../libs/Misc';
import {SessionContext} from '../../contexts/SessionContext';

const SwitchWorkspace = ({container}) =>
{
	const location = useLocation();

	const sessionStore = useContext(SessionContext);

	const [errors, setErrors] = useState([]);

	useEffect(() =>
	{
		const params = Misc.getParams(location.search);
		const token = params['token'];
		const expiration = params['expiration'];
		const rememberMe = params['remember'];
		const idWorkspace = params['workspace'];
		const idUser = params['user'];

		doSwitch(token, expiration, rememberMe, idWorkspace, idUser);
	}, []);

	const doSwitch = async (token, expiration, rememberMe, idWorkspace, idUser) =>
	{
		const res = await sessionStore.switchWorkspace(
			token, expiration, rememberMe, idWorkspace, idUser);
		if(res.errors !== null)
		{
			setErrors(res.errors);
		}
		else
		{
			container.redirect('');
		}
	};

	const renderErrors = () =>
	{
		if(errors && errors.length > 0)
		{
			return (
				'😢'
			);
		}
	};

	return (
		<div>
			Carregando área de trabalho... {renderErrors()}
		</div>
	);
};

SwitchWorkspace.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
};

export default SwitchWorkspace;