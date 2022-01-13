import React, {forwardRef, useCallback, useContext, useState} from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Cell from '../Cell/Cell';
import CollectionUtil from '../../../../../../libs/CollectionUtil';
import { SessionContext } from '../../../../../../contexts/SessionContext';

// eslint-disable-next-line react/display-name
export const SortCell = forwardRef((props, ref) =>
{
	const [showingSelect, setShowingSelect] = useState(false);

	const showSelectBox = useCallback(() =>
	{
		setShowingSelect(true);
	}, [showingSelect]);

	const hideSelectBox = useCallback(() =>
	{
		setShowingSelect(false);
	}, [showingSelect]);

	const {row, item, height, draggable} = props;

	const id = props.container.getItemId(item);

	const colStyle = {
		height: height + 'px', 
		lineHeight: height + 'px'
	};

	const isNew = !id;
	const isDirty = isNew && row.dirty;

	return (
		<div 
			ref={ref}
			key={-1} 
			className={classNames(
				'rf-col-cell order', 
				{
					'draggable': draggable,
				}
			)}
			style={colStyle}
			onMouseEnter={!isNew? showSelectBox: null}
			onMouseLeave={!isNew? hideSelectBox: null}>
			{!isNew? 
				showingSelect || row.selected? 
					<input 
						type="checkbox" 
						onChange={() => props.container.toggleSelectRow(row)} 
						checked={row.selected}
						title="Selecionar item" />: 
					row.num:
				<div 
					className={`rf-col-row-save-icon ${isDirty? 'dirty': ''}`}
					onClick={() => isDirty? props.container.createRow(row): null}
					title="Salvar item">
					<i className="la la-save" />
				</div>}
		</div>
	);
});

SortCell.propTypes = {
	container: PropTypes.object,
	row: PropTypes.object, 
	item: PropTypes.object, 
	height: PropTypes.number, 
	draggable: PropTypes.bool,
	dirty: PropTypes.bool
};

export const OtherCells = (props) =>
{
	const session = useContext(SessionContext);
	const store = props.store;
	
	const formatValue = (classes, name, item) =>
	{
		const column = props.container.getColumnByName(name);
		const klass = column && column.class? 
			classes[column.class]: 
			null;

		const value = CollectionUtil.getColumnValue(column, item, name);
		const formatted = CollectionUtil.getValueFormatted(value, column, klass);
		
		return [value, formatted];
	};
	
	const collection = store.collection;
	const {view, fields, row, item, height} = props;
	
	const schema = collection.schemaObj;
	const sorted = CollectionUtil
		.getVisibleFieldsNamesSorted(schema, fields, session.user);
	const id = props.container.getItemId(item);
	const classes = schema.classes;

	let left = 0;

	return (
		sorted.map((name, index) => {
			const field = fields[name];
			const width = field.width || CollectionUtil.DEFAULT_WIDTH;
			const frozen = index === 0 || (field.frozen);
			const [value, formatted] = formatValue(classes, name, item);
			
			const cell = <Cell 
				key={index} 
				container={props.container}
				store={store}
				view={view}
				row={row}
				item={item}
				id={id}
				field={field} 
				name={name} 
				index={index}
				sortBy={props.sortBy}
				width={width}
				height={height}
				left={left}
				value={value}
				formatted={formatted}
				allowInsert={props.allowInsert} />;
			
			if(frozen)
			{
				left += width;
			}
			
			return cell;
		})
	);
};

OtherCells.propTypes = {
	container: PropTypes.object,
	store: PropTypes.object.isRequired,
	row: PropTypes.object, 
	item: PropTypes.object, 
	height: PropTypes.number, 
	draggable: PropTypes.bool,
	dirty: PropTypes.bool
};

