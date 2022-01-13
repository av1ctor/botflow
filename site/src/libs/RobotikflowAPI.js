import {Client} from '@stomp/stompjs';
import {v4 as uuid} from 'uuid';

const REFRESH_WINDOW = 5*60*1000; // 5 minutes

export default class RobotikflowAPI
{
	token = null;
	expiration = null;
	sessionId = null;

	constructor(options = null)
	{
		if(options)
		{
			this.setSession(options);
		}
		
		this.basePath = process.env.NODE_ENV === 'development'? 
			process.env.REACT_APP_ROBOTIKFLOW_API_URL.replace('{{hostname}}', window.location.hostname):
			process.env.REACT_APP_ROBOTIKFLOW_API_URL;

		this.wsClient = new Client({
			brokerURL: process.env.NODE_ENV === 'development'? 
				process.env.REACT_APP_ROBOTIKFLOW_WS_URL.replace('{{hostname}}', window.location.hostname):
				process.env.REACT_APP_ROBOTIKFLOW_WS_URL,
			connectHeaders: this.__getWsHeaders(),
		});
	}

	async login(email, password, workspaceName)
	{
		try
		{
			const response = await this.requestURL(
				'POST', 
				this.basePath + 'auth/login', 
				{
					'email': email, 
					'password': password, 
					'workspace': workspaceName
				},
				{
					isLogon: true
				});

			if(response.errors !== null)
			{
				return {
					errors: response.errors
				};
			}

			this.setSession(response.data);
				
			return response;
		}
		catch (e)
		{
			return {
				errors: [e? e.message: 'unknown']
			};
		}
	}

	setSession({token, expiration})
	{
		this.token = token;
		this.expiration = expiration;
		this.sessionId = uuid();
	}

	async refresh()
	{
		try
		{
			const response = await this.requestURL(
				'GET', 
				this.basePath + `auth/refresh/${this.token}`, 
				null, 
				{
					isRefresh: true, 
					includeToken: false
				}
			);

			if(!response.data)
			{
				return {errors: response.errors};
			}

			this.token = response.data.token;
			this.expiration = response.data.expiration;
			return response;
		}
		catch(e)
		{
			return {errors: [e? e.message: 'unknown']};	
		}
	}

	async switchWorkspace(
		token, expiration, idWorkspace, idUser)
	{
		this.token = token;
		this.expiration = expiration;

		let res = await this.get(`workspaces/${idWorkspace}`);
		if(res.errors !== null)
		{
			return res;
		}

		const workspace = res.data;

		res = await this.get(`users/${idUser}`);
		if(res.errors !== null)
		{
			return res;
		}

		const user = res.data;

		this.setSession({
			token,
			expiration, 
		});

		return {
			errors: null,
			data: {
				token,
				expiration,
				workspace,
				user,
			}
		};
	}

	getToken()
	{
		return this.token;
	}

	getSessionId()
	{
		return this.sessionId;
	}

	getPath(path = '')
	{
		return this.basePath + path;
	}
	
	async logout()
	{
		try
		{
			this.token = null;
			this.expiration = null;
		}
		catch (e)
		{
			console.error(e);
		}
	}

	async request(method, path, data = null, options = {})
	{
		return await this.requestURL(method, this.basePath + path, data, options);
	}

	async requestURL(
		method, 
		url, 
		data = null, 
		{
			isJsonRequest = true, 
			isJsonResponse = true, 
			includeToken = true, 
			isLogon = false, 
			isRefresh = false, 
			abortController = null
		})
	{
		try
		{
			if(this.expiration && Date.now() >= new Date(this.expiration).getTime() - REFRESH_WINDOW)
			{
				if(!isLogon && !isRefresh)
				{
					const res = await this.refresh();
					if(res.errors)
					{
						return res;
					}
				}
			}
	
			const options = {
				headers: this.__getHeaders({isJsonRequest, includeToken}),
				mode: 'cors',
				cache: 'no-store',
				method: method,
				signal: abortController && abortController.signal
			};

			if(data !== null)
			{
				options.body = !isJsonRequest? data: JSON.stringify(data);
			}

			const response = await fetch(url, options)
				.catch((errors) => console.error(errors));

			if(response && response.ok)
			{
				try
				{
					const body = isJsonResponse? await response.json(): await response.blob();
					return {errors: null, data: body};
				}
				catch(e)
				{
					return {errors: [e? e.message: 'unknown'], data: null};
				}
			}
			else
			{
				const body = response && isJsonResponse? await response.json(): null;

				if(body === null && (response && response.status === 401))
				{
					return {errors: [isLogon? 'Usuário e/ou password inválidos.': 'Sessão expirada.'], data: null};
				}

				return this.remapError(body);
			}
		}
		catch (e)
		{
			return {errors: [e? e.message: 'unknown'], data: null};
		}

	}

	remapError(body)
	{
		const remap = (errors) => {
			var v = Object.values(errors);
			var k = Object.keys(errors);
			var r = [];
			k.forEach((k,i) => {
				r.push(k + ': ' + 
					(v[i]?
						(v[i].constructor !== Array? 
							v[i]:
							v[i].reduce((p,c) => (p !== null? p + '|':'') + c, null)):
						''));
			});
			return r;
		};

		return {
			errors: body && body.errors? 
				(Array.isArray(body.errors.constructor)? 
					body.errors: 
					(body.errors.constructor === Object?
						remap(body.errors):
						[body.errors])): 
				[body && body.message? 
					body.message: 
					'failed'], 
			data: null
		};		
	}

	async get(path, options = {})
	{
		return await this.requestURL('GET', this.basePath + path, null, options);
	}

	async post(path, data, options = {})
	{
		return await this.requestURL('POST', this.basePath + path, data, options);
	}

	async put(path, data, options = {})
	{
		return await this.requestURL('PUT', this.basePath + path, data, options);
	}

	async patch(path, data, options = {})
	{
		return await this.requestURL('PATCH', this.basePath + path, data, options);
	}

	async del(path, options = {})
	{
		return await this.requestURL('DELETE', this.basePath + path, null, options);
	}

	cancel(abortController)
	{
		abortController && abortController.abort();
	}

	__getHeaders({isJsonRequest, includeToken})
	{
		var baseHeader = !isJsonRequest? 
			{} : 
			{'Content-Type': 'application/json'};

		return !includeToken || this.token === null? 
			baseHeader: 
			{
				...baseHeader, 
				'Authorization': 'Bearer ' + this.token,
				'Session-Id': this.sessionId
			};
	}

	__getWsHeaders()
	{
		return {'Authorization': 'Bearer ' + this.token};
	}

	__initWs()
	{
		if(!this.wsClient.active)
		{
			this.wsClient.connectHeaders = this.__getWsHeaders();
			this.wsClient.activate();
		}
	}

	subscribe(path, callback)
	{
		this.__initWs();

		const waitOrSubscribe = (delay) => 
		{
			if(!this.wsClient.connected)
			{
				return new Promise((resolve) =>
					setTimeout(() => resolve(waitOrSubscribe(delay*5)), delay)
				);
			}
			else
			{
				return new Promise((resolve) => 
					resolve(this.wsClient.subscribe(`/topic/${path}`, callback, {...this.__getWsHeaders(), id: this.sessionId}))
				);
			}
		};
		
		return waitOrSubscribe(100);
	}

	unsubscribe(subscription)
	{
		if(this.wsClient.connected && subscription)
		{
			subscription.unsubscribe();
		}
	}

	publish(path, message = {})
	{
		this.wsClient.publish({
			destination: `/app/${path}`, 
			headers: this.__getWsHeaders(), 
			body: JSON.stringify(message)
		});
	}
}
