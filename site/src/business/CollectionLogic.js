import memoize from 'fast-memoize';
import CollectionUtil from '../libs/CollectionUtil';
import Misc from '../libs/Misc';

class CollectionLogic
{
	api = null;
	abortController = null;
	
	constructor(api)
	{
		this.api = api;
	}

	async load(id)
	{
		this.abortController = new AbortController();
		
		const res = await this.api.get(`collections/${id}`, {abortController: this.abortController});
		if(res.errors !== null)
		{
			return res;
		}

		const collection = this.transform(res.data);

		const references = await CollectionLogic.loadReferences(collection.schemaObj);
		
		return {
			data: {
				collection,
				references,
			},
			errors: null
		};
	}

	async getUserRole(id)
	{
		const res = await this.api.get(`collections/${id}/permissions/user`);
		if(res.errors !== null)
		{
			return res;
		}

		return !res.data || res.data.length === 0?
			CollectionUtil.ROLE_NONE:
			res.data.reduce((cur, perm) => perm.roleNum < cur? perm.roleNum: cur, CollectionUtil.ROLE_NONE);
	}

	transform(collection)
	{
		const schemaObj = JSON.parse(collection.schema);

		return {
			...collection,
			schemaObj: CollectionLogic.compile(schemaObj)
		};
	}

	static async loadReferences(schema, currentRefs, compileMethods = true)
	{
		// carregar referências
		const res = {};
		if(schema.refs)
		{
			const refs = Object.entries(schema.refs);
			for(let i = 0; i < refs.length; i++) 
			{
				const [name, ref] = refs[i];
				if(!currentRefs || !currentRefs[name])
				{
					const refRes = await this.api.get(`collections/${ref.collection}`);
					if(refRes.errors !== null)
					{
						console.error(`Unknown reference: ${name} -> ${ref.collection}`);
					}
					else
					{
						const refSchema = JSON.parse(refRes.data.schema);
						if(compileMethods)
						{
							CollectionLogic.compile(refSchema);
						}

						res[name] = {
							...refRes.data,
							schemaObj: refSchema, 
						};
					}
				}
				else
				{
					res[name] = currentRefs[name];
				}
			}
		}
		
		return res;
	}

	static compile(schema) 
	{
		if(schema.classes) 
		{
			// compilar métodos de cada classe
			CollectionLogic._compileMethods(schema.classes);
			
			// e das propriedades delas
			const classes = Object.values(schema.classes);
			for (let i = 0; i < classes.length; i++) 
			{
				const klass = classes[i];
				if(klass.props) 
				{
					CollectionLogic._compileMethods(klass.props);
				}
			}
		}
		
		// compilar métodos nos campos da tabela
		CollectionLogic._compileMethods(schema.columns);

		return schema;
	}

	static _compileMethod = memoize(text =>
	{
		return Misc.compileExpr(text);
	});

	static _compileMethods(obj)
	{
		const fields = Object.values(obj);
		for(let i = 0; i < fields.length; i++)
		{
			const field = fields[i];
			if(field.methods)
			{
				const keys = Object.keys(field.methods);
				for(let j = 0; j < keys.length; j++)
				{
					const key = keys[j];
					field.methods[key].func = 
						memoize(CollectionLogic._compileMethod(field.methods[key].script));
				}
			}
		}
	}

	async update(from, to)
	{
		const diff = to.type === 'SCHEMA'? 
			Misc.diff(from.schema, to.schema):
			null;

		const res = await this.api.request(
			'PATCH', 
			`collections/${from.id}`, 
			{
				id: to.id,
				name: to.name,
				desc: to.desc,
				editable: to.editable,
				schema: diff,
				icon: to.icon,
				order: to.order,
				publicado: !!to.publishedAt,
				provider: to.provider && to.provider.id === ''? 
					null: 
					to.provider
			});

		if(res.errors)
		{
			return res;
		}

		const schemaObj = !diff? 
			to.schemaObj: 
			JSON.parse(to.schema);

		const collection = {
			...res.data, 
			schema: res.data.schema? 
				res.data.schema: 
				to.schema, 
			schemaObj: CollectionLogic.compile(schemaObj)
		};
		
		return {
			data: collection,
			errors: null
		};
	}

	async updateClasses(collection, classes)
	{
		const classesStr = JSON.stringify(classes);

		const res = await this.api.request(
			'PATCH', 
			`collections/${collection.id}/classes`, 
			{schema: classesStr});

		if(res.errors)
		{
			return res;
		}

		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}

	async updateIndexes(collection, indexes)
	{
		const indexesStr = JSON.stringify(indexes);

		const res = await this.api.request(
			'PATCH', 
			`collections/${collection.id}/indexes`, 
			{schema: indexesStr});

		if(res.errors)
		{
			return res;
		}

		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}

	async updateConstants(collection, consts)
	{
		const constsStr = JSON.stringify(consts);

		const res = await this.api.request(
			'PATCH', 
			`collections/${collection.id}/constants`, 
			{schema: constsStr});

		if(res.errors)
		{
			return res;
		}

		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}

	async updateFlows(collection, flows)
	{
		const flowsStr = JSON.stringify(flows);

		const res = await this.api.request(
			'PATCH', 
			`collections/${collection.id}/flows`, 
			{schema: flowsStr});

		if(res.errors)
		{
			return res;
		}

		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}

	async updateSchema(collection, toSchema, useDiff = true)
	{
		const toSchemaStr = JSON.stringify(toSchema);

		let res = null;
		if(useDiff)
		{
			const fromSchemaStr = collection.schema;
			const diff = Misc.diff(fromSchemaStr, toSchemaStr);

			if(!diff)
			{
				return {
					data: collection,
					errors: null
				};
			}
			
			res = await this.api.request(
				'PATCH', 
				`collections/${collection.id}/schema/diff`, 
				{
					diff: diff,
					returnSchema: false
				});
		}
		else
		{
			res = await this.api.request(
				'PATCH', 
				`collections/${collection.id}/schema`, 
				{
					schema: toSchemaStr,
					returnSchema: false
				});
		}

		if(res.errors)
		{
			return res;
		}

		const data = {
			...res.data,
			schema: toSchemaStr, 
			schemaObj: CollectionLogic.compile(toSchema),
		};

		return {
			data: data,
			errors: null
		};
	}

	getVersions(collection, page = 0, limit = 32)
	{
		return this.api.get(`collections/${collection.id}/versions?page=${page}&size=${limit}&sort=id,desc`);
	}

	async deleteVersions(collection, versions, ids)
	{
		for(let i = 0; i < ids.length; i++)
		{
			const id = ids[i];
			const res = await this.api.del(`collections/${collection.id}/versions/${id}`);
			if(res.errors)
			{
				return res;
			}
		}

		const res = {
			data: versions.filter(version => ids.findIndex(id => id === version.id) == 0),
			errors: null
		};
		
		return res;
	}

	async revertToVersion(collection, id)
	{
		const res = await this.api.patch(`collections/${collection.id}/versions/${id}`);
		if(res.errors)
		{
			return res;
		}
		
		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}

	async duplicate(collection, parent, options)
	{
		const res = await this.api.post(
			!parent? 
				`collections/${collection.id}/dup`: 
				`collections/${parent.id}/auxs/dup/${collection.id}?pos=${options.position}`,
			{
				withData: options.data? 1: 0,
				withPermissions: options.permissions? 1 : 0,
				withIntegrations: options.integrations? 1: 0,
				withAutomations: options.automations? 1: 0,
				withAuxs: options.auxs? 1: 0
			});		

		if(res.errors)
		{
			return res;
		}

		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}

	async delete(collection, parent)
	{
		const res = await this.api.del(
			!parent? 
				`collections/${collection.id}`: 
				`collections/${parent.id}/auxs/${collection.id}`);		

		return res;
	}

	createView(collection, views, index, type)
	{
		const schema = Misc.cloneObject(collection.schemaObj);

		const view = {
			type: type,
			id: this._genViewId(views),
			name: this._genViewName(schema.views),
			style: {
				height: this._genViewHeight(schema)
			},
			fields: {
			}
		};

		Object.entries(schema.columns).forEach(([name, column], i) => 
		{
			const field = view.fields[name] = {
				index: i,
				width: this._genColumnWidth(schema, name)
			};
			
			if(column.type === 'object')
			{
				const klass = CollectionUtil.getColumnClass(schema, name);
				if(klass)
				{
					field.fields = {};
					Object.entries(klass.props).forEach(([propName], j) => 
					{
						field.fields[propName] = {
							index: j
						};
					});
				}
			}
		});
		
		schema.views.splice(index+1, 0, view);

		return this.updateSchema(collection, schema);
	}

	async moveView(collection, fromIndex, toIndex, withSchema = null)
	{
		const schema = withSchema == null?
			{...collection.schemaObj}:
			withSchema;
		
		schema.views = Misc.moveArray(schema.views, fromIndex, toIndex);

		return this.updateSchema(collection, schema);
	}

	async duplicateView(collection, views, index)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		
		const view = Misc.cloneObject(schema.views[index]);
		view.id = this._genViewId(views);
		view.name = this._genViewName(schema.views, view.name);
		schema.views.push(view);
		
		if(schema.views.length > 2)
		{
			return this.moveView(collection, schema.views.length-1, index+1, schema);
		}
		else
		{
			return this.updateSchema(collection, schema);
		}
	}

	async deleteView(collection, index)
	{
		const schema = JSON.parse(JSON.stringify(collection.schemaObj));

		schema.views.splice(index, 1);

		return this.updateSchema(collection, schema);
	}

	_genViewId(views, prefix = 'view_')
	{
		let cnt = views.size + 1;
		let id = '';
		do
		{
			const suffix = ('000' + cnt);
			id = prefix + suffix.substr(suffix.length-3);
			++cnt;
		} while(views.has(id));

		return id;
	}

	_genViewName(views, prefix = 'View-')
	{
		let cnt = views.length + 1;
		let name = '';
		do
		{
			const suffix = '000' + cnt;
			++cnt;
			name = prefix + suffix.substr(suffix.length-3);
		} while(views.find(g => g.name.toLowerCase() === name.toLowerCase()));

		return name;
	}	

	_genViewHeight(schema)
	{
		if(!schema.views)
		{
			return 40;
		}

		return Math.floor(schema.views.reduce((acc, view) => acc + view.style.height, 0) / schema.views.length);
	}

	_genColumnWidth(schema, name)
	{
		if(!schema.views)
		{
			return 60;
		}

		let acc = 0;
		let cnt = 0;
		schema.views.forEach(view => 
		{
			if(view.fields)
			{
				const field = view.fields[name];
				if(field && field.width)
				{
					acc += field.width;
					cnt += 1;
				}
			}
		});

		return cnt > 0? Math.floor(acc / cnt): 60;
	}

	async createField(collection, type, at)
	{
		const res = await this.api.post(`collections/${collection.id}/columns`, {
			type: type,
			index: at,
			nullable: true,
			sortable: true,
		});

		if(res.errors)
		{
			return res;
		}

		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}
	
	async updateFields(collection, fields)
	{
		const fieldsStr = JSON.stringify(fields);

		const res = await this.api.request(
			'PATCH', 
			`collections/${collection.id}/columns`, 
			{schema: fieldsStr});

		if(res.errors)
		{
			return res;
		}

		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}

	async updateField(collection, key, field)
	{
		const res = await this.api.patch(
			`collections/${collection.id}/column/${key}`,
			{
				column: field
			});		

		if(res.errors)
		{
			return res;
		}

		const data = {
			collection: this.transform(res.data.collection),
			key: res.data.extra
		};

		return {
			data: data,
			errors: null
		};
	}

	async deleteField(collection, key)
	{
		const res = await this.api.del(`collections/${collection.id}/columns/${key}`);

		if(res.errors)
		{
			return res;
		}

		const data = this.transform(res.data);

		return {
			data: data,
			errors: null
		};
	}

	deleteFieldFromRows(rows, key)
	{
		const allButDeleted = ({ [key]: deleted, ...rest }) => rest;
			
		if(rows && rows.size > 0)
		{
			for(const entry of rows)
			{
				const index = entry[0];
				const row = entry[1];
				rows.set(
					index, 
					{
						...row,
						item: allButDeleted(row.item),
					});
			}
		}

		return rows;
	}

	getViewOnSchemaById(schema, id)
	{
		return schema.views.find(v => v.id === id);
	}

	moveField(collection, viewId, user, parentKey, from, to)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		const view = this.getViewOnSchemaById(schema, viewId);
		const fields = !parentKey? 
			view.fields:
			view.fields[parentKey].fields;

		const sorted = CollectionUtil
			.getVisibleFieldsNamesSorted(schema, fields, user, null, parentKey);

		for(let i = 0; i < sorted.length; i++)
		{
			fields[sorted[i]].index = i;
		}
		
		if(from > to)
		{
			for(let i = from-1; i >= to; i--)
			{
				++fields[sorted[i]].index;
			}
		}
		else
		{
			for(let i = from+1; i < to; i++)
			{
				--fields[sorted[i]].index;
			}
		}

		fields[sorted[from]].index = to;

		return this.updateSchema(collection, schema);
	}

	hideField(collection, viewId, key)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		const view = this.getViewOnSchemaById(schema, viewId);
		const fields = view.fields;

		const field = Misc.getFieldValue(fields, key);
		
		field.hidden = true;
		delete field.frozen;

		return this.updateSchema(collection, schema);
	}

	showField(collection, references, viewId, key, index, sorted)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		const view = this.getViewOnSchemaById(schema, viewId);
		const isSubField = key.indexOf('.') !== -1;
		const parentKey = !isSubField?
			key:
			Misc.getBaseName(key);
		const fields = !isSubField? 
			view.fields:
			view.fields[parentKey].fields;

		for(let i = 0; i < sorted.length; i++)
		{
			const f = fields[sorted[i]];
			f.index = i + (f.index >= index? 1: 0);
		}

		const fieldKey = Misc.getFieldName(key);
		let field = fields[fieldKey];
		if(!field)
		{
			field = fields[fieldKey] = {};
		}

		const column = CollectionUtil.getColumn(schema, key, references);
		if(column.class)
		{
			if(!field.fields)
			{
				field.fields = {};
				Object.keys(CollectionUtil.getClassByName(schema, column.class).props).forEach((propName, index) =>
				{
					field.fields[propName] = {
						index: index
					};
				});
			}
		}
		
		field.index = index;
		
		if(field.hidden)
		{
			delete field.hidden;
		}

		if(column.hidden)
		{
			delete column.hidden;
		}

		return this.updateSchema(collection, schema);
	}	

	async loadAutomations(collection, automations, options = {})
	{
		const res = await this.api.get(
			`collections/${collection.id}/automations?page=${options.offset}&size=${options.size}&sort=${options.sort},${!options.asc? 'desc': 'asc'}`);

		if(res.errors)
		{
			return res;
		}

		return {
			data: automations.concat(res.data),
			errors: null
		};
	}

	async loadIntegrations(collection, integrations, options = {})
	{
		const res = await this.api.get(
			`collections/${collection.id}/integrations?page=${options.offset}&size=${options.size}&sort=${options.sort},${!options.asc? 'desc': 'asc'}`);

		if(res.errors)
		{
			return res;
		}

		return {
			data: integrations.concat(res.data),
			errors: null
		};
	}

	_genReportName(reports, base = 'Report')
	{
		let i = reports.length;
		let name = '';
		do
		{
			const num = '000' + i;
			++i;
			name = base + (i > 0? num.substr(num.length-3): '');
		} while(reports.find(r => r.name.toLowerCase() === name.toLowerCase()));

		return name;
	}

	updateReport(collection, index, report)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		if(!schema.reports)
		{
			schema.reports = [report];
		}
		else
		{
			schema.reports[index] = report;
		}

		return this.updateSchema(collection, schema);
	}	

	createReport(collection, report, at)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		const reports = schema.reports || [];
		
		report.name = this._genReportName(reports, report.name);
		report.id = Misc.genId();
		reports.push(report);
		
		if(reports.length > 2)
		{
			schema.reports = Misc.moveArray(schema.reports, reports.length-1, at+1);
		}
			
		return this.updateSchema(collection, schema);
	}

	generateReport(schema, id, at = null)
	{
		const report = {
			id: id,
			name: '',
			description: '',
			columns: {},
			filter: {},
			order: {}
		};

		if(schema.reports)
		{
			if(at + 1 < schema.reports.length)
			{
				schema.reports.splice(at + 1, 0, report);
			}
			else
			{
				schema.reports.push(report);
			}
		}
		else
		{
			schema.reports = [report];
		}

		return report;
	}

	duplicateReport(collection, index)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		const reports = schema.reports;
		
		const report = Misc.cloneObject(reports[index]);
		report.id = Misc.genId();
		report.name = this._genReportName(reports, report.name);
		reports.push(report);
		
		if(reports.length > 2)
		{
			schema.reports = Misc.moveArray(reports, reports.length-1, index+1);
		}
			
		return this.updateSchema(collection, schema);
	}

	moveReport(collection, from, to)
	{
		const schema = Object.assign({}, collection.schemaObj);
		
		schema.reports = Misc.moveArray(schema.reports, from, to);

		return this.updateSchema(collection, schema);
	}
	
	deleteReport(collection, at)
	{
		const schema = Misc.cloneObject(collection.schemaObj);

		schema.reports.splice(at, 1);

		return this.updateSchema(collection, schema);
	}

	_genFormName(forms, base = 'Form')
	{
		let i = forms.length;
		let name = '';
		do
		{
			const num = '000' + i;
			++i;
			name = base + (i > 0? num.substr(num.length-3): '');
		} while(forms.find(r => r.name.toLowerCase() === name.toLowerCase()));

		return name;
	}

	createForm(collection, form, at)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		const forms = schema.forms || [];

		form.id = Misc.genId();
		forms.push(form);
		
		if(forms.length > 2)
		{
			schema.forms = Misc.moveArray(schema.forms, forms.length-1, at+1);
		}
			
		return this.updateSchema(collection, schema);
	}

	_genFormElements(schema)
	{
		const buildEmpty = (md) =>
		{
			return {
				type: 'empty',
				widths: {
					xs: 12,
					md: md
				},
				height: '4rem'
			};
		};

		const fields = schema?
			Object.entries(schema.columns)
				.filter(([, field]) => !field.hidden)
				.map(([name, ]) => ({
					type: 'field',
					widths: {xs: 12, md: 6},
					name: name,
					label: null,
					desc: '',
					default_: null,
					placeholder: '',
				})):
			[];
	
		const res = [];
		for(let i = 0; i < fields.length; i += 2)
		{
			res.push({
				type: 'row',
				elements: i < fields.length - 1? 
					[fields[i], fields[i+1]]:
					[fields[i], buildEmpty(6)]
			});
		}

		if(res.length === 0)
		{
			res.push({
				type: 'row',
				elements: [buildEmpty(12)]
			});
		}
	
		return res;
	}

	generateForm(collection, at = 0)
	{
		const schema = Misc.cloneObject(collection.schemaObj);

		const form = {
			id: null,
			name: this._genFormName(schema.forms || []),
			desc: '',
			use: 'any',
			active: true,
			public: false,
			disabled: false,
			steps: [{
				elements: this._genFormElements(schema)
			}],
		};
		
		if(schema.forms)
		{
			if(at + 1 < schema.forms.length)
			{
				schema.forms.splice(at + 1, 0, form);
			}
			else
			{
				schema.forms.push(form);
			}
		}
		else
		{
			schema.forms = [form];
		}
		
		return form;
	}
	
	updateForm(collection, index, form)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		if(!schema.forms)
		{
			schema.forms = [form];
		}
		else
		{
			schema.forms[index] = form;
		}

		return this.updateSchema(collection, schema);
	}	

	moveForm(collection, from, to)
	{
		const schema = Object.assign({}, collection.schemaObj);
		
		schema.forms = Misc.moveArray(schema.forms, from, to);

		return this.updateSchema(collection, schema);
	}
	
	duplicateForm(collection, index)
	{
		const schema = Misc.cloneObject(collection.schemaObj);
		const forms = schema.forms;
		
		const form = Misc.cloneObject(forms[index]);
		form.id = Misc.genId();
		form.name = this._genReportName(forms, form.name);
		forms.push(form);
		
		if(forms.length > 2)
		{
			schema.forms = Misc.moveArray(forms, forms.length-1, index+1);
		}
			
		return this.updateSchema(collection, schema);
	}

	createFormStep(steps, at)
	{
		const clone = Array.from(steps);
		
		const step = {
			elements: this._genFormElements()
		};

		if(at + 1 < clone.length)
		{
			clone.splice(at + 1, 0, step);
		}
		else
		{
			clone.push(step);
		}

		return clone;
	}

	duplicateFormStep(steps, index)
	{
		let clone = Array.from(steps);
		
		const step = Misc.cloneObject(clone[index]);
		clone.push(step);
		
		if(clone.length > 2)
		{
			clone = Misc.moveArray(clone, clone.length-1, index+1);
		}
			
		return clone;
	}

	moveFormStep(steps, from, to)
	{
		const clone = Misc.moveArray(steps, from, to);

		return clone;
	}

	deleteFormStep(steps, at)
	{
		const clone = Array.from(steps);
		
		clone.splice(at, 1);

		return clone;
	}

	deleteForm(collection, at)
	{
		const schema = Misc.cloneObject(collection.schemaObj);

		schema.forms.splice(at, 1);

		return this.updateSchema(collection, schema);
	}

	async loadAuxs(collection, offset = 0, size = 32)
	{
		const res = await this.api.get(`collections/${collection.id}/auxs?page=${offset}&size=${size}`);
		if(res.errors)
		{
			return res;
		}

		const auxs = [];
		for(let i = 0; i < res.data.length; i++)
		{
			const aux = this.transform(res.data[i]);
			auxs.push({
				collection: aux,
				references: await CollectionLogic.loadReferences(aux.schemaObj)
			});
		}

		return {
			data: auxs,
			errors: null
		};
	}

	async createAux(collection, templateId, at)
	{
		const res = await this.api.post(
			`collections/${collection.id}/auxs/gen/${templateId}${at !== null? '?pos=' + at: ''}`);
		if(res.errors)
		{
			return res;
		}

		const aux = this.transform(res.data);

		return {
			data: aux,
			errors: null
		};
	}

	moveAux(collection, aux, dir)
	{
		return this.api.patch(`collections/${collection.id}/auxs/${aux.id}/dir/${dir}`);
	}

	getSessionId()
	{
		return this.api.getSessionId();
	}

	subscribe(collection, onMessage)
	{
		return this.api.subscribe(`collections.${collection.id}`, onMessage);
	}

	unsubscribe(subscription)
	{
		this.api.unsubscribe(subscription);
	}
	
	transformItem(fields, classes, item)
	{
		const keys = Object.keys(item);
		for(let i = 0; i < keys.length; i++)
		{
			const key = keys[i];
			const value = item[key];
			if(value !== null)
			{
				const field = fields[key];
				if(field)
				{
					switch(field.type)
					{
					case 'date':
						item[key] = new Date(value);
						break;

					case 'object':
					{
						const klass = classes[field.class];
						if(klass)
						{
							item[key] = this.transformItem(klass.props, classes, value);
						}
						break;
					}

					default:
						break;
					}
				}
			}
		}
		
		return item;
	}
	
	transformItems(collection, items)
	{
		const schema = collection.schemaObj;
		const fields = schema.columns;
		const classes = schema.classes;
		
		const res = [];
		for(let i = 0; i < items.length; i++)
		{
			res.push(this.transformItem(fields, classes, items[i]));
		}

		return res;
	}

	async loadItems(collection, offset, size, options = {})
	{
		const filters = options.filters? 
			options.filters: 
			null;
		const sortBy = options.sortBy;

		const filtersStr = encodeURIComponent(JSON.stringify(filters));
		const sortStr = 
			(options.groupBy? 
				[`sort=${options.groupBy.column},asc`]: [])
				.concat(
					(sortBy? 
						Object.entries(sortBy).map(([field, value]) => `sort=${field},${value.dir}`): 
						[])
				)
				.join('&');

		this.abortController = new AbortController();
		
		const res = await this.api.get(
			`collections/${collection.id}/items?page=${offset}&size=${size}&${sortStr}${filters? '&filters=' + filtersStr: ''}`,
			{abortController: this.abortController});
		
		this.abortController = null;

		return res.errors === null? 
			this.transformItems(collection, res.data): 
			[];
	}

	itemToRow(item, base, index, checkIfMoved)
	{
		return {
			item: item, 
			num: base + index,
			group: null,
			selected: false,
			appended: null,
			dirty: false,
			moved: checkIfMoved? 
				checkIfMoved(item, base+index): 
				false,
			rendered: false,
		};
	}

	transformRows(collection, rows, offset, size, items, options = {})
	{
		const idColumn = CollectionUtil.ID_NAME;
		const doMovedCheck = options.isReorder;
		
		const checkIfMoved = (item, num) => 
		{
			const row = rows.get(num-1);
			if(!row)
			{
				return false;
			}
			
			return row.item[idColumn] !== item[idColumn];
		};
		
		if(!options.groupBy)
		{
			for(let i = 0; i < items.length; i++)
			{
				const item = items[i];
				rows.set(
					offset+i, 
					this.itemToRow(
						item, 
						offset+1, 
						i, 
						doMovedCheck? 
							checkIfMoved: 
							null
					)
				);
			}
		}
		else
		{
			const field = options.groupBy.column;
			const isArray = collection.schemaObj.columns[field].type === 'array';

			const prevRow = offset === 0? 
				null: 
				rows.get(offset-1);
			const prevNum = !prevRow? 
				1: 
				prevRow.num + 1;
			let prevGroupName = !prevRow? 
				undefined: 
				Misc.getFieldValue(prevRow.item, field);
			
			if(prevGroupName && isArray)
			{
				prevGroupName = prevGroupName.join(', ');
			}

			let group = !prevRow? 
				null: 
				prevRow.group;

			for(let i = 0; i < items.length; i++)
			{
				const item = items[i];
				let groupName = Misc.getFieldValue(item, field);
				if(groupName && isArray)
				{
					groupName = groupName.join(', ');
				}

				if(groupName !== prevGroupName)
				{
					group = {
						type: 'group',
						id: Misc.genId(), 
						name: groupName, 
						expanded: true
					};
					prevGroupName = groupName;
				}

				rows.set(
					offset+i,
					{
						item: item, 
						num: prevNum + i, 
						group: group,
						selected: false,
						appended: null,
						dirty: false,
						moved: doMovedCheck? 
							checkIfMoved(item, prevNum + i): 
							false,
						rendered: false
					}
				);
			}
		}

		if(items.length < size)
		{
			for(let i = items.length; i < size; i++)
			{
				rows.delete(offset+i);
			}
		}
		
		return rows;
	}

	getFieldValueFromRef(ref)
	{
		const fields = ref.preview.columns
			.reduce((obj, name) => {
				obj[name] = ''; 
				return obj;
			}, {});


		return {
			_id: '', 
			...fields
		};
	}

	getFieldDefaultValue(field, user)
	{
		const def = field.default;
		if(!def)
		{
			return null;
		}

		if(def.value)
		{
			return def.value;
		}

		if(def.function)
		{
			switch(def.function)
			{
			case 'nowAsDate':
				return new Date();
			case 'currentUser':
				return user? 
					user.email:
					'';
			}
		}
		
		//NOTE: script syntax is java-ish, so let it execute at server-side
		return null;
	}

	genFieldValue(schema, field, user)
	{
		const ref = field.ref && schema.refs[field.ref.name];

		return !ref? 
			this.getFieldDefaultValue(field, user): 
			this.getFieldValueFromRef(ref);
	}

	genAppendedItem(collection, references, view, user)
	{
		const schema = collection.schemaObj;
		const item = {};

		Object.entries(view.fields).forEach(([key, field]) =>
		{
			const column = CollectionUtil.getColumn(schema, key, references);

			if(!CollectionUtil.isColumnHidden(column, field, user))
			{
				item[key] = this.genFieldValue(schema, column, user);
			}
		});

		return item;
	}

	async loadRows(collection, references, view, user, rows, options = {})
	{
		const isEmpty = rows.size === 0;

		if(rows.size < options.offset + options.size)
		{
			for(let i = options.offset; i < options.offset + options.size; i++)
			{
				rows.set(
					i, 
					{
						num: i+1,
						item: 'LOADING'
					}
				);
			}
		}

		let items = await this.loadItems(collection, options.offset, options.size, options);
		if(options.offset === 0 && isEmpty && items.length === 0)
		{
			items = [this.genAppendedItem(collection, references, view, user)];
		}

		const transformed = this.transformRows(
			collection, rows, options.offset, options.size, items, options);

		return {
			data: transformed, 
			allLoaded: items.length < options.size,
			errors: null
		};
	}

	async reloadRows(collection, references, view, user, rows, options = {})
	{
		const pages = rows.size === 0 || options.restart? 
			1: 
			Math.ceil(rows.size / options.rowsPerPage);

		const res = await this.loadRows(
			collection, 
			references, 
			view, 
			user, 
			rows, {
				...options,
				offset: 0,
				size: pages * options.rowsPerPage
			});		
		
		return res;
	}

	sortRows(collection, references, rows, options)
	{
		const schema = collection.schemaObj;
	
		let sorted = Misc.mapValues(rows);

		const keys = Object.keys(options.sortBy);
		for(let i = keys.length - 1; i >= 0; i--)
		{
			const key = keys[i];
			const dir = options.sortBy[key].dir === 'asc'? 1: -1;
			sorted = sorted.sort((a, b) => 
			{	
				const column = CollectionUtil.getColumn(schema, key, references);
				let aval = CollectionUtil.getColumnValue(column, a.item, key);
				let bval = CollectionUtil.getColumnValue(column, b.item, key);
				if(aval === null || aval === undefined)
				{
					return bval === null || bval === undefined? 0: -dir;
				}
				else if(bval === null || bval === undefined)
				{
					return dir;
				}

				switch(column.type || 'string')
				{
				case 'string':
					aval = aval.toLowerCase();
					bval = bval.toLowerCase();
					break;
				}
				
				return aval === bval? 0: (aval < bval? -dir: dir);
			});
		}

		sorted.forEach((row, index) => 
		{
			const moved = options.isReorder && row.num !== 1+index;
			rows.set(
				index,
				{
					...row,
					appended: null, 
					moved: moved, 
					rendered: moved? false: row.rendered, 
					num: 1+index
				}
			);
		});

		return {
			data: rows,
			errors: null
		};
	}

	updateItem(item, data)
	{
		let res = Object.assign({}, item);
		const keys = Object.keys(data);
		for(let i = 0; i < keys.length; i++)
		{
			const key = keys[i];
			const value = data[key];
			if(res[key] === undefined || res[key] === null)
			{
				res[key] = value;
			}
			else
			{
				if(res[key].constructor !== Object)
				{
					res[key] = value;
				}
				else
				{
					if(value === null || value.constructor !== Object)
					{
						res[key] = value;
					}
					else
					{
						const props = Object.keys(value);
						for(let j = 0; j < props.length; j++)
						{
							const prop = props[j];
							res[key][prop] = value[prop];
						}
					}
				}
			}
		}

		return res;
	}

	deleteRow(id, counters, rows)
	{
		const idColumn = CollectionUtil.ID_NAME;

		let from = -1;
		for(let i = 0; i < rows.size && from === -1; i++)
		{
			const row = rows.get(i);
			if(row.item[idColumn] === id)
			{
				from = i;
				if(row.selected)
				{
					--counters.selectedCnt;
				}

				if(row.appended)
				{
					--counters.appendedCnt;
				}
			}
		}

		if(from > -1)
		{
			if(from < rows.size-1)
			{
				for(let i = from; i < rows.size-1; i++)
				{
					const row = rows.get(i+1);
					--row.num;
					rows.set(i, row);
				}
			}
		}

		rows.delete(rows.size-1);

		return rows;
	}

	async deleteRows(collection, references, view, counters, user, rows, toRemove)
	{
		const idColumn = CollectionUtil.ID_NAME;

		for(let i = 0; i < toRemove.length; i++)
		{
			const row = toRemove[i];
			const id = row.item[idColumn];

			const res = await this.api.del(`collections/${collection.id}/items/${id}`);
			if(res.errors)
			{
				return res;
			}
				
			rows = this.deleteRow(id, counters, rows);
		}

		if(rows.size === 0)
		{
			rows = this.transformRows(
				collection, 
				new Map(), 
				0, 
				1, 
				[this.genAppendedItem(collection, references, view, user)]);
		}

		return {
			data: rows,
			errors: null
		};
	}

	async createRow(collection, references, views, view, counters, user, rows, at, options)
	{
		const isFirst = at.num-1 === 0 && at.appended === null;
		
		let item = isFirst? 
			at.item:
			at.appended.item;
	
		const res = await this.api.request(
			'POST', 
			`collections/${collection.id}/items`, 
			{
				vars: item
			});

		if(res.errors)
		{
			return res;
		}
	
		item = this.updateItem(item, res.data);
		
		let row = null;
		if(!isFirst)
		{
			rows.set(
				at.num-1, 
				{
					...at, 
					appended: null
				}
			);

			if(!options.isPasted)
			{
				--counters.appendedCnt;
			}
			
			row = this.itemToRow(item, rows.size, 1, false);
		}
		else
		{
			row = this.itemToRow(item, 0, 1, false);
		}
		
		rows.set(row.num-1, row);

		return this.migrateOrReloadRow(
			collection, 
			references, 
			views,
			view, 
			counters, 
			user, 
			rows, 
			row, 
			{
				...options,
				reorder: true, 
				reset: true
			}
		);
	}

	async updateAppendedRow(collection, references, rows, row, item, key, value)
	{
		const isFirst = row.num-1 === 0 && row.appended === null;
		
		const field = CollectionUtil.getColumn(collection.schemaObj, key, references);
		if(field.ref)
		{
			const refName = field.ref.name;
			const refField = `${refName}-${key}`;
			if(!(refField in item) || item[refField]._id !== value)
			{
				const res = await this.api.get(`collections/${references[refName].id}/items/${value}`);
				if(res.errors)
				{
					return res;
				}
					
				item[refField] = res.data;
			}
		}

		Misc.setFieldValue(item, key, value);
		
		rows.set(
			row.num-1, 
			{
				...row, 
				appended: !isFirst?
					{
						item: item
					}:
					null, 
				dirty: true
			}
		);

		return {
			errors: null,
			data: rows,
		};
	}

	async updateRow(
		collection, references, views, view, counters, user, rows, row, item, key, value, options)
	{
		const id = item[CollectionUtil.ID_NAME];

		if(!id)
		{
			return this.updateAppendedRow(
				collection, references, rows, row, item, key, value);
		}
		
		let varName = key;
		let varValue = value;
		const p = key.indexOf('.');
		if(p >= 0)
		{
			varValue = {[key.substr(p+1)]: value};
			varName = key.substr(0, p);
		}

		const res = await this.api.request(
			'PATCH', 
			`collections/${collection.id}/items/${id}`, 
			{
				vars: {
					[varName]: varValue
				}
			});

		if(res.errors)
		{
			return res;
		}

		item = this.updateItem(item, res.data);
		row = {...row, item: item};
		rows.set(row.num-1, row);

		return this.migrateOrReloadRow(
			collection, 
			references, 
			views, 
			view, 
			counters, 
			user, 
			rows, 
			row, 
			{
				...options,
				reorder: !!options.sortBy[key], 
				reset: false
			});
	}	

	moveRow(collection, counters, rows, row, dir)
	{
		const i = row.num;

		let toRow = null;
		if(dir === 'up')
		{
			toRow = i-1 <= 0? 
				null: 
				rows.get(i-1-1);
		}
		else
		{
			toRow = i+1 >= rows.size? 
				null: 
				rows.get(i-1+1);
		}

		if(!toRow)
		{
			return {
				errors: ['Source and destine are the same']
			};
		}

		return this.moveItem(collection, counters, rows, row.item, toRow.item);
	}

	_getFieldByName(name, collection, references)
	{
		return CollectionUtil.getColumn(collection.schemaObj, name, references);
	}

	async migrateOrReloadRow(
		collection, references, views, view, counters, user, rows, row, options = {reorder: false, reset: false})
	{
		const idColumn = CollectionUtil.ID_NAME;
		const item = row.item;
		const id = item[idColumn];
		let migrated = false;
		let moved = false;
		
		if(view.meta.filters)
		{
			for(let i = 0; i < view.meta.filters.length; i++)
			{
				const filter = view.meta.filters[i];
				const key = filter.name;
				
				const value = Misc.getFieldValue(item, key);
				if(!CollectionUtil.executeBop(filter.op, value, filter.value))
				{
					const field = this._getFieldByName(key, collection, references);
					this.migrateItemToOtherViews(views, view, item, key, field, value, options);
					
					rows = this.deleteRow(id, counters, rows);
					
					migrated = true;
					break;
				}
			}
		
			if(rows.size === 0)
			{
				rows = this.transformRows(
					collection, 
					new Map(), 
					0, 
					1, 
					[this.genAppendedItem(collection, references, view, user)]
				);
			}
		}

		if(!migrated)
		{
			if(options.reorder)
			{
				const fromPos = row.num;
				let res = null;
				if(!view.meta.allLoaded || view.groupBy)
				{
					res = await this.reloadRows(collection, references, view, user, rows, options);
				}
				else
				{
					res = this.sortRows(collection, references, rows, {...options, isReorder: true});
				}

				if(res.errors)
				{
					return res;	
				}

				rows = res.data;
				if(rows !== null)
				{
					const row = Misc.mapValues(rows)
						.find(row => row.item[idColumn] === id);
					const toPos = row? row.num: fromPos;
					moved = fromPos !== toPos;
				}
			}
		}

		return {
			errors: null,
			data: rows,
			migrated,
			moved
		};
	}

	migrateItemToOtherViews(views, from, item, key, field, value, options)
	{
		for(let [, view] of views)
		{
			if(view.id !== from.id)
			{
				let migrated = false;
				if(view.meta.filters)
				{
					const filter = view.meta.filters.find(f => f.name === key);
					if(filter)
					{
						migrated = CollectionUtil.executeBop(
							filter.op, filter.value, value, field && field.type);
					}
				}
				else
				{
					migrated = true;
				}

				if(migrated)
				{
					const itemIds = (options.migrations.get(view.id) || []);
					itemIds.push(item[CollectionUtil.ID_NAME]);
					options.migrations.set(view.id, itemIds);
				}
			}
		}
	}

	async createItem(collection, item)
	{
		const res = await this.api.post(`collections/${collection.id}/items`, 
			{vars: item});
		
		return res;
	}

	async createItemsFromCSV(collection, csv, replaceExisting = true)
	{
		const res = await this.api.post(`collections/${collection.id}/items/csv`, {
			csv: csv,
			substituirSeExistir: replaceExisting,
		});

		return res;
	}

	async moveItem(collection, counters, rows, from, to)
	{
		const posId = collection.positionalId;
		const colId = CollectionUtil.ID_NAME;
		
		const fromPos = from[posId];
		const toPos = to[posId];

		const res = await this.api.request(
			'PATCH', `collections/${collection.id}/items/${from[colId]}/loc/${toPos}`);
		if(res.errors)
		{
			return res;
		}

		let first = toPos;
		let last = fromPos + (fromPos > toPos? -1: +1);
		if(first > last)
		{
			let temp = first;
			first = last;
			last = temp;
		}
		const inc = fromPos > toPos? +1: -1;

		const arr = Misc.mapValues(rows);

		const fromId = from[colId];
		const toId = to[colId];
		const toNum = arr.find(row => row.item[colId] === toId).num;
		let unappendedCnt = 0;

		const moved = arr.map(row => 
		{
			const item = row.item;
			if(item[posId] >= first && item[posId] <= last)
			{
				row.num += inc;
				item[posId] += inc;
				if(row.appended)
				{
					++unappendedCnt;
					row.appended = null;
				}
			}

			if(item[colId] === fromId)
			{
				item[posId] = toPos;
				row.num = toNum;
				if(row.appended)
				{
					++unappendedCnt;
					row.appended = null;
				}
			}

			return row;
		});

		counters.appendedCnt -= unappendedCnt;

		const sorted = new Map(
			moved.sort((a, b) => a.num - b.num)
				.map((row, index) => [index, row])
		);

		return {
			data: sorted,
			errors: null
		};
	}	
}

export default CollectionLogic;