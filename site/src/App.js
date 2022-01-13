import React, {useCallback, useContext, useRef, useState} from 'react';
import {Route, Switch, useHistory} from 'react-router-dom';
import {observer} from 'mobx-react-lite';
import {ContextMenu} from 'primereact/contextmenu';
import {TieredMenu} from 'primereact/tieredmenu';
import {Growl} from 'primereact/growl';

import AuthRoute from './components/Route/AuthRoute';
import GuestRoute from './components/Route/GuestRoute';
import AppliedRoute from './components/Route/AppliedRoute';
import LoadingSpinner from './components/Misc/LoadingSpinner';

import Main from './views/Container/Main';
import Form from './views/Modules/Collections/Forms/Viewer/Page';
import App_ from './views/Modules/Apps/App';
import Page404 from './views/Errors/Page404';
import {Login, Signup, SwitchWorkspace} from './views/Auth';
import OAuthPrivacyPolicy from './views/OAuth/Policies/PrivacyPolicy';

import {SessionContext} from './contexts/SessionContext';

const severityToSummaryMap = {
	'info': 'Information',
	'success': 'Success!',
	'error': 'Error :/',
};

const App = observer(props =>
{
	const history = useHistory();
	const sessionStore = useContext(SessionContext);

	const [spinnerVisible, setSpinnerVisible] = useState(false);
	const [contextMenuModel, setContextMenuModel] = useState(null);
	const [popupMenuModel, setPopupMenuModel] = useState(null);
	
	const contextMenuElm = useRef();
	const popupMenuElm = useRef();
	const growlElm = useRef();

	const redirect = (path) =>
	{
		history.push(path);
	};

	const isMobile = () =>
	{
		return props.isMobile;
	};

	const showMessage = (msg, severity = 'info', life = 5000) =>
	{
		if(!msg)
		{
			return;
		}

		const detail = msg.constructor !== Array? 
			(msg.constructor !== Object? 
				msg: 
				JSON.stringify(msg)): 
			msg.reduce((p,c,index) => p + `${1+index}: ${(c.constructor !== Object? c: JSON.stringify(c))}.\r\n`, '');
		
		growlElm.current && 
			growlElm.current.show({
				severity: severity, 
				summary: severityToSummaryMap[severity], 
				detail: <pre className="rf-growl-detail">{detail}</pre>, 
				life: life * (severity==='error'? 3: 1)
			});
	};

	const showSpinner = () =>
	{
		setSpinnerVisible(true);
	};

	const hideSpinner = () =>
	{
		setSpinnerVisible(false);
	};

	const showContextMenu = useCallback((event, menu) =>
	{
		setContextMenuModel(menu);
		
		popupMenuElm.current.hide();
		contextMenuElm.current.show(event);
	}, []);

	const showPopupMenu = useCallback((event, menu) =>
	{
		setPopupMenuModel(menu);
		
		contextMenuElm.current.hide();
		popupMenuElm.current.show(event);
	}, []);

	const hidePopupMenu = useCallback(() =>
	{
		setPopupMenuModel(null);
		
		popupMenuElm.current.hide();
	}, []);

	const handleHidePopupMenu = useCallback(() =>
	{
		if(popupMenuModel)
		{
			setPopupMenuModel(null);
		}	
	}, [popupMenuModel]);

	const handleHideContextMenu = useCallback(() =>
	{
		if(contextMenuModel)
		{
			setContextMenuModel(null);
		}	
	}, [contextMenuModel]);

	const container = useRef({});
	Object.assign(container.current, {	
		redirect,
		isMobile,
		showMessage,
		showSpinner,
		hideSpinner,
		showContextMenu,
		showPopupMenu,
		hidePopupMenu,
	});

	const isAuthenticated = sessionStore.isAuthenticated;

	const childProps =
	{
		api: props.api,
		history,
		isAuthenticated,
		container: container.current,
	};

	return (
		<>
			<Switch>
				<GuestRoute 
					exact 
					path="/auth/login" 
					component={Login} 
					props={childProps} 
					isAuthenticated={isAuthenticated}
				/>
				<GuestRoute 
					exact 
					path="/auth/signup" 
					component={Signup} 
					props={childProps} 
					isAuthenticated={isAuthenticated}
				/>
				<GuestRoute 
					exact 
					path="/auth/switch" 
					component={SwitchWorkspace} 
					props={childProps} 
					isAuthenticated={isAuthenticated}
				/>
				<AppliedRoute 
					exact 
					path="/oauth/policy" 
					component={OAuthPrivacyPolicy} 
					props={childProps} 
				/>
				<AppliedRoute 
					path="/f/:colId/:id"
					component={Form} 
					props={childProps} 
				/>
				<AuthRoute 
					path="/a/:id/:screenId?"
					component={App_} 
					props={childProps} 
					isAuthenticated={isAuthenticated}
				/>
				<AuthRoute 
					path="/" 
					component={Main} 
					props={childProps}
					isAuthenticated={isAuthenticated}
				/>
				<Route 
					component={Page404} 
					props={childProps} 
				/>
			</Switch>
			
			<LoadingSpinner
				visible={spinnerVisible} />

			<Growl 
				ref={growlElm}
				style={{zIndex: 10001}} 
			/>

			<ContextMenu
				model={contextMenuModel}
				ref={contextMenuElm}
				onHide={handleHideContextMenu}
				appendTo={document.body} 
			/>

			<TieredMenu
				model={popupMenuModel}
				ref={popupMenuElm}
				popup={true}
				onHide={handleHidePopupMenu}
				appendTo={document.body} 
			/>
		</>
	);
});

export default App;
