import {makeObservable, runInAction, action, observable} from 'mobx';
import Misc from '../libs/Misc';

export default class WorkspaceStore
{
	api = null;
	menu = [];
	schemas = new Map();
	objs = new Map();
	apps = new Map();

	constructor(api)
	{
		this.api = api;
		
		makeObservable(this, {
			menu: observable,
			schemas: observable.shallow,
			objs: observable.shallow,
			apps: observable.shallow,
			setMenu: action,
		});
	}

	switchTo(id)
	{
		return this.api.get(`workspaces/switch/${id}`);
	}

	async loadMenu()
	{
		const res = await this.api.get('menus');
		if(res.errors)
		{
			return res;
		}

		return {
			data: res.data,
			errors: null
		};
	}

	async loadCollectionsMenu(parentId)
	{
		const res = await this.api.get(`collections/${parentId}/children/pub?sort=order,asc`);
		if(res.errors)
		{
			return res;
		}

		return {
			data: res.data,
			errors: null
		};
	}

	setMenu(menu)
	{
		this.menu = menu;
	}

	_transformSchemas(items)
	{
		return items.map(item => ({
			...item, 
			schemaObj: JSON.parse(item.schema)
		}));
	}
	
	async loadObjSchemas(type, page = 0, size = 30, options = {})
	{
		const schemas = this.schemas.get(type);
		const offset = page * size;
		const reload = options.reload || options.nostore;
			
		if(!reload && schemas && 
			((offset < schemas.data.length && 
				offset + size <= schemas.data.length) || 
			schemas.completed))
		{
			if(offset >= schemas.data.length)
			{
				return {
					errors: null,
					data: []
				};
			}

			return {
				errors: null,
				data: schemas.data.slice(offset, offset + Math.min(size, schemas.data.length - offset))
			};
		}
		
		const res = await this.api.get(`config/${type}/schemas?page=${page}&size=${size}`);
		if(res.errors)
		{
			return res;
		}

		let data = this._transformSchemas(res.data);
		
		if(options.nostore)
		{
			return {
				errors: null,
				data: data
			};
		}

		if(schemas && !reload)
		{
			data = schemas.data.concat(data);
		}

		runInAction(() =>
		{
			this.schemas.set(type, {
				data: data,
				completed: data.length < size
			});
		});

		return {
			errors: null,
			data: data
		};
	}

	_filtersToString(filters)
	{
		if(!filters)
		{
			return null;
		}

		const res = JSON.stringify(filters);
		return res.substr(1, res.length - 2);
	}
	
	async loadObjs(type, page = 0, size = 30, sortBy = null, ascending = true, filters = null, options = {})
	{
		const objs = this.objs.get(type);
		const offset = page * size;
		const filtersStr = this._filtersToString(filters);

		const reload = options.reload || options.nostore ||
			(!!objs &&
				(sortBy !== objs.sortBy ||
					ascending !== objs.ascending ||
					filtersStr !== objs.filtersStr));

		if(!reload && objs && 
			((offset < objs.data.length && 
				offset + size <= objs.data.length) || 
			objs.completed))
		{
			if(offset >= objs.data.length)
			{
				return {
					errors: null,
					data: []
				};
			}

			return {
				errors: null,
				data: objs.data.slice(offset, offset + Math.min(size, objs.data.length - offset))
			};
		}

		const res = await this.api.get(
			`config/${type}?withSchema=true&withUser=true&page=${page}&size=${size}${sortBy?'&sort='+sortBy+','+(ascending?'asc':'desc'):''}${filtersStr? '&filters=' + filtersStr: ''}`);
		if(res.errors || options.nostore)
		{
			return res;
		}

		let data = null;
		if(!objs || reload)
		{
			data = res.data;	
		}
		else
		{
			data = objs.data.concat(res.data);
		}

		runInAction(() =>
		{
			this.objs.set(type, {
				data: data,
				sortBy: sortBy,
				ascending: ascending,
				filters: filters,
				filtersStr: filtersStr,
				completed: data.length < size
			});
		});

		return {
			errors: null,
			data: data
		};
	}

	async reloadObjs(type)
	{
		const objs = this.objs.get(type);
		return await this.loadObjs(
			type, 
			0, 
			Math.max(objs.data.length, 30), 
			objs.sortBy, 
			objs.ascending, 
			objs.filters, 
			{reload: true});
	}

	async loadObj(type, id)
	{
		const objs = this.objs.get(type);
		if(objs)
		{
			const obj = objs.data.find(o => o.id === id);
			if(obj)
			{
				return {
					errors: null,
					data: obj
				};
			}
		}

		return await this.api.get(`config/${type}/${id}`);
	}

	_isObjVisible(objs, obj)
	{
		if(!objs.completed)
		{
			throw new Error('Should only be used when complete');
		}
		
		if(!objs.filtersStr)
		{
			return true;
		}

		for(let i = 0; i < objs.filters.length; i++)
		{
			const filter = objs.filters[i];
			if(!Object.getOwnPropertyDescriptor(obj, filter.name) || obj[filter.name] !== filter.value)
			{
				return false;
			}
		}

		return true;
	}

	_sortObjs(objs)
	{
		if(!objs.completed)
		{
			throw new Error('Should only be used when complete');
		}
		
		if(!objs.sortBy)
		{
			return objs.data;
		}

		const key = objs.sortBy;
		const dir = objs.ascending? 1: -1;

		return objs.data.sort((a, b) => 
		{	
			let aval = a[key];
			let bval = b[key];
			if(aval === null || aval === undefined)
			{
				return bval === null || bval === undefined? 
					0: 
					-dir;
			}
			else if(bval === null || bval === undefined)
			{
				return dir;
			}

			if(aval.constructor === String)
			{
				aval = aval.toLowerCase();
				bval = bval.toLowerCase();
			}
			
			return aval === bval? 
				0: 
				(aval < bval? -dir: dir);
		});
	}

	async createObj(type, data)
	{
		const res = await this.api.post(`config/${type}`, data);
		if(res.errors)
		{
			return res;
		}

		const objs = this.objs.get(type);
		if(!objs.completed)
		{
			this.reloadObjs(type);
		}
		else
		{
			// is the new obj visible? 
			if(this._isObjVisible(objs, res.data))
			{
				// add to objs, sort and chop the last item
				objs.data.push(res.data);
				const data = this._sortObjs(objs).slice(0, objs.data.length - 1);

				runInAction(() =>
				{
					this.objs.set(type, {
						...objs,
						data: data
					});
				});
			}
		}

		return res;
	}

	async updateObj(type, id, data)
	{
		const res = await this.api.patch(`config/${type}/${id}`, data);
		if(res.errors)
		{
			return res;
		}

		const objs = this.objs.get(type);
		if(!objs.completed)
		{
			this.reloadObjs(type);
		}
		else
		{
			// is obj still visible? 
			if(this._isObjVisible(objs, res.data))
			{
				// sort
				const data = this._sortObjs(objs);

				runInAction(() =>
				{
					this.objs.set(type, {
						...objs,
						data: data
					});
				});
			}
		}

		return res;
	}
}