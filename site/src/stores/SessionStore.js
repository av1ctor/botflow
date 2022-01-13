import {makeObservable, runInAction, action, observable} from 'mobx';

export default class SessionStore
{
	logic = null;
	isAuthenticated = false;
	rememberMe = false;
	user = {};
	workspace = {};

	constructor(sessionLogic)
	{
		this.logic = sessionLogic;
		
		makeObservable(this, {
			isAuthenticated: observable,
			user: observable,
			workspace: observable,
			logonUser: action,
			logoutUser: action,
			setUser: action,
			switchWorkspace: action,
		});

		const session = this.logic.loadSession();
		if(session)		
		{
			this.isAuthenticated = session.isAuthenticated;
			this.user = session.user;
			this.workspace = session.workspace;
			this.rememberMe = session.rememberMe;
		}
	}

	async logonUser(email, password, workspace, rememberMe)
	{
		const res = await this.logic.logonUser(email, password, workspace, rememberMe);
		if(res.errors)
		{
			return res;
		}
			
		runInAction(() =>
		{
			const {data} = res;
			this.isAuthenticated = true;
			this.token = data.token;
			this.expiration = data.expiration;
			this.rememberMe = rememberMe;
			this.user = data.user;
			this.workspace = data.workspace;
		});
		
		return res;
	}

	logoutUser()
	{
		this.isAuthenticated = false;
		this.token = null;
		this.expiration = null;
		this.rememberMe = false;
		this.user = {};
		this.workspace = {};

		this.logic.logoutUser();
	}

	setUser(user)
	{
		this.user = user;
	}

	async switchWorkspace(token, expiration, rememberMe, idUser, idWorkspace)
	{
		const res = await this.logic.switchWorkspace(
			token, expiration, rememberMe, idUser, idWorkspace);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			const {data} = res;
			this.isAuthenticated = true;
			this.token = data.token;
			this.expiration = data.expiration;
			this.rememberMe = rememberMe;
			this.user = data.user;
			this.workspace = data.workspace;			
		});
	}
}
