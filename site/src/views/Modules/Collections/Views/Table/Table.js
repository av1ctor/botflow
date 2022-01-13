import React, {forwardRef, Fragment, memo, useState, useRef, useContext, useCallback, useEffect} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {areEqual} from 'react-window';
import InfiniteLoader from 'react-window-infinite-loader';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import {VariableSizeList} from 'react-window';
import {OverlayPanel} from 'primereact/overlaypanel';
import Header from './Header';
import Filters from './Filters';
import ItemFixed from './Item/ItemFixed';
import ItemDraggable from './Item/ItemDraggable';
import Group from './Group';
import Aggregates from './Aggregates';
import ActionsManager from '../../Items/Manager';
import Misc from '../../../../../libs/Misc';
import Editor from './Cell/Editors/Editor';
import { SessionContext } from '../../../../../contexts/SessionContext';

const compareFields = (fields1, fields2) =>
{
	const keys1 = Object.keys(fields1);
	for(let i = 0; i < keys1.length; i++)
	{
		const key = keys1[i];
		if(fields2[key] === undefined)
		{
			return 'reload';
		}

		if(
			fields1[key].index !== fields2[key].index ||
			fields1[key].hidden !== fields2[key].hidden)
		{
			return 'reset';
		}
	}

	const keys2 = Object.keys(fields2);
	for(let i = 0; i < keys2.length; i++)
	{
		const key = keys2[i];
		if(fields1[key] === undefined)
		{
			return 'reload';
		}
	}
	
	return '';
};

const initialEditorState = {
	type: null,
	title: '',
	extra: {},
	data: {},
	onChange: () => null,
	onSubmit: () => null,
};

const Table = observer((props) =>
{
	const session = useContext(SessionContext);
	const store = props.store;
	
	const [filters, setFilters] = useState([]);
	const [filtersVisible, setFiltersVisible] = useState(false);

	const listContainer = useRef(null);
	const resizerHelper = useRef(null);
	const listRef = useRef(null);
	const highlightedCell = useRef(null);
	
	const editorRef = useRef();

	const [editor, setEditor] = useState(initialEditorState);

	const view = store.views.get(props.viewId);
	
	const prevViewRef = useRef(view);

	useEffect(() =>
	{
		(async () =>
		{
			const prevView = prevViewRef.current;
			if(prevView === view)
			{
				return;
			}
			
			if(!Misc.compare(prevView.meta.filters, view.meta.filters))
			{
				await load();
				resetCache();
			}
			else 
			{
				switch(compareFields(prevView.fields, view.fields))
				{
				case 'reload':
					await load();
					resetCache();
					break;
				case 'reset':
					resetCache();
					break;
				default:
					if(!Misc.compare(prevView.meta.sortBy, view.meta.sortBy))
					{
						reloadOrReorder({restart: true, reset: true});
					}
				}
			}

			prevViewRef.current = view;
		})();
	}, [view]);

	const calcRowsPerPage = () =>
	{
		const view = store.views.get(props.viewId);
		return Math.max(CollectionUtil.PAGE_SIZE, Math.ceil(props.height / (view.style.height || CollectionUtil.DEFAULT_HEIGHT)));
	};

	const findRowById = (id) =>
	{
		return store.findRowById(props.viewId, id);
	};

	const resetCache = (forceReload = true) =>
	{
		listRef.current && listRef.current.resetAfterIndex(0, forceReload);
	};

	const load = async (offset = 0, size = 0, options = {}) =>
	{
		try
		{
			props.container.showSpinner();

			await store.loadRows(
				props.viewId, 
				session.user, 
				offset, 
				size || calcRowsPerPage(),
				options);
		}
		catch(e)
		{
			props.container.showMessage(e, 'error');
		}
		finally
		{
			props.container.hideSpinner();
		}
		
		return;
	};

	const reload = async (options = {}) =>
	{
		const view = store.views.get(props.viewId);

		try
		{
			const doReset = options.reset || view.meta.appendedCnt > 0 || view.groupBy;

			await store.reloadRows(
				props.viewId,
				session.user, 
				{
					...options, 
					rowsPerPage: calcRowsPerPage(),
					isReload: true
				});
			
			if(doReset)
			{
				resetCache();
			}

			return;
		}
		catch(e)
		{
			props.container.showMessage(e, 'error');
			return;
		}
	};

	const reloadOrReorder = async (options = {}) =>
	{
		const view = store.views.get(props.viewId);
		
		if(!view.meta.allLoaded || view.groupBy)
		{
			reload(options);
			return;
		}

		const doReset = options.reset || view.meta.appendedCnt > 0;

		store.sortRows(props.viewId, options);

		if(doReset)
		{
			resetCache();
		}

		return;
	};

	const loadMoreRows = useCallback((startIndex, stopIndex) =>
	{
		const view = store.views.get(props.viewId);

		return new Promise((resolve) => 
		{
			if(view.meta.allLoaded)
			{
				resolve();
				return;
			}

			load(startIndex, (stopIndex - startIndex) + 1).then(() => 
			{
				resolve();
			});
		});
	}, []);

	const filter = useCallback(async (filters) =>
	{
		setFilters(filters);
		store.setViewFilters(props.viewId, filters);
	}, []);

	const sort = useCallback((by, field, ascending = null) =>
	{
		const sorted = by[field];
		if(ascending === null)
		{
			if(sorted)
			{
				ascending = sorted.dir !== 'asc';
			}
			else
			{
				ascending = true;
			}
		}

		if(sorted && sorted.dir === 'asc' && ascending)
		{
			return;
		}

		const to = {
			[field]: {
				dir: ascending? 'asc': 'desc'
			}
		};
		
		store.setViewSortBy(props.viewId, to);
	}, []);

	const sprintf = (fmt, args) =>
	{
		return fmt.replace(/{(\d+)}/g, (match, number) =>
		{ 
			return args[number] !== undefined
				? args[number]
				: match;
		});
	};

	const getContainer = () =>
	{
		return listContainer.current.parentNode;
	};

	const getColumnByName = (name) =>
	{
		return CollectionUtil.getColumn(store.collection.schemaObj, name, store.references);
	};

	const getFieldByName = (name, isObject) =>
	{
		return CollectionUtil.getField(props.view, name, isObject);
	};

	const getItemId = (item) =>
	{
		return item[CollectionUtil.ID_NAME];
	};

	const getItemCellColor = (color, value) =>
	{
		if(!color)
		{
			return 'inherit';
		}
		
		if(color.value)
		{
			return '#' + color.value;
		}

		return !color.criteria || !color.criteria[value]? 'inherit': '#' + color.criteria[value];
	};

	const resizeColumn = (field, name, delta) =>
	{
		const size = (field.width || CollectionUtil.DEFAULT_WIDTH) + delta;
		const width = Math.max(Math.min(size, CollectionUtil.MAX_WIDTH), CollectionUtil.MIN_WIDTH)|0;

		store.setFieldWidth(props.viewId, name, width);

		resetCache(false);
		
		return delta - (size - field.width);
	};

	const handleTransformEditorData = useCallback((editor, value) =>
	{
		if(value === '')
		{
			value = null;
		}
		else
		{
			const column = getColumnByName(editor.extra.name);
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
					throw 'Campo do type array, mas valor não é uma array';
				}
				break;

			default:
				break;
			}
		}

		return value;
	}, []);

	const deleteRows = async (toRemove) =>
	{
		const res = await store.deleteRows(props.viewId, session.user, toRemove);
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
		}
	
		resetCache();
	};

	const createRow = async (at, options = {}) =>
	{
		const res = await store.createRow(
			props.viewId, 
			at, 
			session.user,
			{
				...options,
				rowsPerPage: calcRowsPerPage(),
			});

		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return false;
		}

		resetCache();
	
		return res;
	};

	const updateRow = async (row, item, key, value, options = {}) =>
	{
		const res = await store.updateRow(
			props.viewId, 
			session.user,
			row, 
			item, 
			key, 
			value,
			{
				...options,
				rowsPerPage: calcRowsPerPage(),
			});
		
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return false;
		}

		return true;
	};

	const isDisabledForInsert = (field) =>
	{
		return CollectionUtil.isDisabledForInsert(
			field, session.user);
	};

	const isDisabledForUpdate = (field, value) =>
	{
		return CollectionUtil
			.isDisabledForUpdate(field, value, session.user);
	};

	const isDependencyResolvedForUpdate = (item, field) =>
	{
		return CollectionUtil.isDependencyResolvedForUpdate(
			item, field, store.collection.schemaObj, store.references);
	};

	const isColumnHidden = (column, field) =>
	{
		return CollectionUtil.isColumnHidden(column, field, session.user);
	};

	const handleMoveRow = async (row, dir) =>
	{
		store.moveRow(props.viewId, row, dir);
	};

	const handleMoveItemTo = async (from, to) =>
	{
		const fromId = getItemId(from);
		const toId = getItemId(to);
		if(from === to || fromId === toId || !fromId || !toId || from.group !== to.group)
		{
			return;
		}
		
		const view = store.views.get(props.viewId);
		const doReset = view.meta.appendedCnt > 0 || view.groupBy;

		await store.moveItem(props.viewId, from, to);
		
		if(doReset)
		{
			resetCache();
		}
	};

	const toggleSelectRow = (row) =>
	{
		store.rowToggleSelected(props.viewId, row);
	};

	const handleToggleAppendRow = (row) =>
	{
		store.rowToggleAppended(props.viewId, row, session.user);
		resetCache();
	};

	const toggleGroup = (row) => 
	{
		const view = store.views.get(props.viewId);
		if(view.meta.allLoaded)
		{
			store.rowToggleGroup(props.viewId, row);
			resetCache();
		}
	};

	const highlightCell = (element) =>
	{
		if(highlightedCell.current)
		{
			highlightedCell.current.classList.remove('rf-col-cell-clicked');
		}
		
		highlightedCell.current = element;
		element.classList.add('rf-col-cell-clicked');
	};

	const hideHighlightedCell = () =>
	{
		if(highlightedCell.current)
		{
			highlightedCell.current.classList.remove('rf-col-cell-clicked');
			highlightedCell.current = null;
		}
	};

	const showEditor = useCallback((options, data) =>
	{
		if(options.element === editor.element)
		{
			hideEditor();
			return;
		}
		
		hideEditor();

		setEditor({
			...options,
			visible: true,
			data: data,
		});
		
		highlightCell(options.element);
		
		editorRef.current.show({}, options.element);
	}, [editor]);

	const hideEditor = useCallback(async () =>
	{
		if(editor.type)
		{
			destroyEditor();
		}
	}, [editor]);

	const destroyEditor = useCallback(() =>
	{
		if(editor.visible)
		{
			hideHighlightedCell();
		
			editorRef.current.hide();
			
			setEditor(initialEditorState);
		}
	}, [editor]);

	const handleSubmitEditor = useCallback(value =>
	{
		if(!editor.element)
		{
			return;
		}

		const {extra} = editor;
		updateRow(extra.row, extra.item, extra.name, value);

		setEditor(editor => 
			({
				...editor,
				data: {
					...editor.data,
					value: value,
				}
			})
		);

		hideEditor();

		return false;
	}, [editor]);

	const toggleFiltersPanel = () =>
	{
		setFiltersVisible(visible => !visible);
		resetCache(false);
	};
	
	const calcRowWidth = (view) =>
	{
		const schema = store.collection.schemaObj;
		const fields = view.fields;
		const user = session.user;
		const order = CollectionUtil
			.getVisibleFieldsNamesSorted(schema, fields, user, store.references);
		
		return order.reduce((acc, name) => 
			acc += (fields[name].width || CollectionUtil.DEFAULT_WIDTH), 0) + CollectionUtil.ORDER_WIDTH;
	};

	const calcRowHeight = useCallback((index) =>
	{
		const rows = store.rows.get(props.viewId);
		if(!rows)
		{
			return 0;
		}

		const row = rows.get(index);
		if(!row)
		{
			return 0;
		}

		const view = store.views.get(props.viewId);
			
		const collapsed = row.group && !row.group.expanded;
		
		let height = collapsed? 
			0: 
			view.style.height || CollectionUtil.DEFAULT_HEIGHT;

		if(row.appended)
		{
			height *= 2;
		}
		
		if(view.groupBy)
		{
			if(index === 0 || rows.get(index-1).group.id !== row.group.id)
			{
				height += CollectionUtil.GROUP_HEIGHT + (index > 0 && view.groupBy.aggregates? CollectionUtil.GROUP_AGGREGATES_HEIGHT: 0);
			}
			else if(index === rows.size - 1 && view.meta.allLoaded)
			{
				height += view.groupBy.aggregates? CollectionUtil.GROUP_AGGREGATES_HEIGHT: 0;
			}
		}

		return height + (index === 0? CollectionUtil.HEADER_HEIGHT + (filtersVisible? CollectionUtil.FILTERS_HEIGHT: 0): 0);
	}, [filtersVisible]);

	// eslint-disable-next-line react/prop-types
	const renderRow = memo(({data: rows, index, style}) =>
	{
		const collection = store.collection;
		const view = store.views.get(props.viewId);

		const height = view.style.height || CollectionUtil.DEFAULT_HEIGHT;

		const rowStyle = index === 0? 
			{...style, top: style.top + CollectionUtil.HEADER_HEIGHT + (filtersVisible? CollectionUtil.FILTERS_HEIGHT: 0), height: height}:
			{...style};

		const row = rows && rows.get(index);
		if(!row || row.item === 'LOADING')
		{
			return null;
		}

		const collapsed = row.group && !row.group.expanded;

		let groupCompo = null;
		let aggregateCompo = null;
		let lastAggregateCompo = null;
		if(view.groupBy && row.group)
		{
			if(index === 0 || rows.get(index-1).group.id !== row.group.id)
			{
				if(view.groupBy.aggregates && index > 0)
				{
					aggregateCompo = <Aggregates
						container={container.current}
						store={store}
						view={view}
						rows={rows}
						group={rows.get(index-1).group}
						style={{
							...rowStyle, 
							height: CollectionUtil.GROUP_AGGREGATES_HEIGHT
						}} />;
					rowStyle.top += CollectionUtil.GROUP_AGGREGATES_HEIGHT;	
				}

				groupCompo = <Group 
					container={container.current}
					view={view} 
					row={row} 
					group={row.group} 
					style={{...rowStyle, height: CollectionUtil.GROUP_HEIGHT}} 
					width={calcRowWidth(view)} />;

				rowStyle.top += CollectionUtil.GROUP_HEIGHT;
			}
			
			if(view.groupBy.aggregates && (index === rows.size - 1) && view.meta.allLoaded)
			{
				lastAggregateCompo = <Aggregates
					container={container.current}
					store={store}
					view={view}
					rows={rows} 
					group={row.group}
					style={{
						...rowStyle, 
						height: CollectionUtil.GROUP_AGGREGATES_HEIGHT, 
						top: rowStyle.top + (collapsed? 0: height + (row.appended? height: 0))
					}} />;
			}
		}

		const sortBy = store.getViewSortBy(view.id);

		const commonProps = 			
		{
			api: props.api,
			container: container.current,
			store: store,
			view: view,
			fields: view.fields,
			sortBy: sortBy,
			height: height,
			row: row,
			allowInsert: props.insertableFieldsCount > 0,
			isEven: (index & 1) === 0
		};

		const itemProps = 
		{
			...commonProps,
			item: row.item,
			style: rowStyle,
			moved: row.moved && !row.rendered? true: false,
		};

		const appendedProps = row.appended?
			{
				...commonProps,
				item: row.appended.item,
				style: {...rowStyle, top: rowStyle.top + height},
				moved: false
			}:
			null;

		row.rendered = true;

		const Compo = !collection.positionalId || !sortBy[collection.positionalId]? 
			ItemFixed:
			ItemDraggable;

		const compo =
			collapsed? 
				null: 
				(!row.appended? 
					<Compo {...itemProps} />: 
					<Fragment>
						<Compo {...itemProps} />
						<Compo {...appendedProps} />
					</Fragment>);
		
		return (aggregateCompo || groupCompo || lastAggregateCompo?
			<Fragment>
				{aggregateCompo}
				{groupCompo}
				{compo}
				{lastAggregateCompo}
			</Fragment>:
			compo
		);
	}, areEqual);

	// eslint-disable-next-line react/prop-types, react/display-name
	const renderInnerContainer = forwardRef(({ children, ...rest }, ref) => 
	{
		const view = store.views.get(props.viewId);

		return (
			<div ref={ref} {...rest} className="rf-col-grid-content">
				<div ref={resizerHelper} className="rf-column-helper" style={{display:'none'}}></div>
				<Header 
					container={container.current}
					store={store}
					view={view}
					fields={view.fields}
					resizerHelper={resizerHelper}
					sortBy={store.getViewSortBy(view.id)}
					filters={store.getViewFilters(view.id)}
					height={CollectionUtil.HEADER_HEIGHT} />
				{filtersVisible &&
					<Filters
						container={container.current}
						store={store}
						view={view}
						fields={view.fields}
						filters={filters}
						height={CollectionUtil.FILTERS_HEIGHT} />
				}
				{children}
			</div>
		);
	});

	const isRowLoaded = useCallback((index) =>
	{
		const rows = store.rows.get(props.viewId);
		return rows && rows.has(index);
	}, []);

	const getRowKey = useCallback((index) =>
	{
		const rows = store.rows.get(props.viewId);
		const row = rows && rows.get(index);
		if(!row || row.item === 'LOADING')
		{
			return index;
		}

		return row.item._id || index;
	}, []);

	const container = useRef({}); 
	Object.assign(container.current, props.container, {
		filter,
		calcRowsPerPage,
		findRowById,
		sort,
		sprintf,
		getContainer,
		getColumnByName,
		getFieldByName,
		getItemId,
		getItemCellColor,
		moveItem: handleMoveItemTo,
		resizeColumn,
		createRow,
		updateRow,		
		deleteRows,
		moveRow: handleMoveRow,		
		isDisabledForInsert,
		isDisabledForUpdate,
		isDependencyResolvedForUpdate,
		isColumnHidden,
		showEditor,
		hideEditor,
		toggleSelectRow,
		toggleAppendRow: handleToggleAppendRow,
		toggleFiltersPanel,		
		toggleGroup,
		highlightCell,
		hideHighlightedCell
	});

	const rows = store.rows.get(props.viewId);
	
	if(!props.visible && (!rows || rows.size === 0))
	{
		return null;
	}

	const isCreator = props.container.isCreator();
	const rowsPerPage = calcRowsPerPage();

	return (
		<>
			<InfiniteLoader
				isItemLoaded={isRowLoaded}
				loadMoreItems={loadMoreRows}
				itemCount={100000}
				minimumBatchSize={rowsPerPage}
				threshold={Math.ceil(rowsPerPage / 2)}>
				{({onItemsRendered, ref}) => 
					<VariableSizeList
						ref={(el) => {listRef.current = el; ref(el);}}
						innerRef={listContainer}
						innerElementType={renderInnerContainer}
						height={props.height}
						width='100%'
						onItemsRendered={onItemsRendered}
						itemCount={rows && rows.size? rows.size: rowsPerPage}
						itemSize={calcRowHeight}
						itemKey={getRowKey}
						itemData={rows}>
						{renderRow}
					</VariableSizeList>
				}
			</InfiniteLoader>

			<OverlayPanel
				ref={editorRef}
				onHide={destroyEditor}
				appendTo={document.body}>
				<Editor 
					api={props.api}
					container={container.current}
					store={store}
					editor={editor}
					isCreator={isCreator}
					onTransform={handleTransformEditorData}
					onSubmit={handleSubmitEditor}
				/>
			</OverlayPanel>
			
			<ActionsManager 
				container={container.current}
				store={store}
				view={view}
				rows={rows}
				selectedCnt={view.meta.selectedCnt}
			/>
		</>
	);
});

Table.propTypes = {
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	viewId: PropTypes.string,
	insertableFieldsCount: PropTypes.number,
};

export default Table;

