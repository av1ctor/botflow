import React, {useCallback, useRef, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import CollectionUtil from '../../../../../../libs/CollectionUtil';

const Cell = observer(props =>
{
	const store = props.store;
	
	const [value, setValue] = useState(props.value);
	const [formatted, setFormatted] = useState(props.formatted);

	const cellElement = useRef();

	const buildGroupListForObjectColumn = (schema, column, name, field) =>
	{
		let res = [];
		
		const klass = CollectionUtil.getClassByName(schema, column.class);
		if(field.fields)
		{
			const fields = Object.entries(field.fields)
				.sort((a,b) => a[1].index - b[1].index);
				
			for(let i = 0; i < fields.length; i++)
			{
				const fieldName = fields[i][0];
				const field = fields[i][1];
				const prop = klass.props[fieldName];
				if(!props.container.isColumnHidden(prop, field))
				{
					const fullName = name + '.' + fieldName;
					res.push(fullName);
				}
			}
		}

		return res;
	};

	const genRefFields = (ref, value) =>
	{
		if(value)
		{
			return Object.assign({}, value);
		}
		
		const fields = ref.preview.columns
			.reduce((obj, name) => {
				obj[name] = ''; 
				return obj;
			}, {});

		return {
			_id: '', 
			...fields
		};
	};

	const handleEditCell = useCallback((event, element = null) =>
	{
		const {row, item, field, name} = props;
		
		const collection = store.collection;
		const schema = collection.schemaObj;
		const column = props.container.getColumnByName(name);
		
		const cellType = (column && column.type) || 'string';
		const cellCompo = (column && column.component) || '';

		const ref = column.ref && schema.refs[column.ref.name];

		const options = {
			element: element || cellElement.current, 
			title: column.label || name,
			extra: {
				row,
				item, 
				col: field, 
				name,
			}
		};
		
		let data = null;

		if(ref)
		{
			options.type = 'auto';
			options.extra.ref = ref;
			options.extra.isMultiple = column.type === 'array';
			options.extra.display = column.ref.display;
			data = {
				value: genRefFields(ref, item[column.ref.name + '-' + name]),
				suggestions: []
			};
		}
		else
		{
			const value = item[name];
		
			switch(cellCompo)
			{
			case 'select':
				options.type = 'list';
				data = {
					list: CollectionUtil.buildListForColumn(column), 
					value: value
				};
				break;
			case 'grid':
				options.type = 'grid';
				data = {
					list: CollectionUtil.buildListForColumn(column), 
					value: value
				};
				break;
			case 'rating':
				options.type = 'rating';
				data = {value: value};
				break;
			case 'textarea':
				options.type = 'textarea';
				data = {value: value};
				break;
			case 'bool':
				options.type = 'bool';
				data = {value: value};
				break;
			default:
				switch(cellType)
				{
				case 'date':
					options.type = 'date';
					data = {value: value};
					break;
				case 'enumeration':
					options.type = 'list';	
					data = {
						list: CollectionUtil.buildListForColumn(column), 
						value: value
					};
					break;
				case 'array':
					options.type = 'array';
					data = {value: value};
					break;
				case 'bool':
					options.type = 'bool';
					data = {value: value};
					break;
				case 'object':
					options.type = 'group';
					data = {
						list: buildGroupListForObjectColumn(schema, column, name, field),
						row: row,
						item: item, 
						id: props.id,
						viewId: props.view.id,
						fieldName: name,
					};
					break;
				default:
					options.type = 'default';
					data = {value: value};
					break;
				}
			}
		}

		props.container.showEditor(options, data);
	}, []);

	const copyCell = useCallback(async (element) => 
	{
		const range = document.createRange();
		const selection = window.getSelection();
		selection.removeAllRanges();
		range.selectNode(element);
		selection.addRange(range);
		document.execCommand('copy');
		selection.removeAllRanges();
	}, []);

	const buildContextMenu = useCallback((column, editable) =>
	{
		const {container, row, item} = props;

		const element = cellElement.current;

		return [
			{
				label: 'Editar célula',
				icon: 'la la-edit',
				disabled: !editable,
				command: () => handleEditCell(null, element)
			},
			{
				label: 'Copiar célula',
				icon: 'la la-copy',
				command: () => copyCell(element)
			},
			{
				label: 'Limpar célula',
				icon: 'la la-trash',
				disabled: container.isDisabledForUpdate(column),
				command: async () => 
				{
					container.updateRow(row, item, props.name, null);
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
						label: 'Cima',
						icon: 'la la-angle-up',
						disabled: !props.sortBy[store.collection.positionalId],
						command: async () => 
						{
							container.moveRow(row, 'up');
						}
					},
					{
						label: 'Baixo',
						icon: 'la la-angle-down',
						disabled: !props.sortBy[store.collection.positionalId],
						command: async () => 
						{
							container.moveRow(row, 'down');
						}
					}
				]
			}
		];
	}, []);

	const handleContextMenu = useCallback((event, column, editable) =>
	{
		props.container.hideEditor();
		
		props.container.showContextMenu(event, buildContextMenu(column, editable));
	}, []);

	const handleOpenDetails = useCallback((e) =>
	{
		e.stopPropagation();
		
		props.container.showDetails(props.item);
	}, []);

	const handleToggleAppendRow = useCallback((e) =>
	{
		e.stopPropagation();

		props.container.toggleAppendRow(props.row);
	}, []);

	const handleBeginInlineEdit = useCallback((e) =>
	{
		e.target.contentEditable = true;
	}, []);

	const handleEndInlineEdit = useCallback((e) =>
	{
		e.target.contentEditable = false; 

		const to = e.target.innerText;
		setValue(to);
		setFormatted(to);
	}, []);
	
	const handleKeyupInlineEdit = useCallback((e) =>
	{
		if(e.keyCode === 13 && !e.shiftKey) 
		{
			handleEndInlineEdit(e);
		}
		else if(e.keyCode === 27)
		{
			e.target.innerText = formatted;
			e.target.contentEditable = false;
		}
	}, []);

	const canEdit = (column, item, value, isNew) => 
	{
		let editable = true;
		
		if(column)
		{
			editable = !(!isNew? 
				props.container.isDisabledForUpdate(column, value): 
				props.container.isDisabledForInsert(column));
		}

		if(editable && column && column.depends)
		{
			editable = props.container.isDependencyResolvedForUpdate(item, column);
		}
		
		return editable;
	};

	const collection = store.collection;
	const {view, item, field, name, index, height} = props;

	const isNew = !props.id;

	const column = props.container.getColumnByName(name);
	const klass = column && column.class? 
		CollectionUtil.getClassByName(collection.schemaObj, column.class): 
		null;

	const frozen = index === 0 || field.frozen;

	const editable = canEdit(column, item, value, isNew);
	const contentEditable = false;

	let style = {
		width: props.width + 'px',
		height: height + 'px',
		lineHeight: height + 'px'
	};
	
	if(frozen)
	{
		style.left = props.left;
	}
	
	if(field.color)
	{
		style.backgroundColor = props.container.getItemCellColor(field.color, value);
	}
	else if(klass && klass.methods && klass.methods.getColor)
	{
		style.backgroundColor = klass.methods.getColor.func({'props': value});
	}

	const vstyle = view.style || {};
	const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;

	const classes = classNames(
		'rf-col-cell', 
		{
			'enabled': editable, 
			'disabled': !editable,
			'frozen': frozen,
			[color]: frozen && color,
			'type-id': index === 0
		},
		`type-${column.type}`);

	return (
		<div 
			ref={cellElement}
			className={classes}
			style={style}
			onContextMenu={!isNew? (e) => handleContextMenu(e, column, editable): null}
			onClick={editable? (contentEditable? handleBeginInlineEdit: handleEditCell): null}
			onKeyDown={editable && contentEditable? handleKeyupInlineEdit: null}
			onBlur={editable && contentEditable? handleEndInlineEdit: null}
			title={editable? 'Editar célula': ''}>
			{index !== 0 || isNew || !props.allowInsert? 
				null: 
				<div 
					className="rf-col-row-add-handle" 
					title="Inserir item"
					onClick={handleToggleAppendRow}>
					<i className="la la-plus-square"/>
				</div>}
			{index !== 0 || isNew? 
				null: 
				<div 
					className="rf-col-cell-details-handle" 
					title="Detalhes do item"
					onClick={handleOpenDetails}>
					<i className="la la-external-link"/>
				</div>}
			<div>{formatted}</div>
		</div>
	);
	
});

Cell.propTypes = {
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
	view: PropTypes.object, 
};

export default Cell;