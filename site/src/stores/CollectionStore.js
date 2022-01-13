import {makeObservable, runInAction, action, observable, computed} from 'mobx';
import CollectionUtil from '../libs/CollectionUtil';
import Misc from '../libs/Misc';

class CollectionStore
{
	logic = null; // CollectionLogic
	subscription = null; // ws
	parent = null; // CollectionStore
	/* observables */
	collection = null; // Collection
	references = []; // Array<Object>
	views = new Map(); // Array<View>
	rows = new Map(); // Map<Map<Row>>
	auxs = null; // Array<CollectionsStore>
	versions = []; //
	integrations = []; // Array<Integration>
	automations = []; // Array<Automation>
	activeView = null; // View
	user = {};

	constructor(collectionLogic, collection, parent = null)
	{
		makeObservable(this, {
			/* properties */
			collection: observable.ref,
			references: observable.ref,
			views: observable.shallow,
			rows: observable.shallow,
			auxs: observable.ref,
			versions: observable.ref,
			integrations: observable.ref,
			automations: observable.ref,
			activeView: observable.ref,
			user: observable.ref,
			/* actions */
			load: action,
			update: action,
			delete: action,
			updateSchema: action,
			updateClasses: action,
			updateIndexes: action,
			updateConstants: action,
			setSchema: action,
			setView: action,
			createView: action,
			duplicateView: action,
			moveView: action,
			deleteView: action,
			setActiveView: action,
			setViewSortBy: action,
			setViewFilters: action,
			createField: action,
			updateFields: action,
			updateField: action,
			moveField: action,
			setFieldWidth: action,
			deleteField: action,
			loadVersions: action,
			deleteVersions: action,
			revertToVersion: action,
			createReport: action,
			updateReport: action,
			duplicateReport: action,
			moveReport: action,
			deleteReport: action,
			createForm: action,
			updateForm: action,
			duplicateForm: action,
			moveForm: action,
			deleteForm: action,
			loadAuxs: action,
			createAux: action,
			moveAux: action,
			onAuxUpdated: action,
			onAuxDuplicated: action,
			onAuxDeleted: action,
			loadAutomations: action,
			loadIntegrations: action,
			onMessage: action,
			loadRows: action,
			reloadRows: action,
			sortRows: action,
			createRow: action,
			updateRow: action,
			moveRow: action,
			deleteRows: action,
			moveItem: action,
			rowToggleSelected: action,
			rowToggleAppended: action,
			rowToggleGroup: action,
			incUpdateCnt: action,
			resetUpdatedCnt: action,
			/* computed */
			isAux: computed,
		});

		this.logic = collectionLogic;
		this.collection = collection;
		this.parent = parent;
	}

	async load(id)
	{
		const res = await this.logic.load(id, this.references);
		if(res.errors)
		{
			return res;
		}

		const role = await this.logic.getUserRole(id);

		runInAction(() =>
		{
			this.collection = res.data.collection;
			if(res.data.collection.type === 'SCHEMA')
			{
				this._config(res.data.collection, res.data.references);
			}
			
			this.user = {
				role
			};
		});

		return res;
	}

	_config(collection, references)
	{
		const schemaViews = collection.schemaObj.views;
		this.references = references;
		this.views = this._transformViews(collection);
		this.activeView = this.views.get(schemaViews[0].id);
		this.rows = new Map(schemaViews.map(view => ([view.id, new Map()])));
	}

	_transformViews(collection)
	{
		return new Map(
			collection.schemaObj.views.map(
				view => ([view.id, this._transformView(collection, view)])));
	}

	_transformFilters(viewFilters)
	{
		return viewFilters? 
			Object.entries(viewFilters)
				.map(([k, f]) => ({ 
					name: k, 
					op: f.op || 'eq', 
					value: f.value 
				})):
			[];
	}

	_transformView(collection, view, selectedCnt = 0, appendedCnt = 0)
	{
		return {
			...view,
			meta: {
				sortBy: view.sort || 
					{
						[collection.positionalId || CollectionUtil.ID_NAME]: {dir: 'asc'}
					},
				filters: this._transformFilters(view.filters),
				selectedCnt,
				appendedCnt,
				updatedCnt: 0,
			}
		};
	}

	_rebuildViews(collection)
	{
		const views = new Map();
		
		collection.schemaObj.views.forEach(view => 
		{
			const cur = this.views.get(view.id);
			views.set(
				view.id, 
				this._transformView(collection, view, cur.meta.selectedCnt, cur.meta.appendedCnt)
			);
		});

		return views;
	}

	async update(to)
	{
		const res = await this.logic.update(this.collection, to);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
		});

		if(this.isAux)
		{
			this.parent.onAuxUpdated(res.data);
		}

		return res;
	}

	async updateSchema(toSchema, options = {rebuild: false, useDiff: true})
	{
		const res = await this.logic.updateSchema(this.collection, toSchema, options.useDiff);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			if(options.rebuild)
			{
				this._config(res.data);
			}
		});

		return res;
	}

	async updateView(viewId, view)
	{
		const toSchema = Misc.cloneObject(this.collection.schemaObj);
		const index = toSchema.views.findIndex(v => v.id === viewId);
		toSchema.views[index] = view;
		
		const res = await this.logic.updateSchema(this.collection, toSchema);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			const schema = res.data.schemaObj;
			const index = schema.views.findIndex(v => v.id === viewId);
			const view = schema.views[index];
			this.views.set(viewId, this._transformView(res.data, view));
		});

		return res;
	}

	async updateFields(fields)
	{
		const res = await this.logic.updateFields(this.collection, fields);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			this.views = this._rebuildViews(res.data);
		});

		return res;
	}

	async updateField(key, field)
	{
		const res = await this.logic.updateField(this.collection, key, field);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data.collection;
			this.views = this._rebuildViews(res.data.collection);
		});

		return res;
	}

	async updateClasses(classes)
	{
		const res = await this.logic.updateClasses(this.collection, classes);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
		});

		return res;
	}

	async updateIndexes(indexes)
	{
		const res = await this.logic.updateIndexes(this.collection, indexes);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
		});

		return res;
	}

	async updateConstants(consts)
	{
		const res = await this.logic.updateConstants(this.collection, consts);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
		});

		return res;
	}

	async updateFlows(flows)
	{
		const res = await this.logic.updateFlows(this.collection, flows);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
		});

		return res;
	}

	getViewOnSchemaById(viewId, schema = null)
	{
		return this.logic.getViewOnSchemaById(schema || this.collection.schemaObj, viewId);
	}

	async createView(index, type)
	{
		const res = this.logic.createView(this.collection, this.views, index, type);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			const view = res.data.schemaObj.views[index];
			this.views.set(view.id, this._transformView(res.data, view));
			this.rows.set(view.id, new Map());
		});

		return res;
	}

	async moveView(fromIndex, toIndex)
	{
		const res = await this.logic.moveView(this.collection, fromIndex, toIndex);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			this.setActiveView(toIndex, false);
		});

		return res;
	}

	async duplicateView(index)
	{
		const res = await this.logic.duplicateView(this.collection, this.views, index);
		if(res.errors)
		{
			return res;
		}

		const view = res.data.schemaObj.views[index+1];
		
		runInAction(() =>
		{
			this.collection = res.data;
			this.views.set(view.id, this._transformView(res.data, view));
			this.rows.set(view.id, new Map());

			this.setActiveView(index+1, false);
		});

		return res;
	}

	setActiveView(index, resetUpdatedCounter = true)
	{
		const view = this.views.get(
			this.collection.schemaObj.views[index].id);
		
		this.activeView = view;
		if(resetUpdatedCounter)
		{
			this.resetUpdatedCnt(view.id);
		}
	}

	setViewFilters(viewId, filters)
	{
		const view = Misc.cloneObject(this.views.get(viewId));
		view.meta.filters = this._transformFilters(view.filters).concat(filters);
		this.views.set(viewId, view);
	}

	getViewFilters(viewId)
	{
		const view = this.views.get(viewId);
		return view.meta.filters;
	}

	setViewSortBy(viewId, sortBy)
	{
		const view = Misc.cloneObject(this.views.get(viewId));
		view.meta.sortBy = sortBy;
		this.views.set(viewId, view);
	}

	getViewSortBy(viewId)
	{
		const view = this.views.get(viewId);
		return view.meta.sortBy;
	}

	async deleteView(index)
	{
		const view = this.collection.schemaObj.views[index];

		const res = await this.logic.deleteView(this.collection, index);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			this.views.delete(view.id);
			this.rows.delete(view.id);

			if(this.activeView.id === view.id)
			{
				this.setActiveView(index > 0? index - 1: 0);
			}
		});

		return res;
	}

	setSchema(schema)
	{
		this.collection.schema = schema;
	}

	setView(viewId, view)
	{
		this.views.set(viewId, view);
	}

	async createField(type, at)
	{
		const res = await this.logic.createField(this.collection, type, at);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
			this.views = this._rebuildViews(res.data);
		});

		return res;
	}

	async deleteField(key)
	{
		const res = await this.logic.deleteField(this.collection, key);
		if(res.errors)
		{
			return res;
		}
		
		runInAction(() => 
		{
			this.collection = res.data;
			this.views = this._rebuildViews(res.data);

			this.rows.forEach((id, rows) =>
			{
				this.rows.set(
					id, 
					this.logic.deleteFieldFromRows(rows, key)
				);
			});

		});

		return res;
	}

	async moveField(viewId, user, parentKey, fromIndex, toIndex)
	{
		const res = await this.logic.moveField(
			this.collection, viewId, user, parentKey, fromIndex, toIndex);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			const view = this.logic.getViewOnSchemaById(res.data.schemaObj, viewId);
			const meta = this.views.get(viewId).meta;
			this.views.set(viewId, this._transformView(res.data, view, meta.selectedCnt, meta.appendedCnt));
		});

		return res;
	}

	async hideField(viewId, key)
	{
		const res = await this.logic.hideField(this.collection, viewId, key);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			const view = this.logic.getViewOnSchemaById(res.data.schemaObj, viewId);
			const meta = this.views.get(viewId).meta;
			this.views.set(viewId, this._transformView(res.data, view, meta.selectedCnt, meta.appendedCnt));
		});

		return res;
	}

	async showField(viewId, key, index, sorted)
	{
		const res = await this.logic.showField(
			this.collection, this.references, viewId, key, index, sorted);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			const view = this.logic.getViewOnSchemaById(res.data.schemaObj, viewId);
			const meta = this.views.get(viewId).meta;
			this.views.set(viewId, this._transformView(res.data, view, meta.selectedCnt, meta.appendedCnt));
		});

		return res;
	}

	setFieldWidth(viewId, key, width)
	{
		const view = Object.assign({}, this.views.get(viewId));
		view.fields[key].width = width;
		this.setView(viewId, view);
	}

	async loadVersions(page = 0, limit = 32)
	{
		const res = await this.logic.getVersions(this.collection, page, limit);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.versions = res.data;
		});

		return res;
	}

	async deleteVersions(ids)
	{
		const res = await this.logic.deleteVersions(this.collection, this.versions, ids);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.versions = res.data;
		});

		return res;
	}

	async revertToVersion(id)
	{
		const res = await this.logic.revertToVersion(this.collection, id);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.collection = res.data;
			this.views = this._rebuildViews(res.data);
		});

		return res;
	}

	async duplicate(options)
	{
		const res = await this.logic.duplicate(
			this.collection, this.isAux? this.parent.collection: null, options);
		if(res.errors)
		{
			return res;
		}

		if(this.isAux)
		{
			this.parent.onAuxDuplicated(res.data, this.references, options.position);
		}

		return res;
	}

	async delete()
	{
		const res = await this.logic.delete(
			this.collection, this.isAux? this.parent.collection: null);
		if(res.errors)
		{
			return res;
		}

		if(this.isAux)
		{
			this.parent.onAuxDeleted(this.collection.id);
		}

		return res;
	}

	get isAux()
	{
		return this.parent !== null;
	}

	async loadAutomations(offset = 0, size = 64, sort = 'id', asc = true)
	{
		const automations = offset === 0?
			[]:
			Array.from(this.automations);
		
		const res = await this.logic.loadAutomations(
			this.collection, automations, {offset, size, sort, asc});
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.automations = res.data;
		});

		return res;
	}

	async loadIntegrations(offset = 0, size = 64, sort = 'id', asc = true)
	{
		const integrations = offset === 0?
			[]:
			Array.from(this.integrations);
		
		const res = await this.logic.loadIntegrations(
			this.collection, integrations, {offset, size, sort, asc});
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.integrations = res.data;
		});

		return res;
	}

	async updateReport(at, report)
	{
		const res = await this.logic.updateReport(this.collection, at, report);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	async createReport(report, at)
	{
		const res = await this.logic.createReport(this.collection, report, at);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	generateReport(schema, id, at = null)
	{
		return this.logic.generateReport(schema, id, at);
	}

	async duplicateReport(index)
	{
		const res = await this.logic.duplicateReport(this.collection, index);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	async moveReport(from, to)
	{
		const res = await this.logic.moveReport(this.collection, from, to);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	async deleteReport(index)
	{
		const res = await this.logic.deleteReport(this.collection, index);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	async updateForm(at, form)
	{
		const res = await this.logic.updateForm(this.collection, at, form);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	async createForm(form, at)
	{
		const res = await this.logic.createForm(this.collection, form, at);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}
	
	generateForm()
	{
		return this.logic.generateForm(this.collection);
	}

	async duplicateForm(index)
	{
		const res = await this.logic.duplicateForm(this.collection, index);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	async moveForm(from, to)
	{
		const res = await this.logic.moveForm(this.collection, from, to);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	createFormStep(steps, index)
	{
		return this.logic.createFormStep(steps, index);
	}

	duplicateFormStep(steps, index)
	{
		return this.logic.duplicateFormStep(steps, index);
	}

	moveFormStep(steps, from, to)
	{
		return this.logic.moveFormStep(steps, from, to);
	}

	deleteFormStep(steps, index)
	{
		return this.logic.deleteFormStep(steps, index);
	}

	async deleteForm(index)
	{
		const res = await this.logic.deleteForm(this.collection, index);
		if(res.errors)
		{
			return res;
		}

		runInAction(() => 
		{
			this.collection = res.data;
		});

		return res;
	}

	_transformAux(collection, references)
	{
		const store = new CollectionStore(this.logic, collection, this);

		store._config(collection, references);
		
		return store;
	}

	async loadAuxs(page = 0, size = 32)
	{
		const res = await this.logic.loadAuxs(this.collection, page, size);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.auxs = res.data
				.map(({collection, references}) => this._transformAux(collection, references));
		});

		return res;
	}

	async createAux(templateId, at)
	{
		const res = await this.logic.createAux(this.collection, templateId, at);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			const aux = this._transformAux(res.data, null);

			if(!this.auxs || this.auxs.length === 0)
			{
				this.auxs = [aux];
			}
			else
			{
				this.auxs = Misc.addToArray(this.auxs, at || 0, [aux]);
			}
		});

		return res;
	}

	async moveAux(aux, dir, from, to)
	{
		const res = await this.logic.moveAux(this.collection, aux, dir);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.auxs = Misc.moveArray(this.auxs, from, to);
		});

		return res;
	}

	onAuxUpdated(/*aux*/)
	{
		this.auxs = this.auxs?
			Array.from(this.auxs):
			null;
	}
	
	onAuxDuplicated(aux, references, index)
	{
		this.auxs = this.auxs?
			Misc.addToArray(this.auxs, index, [this._transformAux(aux, references)]):
			null;
	}
	
	onAuxDeleted(id)
	{
		const auxs = this.auxs?
			this.auxs.filter(aux => aux.collection.id !== id):
			null;
		
		this.auxs = auxs && auxs.length > 0?
			auxs:
			null;
	}

	dispatchEvent(type, data = {})
	{
		document.dispatchEvent(new CustomEvent(type, data));
	}

	async subscribe()
	{
		const res = await this.logic.subscribe(this.collection, this.onMessage);
		this.subscription = res;
		return res;
	}

	unsubscribe()
	{
		if(this.subscription)
		{
			this.logic.unsubscribe(this.subscription);
			this.subscription = null;
		}
	}

	onMessage = async (message) =>
	{
		const msg = JSON.parse(message.body);

		if(msg.origin === this.logic.getSessionId())
		{
			return;
		}

		switch(msg.event)
		{
		case 'PROPS_UPDATED':
			this.load(this.collection.id);

			if(msg.extra)
			{
				this.dispatchEvent('rf::updated::collection', {
					type: msg.event,
					id: this.collection.id,
					extra: msg.extra,
				});
			}
			break;

		case 'SCHEMA_UPDATED':
		case 'COLUMN_CREATED':
		case 'COLUMNS_CREATED':
		case 'COLUMN_DELETED':
		case 'COLUMNS_DELETED':
		case 'COLUMN_RENAMED':
		{			
			const schemaStr = msg.schema;
			const schemaObj = JSON.parse(schemaStr);
			
			const to = {
				...this.collection,
				schema: schemaStr, 
				schemaObj: this.logic.compile(schemaObj)
			};
			
			runInAction(() =>
			{
				this.onCollectionUpdated(to, msg.event);
				this.onSchemaUpdated(to, msg.event, msg.extra);
			});

			break;
		}

		case 'ITEM_CREATED':
		case 'ITEMS_CREATED':
		case 'ITEM_UPDATED':
		case 'ITEMS_UPDATED':
		case 'ITEM_DELETED':
		case 'ITEMS_DELETED':
		case 'CELL_UPDATED':
		case 'CELLS_UPDATED':
			this.onDataChanged(msg.event, msg.extra);
			break;
		}
	}
	
	async onCollectionUpdated(to, type)
	{
		const from = this.collection;
		
		this.collection = to;
		this._config(to, this.references);
		
		if(this.isAux)
		{
			this.parent.onAuxUpdated(to);
		}

		this.reloadCollectionsMenu(from, to, type);
	}

	async reloadCollectionsMenu(from, to, type)
	{
		const needsReload = 
			(!to.name !== from.name) ||
			((to.icon !== null && from.icon === null) || (to.icon === null && from.icon !== null) || to.icon !== from.icon) ||
			((to.order !== null && from.order === null) || (to.order === null && from.order !== null) || to.order !== from.order) ||
			((to.publishedAt !== null && from.publishedAt === null) || (to.publishedAt === null && from.publishedAt !== null));
	
		if(needsReload)
		{
			this.dispatchEvent('rf::updated::menu', {
				type: type,
				id: to.id,
			});
		}
	}
	
	onSchemaUpdated(collection, type, extra = {}, onlyForView = null)
	{
		this.dispatchEvent('rf::updated::schema', {
			type: type, 
			id: collection.id, 
			view: onlyForView, 
			extra: extra
		});
	}

	onDataChanged(type, data = {})
	{
		for(let [viewId, ] of this.views)
		{
			this.reloadRows(viewId, null);
		}
	}
	
	onAuxCollectionUpdated(aux)
	{
		const index = this.auxs?
			this.auxs.findIndex(a => a.id === aux.id):
			-1;
		
		if(index >= 0)
		{
			const auxs = Array.from(this.auxs);
			auxs[index] = aux;
			this.auxs = auxs;
		}
	}

	findRowById(viewId, id)
	{
		const rows = this.rows.get(viewId);
		if(rows.size === 0)
		{
			return null;
		}

		const idColumn = CollectionUtil.ID_NAME;

		return Misc.mapValues(rows)
			.find(row => row.item[idColumn] === id);
	}

	async loadRows(viewId, user, offset, size, options = {})
	{
		const view = this.views.get(viewId);
		const rows = offset === 0?
			new Map():
			new Map(this.rows.get(viewId));
		
		if(offset === 0)
		{
			view.meta.allLoaded = false;
			view.meta.appendedCnt = 0;
			view.meta.selectedCnt = 0;
		}

		const filters = options.filters? 
			view.meta.filters.concat(options.filters): 
			(view.meta.filters.length > 0? 
				view.meta.filters: 
				null);

		const res = await this.logic.loadRows(
			this.collection,
			this.references, 
			view, 
			user,
			rows,
			{
				...options,
				sortBy: options.sortBy || view.meta.sortBy,
				filters,
				groupBy: view.groupBy,
				offset,
				size
			});

		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.rows.set(viewId, res.data);
			view.meta.allLoaded = res.allLoaded;
		});

		return res;
	}

	reloadRows(viewId, user, options = {})
	{
		const rows = new Map(this.rows.get(viewId));

		const rowsPerPage = options.rowsPerPage?
			options.rowsPerPage:
			CollectionUtil.PAGE_SIZE;
		
		const size = rows.size === 0 || options.restart? 
			rowsPerPage: 
			rows.size;
		
		return this.loadRows(
			viewId,
			user,
			0, 
			size, 
			{
				...options, 
				isReload: true
			});
	}
	
	sortRows(viewId, options = {})
	{
		const view = this.views.get(viewId);

		const res = this.logic.sortRows(
			this.collection, 
			this.references, 
			new Map(this.rows.get(viewId)), 
			{
				...options,
				sortBy: options.sortBy || view.meta.sortBy,
				filters: view.meta.filters,
				groupBy: view.groupBy,
			});

		this.rows.set(viewId, res.data);

		view.meta.appendedCnt = 0;

		return res.data;
	}

	async createRow(viewId, at, user, options = {})
	{
		const view = this.views.get(viewId);
		const rows = new Map(this.rows.get(viewId));
		const counters = {
			appendedCnt: view.meta.appendedCnt, 
			selectedCnt: view.meta.selectedCnt,
		};		
		
		const moreOptions = {
			...options,
			sortBy: view.meta.sortBy,
			filters: view.meta.filters,
			groupBy: view.meta.groupBy,
			migrations: new Map(),
		};
		
		const res = await this.logic.createRow(
			this.collection, 
			this.references, 
			this.views,
			view, 
			counters, 
			user, 
			rows, 
			at, 
			moreOptions);

		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.rows.set(viewId, res.data);
			view.meta.appendedCnt = counters.appendedCnt;
			view.meta.selectedCnt = counters.selectedCnt;
		});

		for(let [viewId, itemIds] of moreOptions.migrations)
		{
			this.incUpdateCnt(viewId, itemIds.length);
			this.reloadRows(viewId, user);
		}

		return res;
	}

	incUpdateCnt(viewId, amount = 1)
	{
		const view = Object.assign({}, this.views.get(viewId));
		view.meta.updatedCnt += amount;
		this.setView(viewId, view);
	}

	resetUpdatedCnt(viewId)
	{
		const view = Object.assign({}, this.views.get(viewId));
		view.meta.updatedCnt = 0;
		this.setView(viewId, view);
	}

	async updateRow(
		viewId, user, row, item, key, value, options)
	{
		const view = this.views.get(viewId);
		const rows = new Map(this.rows.get(viewId));

		const counters = {
			appendedCnt: view.meta.appendedCnt, 
			selectedCnt: view.meta.selectedCnt,
		};
		
		const moreOptions = {
			...options,
			sortBy: view.meta.sortBy,
			filters: view.meta.filters, 
			groupBy: view.groupBy,
			migrations: new Map(),
		};
		
		const res = await this.logic.updateRow(
			this.collection, 
			this.references, 
			this.views,
			view, 
			counters,
			user, 
			rows, 
			row, 
			item, 
			key, 
			value, 
			moreOptions);
			
		if(res.errors)
		{
			return res;
		}
	
		runInAction(() =>
		{
			this.rows.set(viewId, res.data);
		});

		for(let [viewId, itemIds] of moreOptions.migrations)
		{
			this.incUpdateCnt(viewId, itemIds.length);
			this.reloadRows(viewId, user);
		}

		return res;
	}

	async moveRow(viewId, row, dir)
	{
		const view = this.views.get(viewId);
		const rows = this.rows.get(viewId);
		
		const counters = {
			appendedCnt: view.meta.appendedCnt, 
			selectedCnt: view.meta.selectedCnt,
		};
		const res = this.logic.moveRow(this.collection, counters, rows, row, dir);

		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.rows.set(viewId, res.data);
			view.meta.appendedCnt = counters.appendedCnt;
		});

		return res;
	}

	async deleteRows(viewId, user, toRemove)
	{
		const view = this.views.get(viewId);
		const counters = {
			appendedCnt: view.meta.appendedCnt, 
			selectedCnt: view.meta.selectedCnt,
		};		

		const res = await this.logic.deleteRows(
			this.collection, this.references, view, counters, user, new Map(this.rows.get(viewId)), toRemove);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.rows.set(viewId, res.data);
			view.meta.appendedCnt = counters.appendedCnt;
			view.meta.selectedCnt = counters.selectedCnt;
		});

		return {
			data: res.data,
			...counters
		};
	}

	async createItem(item, user)
	{
		const res = await this.logic.createItem(this.collection, item);
		if(res.errors)
		{
			return res;
		}

		this.views.forEach(view => this.reloadRows(view.id, user));

		return res;
	}

	async createItemsFromCSV(csv, replaceExisting, user)
	{
		const res = await this.logic.createItemsFromCSV(this.collection, csv, replaceExisting);
		if(res.errors)
		{
			return res;
		}

		this.views.forEach(view => this.reloadRows(view.id, user));

		return res;
	}

	async moveItem(viewId, from, to)
	{
		const view = this.views.get(viewId);
		const rows = this.rows.get(viewId);
		const counters = {
			appendedCnt: view.meta.appendedCnt, 
			selectedCnt: view.meta.selectedCnt,
		};

		const res = await this.logic.moveItem(
			this.collection, counters, rows, from, to);
		if(res.errors)
		{
			return res;
		}

		runInAction(() =>
		{
			this.rows.set(viewId, res.data);
			view.meta.appendedCnt = counters.appendedCnt;
		});

		return res;
	}

	rowToggleSelected(viewId, row)
	{
		const view = this.views.get(viewId);
		const rows = new Map(this.rows.get(viewId));

		const selected = !row.selected;
		rows.set(row.num-1, {
			...row, 
			selected: selected
		});

		this.rows.set(viewId, rows);

		view.meta.selectedCnt = view.meta.selectedCnt + (selected? 1: -1);
	}

	rowToggleAppended(viewId, row, user)
	{
		const view = this.views.get(viewId);
		const rows = new Map(this.rows.get(viewId));

		const appended = !row.appended;

		rows.set(row.num-1, {
			...row, 
			appended: appended? 
				{
					item: this.logic.genAppendedItem(this.collection, this.references, view, user),
				}:
				null,
			dirty: false,
		});

		this.rows.set(viewId, rows);

		view.meta.appendedCnt = view.meta.appendedCnt + (appended? 1: -1);
	}

	rowToggleGroup(viewId, row)
	{
		const rows = new Map(this.rows.get(viewId));

		const group = row.group;
		group.expanded = !group.expanded;
		
		rows.set(row.num-1, {
			...row, 
			group: group,
		});

		this.rows.set(viewId, rows);
	}
}

export default CollectionStore;