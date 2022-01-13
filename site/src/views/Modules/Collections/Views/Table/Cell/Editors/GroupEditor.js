import React, {useCallback, useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import CollectionUtil from '../../../../../../../libs/CollectionUtil';
import TextEditor from './TextEditor';
import TextareaEditor from './TextareaEditor';
import BoolEditor from './BoolEditor';
import CellDateEdit from './DateEditor';
import CellListEdit from './ListEditor';
import GridEditor from './GridEditor';
import ArrayEditor from './ArrayEditor';
import RatingEditor from './RatingEditor';
import Misc from '../../../../../../../libs/Misc';

const transformFields = (props, store) =>
{
	const {fields} = props;
	if(!fields)
	{
		return null;
	}

	const res = [];

	const schema = store.collection.schemaObj;
	const view = store.views.get(props.viewId);
	
	for (let i = 0; i < fields.length; i++) 
	{
		const name = fields[i];
		res.push(transformField(props, schema, view, name));
	}
	
	return res;
};

const transformField = (props, schema, view, name) =>
{
	const {row, item} = props;
	
	const column = CollectionUtil.getColumn(schema, name, null);
	const field = CollectionUtil.getField(view, name, !!props.fieldName);
	
	const value = Misc.getFieldValue(item, name);
	
	const cellType = (column && column.type) || 'string';
	const cellCompo = (column && column.component) || '';

	const id = props.container.getItemId(item);

	const cell = {
		row,
		item, 
		col: column, 
		field,
		name,
		title: column.label || name,
		value,
		disabled: !id?
			props.container.isDisabledForInsert(column):
			props.container.isDisabledForUpdate(column, value)
	};

	switch(cellCompo)
	{
	case 'select':
		cell.type = 'list';
		cell.list = CollectionUtil.buildListForColumn(column);
		break;
	case 'grid':
		cell.type = 'grid';
		cell.list = CollectionUtil.buildListForColumn(column);
		break;
	case 'rating':
		cell.type = 'rating';
		break;
	case 'textarea':
		cell.type = 'textarea';
		break;
	case 'bool':
		cell.type = 'bool';
		break;
	default:
		switch(cellType)
		{
		case 'date':
			cell.type = 'date';
			break;
		case 'enumeration':
			cell.type = 'list';
			cell.list = CollectionUtil.buildListForColumn(column);
			break;
		case 'array':
			cell.type = 'array';
			break;
		case 'bool':
			cell.type = 'bool';
			break;
		default:
			cell.type = 'default';
			break;
		}
	}

	return cell;
};

const GroupEditor = props =>
{
	const store = props.store;
	
	const [fields, setFields] = useState(() => transformFields(props, store));

	useEffect(() =>
	{
		setFields(transformFields(props, store));
	}, [props.fields]);

	const getFieldByName = useCallback((fields, name) =>
	{
		return fields.find(field => field.name === name);
	}, []);

	const handleChange = useCallback((field, value) =>
	{
		if(value === '')
		{
			value = null;
		}
		else
		{
			const column = props.container.getColumnByName(field.name);
			switch(column.type)
			{
			case 'number':
				if(value.constructor === String)
				{
					value = Number.parseInt(value);
				}
				break;
			case 'decimal':
				if(value.constructor === String)
				{
					value = Number.parseFloat(value);
				}
				break;
			case 'array':
				if(value.constructor !== Array)
				{
					throw new Error('Campo do type array, mas valor não é uma array');
				}
				break;
			default:
				break;
			}
		}
		
		setFields(fields =>
		{
			const to = Array.from(fields);
			getFieldByName(to, field.name).value = value;
			return to;
		});
	}, []);
	
	const handleSubmit = useCallback(async (field, e) =>
	{
		if(e)
		{
			e.preventDefault();
		}

		if(field.disabled)
		{
			return;
		}

		const moved = await props.container
			.updateRow(field.row, field.item, field.name, getFieldByName(fields, field.name).value);
		if(moved)
		{
			props.container.hideEditor();
		}
	}, [fields]);

	const moveField = async (parentName, from, to) =>
	{
		await store.moveField(props.viewId, props.user, parentName, from, to);
		setFields(fields => Misc.moveArray(fields, from, to));
	};

	const hideField = async (index, name) =>
	{
		const key = `${props.fieldName}.${name}`;
		await store.hideField(props.viewId, key);
		setFields(fields => Misc.deleteFromArray(fields, index, 1));
	};

	const showField = async (schema, view, field, index, name) =>
	{
		const sorted = CollectionUtil.getFieldsNamesSorted(field.fields);

		const key = `${props.fieldName}.${name}`;

		await store.showField(props.viewId, key, index, sorted);

		const cell = transformField(props, schema, view, key);
		setFields(fields => Misc.addToArray(fields, index, [cell]));
	};

	const handleShowConfig = (event, cell, index) =>
	{
		props.container.showPopupMenu(event, buildConfigMenu(cell, index));
	};

	const showProperties = (view, field, column, name) =>
	{
		props.container.showPanel('fieldProps', {
			viewId: view.id, 
			name, 
			column, 
			field, 
			isProp: true
		});
	};

	const buildConfigMenu = useCallback((cell, index) =>
	{
		const {viewId, fieldName} = props;
		
		const hideMenu = () => props.container.hidePopupMenu('group');
		const getHiddenFields = (fieldProps) => 
			fieldProps.filter(prop => !fields[prop.name] || 
				props.container.isColumnHidden(klass.props[prop.name], fields[prop.name]));

		const schema = store.collection.schemaObj;
		const column = schema.columns[fieldName];
		const view = store.views.get(viewId);
		const field = view.fields[fieldName];
		const fields = field.fields;
		const fieldsLength = Object.keys(fields).length;
		const klass = CollectionUtil.getClassByName(schema, column.class);
		const fieldProps = Object.entries(klass.props).map(([name, prop]) => ({name: name, ...prop}));
		
		return [
			{
				label: 'Propriedades',
				icon: 'la la-cog',
				command: async () => 
				{
					hideMenu();
					showProperties(view, cell.field, cell.col, cell.name);
				}
			},
			{
				separator: true
			},
			{
				label: 'Mostrar',
				icon: 'la la-eye',
				disabled: fieldsLength === fieldProps.length,
				items: getHiddenFields(fieldProps).map(prop => ({
					label: prop.label,
					command: async () => 
					{
						await showField(schema, view, field, index, prop.name);
						hideMenu();
					}
				}))
			},
			{
				label: 'Ocultar',
				icon: 'la la-eye-slash',
				disabled: fieldsLength === 1,
				command: async () => 
				{
					await hideField(index, cell.name);
					hideMenu();
				}
			},
			{
				separator: true
			},
			{
				label: 'Mover',
				icon: 'la la-arrows',
				items: [
					{
						label: 'Início',
						icon: 'la la-angle-double-left',
						disabled: index === 0,
						command: async () => 
						{
							await moveField(fieldName, index, 0);
							hideMenu();
						}
					},
					{
						label: 'Cima',
						icon: 'la la-angle-up',
						disabled: index === 0,
						command: async () => 
						{
							await moveField(fieldName, index, index-1);
							hideMenu();
						}
					},
					{
						label: 'Baixo',
						icon: 'la la-angle-down',
						disabled: index === fieldsLength - 1,
						command: async () => 
						{
							await moveField(fieldName, index, index+1);
							hideMenu();
						}
					},
					{
						label: 'Final',
						icon: 'la la-angle-double-right',
						disabled: index === fieldsLength - 1,
						command: async () => 
						{
							await moveField(fieldName, index, fieldsLength - 1);
							hideMenu();
						}
					}				
				]
			},
		];
	}, [props.viewId]);

	const renderEdit = (cell, index) =>
	{
		if(!cell)
		{
			return null;
		}

		const onChange = (e) => handleChange(cell, e.target.value);
		const onSubmit = (e) => handleSubmit(cell, e);

		switch(cell.type)
		{
		case 'date':
			return (
				<CellDateEdit
					onChange={onChange} 
					onSubmit={onSubmit} 
					value={cell.value && new Date(cell.value)}
					disabled={cell.disabled}
					visible
					focus={false} />
			);
		case 'list':
			return (
				<CellListEdit
					onChange={onChange} 
					onSubmit={onSubmit} 
					value={cell.value || ''}
					list={cell.list}
					disabled={cell.disabled}
					visible
					focus={false} />
			);
		case 'array':
			return (
				<ArrayEditor
					onChange={onChange} 
					onSubmit={onSubmit} 
					value={cell.value || []}
					disabled={cell.disabled}
					visible
					focus={false} />
			);

		case 'grid':
			return (
				<GridEditor
					onChange={onChange} 
					onSubmit={onSubmit} 
					container={props.container} 
					value={cell.value || ''}
					list={cell.list}
					disabled={cell.disabled}
					visible
					focus={false} />
			);

		case 'rating':
			return (
				<RatingEditor
					onChange={onChange} 
					onSubmit={onSubmit} 
					value={cell.value !== null? cell.value: 0}
					disabled={cell.disabled}
					visible
					focus={false} />
			);

		case 'textarea':
			return (
				<TextareaEditor
					onChange={onChange} 
					onSubmit={onSubmit} 
					value={cell.value || ''}
					disabled={cell.disabled}
					visible
					focus={false} />
			);

		case 'bool':
			return (
				<BoolEditor
					onChange={onChange} 
					onSubmit={onSubmit} 
					value={cell.value}
					disabled={cell.disabled}
					visible
					focus={false} />
			);

		default:
			return (
				<TextEditor 
					onChange={onChange} 
					onSubmit={onSubmit} 
					value={cell.value || ''}
					disabled={cell.disabled}
					visible
					focus={false} />
			);
		}
	};

	const {fieldName} = props;

	if(!props.fields || !fields)
	{
		return null;
	}

	return(
		<div className="rf-col-edit-group">
			{fields.map((cell, index) => 
			{
				return (
					<div 
						key={cell.name}	
						className="rf-col-edit-group-title" 
					>
						<div>
							<div>
								<strong>{cell.col.label}</strong>
							</div>
							{!fieldName || !props.isCreator? null:
								<div className="rf-col-config-icon">
									<i 
										className="la la-caret-down" 
										onClick={(e) => handleShowConfig(e, cell, index)} 
										title="Abrir menu" />
								</div>
							}
						</div>
						<div>
							{renderEdit(cell)}
						</div>
						{index < fields.length -1 && <hr />}
					</div>
				);
			})}
		</div>
	);
};

GroupEditor.propTypes = 
{
	container: PropTypes.object.isRequired,
	user: PropTypes.object,
	store: PropTypes.object,
	viewId: PropTypes.string,
	fieldName: PropTypes.string,
	row: PropTypes.object,
	item: PropTypes.object,
	fields: PropTypes.array,
	isCreator: PropTypes.bool.isRequired,
	visible: PropTypes.bool,
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
};

export default GroupEditor;