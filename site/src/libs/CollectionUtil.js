import React from 'react';
import dayjs from 'dayjs';
import Rating from 'react-rating';
import memoize from 'fast-memoize';
import 'inputmask/lib/extensions/inputmask.numeric.extensions';
import Inputmask from 'inputmask/lib/extensions/inputmask.date.extensions';
import Misc from './Misc';

Inputmask.extendDefaults({
	'autoUnmask': true
});

const DATE_FORMAT = 'DD/MM/YYYY HH:mm:ss';

export default class CollectionUtil
{
	static DEFAULT_BG_COLOR = 'grey';
	static DEFAULT_BORDER_COLOR = 'white';
	static DEFAULT_WIDTH = 100;
	static DEFAULT_HEIGHT = 40;
	static MIN_WIDTH = 80;
	static MAX_WIDTH = CollectionUtil.MIN_WIDTH*5;
	static ORDER_WIDTH = 60;
	static PAGE_SIZE = 20;
	static HEADER_HEIGHT = 60;
	static FILTERS_HEIGHT = 60;
	static GROUP_HEIGHT = 60;
	static GROUP_AGGREGATES_HEIGHT = 30;
	static ID_NAME = '_id';
	static INTERNAL_STORAGE_PROVIDER_NAME = 'internalStorageProvider';

	static TRIGGER_OPTIONS_ALLOW_FREQ = 1;
	
	static transformColumns(schema, columns, options = {})
	{
		let res = [];

		Object.entries(columns)
			.forEach(([name, col]) => 
			{
				if(col.class)
				{
					if(!options.skipObjects)
					{
						const props = schema.classes[col.class].props;
						res = res.concat(
							this.transformColumns(
								schema, 
								props, 
								{
									baseName: name, 
									baseLabel: col.label || name
								}));
					}
				}
				else
				{
					if(!col.ref || !options.skipRefs)
					{
						res.push({
							name: (options.baseName? options.baseName + '.': '') + name, 
							label: (options.baseLabel? options.baseLabel + '.': '') + (col.label || name), 
							type: col.type, 
							subtype: col.subtype,
							auto: col.auto,
							ref: col.ref
						});
					}
				}
			});
		
		return res;
	}

	static getColumn(schema, name, references)
	{
		const {columns, refs} = schema;

		const p = name.indexOf('.');
		if(p < 0)
		{
			if(name !== this.ID_NAME)
			{
				return columns[name];
			}
			else
			{
				return {
					type: 'string',
					label: '_Id',
					unique: true,
					sortable: true,
					hidden: true
				};
			}
		}
		
		const baseName = name.substr(0, p);
		const column = columns[baseName];
		if(column && column.class)
		{
			const klass = schema.classes[column.class];
			if(!klass)
			{
				return null;
			}
			
			return klass.props[name.substr(p+1)];
		}

		if(!refs || !references)
		{
			return null;
		}
		
		const ref = refs[baseName];
		if(!ref)
		{
			return null;
		}

		const fkName = name.substr(p+1);
		return references[baseName].schemaObj.columns[fkName];
	}

	static getColumnLabel(schema, name, references)
	{
		const {columns, refs} = schema;

		const p = name.indexOf('.');
		if(p < 0)
		{
			return (columns[name] && columns[name].label) || name;
		}

		const baseName = name.substr(0, p);
		let column = columns[baseName];
		if(column && column.class)
		{
			const klass = schema.classes[column.class];
			if(klass)
			{
				const propName = name.substr(p+1);
				const props = klass.props[propName];
				return (column.label || baseName) + '.' + (props.label || propName);
			}
		}
				
		if(!refs)
		{
			return name;
		}
		
		const ref = refs[baseName];
		if(!ref)
		{
			return name;
		}

		const refSchema = references? references[baseName].schemaObj: null;
		const fkName = name.substr(p+1);
		const propName = refSchema && refSchema.columns[fkName]? 
			refSchema.columns[fkName].label: 
			null;
		return propName || fkName;
	}

	static getClassByName(schema, name)
	{
		if(!name)
		{
			return null;
		}

		return schema.classes && schema.classes[name];
	}

	static getColumnClass(schema, name)
	{
		return this.getClassByName(schema, schema.columns[name].class);
	}

	static getColumnValue(column, item, name)
	{
		let value = null;
		const {ref, methods} = column;
		if(!ref)
		{
			value = Misc.getFieldValue(item, name);
		}
		else
		{
			const refValue = Misc.getFieldValue(item, ref.name + '-' + name);
			value = refValue? refValue[ref.display]: null;
		}

		if(methods && methods.toString)
		{
			value = methods.toString.func({'props': value});
		}
	
		return value;
	}

	static format = memoize((value, column, createElement = true) =>
	{
		if(!value)
		{
			return null;
		}

		switch(column.type)
		{
		case 'date':
			return dayjs(value).format('DD/MM/YYYY HH:mm:ss');
		
		case 'array':
			return value.join(', ');

		case 'object':
			return JSON.stringify(value);

		case 'enumeration':
			if(column.options)
			{
				const index = column.options.findIndex(option => 
				{
					const p = option.indexOf('|');
					return value === (p !== -1?
						option.substring(0, p):
						option);
				});
	
				if(index !== -1)
				{
					const option = column.options[index];
					const p = option.indexOf('|');
					return p !== -1?
						option.substring(p+1):
						option;
				}
			}
			break;
		}

		switch(column.component)
		{
		case 'rating':
			if(createElement)
			{
				return React.createElement(Rating,
					{
						initialRating: value,
						fractions: 2,
						emptySymbol: 'la la-star-o',
						fullSymbol: 'la la-star',
						readonly: true
					}, null);
			}
			else
			{
				return value;
			}
		}

		if(!column.mask)
		{
			return value;
		}
		
		const options = {
			groupSeparator: '.',
			radixPoint: ',',
		};

		switch(column.mask)
		{
		case 'money:USD':
			options.alias = 'currency';
			options.prefix = '$ ';
			break;
		case 'money:BRL':
			options.alias = 'currency';
			options.prefix = 'R$ ';
			break;
		default:
			options.mask = column.mask.replace(/#/g, '9');
			break;
		}

		if(value.constructor !== String)
		{
			value = value.toString();
		}

		return Inputmask.format(value, options);
	});


	static getValueFormatted(value, column, klass, createElement = true)
	{
		try
		{
			const methods = klass? klass.methods: null;
			if(methods && methods.toString)
			{
				value = methods.toString.func({'props': value || {}});
			}
	
			return CollectionUtil.format(value, column, createElement);
	
		}
		catch(e)
		{
			return e.message;
		}
	}
	
	static isColumnIndexed(schema, name, references)
	{
		if(schema.indexes && 
			schema.indexes.find(i => i.columns.indexOf(name) > -1)? true: false)
		{
			return true;
		}

		const p = name.indexOf('.');
		if(!references || p === -1)
		{
			return false;
		}

		const baseName = name.substr(0, p);
		if(!references[baseName])
		{
			return false;
		}
		const fkName = name.substr(p+1);
		const refSchema = references[baseName].schemaObj;
		return refSchema.indexes && 
			refSchema.indexes.find(i => i.columns.indexOf(fkName) > -1)? true: false;
	}

	static isColumnSortable(column)
	{
		return column.sortable || column.unique || column.positional;
	}

	static getField(view, name, isObject = false)
	{
		const p = name.indexOf('.');
		if(isObject && p >= 0)
		{
			const baseName = name.substr(0, p);
			const field = view.fields[baseName];
			if(!field)
			{
				return null;
			}
			const propName = name.substr(p+1);
			return field.fields[propName];
		}
		else
		{
			return view.fields[name];
		}
	}
	
	static logicalEval(expr, evalCond)
	{
		if(!expr)
		{
			return false;
		}

		if(expr.and)
		{
			for(let i = 0; i < expr.and.length; i++)
			{
				const child = expr.and[i];
				if(!this.logicalEval(child, evalCond))
				{
					return expr.neg? 
						true: 
						false;
				}
			}

			return expr.neg? 
				false: 
				true;
		}
		else if(expr.or)
		{
			for(let i = 0; i < expr.or.length; i++)
			{
				const child = expr.or[i];
				if(this.logicalEval(child, evalCond))
				{
					return expr.neg? 
						false:
						true;
				}
			}            

			return expr.neg?
				true:
				false;
		}
		else
		{
			return evalCond(expr.cond);
		}
	}

	static testUser(email, op, user)
	{
		let userEmail = user.email;
		if(email.indexOf('{') === 0)
		{
			switch(email)
			{
			case '{admin}':
				if(user.admin)
				{
					email = userEmail;
				}
				break;
			default:
				break;
			}
		}
		
		switch(op)
		{
		case 'eq':
			return email === userEmail;
		case 'ne':
			return email !== userEmail;
		case 'in':
		case 'notin':
			return email.split('|').indexOf(userEmail) > -1? 
				(op === 'in'): 
				(op === 'notin');
		default:
			return false;
		}

	}

	static isDisabledForInsert(column, user)
	{
		const disabled = column.disabled;
		if(!disabled)
		{
			return false;
		}

		return this.logicalEval(
			disabled.insert,
			(dis) =>
			{
				switch(dis.when)
				{
				case 'ALWAYS':
					return true;
				case 'USER':
					return this.testUser(dis.user.email, dis.user.op, user);
				default:
					return false;
				}
			}
		);
	}

	static isDisabledForUpdate(column, value, user)
	{
		const disabled = column.disabled;
		if(!disabled)
		{
			return false;
		}

		return this.logicalEval(
			disabled.update,
			(dis) =>
			{
				switch(dis.when)
				{
				case 'ALWAYS':
					return true;
				case 'USER':
					return this.testUser(dis.user.email, dis.user.op, user);
				case 'VALUE':
					return this.executeBop(dis.value.op, value, dis.value.value, column.type);
				default:
					return false;
				}
			}
		);
	}

	static isDependencyResolvedForUpdate(item, column, schema, references)
	{
		return this.logicalEval(
			column.depends,
			(dep) =>
			{
				if(dep.column)
				{
					const col = dep.column;
					const left = Misc.getFieldValue(item, col.name);
					const depColumn = this.getColumn(schema, col.name, references);
					const op = col.op || 'eq';
					if(op === 'isnull' || op === 'notnull')
					{
						return this.executeUop(op, left, depColumn && depColumn.type);
					}
					else
					{
						const right = col.value;
						return this.executeBop(op, left, right, depColumn && depColumn.type);
					}
				}
				else
				{
					const flow = dep.flow.name;
					const meta = item._meta || {flows: {}};
					return meta.flows? !!meta.flows[flow]: false;
				}
			}
		);
	}

	static isColumnHidden(column, field, user)
	{
		if(field.hidden)
		{
			return true;
		}

		const hidden = column.hidden;
		if(!hidden)
		{
			return false;
		}
		
		var type = hidden.type;
		if(type == 'ALWAYS')
		{
			return true;
		}

		return user?
			this.testUser(hidden.user.email, hidden.user.op, user):
			true;
	}	

	static executeUop(op, left, type)
	{
		if(type === 'date')
		{
			left = left? 
				dayjs(left):
				null;
		}

		return Misc.executeUop(op, left);
	}

	static executeBop(op, left, right, type)
	{
		if(type === 'date')
		{
			left = left? 
				dayjs(left):
				null;
			right = right?
				Array.isArray(right)?
					right.map(r => dayjs(r)):
					dayjs(right):
				null;
		}

		return Misc.executeBop(op, left, right);
	}

	static getFieldsNamesSorted(fields)
	{
		return Object.entries(fields)
			.sort((a, b) => (a[1].index||0) - (b[1].index||0))
			.map(([key]) => key);
	}

	static getVisibleFieldsNamesSorted(schema, fields, user, references, parentKey = null)
	{
		return Object.entries(fields)
			.filter(([key, field]) => 
				!this.isColumnHidden(
					CollectionUtil.getColumn(
						schema, !parentKey? key: `${parentKey}.${key}`, references), 
					field, 
					user
				))
			.sort((a, b) => (a[1].index||0) - (b[1].index||0))
			.map(([key]) => key);
	}

	static calcFrozenFields(fields)
	{
		const cnt = Object.values(fields).reduce((acc, field) => acc + (field.frozen? 1: 0), 0);
		return cnt > 0? cnt: 1;
	}

	static buildListForColumn(column)
	{
		if(!column)
		{
			return [];
		}

		if(column.methods && column.methods.getList)
		{
			return this.buildListForColumnFromMethod(column.methods);
		}
		else if(column.options)
		{
			return column.options.map(option => 
			{
				const p = option.indexOf('|');
				return p !== -1?
					{
						value: option.substring(0, p),
						name: option.substring(p+1)
					}:
					{
						value: option, 
						name: option
					};
			});
		}

		return [];
	}

	static buildListForColumnFromMethod(methods)
	{
		let list = methods.getList.func().map(item => (
			{
				name: item[0], 
				value: item[1]
			})
		);

		if(!methods.getColor)
		{
			return list;
		}
			
		return list.map(item => (
			{
				...item,
				getColor: methods.getColor.func
			})
		);
	}

	static stringToDate(value)
	{
		return value?
			dayjs(value.replace(/[HMs]/g, '0'), DATE_FORMAT).toDate():
			null;
	}

	static convertToType(type, subtype, value)
	{
		type = (type !== 'array'? type : subtype) || 'string';

		if(value === null)
		{
			return value;
		}

		switch(type)
		{
		case 'date':
			if(Array.isArray(value))
			{
				return value.map(v => this.stringToDate(v));
			}
			else
			{
				return this.stringToDate(value);
			}
		
		case 'bool':
			if(Array.isArray(value))
			{
				return value.map(v => !v || v === '0'? false: true);
			}
			else
			{
				return !value || value === '0'? false: true;
			}

		case 'number':
			if(!Array.isArray(value))
			{
				return parseInt(value);
			}
			break;

		case 'decimal':
			if(!Array.isArray(value))
			{
				return parseFloat(value);
			}
			break;
		}

		return value;
	}

	static labelToKey(name)
	{
		if(!name)
		{
			return null;
		}
		
		return name.trim().toLowerCase()
			.replace(/[\\|/|:|*|!|@|"|<|>|(|)|[|\]|{|}|^|~|´|`|.|,|-|+|||'|$|#|%|&|?| |\t]/g, '_')
			.replace(/ç/g, 'c')
			.replace(/[á|à|â|ã|ä]/g, 'a')
			.replace(/[é|è|ê|ë]/g, 'e')
			.replace(/[í|ì|î|ï]/g, 'i')
			.replace(/[ó|ò|ô|õ|ö]/g, 'i')
			.replace(/[ú|ù|û|ü]/g, 'u')
			.replace(/[^a-zA-Z0-9]+(.)/g, (m, chr) => chr.toUpperCase());
	}

	static getObjectIcon(icon = 'wallet')
	{
		const sep = icon.indexOf(':');
		if(sep < 0)
		{
			return <i className={`la la-${icon}`} />;
		}

		const name = icon.substring(sep+1);

		return <img src={`icons/${name}.svg`} />;
	}

	static automationTypes = {
		'': {name: ''}, 
		'FIELD': {name: 'Campo'},
		'ITEM': {name: 'Item'},
		'DATE': {name: 'Data'},
	};

	static getAutomationTypes()
	{
		return this.automationTypes;
	}

	static automationTriggers = {
		'': {type: 'ANY', name: ''}, 
		'FIELD_UPDATED_TO': {type: 'FIELD', name: 'Campo atualizado'},
		'ITEM_INSERTED': {type: 'ITEM', name: 'Item inserido'},
		'ITEM_UPDATED': {type: 'ITEM', name: 'Item atualizado'},
		'DATE_ARRIVED': {type: 'DATE', name: 'A cada período'},
	};

	static getAutomationTriggers()
	{
		return this.automationTriggers;
	}

	static getAutomationTriggerName(trigger)
	{
		return this.automationTriggers[trigger].name;
	}

	static automationColumnValueTypes = {
		'': {name: ''},
		'value': {name: 'Valor'},
		'function': {name: 'Função'},
		'script': {name: 'Script'}
	};

	static getAutomationColumnValueTypes()
	{
		return this.automationColumnValueTypes;
	}

	static automationColumnConditionTypes = {
		'': {name: ''},
		'VALUE': {name: 'Valor'},
		'REGEX': {name: 'Expressão regular'},
		'SCRIPT': {name: 'Script'}
	};

	static getAutomationColumnConditionTypes()
	{
		return this.automationColumnConditionTypes;
	}

	static functionNames = [
		{value: '', label: ''},
		{value: 'nowAsDate', label: 'Data atual'},
	];

	static getFunctionNames()
	{
		return this.functionNames;
	}

	static ROLE_CREATOR = 0;
	static ROLE_EDITOR = 10;
	static ROLE_COMMENTER = 20;
	static ROLE_READER = 30;
	static ROLE_NONE = 90;
	
	static permissionRoles = [
		{value: '', label: ''},
		{value: 'CREATOR', label: 'Criador'},
		{value: 'EDITOR', label: 'Editor'},
		{value: 'COMMENTER', label: 'Comentarista'},
		{value: 'READER', label: 'Leitor'},
	];

	static getPermissionRoles()
	{
		return this.permissionRoles;
	}

	static getPermissionRoleName(role)
	{
		return this.permissionRoles.find(p => p.value === role).label;
	}

	static userRoles = [
		{value: '', label: ''},
		{value: 'ROLE_USER_PARTNER', label: 'Partner'},
		{value: 'ROLE_USER_CONTRIBUTOR', label: 'User'},
		{value: 'ROLE_USER_ADMIN', label: 'Administrator'},
	];

	static getUserRoles()
	{
		return this.userRoles;
	}

	static userFilterOps = [
		{value: '', label: '', symbol: '\u2800'},
		{value: 'eq', label: 'Igual', symbol: '\u003D'},
		{value: 'ne', label: 'Diferente', symbol: '\u2260'},
		{value: 'like', label: 'Começa com', symbol: '\u2248'},
		{value: 'in', label: 'Uma das', symbol: '\u2208'},
		{value: 'notin', label: 'Nenhuma das', symbol: '\u2209'},
	];
	
	static getUserFilterOps()
	{
		return this.userFilterOps;
	}

	static userLangs = [
		{value: '', label: ''},
		{value: 'en_US', label: 'English (USA)'},
		{value: 'pt_BR', label: 'Português (Brasil)'},
	];

	static getUserLangs()
	{
		return this.userLangs;
	}

	static userThemes = [
		{value: '', label: ''},
		{value: 'default', label: 'Padrão'},
		{value: 'clear', label: 'Claro'},
		{value: 'dark', label: 'Escuro'},
	];

	static getUserThemes()
	{
		return this.userThemes;
	}

	static filterOps = [
		{value: '', label: '', symbol: '\u2800', uop: true},
		{value: 'isnull', label: 'Nulo', symbol: '\u2205', uop: true},
		{value: 'notnull', label: 'Não nulo', symbol: '\u00AC', uop: true},
		{value: 'eq', label: 'Igual', symbol: '\u003D'},
		{value: 'ne', label: 'Diferente', symbol: '\u2260'},
		{value: 'like', label: 'Começa com', symbol: '\u2248', onlyFor: ['string']},
		{value: 'lt', label: 'Menor', symbol: '\u003C', onlyFor: ['number', 'decimal', 'date']},
		{value: 'lte', label: 'Menor ou igual', symbol: '\u2266', onlyFor: ['number', 'decimal', 'date']},
		{value: 'gt', label: 'Maior', symbol: '\u003E', onlyFor: ['number', 'decimal', 'date']},
		{value: 'gte', label: 'Maior ou igual', symbol: '\u2267', onlyFor: ['number', 'decimal', 'date']},
		{value: 'in', label: 'Uma das', symbol: '\u2208', onlyFor: ['number', 'decimal', 'string', 'enumeration']},
		{value: 'notin', label: 'Nenhuma das', symbol: '\u2209', onlyFor: ['number', 'decimal', 'string', 'enumeration']},
		{value: 'between', label: 'Entre', symbol: '\u226C', onlyFor: ['number', 'decimal', 'date']},
	];

	static existsOps = [
		{value: 'exists', label: 'Existe/Modificado', symbol: '\u2203'},
		{value: 'notexists', label: 'Não existe/Não modificado', symbol: '\u2204'},
	];
	
	static getFilterOps(type = null, allowExists = false)
	{
		const ops = !type?
			this.filterOps:
			type !== 'id'?
				this.filterOps.filter(f => !f.onlyFor || (f.onlyFor.indexOf(type) > -1)):
				this.filterOps.filter(f => f.uop);

		return !allowExists?
			ops:
			ops.concat(this.existsOps);
	}

	static getFilterOp(op)
	{
		return this.filterOps.find(f => f.value === op);
	}

	static filterTypes = [
		{value: '', label: ' '},
		{value: 'text', label: 'Texto'},
		{value: 'number', label: 'Número'},
		{value: 'bool', label: 'Boleano'},
		{value: 'date', label: 'Data'},
		{value: 'time', label: 'Tempo'},
		{value: 'datetime', label: 'Data+Tempo'},
		{value: 'email', label: 'e-mail'},
		{value: 'year', label: 'Ano'},
		{value: 'month', label: 'Mês'},
		{value: 'day', label: 'Dia'},
		{value: 'hour', label: 'Hora'},
		{value: 'minute', label: 'Minuto'},
		{value: 'second', label: 'Segundo'},
	];
	
	static getFilterTypes()
	{
		return this.filterTypes;
	}

	static reportGroupIdsValueTypes = {
		'': {name: ''},
		'value': {name: 'Valor'},
		'function': {name: 'Função'},
		'script': {name: 'Script'}
	};

	static getReportGroupIdsValueTypes()
	{
		return this.reportGroupIdsValueTypes;
	}

	static reportGroupFunctionNames = [
		{value: '', label: ''},
		{value: 'hourOfDay', label: 'Hora do dia'},
		{value: 'dayOfMonth', label: 'Dia do mês'},
		{value: 'monthOfYear', label: 'Mês do ano'},
	];

	static getReportGroupFunctionNames()
	{
		return this.reportGroupFunctionNames;
	}

	static reportGroupAggregatesValueTypes = {
		'': {name: ''},
		'value': {name: 'Valor'},
		'column': {name: 'Campo'},
		'function': {name: 'Função'},
		'script': {name: 'Script'}
	};

	static getReportGroupAggregatesValueTypes()
	{
		return this.reportGroupAggregatesValueTypes;
	}

	static reportFilterFieldValueTypes = {
		'': {name: ''},
		'value': {name: 'Valor'},
		'script': {name: 'Script'}
	};

	static getReportFilterFieldValueTypes()
	{
		return this.reportFilterFieldValueTypes;
	}

	static reportGroupAggregatesOps = {
		'': {name: ''},
		'count': {name: 'Contagem'},
		'avg': {name: 'Média'},
		'sum': {name: 'Soma'},
		'min': {name: 'Mínimo'},
		'max': {name: 'Máximo'},
	};

	static getReportGroupAggregatesOps()
	{
		return this.reportGroupAggregatesOps;
	}

	static classMethodNames = [
		{value: '', label: ''},
		{value: 'fromString', label: 'fromString'},
		{value: 'getColor', label: 'getColor'},
		{value: 'getList', label: 'getList'},
		{value: 'getOutput', label: 'getOutput'},
		{value: 'toString', label: 'toString'},
	]

	static getClassMethodNames()
	{
		return this.classMethodNames;
	}

	static columnTypes = [
		{value: '', label: ''},
		{value: 'string', label: 'Texto'},
		{value: 'number', label: 'Inteiro'},
		{value: 'decimal', label: 'Decimal'},
		{value: 'date', label: 'Data'},
		{value: 'enumeration', label: 'Enumeração'},
		{value: 'array', label: 'Array', noSubtype: true},
		{value: 'object', label: 'Objeto'},
		{value: 'bool', label: 'Boleano'},
	]

	static getColumnTypes(isSubtype = false)
	{
		return !isSubtype?
			this.columnTypes:
			this.columnTypes.filter(t => !t.noSubtype);
	}

	static columnComponents = [
		{value: '', label: ''},
		{value: 'text', label: 'Caixa de texto', onlyFor: ['string']},
		{value: 'textarea', label: 'Área de texto', onlyFor: ['string']},
		{value: 'bool', label: 'Checkbox', onlyFor: ['bool']},
		{value: 'select', label: 'Caixa de seleção', onlyFor: ['enumeration']},
		{value: 'grid', label: 'Grid', onlyFor: ['array', 'object']},
		{value: 'rating', label: 'Avaliação', onlyFor: ['decimal']},
		{value: 'array', label: 'Array', onlyFor: ['array']},
		{value: 'autocomplete', label: 'Autocomplete', onlyFor: ['string']},
		{value: 'email', label: 'E-mail', onlyFor: ['string']},
		{value: 'number', label: 'Número', onlyFor: ['number', 'decimal']},
		{value: 'date', label: 'Data', onlyFor: ['date']},
		{value: 'password', label: 'Senha', onlyFor: ['string']},
		{value: 'reference', label: 'Referência', onlyFor: ['string']},
	]

	static getColumnComponents(forType = null)
	{
		return !forType? 
			this.columnComponents:
			this.columnComponents.filter(c => !c.onlyFor || c.onlyFor.indexOf(forType) > -1);
	}

	static columnDefaultTypes = {
		'': {name: ''},
		'value': {name: 'Valor'},
		'function': {name: 'Função'},
		'script': {name: 'Script'}
	};

	static getColumnDefaultTypes()
	{
		return this.columnDefaultTypes;
	}

	static columnDefaultFunctionNames = [
		{value: '', label: ''},
		{value: 'nowAsDate', label: 'Agora'},
		{value: 'currentUser', label: 'Usuário atual'},
	];

	static getColumnDefaultFunctionNames()
	{
		return this.columnDefaultFunctionNames;
	}

	static flowConditionValueTypes = {
		'': {name: ''},
		'value': {name: 'Valor'},
		'column': {name: 'Campo'},
		'script': {name: 'Script'}
	};

	static getFlowConditionValueTypes()
	{
		return this.flowConditionValueTypes;
	}

	static flowConditionTypes = {
		'COLUMN': {label: 'Campo'},
		'AUTOMATION': {label: 'Automação'}
	};

	static getFlowConditionTypes()
	{
		return this.flowConditionTypes;
	}

	static columnDependsTypes = [
		{value: '', label: ''},
		{value: 'column', label: 'Campo'},
		{value: 'flow', label: 'Fluxo'}
	];

	static getColumnDependsTypes()
	{
		return this.columnDependsTypes;
	}

	static columnHiddenTypes = [
		{value: '', label: 'Nunca'},
		{value: 'ALWAYS', label: 'Sempre'},
		{value: 'USER', label: 'Se usuário'}
	];

	static getColumnHiddenTypes()
	{
		return this.columnHiddenTypes;
	}

	static columnDisabledTypes = [
		{value: '', label: '', for_: ''},
		{value: 'ALWAYS', label: 'Sempre', for_: ''},
		{value: 'USER', label: 'Se usuário', for_: ''},
		{value: 'VALUE', label: 'Se campo', for_: 'update'},
	];

	static getColumnDisabledTypes(for_ = null)
	{
		const types = this.columnDisabledTypes;
		return for_? 
			types.filter(t => t.for_ === for_ || !t.for_):
			types;
	}	

	static authActionWhenTypes = [
		{value: '', label: '', notFor: '', help: ''},
		{value: 'USER_SAME', label: 'Somente se o usuário for o mesmo que constar no(s) campo(s)', notFor: 'create', help: 'Permitir somente se usuário tiver acessa a esta coleção e constar nos campos'},
		{value: 'COLUMN_VALUE', label: 'Somente se o campo', notFor: 'create', help: 'Permitir somente se usuário tiver acesso a esta coleção e se o teste do campo for verdadeiro'},
	];

	static getAuthActionWhenTypes(for_ = null)
	{
		const types = this.authActionWhenTypes;
		return for_? 
			types.filter(t => t.notFor !== for_ || !t.notFor):
			types;
	}	

	static flowColumnActionValueTypes = {
		'': {name: ''},
		'value': {name: 'Valor'},
		'script': {name: 'Script'}
	};

	static getFlowColumnActionValueTypes()
	{
		return this.flowColumnActionValueTypes;
	}

	static flowActions = {
		'': {name: ''}, 
		'UPDATE_COLUMNS': {name: 'Atualizar campo(s)'},
	};

	static getFlowActions()
	{
		return this.flowActions;
	}

	static getFlowActionName(action)
	{
		return this.flowActions[action].name;
	}

	static integrationRates = [
		{value: 0, label: ''},
		{value: 1, label: 'Every minute'},
		{value: 3, label: 'Every 3 minutes'},
		{value: 5, label: 'Every 5 minutes'},
		{value: 10, label: 'Every 10 minutes'},
		{value: 15, label: 'Every 15 minutes'},
		{value: 30, label: 'Every 30 minutes'},
		{value: 60, label: 'Every hour'},
		{value: 60*6, label: 'Every 6 hours'},
		{value: 60*12, label: 'Every 12 hours'},
		{value: 60*24, label: 'Every day'},
		{value: 60*24*7, label: 'Every 7 days'},
		{value: 60*24*15, label: 'Every 15 days'},
		{value: 60*24*30, label: 'Every 30 days'},
	];

	static getIntegrationRates()
	{
		return this.integrationRates;
	}	

	static cellBorderStyles = [
		{value: '', label: 'Nenhum'},
		{value: 'solid', label: 'Sólida'},
		{value: 'dashed', label: 'Tracejada'},
		{value: 'dotted', label: 'Pontilhada'},
		{value: 'double', label: 'Dobrada'},
		{value: 'outset', label: 'Alto relevo'},
		{value: 'inset', label: 'Baixo relevo'}
	];

	static getCellBorderStyles()
	{
		return this.cellBorderStyles;
	}

	static formUses = [
		{name: 'any', label: 'Any', desc: 'Any'},
		{name: 'create', label: 'Create', desc: 'Create item'},
		{name: 'edit', label: 'Edit', desc: 'Edit item'},
		{name: 'view', label: 'View', desc: 'View item'},
	];

	static getFormUses()
	{
		return this.formUses;
	}

	static formElementTypes = [
		{name: 'empty', label: 'Empty', desc: 'Empty element', icon: 'scroll'},
		{name: 'field', label: 'Field', desc: 'Field element', icon: 'stream'},
		{name: 'text', label: 'Text', desc: 'Text element', icon: 'file-alt'},
		//{name: 'row', label: 'Row', desc: 'Row element (container)'},
	];

	static getFormElementTypes()
	{
		return this.formElementTypes;
	}

	static genFormUrl(collectionId, id)
	{
		return `${window.location.protocol}//${window.location.host}/#/f/${collectionId}/${id}`;
	}

	static appScreenElementTypes = [
		{name: 'empty', label: 'Empty', desc: 'Empty element', icon: 'scroll'},
		{name: 'text', label: 'Text', desc: 'Text element', icon: 'file-alt'},
		{name: 'form', label: 'Form', desc: 'Form element', icon: 'wpforms'},
	];

	static getAppScreenElementTypes()
	{
		return this.appScreenElementTypes;
	}

	static appBarElementTypes = [
		{name: 'empty', label: 'Empty', desc: 'Empty element', icon: 'scroll'},
		{name: 'button', label: 'Button', desc: 'Button element', icon: 'mouse'},
		{name: 'menu', label: 'Menu', desc: 'Menu element', icon: 'bars'},
	];

	static getAppBarElementTypes()
	{
		return this.appBarElementTypes;
	}

	static genAppUrl(id)
	{
		return `${window.location.protocol}//${window.location.host}/#/a/${id}`;
	}

	static genAppScreenUrl(appId, id)
	{
		return `${window.location.protocol}//${window.location.host}/#/a/${appId}/${id}`;
	}
}