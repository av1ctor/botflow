import React, {useCallback, useContext} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import ColumnResizer from '../../../../../components/Misc/ColumnResizer';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import Misc from '../../../../../libs/Misc';
import { SessionContext } from '../../../../../contexts/SessionContext';

const propertiesMenuItems = [
	{title: 'Gerais', icon: 'book', compo: 'general'},
	{title: 'Exibição', icon: 'book', compo: 'visual'},
	{title: 'Campos', icon: 'stream', compo: 'fields'},
];

const Header = observer((props) =>
{
	const session = useContext(SessionContext);
	const store = props.store;
	
	const handleCreate = useCallback(async (index, type) =>
	{
		store.createField(type, index);
	}, []);

	const moveField = useCallback((from, to) =>
	{
		store.moveField(props.view.id, session.user, null, from, to);
	}, []);

	const deleteField = useCallback(async (data) =>
	{
		store.deleteField(data.name);
	}, []);

	const handleDelete = useCallback((field, column, name, index) =>
	{
		props.container
			.showUserConfirmation(
				deleteField, 
				{field, column, name, index}, 
				`Remover campo '${column.label}' e apagar todos dados referentes a ele? (Nota: esta operação é irreversível!)`);
	}, []);

	const hideField = useCallback((name) =>
	{
		store.hideField(props.view.id, Misc.getFieldName(name));
	}, []);

	const showField = useCallback((sorted, index, name) =>
	{
		store.showField(props.view.id, Misc.getFieldName(name), index, sorted);
	}, []);

	const buildHeaderMenu = useCallback((sorted, field, column, name, index) =>
	{
		const {fields} = props;

		const hideMenu = () => props.container.hidePopupMenu();
		
		const keys = Object.keys(sorted);
		const schema = store.collection.schemaObj;
		const frozenCnt = CollectionUtil.calcFrozenFields(fields);
		const frozen = field.frozen;
		const hiddenFields = Object.entries(schema.columns)
			.filter(([key, col]) => !fields[key] || props.container.isColumnHidden(col, fields[key]))
			.map(([key, col]) => ({name: key, label: col.label}));

		return [
			{
				label: 'Propriedades',
				icon: 'la la-cog',
				items: propertiesMenuItems.map(sub => ({
					label: sub.title,
					icon: 'la la-' + sub.icon,
					command: () => 
					{
						hideMenu();
						props.container.showPanel('fieldProps', {
							viewId: props.view.id, 
							...sub,
							name, 
							column, 
							field, 
							isProp: false
						});
					}
				}))
			},
			{
				separator: true
			},
			{
				label: 'Criar',
				icon: 'la la-plus-circle',
				items: [
					{
						label: 'Avaliação',
						icon: 'la la-star',
						command: async () => 
						{
							hideMenu();
							await handleCreate(index, 'rating');
						}
					},
					{
						label: 'Data',
						icon: 'la la-calendar',
						command: async () => 
						{
							hideMenu();
							await handleCreate(index, 'date');
						}
					},
					{
						label: 'Geral',
						icon: 'la la-columns',
						command: async () => 
						{
							hideMenu();
							await handleCreate(index, 'default');
						}
					},
					{
						label: 'Grid',
						icon: 'la la-table',
						command: async () => 
						{
							hideMenu();
							await handleCreate(index, 'grid');
						}
					},					
					{
						label: 'Lista',
						icon: 'la la-list',
						command: async () => 
						{
							hideMenu();
							await handleCreate(index, 'list');
						}
					},
					{
						label: 'Tags',
						icon: 'la la-tags',
						command: async () => 
						{
							hideMenu();
							await handleCreate(index, 'array');
						}
					},
					{
						label: 'Texto',
						icon: 'la la-file-text',
						command: async () => 
						{
							hideMenu();
							await handleCreate(index, 'textarea');
						}
					}
				]
			},
			{
				label: 'Apagar',
				icon: 'la la-trash',
				command: async () => 
				{
					hideMenu();
					await handleDelete(field, column, name, index);
				}
			},
			{
				separator: true
			},
			{
				label: 'Mostrar',
				icon: 'la la-eye',
				disabled: hiddenFields.length === 0,
				items: hiddenFields.map(field => ({
					label: field.label || field.name,
					command: async () => 
					{
						await showField(sorted, index, field.name);
						hideMenu();
					}
				}))
			},
			{
				label: 'Ocultar',
				icon: 'la la-eye-slash',
				disabled: fields.length === 1,
				command: async () => 
				{
					await hideField(name);
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
						disabled: frozen? 
							index === 0: 
							index <= frozenCnt,
						command: async () => 
						{
							hideMenu();
							await moveField(index, frozen? 0: frozenCnt);
						}
					},
					{
						label: 'Esquerda',
						icon: 'la la-angle-left',
						disabled: frozen? 
							index === 0: 
							index <= frozenCnt,
						command: async () => 
						{
							hideMenu();
							await moveField(index, index-1);
						}
					},
					{
						label: 'Direita',
						icon: 'la la-angle-right',
						disabled: frozen?
							index === frozenCnt - 1:
							index === keys.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveField(index, index+1);
						}
					},
					{
						label: 'Final',
						icon: 'la la-angle-double-right',
						disabled: frozen? 
							index === frozenCnt - 1: 
							index === keys.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveField(index, frozen? frozenCnt - 1: keys.length - 1);
						}
					}				
				]
			},
		];
	}, []);

	const handleHeaderMenu = useCallback((event, sorted, field, column, name, index, element) =>
	{
		props.container.hideEditor();
		props.container.showPopupMenu(event, buildHeaderMenu(sorted, field, column, name, index, element));
	}, []);

	const handleShowFilters = useCallback((field) =>
	{
		props.container.toggleFiltersPanel(field);
	}, []);

	const {view, fields, filters} = props;
	const collection = store.collection;
	const references = store.references;
	const schema = collection.schemaObj;
	const sorted = CollectionUtil
		.getVisibleFieldsNamesSorted(schema, fields, session.user);
	const isCreator = props.container.isCreator();

	let left = 0;

	const renderHeaderField = (field, column, name, label, index) =>
	{
		const sort = props.sortBy[name];
		const sortable = CollectionUtil.isColumnSortable(column);
		const frozen = index === 0 || field.frozen;
		const filtered = filters.find(filter => filter.name === name);
		const width = (field.width|0) || CollectionUtil.DEFAULT_WIDTH;

		const style = {
			width: width, 
			height: props.height + 'px', 
			lineHeight: props.height + 'px'
		};

		if(frozen)
		{
			style.left = left;
			left += width;
		}

		const vstyle = view.style || {};
		const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;
		const border = vstyle.border || {};

		const classes = classNames(
			'rf-col-cell rf-col-header',
			{
				'frozen': frozen,
				[color]: frozen && color,
				[border.color]: border.color,
				[border.style]: border.style,
				'rounded': border.rounded,
			}
		);
		
		let element = null;

		return (
			<div 
				ref={el => element = el}
				key={index}
				className={classes} 
				style={style}>
				{column.class && 
					<i 
						className={`rf-col-header-icon la la-${CollectionUtil.getClassByName(schema, column.class).icon || 'list'}`} />}
				{label}
				{sortable && 
					<i 
						className={`rf-col-sort-handle ${sort? 'sorted': ''} la la-arrow-${sort && sort.dir === 'asc'? 'up': 'down'}`} 
						title="Ordenar"
						onClick={() => props.container.sort(props.sortBy, name)} />}
				{(sortable || filtered) && 
					<div 
						className={`rf-col-filter-handle ${filtered? 'filtered': ''}`}
						title="Filtros">
						<i className="la la-filter" onClick={() => handleShowFilters(field)} />
					</div>}
				{isCreator &&
					<div 
						className="rf-menu-handle"
						title="Abrir menu">
						<i 
							className="la la-caret-down" 
							onClick={(e) => handleHeaderMenu(e, sorted, field, column, name, index, element)} />
					</div>
				}
				<ColumnResizer 
					onResize={(delta) => props.container.resizeColumn(field, name, delta)}
					getContainer={props.container.getContainer}
					resizerHelper={() => props.resizerHelper.current} />
			</div>
		);
	};

	const colStyle = {
		height: props.height + 'px', 
		lineHeight: props.height + 'px'
	};

	const sortable = !!collection.positionalId;
	const sort = sortable? props.sortBy[collection.positionalId]: null;
	const vstyle = view.style || {};
	const color = vstyle.color || CollectionUtil.DEFAULT_BG_COLOR;
	const border = vstyle.border || {};

	return (
		<div className={classNames(
			'rf-col-row rf-col-row-header',
			{
				[color]: color,
				[border.color]: border.color,
				[border.style]: border.style,
				'rounded': border.rounded,
			})}>
			<div 
				key={-1} 
				className="rf-col-cell rf-col-header rf-col-header-order"
				style={colStyle}>
				<span>#</span>
				{sortable && 
					<i 
						className={`rf-col-sort-handle ${sort? 'sorted': ''} la la-arrow-${sort && sort.dir === 'asc'? 'up': 'down'}`} 
						onClick={() => props.container.sort(props.sortBy, collection.positionalId, true)} />}
				
			</div>
			{sorted.map((name, index) => 
				renderHeaderField(
					fields[name], 
					CollectionUtil.getColumn(schema, name, references), 
					name, 
					CollectionUtil.getColumnLabel(schema, name, references), 
					index)
			)}
		</div>
	);
});

Header.propTypes = {
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
	view: PropTypes.object,
};

export default Header;