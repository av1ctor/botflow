import {diff_match_patch} from 'diff-match-patch';
import seedrandom from 'seedrandom';
import dayjs from 'dayjs';
import expr from 'expression-eval';

class Util
{
	static getIndexDigits = (field, start) => 
	{
		const digits = field.charAt(start + 2) === ']'? 1: 2;
		return parseInt(field.substr(start + 1, digits));
	}
	
	static extractArrayIndex = (field) => 
	{
		const index = field.indexOf('[');
		if(index < 0)
		{
			return [
				field, 
				-1
			];
		}
		
		return [
			field.substring(0, index), 
			Util.getIndexDigits(field, index)
		];
	}

}

export default class Misc 
{
	static dateToString(date, format = 'DD/MM/YYYY HH:mm:ss')
	{
		return date?
			dayjs(date).format(format):
			null;
	}

	static dateToISO(date, format = 'YYYY-MM-DDTHH:mm:ss')
	{
		return date?
			dayjs(date).format(format):
			null;
	}

	static stringToDate(date, format = null)
	{
		return date?
			dayjs(date, {utc: true, format}).toDate():
			null;
	}

	static compareArrays(array1, array2)
	{
		if(array1 === null)
		{
			return array2 === null || array2.length === 0;
		}
		else if(array2 === null)
		{
			return array1.length === 0;
		}

		if(!Array.isArray(array1) || !Array.isArray(array2))
		{
			return false;
		}

		if(array1.length !== array2.length)
		{
			return false;
		}
		
		const array1Sorted = array1.slice().sort();
		const array2Sorted = array2.slice().sort();
		
		return array1Sorted.every((v1, index) => 
		{
			const v2 = array2Sorted[index];
			return this.compare(v1, v2);
		});
	}

	static compareSets(set1, set2)
	{
		if(set1 === null)
		{
			return set2 === null || set2.size === 0;
		}
		else if(set2 === null)
		{
			return set1.size === 0;
		}

		if(!(set1 instanceof Set) || !(set2 instanceof Set))
		{
			return false;
		}

		if(set1.size !== set2.size)
		{
			return false;
		}
		
		return this.compareArrays(Array.from(set1), Array.from(set2));
	}	
	
	static compareObjects(obj1, obj2, checkOrder = false)
	{
		if(!obj1)
		{
			return !obj2;
		}
		else if(!obj2)
		{
			return false;
		}

		for(let p in obj1)
		{
			if(!obj2.hasOwnProperty(p))
			{
				return false;
			}

			const v1 = obj1[p];
			const v2 = obj2[p];

			if(!this.compare(v1, v2))
			{
				return false;
			}
		}

		for(let p in obj2)
		{
			if(!obj1.hasOwnProperty(p))
			{
				return false;
			}
		}

		if(checkOrder)
		{
			const array1 = Object.keys(obj1);
			const array2 = Object.keys(obj2);
			if(!array1.every((value, index) => array2[index] === value))
			{
				return false;
			}
		}
		
		return true;
	}	

	static compare(v1, v2)
	{
		if(v1 === null || v1 === undefined)
		{
			if(v2 !== null && v2 !== undefined)
			{
				return false;
			}
		}
		else if(v2 === null || v2 === undefined)
		{
			return false;
		}

		if(typeof v1 !== typeof v2)
		{
			return false;
		}

		if(Array.isArray(v1))
		{
			return this.compareArrays(v1, v2);
		}
		else if(v1 instanceof Date)
		{
			return v1.getTime() === v2.getTime();
		}
		else if(v1 instanceof Set)
		{
			return this.compareSets(v1, v2);
		}
		else if(typeof v1 === 'object')
		{
			return this.compareObjects(v1, v2);
		}
			
		return v1 === v2;
	}

	static cloneObject(obj)
	{
		return obj? JSON.parse(JSON.stringify(obj)): null;
	}

	static objToArray = (obj, transform = null) => 
	{
		return Object.entries(obj || {})
			.map(([key, field]) => ({
				key: key, 
				...(transform? transform(field || {}): field)
			}));
	}

	static arrayToObj = (arr, transform = null, keygen = () => null) => 
	{
		return (arr || [])
			.reduce((obj, item) => 
			{
				const key = item.key || keygen(item);
				if(key)
				{
					obj[key] = {
						...(transform? transform(item, key): item),
						key: undefined
					}; 
				}
				return obj;
			}, {});
	}

	static moveArray = (source, from, to) => 
	{
		const array = Array.from(source);
		array.splice(to < 0 ? array.length + to : to, 0, array.splice(from, 1)[0]);
		return array;
	};	

	static deleteFromArray(source, start, count)
	{
		const array = Array.from(source);
		array.splice(start, count);
		return array;
	}

	static addToArray(source, start, itens)
	{
		const array = Array.from(source);
		array.splice(start, 0, ...itens);
		return array;
	}

	static mapValues(map)
	{
		return Array.from(map.values());
	}

	static mapKeys(map)
	{
		return Array.from(map.keys());
	}

	static mapEntries(map)
	{
		return Array.from(map.entries());
	}

	static getBaseName(name)
	{
		const p = name.indexOf('.');
		return p < 0? 
			name: 
			name.substring(0, p);
	}

	static getFieldName(name)
	{
		const p = name.indexOf('.');
		return p < 0? 
			name: 
			name.substr(p+1);
	}

	static getFieldValue(obj, name)
	{
		if(name.indexOf('.') >= 0)
		{
			const fields = name.split('.');
			name = fields.pop();
			fields.forEach(field =>
			{
				obj = obj[field];
				if(!obj)
				{
					obj = {};
				}
			});
		}
			
		return obj[name];
	}

	static setFieldValue(obj, name, value)
	{
		if(name.constructor !== Array)
		{
			Misc.setSingleFieldValue(obj, name, value);
		}
		else
		{
			name.forEach((name, index) =>
				Misc.setSingleFieldValue(obj, name, value[index])
			);
		}
	}
		
	static setSingleFieldValue(obj, name, value)
	{
		if(name[0] === '[')
		{
			let index = Util.getIndexDigits(name, 0);
			name = name.substr(name.indexOf(']') + 2);
			if(!obj[index])
			{
				obj[index] = {};
			}
			obj = obj[index];
		}
		
		if(name.indexOf('.') >= 0)
		{
			const fields = name.split('.');
			name = fields.pop();
			let index = 0;
			fields.forEach(field =>
			{
				const parent = obj;
				[field, index] = Util.extractArrayIndex(field);
				if(index >= 0)
				{
					obj = obj[field][index];
					if(!obj)
					{
						obj = parent[field] = [{}];
					}
				}
				else
				{
					obj = obj[field];
					if(!obj)
					{
						obj = parent[field] = {};
					}
				}
			});
		}
			
		let index = 0;
		[name, index] = Util.extractArrayIndex(name);
		if(index >= 0)
		{
			obj[name][index] = value;
		}
		else
		{
			obj[name] = value;
		}
	}

	static executeUop(op, left)
	{
		if(op === 'isnull')
		{
			return !left;
		}
		else
		{
			return !!left;
		}
	}

	static executeBop(op, left, right)
	{
		switch (op || 'eq') 
		{
		case 'eq':
			return left === right;
		case 'ne':
			return left !== right;
		case 'lt':
			return left < right;
		case 'lte':
			return left <= right;
		case 'gt':
			return left > right;
		case 'gte':
			return left >= right;
		case 'like':
			return new RegExp(right + '.*', 'i').test(left);
		case 'in':
			return right.indexOf(left) > -1;
		case 'notin':
			return right.indexOf(left) === -1;
		case 'between':
			return left >= right[0] && left <= right[1];
		case 'isnull':
			return left === null;
		case 'notnull':
			return left !== null;
		default:
			return false;
		}
	}

	static dmp = new diff_match_patch();

	static diff(from, to)
	{
		const res = this.dmp.patch_toText(this.dmp.patch_make(from, to));
		return !res || res === ''? null: res;
	}

	static patch(diff, to)
	{
		return !diff || diff === ''? 
			to: 
			this.dmp.patch_apply(this.dmp.patch_fromText(diff), to)[0];
	}

	static shuffleArray(array, seed = 's33d') 
	{
		const rng = seedrandom(seed);
		
		for (let i = array.length - 1; i > 0; i--) 
		{
			const j = Math.floor(rng() * (i + 1));
			[array[i], array[j]] = [array[j], array[i]];
		}

		return array;
	}	

	static round(num)
	{
		return Math.round((num + Number.EPSILON) * 100) / 100;
	}

	static genUniqueName(name, suffix, isUnique)
	{
		let cnt = 0;
		while(cnt < 1000)
		{
			const newName = `${name}${suffix}${('000' + ++cnt).substr(-3)}`;
			if(isUnique(newName))
			{
				return newName;
			}
		}

		return name;
	}

	static debounce(func, wait, options) 
	{
		return () => {
			clearTimeout(options.timer);
			options.timer = setTimeout(func, wait);
		};
	}	

	static getParams(search)
	{
		const res = {};
		const params = decodeURI(search).match(/([a-zA-Z_\-0-9]+=[^&]+)/g);
		params.forEach(param => 
		{
			const sep = param.indexOf('=');
			res[param.substring(0, sep)] = param.substring(sep+1);
		});

		return res;
	}

	static genId()
	{
		const h = 16;
		const toHex = n => Math.floor(n).toString(h);
		return toHex(Date.now() / 1000) + ' '.repeat(h).replace(/./g, () => toHex(Math.random() * h));
	}

	static stringToExpr(text)
	{
		const re = /\$\{([^}]+)?\}/g;

		const parts = [];
		let p = 0;

		let match = null;  
		while((match = re.exec(text)))
		{
			const literal = text.slice(p, match.index);
			if(literal.length > 0)
			{
				parts.push('\'' + literal + '\'');
			}
			parts.push(match[1]);
			p = match.index + match[0].length;
		}

		const literal = text.slice(p);
		if(literal.length > 0)
		{
			parts.push('\'' + literal + '\'');
		}

		return parts.join('+');
	}

	static compileExpr(text)
	{
		return expr.compile(text);
	}
}