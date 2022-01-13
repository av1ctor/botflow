const STORAGE_SESSION_KEY = 'RF_SESSION';

class SessionLogic
{
	api = null;
	
	constructor(api)
	{
		this.api = api;
	}

	async logonUser(email, password, workspace, rememberMe)
	{
		const res = await this.api.login(
			email, password, workspace, rememberMe);
		if(!res.errors)
		{
			const {data} = res;

			this.saveSession(
				data.token, 
				data.expiration, 
				rememberMe, 
				data.user, 
				data.workspace);
		}

		return res;
	}

	logoutUser()
	{
		this.api.setSession({
			token: null, 
			expiration: null
		});
			
		this.clearSession();
	}

	async switchWorkspace(token, expiration, rememberMe, idWorkspace, idUser)
	{
		const res = await this.api.switchWorkspace(token, expiration, idWorkspace, idUser);
		if(!res.errors)
		{
			const {data} = res;
			this.saveSession(
				data.token, 
				data.expiration, 
				rememberMe, 
				data.user, 
				data.workspace);
		}

		return res;
	}

	loadSession()
	{
		let item = localStorage.getItem(STORAGE_SESSION_KEY);
		const rememberMe = !!item;
		if(!item)
		{
			item = sessionStorage.getItem(STORAGE_SESSION_KEY);
		}
		
		const session = JSON.parse(item);
		
		if(!item || !session.token || !session.user || !session.workspace)
		{
			return null;
		}
			
		this.api.setSession({
			token: session.token, 
			expiration: session.expiration
		});
			
		return {
			isAuthenticated: true,
			token: session.token,
			expiration: session.expiration,
			rememberMe: rememberMe,
			user: session.user,
			workspace: session.workspace,
		};
	}

	saveSession(token, expiration, rememberMe, user, workspace)
	{
		const session = {
			token: token,
			expiration: expiration,
			user: user,
			workspace: workspace,
		};

		(rememberMe? localStorage : sessionStorage)
			.setItem(STORAGE_SESSION_KEY, JSON.stringify(session));
	
		(rememberMe? sessionStorage : localStorage)
			.removeItem(STORAGE_SESSION_KEY);
	}
	
	clearSession()
	{
		localStorage.removeItem(STORAGE_SESSION_KEY);
		sessionStorage.removeItem(STORAGE_SESSION_KEY);
	}
	
}

export default SessionLogic;