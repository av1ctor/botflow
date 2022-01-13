import './polyfills.js';
import React from 'react';
import ReactDOM from 'react-dom';
import {HashRouter as Router} from 'react-router-dom';
import HTML5Backend from 'react-dnd-html5-backend';
import {TouchBackend} from 'react-dnd-touch-backend';
import {DndProvider} from 'react-dnd';
import RobotikflowAPI from './libs/RobotikflowAPI';
import WorkspaceStore from './stores/WorkspaceStore';
import SessionStore from './stores/SessionStore.js';
import {WorkspaceContextProvider} from './contexts/WorkspaceContext.js';
import {SessionContextProvider} from './contexts/SessionContext';

import App from './App';

import './layout/bootstrap-themes.css';
import './layout/bootstrap.min.css';
import './layout/avabot.css';
import './layout/interface.css';
import 'primereact/resources/primereact.min.css';
import 'primereact/resources/themes/nova-light/theme.css';
import 'line-awesome/dist/line-awesome/css/line-awesome.min.css';
import './index.css';
import SessionLogic from './business/SessionLogic.js';

const isMobile = !window.matchMedia('(min-width: 1024px)').matches;
const api = new RobotikflowAPI();

const workspaceStore = new WorkspaceStore(api);
const sessionStore = new SessionStore(new SessionLogic(api));

ReactDOM.render(
	<DndProvider backend={!isMobile? HTML5Backend: TouchBackend}>
		<Router>
			<WorkspaceContextProvider store={workspaceStore}>
				<SessionContextProvider store={sessionStore}>
					<App api={api} isMobile={isMobile} />
				</SessionContextProvider>
			</WorkspaceContextProvider>
		</Router>
	</DndProvider>,
	document.getElementById('root')
);
