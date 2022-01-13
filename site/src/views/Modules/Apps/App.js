import React, { useCallback, useContext, useEffect, useState } from 'react';
import {useParams} from 'react-router';
import PropTypes from 'prop-types';
import { SessionContext } from '../../../contexts/SessionContext';
import Bar from './Bar';
import Screen from './Screen';

import './App.css';

const transform = (app) =>
{
	return {
		...app,
		schemaObj: JSON.parse(app.schema)
	};
};

const App = (props) =>
{
	const session = useContext(SessionContext);

	const params = useParams();
	const [errors, setErrors] = useState(null);
	const [app, setApp] = useState(null);
	const [screen, setScreen] = useState(null);

	const loadApp = async () =>
	{
		const res = await props.api.get(
			`apps/${params.id}`);
		setErrors(res.errors);
		if(!res.errors)
		{
			const app = transform(res.data);
			setApp(app);

			setScreen( 
				(params.screenId?
					app.schemaObj.screens.find(s => s.id === params.screenId):
					app.schemaObj.screens.find(s => s.index === 0)) || 
				app.schemaObj.screens[0]);
		}
	};

	useEffect(() => 
	{
		loadApp();
	}, []);

	const handleOnClick = (elm) =>
	{
		if(elm.onClick)
		{
			switch(elm.onClick.action)
			{
			case 'show':
				setScreen(app.schemaObj.screens.find(s => s.id === elm.onClick.target));
				break;
			
			case 'logout':
				session.logoutUser();
				break;
			}
		}
	};

	const handleClick = useCallback((elm, e) => 
	{
		e.stopPropagation();
		handleOnClick(elm);
	}, [app, screen]);

	const buildMenu = useCallback((elm) =>
	{
		const hideMenu = () => props.container.hidePopupMenu();
		
		return elm.elements.map((item) => ({
			label: item.title,
			icon: `la la-${item.icon}`,
			command: () =>
			{
				hideMenu();
				handleOnClick(item);
			}
		}));
	}, [app]);

	const handleShowMenu = useCallback((elm, e) =>
	{
		props.container.showPopupMenu(e, buildMenu(elm));
	}, [app]);
	
	const renderErrors = (errors) =>
	{
		return errors.map((err, index) => 
			<div key={index}>{JSON.stringify(err)}</div>);
	};

	if(!app || !screen)
	{
		return null;
	}

	const bars = (app.schemaObj.bars || [])
		.reduce((obj, bar) => ({...obj, [bar.type]: bar}), {});
	
	return (
		<div className="rf-app-container">
			{bars.top && 
				<Bar
					container={props.container}
					app={app}
					bar={bars.top}
					onClick={handleClick}
					onShowMenu={handleShowMenu}
				/>
			}
			<Screen
				api={props.api}
				container={props.container}
				app={app}
				screen={screen}
			/>
			{bars.bottom && 
				<Bar
					container={props.container}
					app={app}
					bar={bars.bottom}
					onClick={handleClick}
					onShowMenu={handleShowMenu}
				/>
			}
			{renderErrors(errors || [])}
		</div>
	);
};

App.propTypes = {
	api: PropTypes.object,
	container: PropTypes.object,
};

export default App;