import React, {useCallback, useContext, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import TextEditor from './TextEditor';
import TextareaEditor from './TextareaEditor';
import BoolEditor from './BoolEditor';
import CellDateEdit from './DateEditor';
import CellListEdit from './ListEditor';
import GridEditor from './GridEditor';
import ArrayEditor from './ArrayEditor';
import RatingEditor from './RatingEditor';
import GroupEditor from './GroupEditor';
import AutoEditor from './AutoEditor';
import CollectionUtil from '../../../../../../../libs/CollectionUtil';
import { SessionContext } from '../../../../../../../contexts/SessionContext';

const Editor = observer(props =>
{
	const session = useContext(SessionContext);
	const store = props.store;

	const [data, setData] = useState(props.editor.data);

	useEffect(() => 
	{
		setData(props.editor.data);
	}, [props.editor.data]);

	const handleChange = useCallback((e) =>
	{
		const value = e.target.value;
		const editor = props.editor;
		
		setData(data => ({
			...data,
			value: props.onTransform(editor, value)
		}));
	}, [props.editor]);

	const handleSubmit = useCallback((e, value = null) =>
	{
		if(e)
		{
			e.preventDefault();
		}

		return props.onSubmit(value || data.value);
	}, [data]);

	const handleAutoChange = useCallback((e) =>
	{
		const value = e.target.value;
		
		const editor = props.editor;
		
		setData(data => ({
			...data,
			value: typeof value === 'object'?
				value:
				{
					...data.value,
					[editor.extra.display]: value
				}
		}));
	}, [props.editor]);

	const handleAutoSubmit = useCallback((e) =>
	{
		const value = data.value[CollectionUtil.ID_NAME];
		
		handleSubmit(e, value);
	}, [data]);

	const handleAutoFilter = useCallback(async (e) =>
	{
		const ref = props.editor.extra.ref;

		const filters = [];
		ref.filters.forEach(filter =>
		{
			filters.push({
				name: filter.column,
				op: filter.op || 'like',
				value: e.query
			});
		});
		
		const filtersStr = encodeURIComponent(JSON.stringify(filters));
		
		const res = await props.api.get(
			`collections/${ref.collection}/items?page=0&size=20&sort=${ref.filters[0].column},asc&filters=${filtersStr}`);

		if(!res.errors)
		{
			setData(data => ({
				...data,
				suggestions: res.data
			}));
		}
	}, [props.editor]);	
	
	const renderAutoOverlayOptions = useCallback((item) =>
	{
		const ref = props.editor.extra.ref;
		if(!ref)
		{
			return null;
		}

		return (
			<div className="rf-autocomplete-row">
				{ref.preview.columns.map((name, index) => 
					<div className="rf-autocomplete-col" key={index}>{item[name]}</div>
				)}
			</div>
		);
	}, [props.editor]);

	const {editor} = props;
	if(!editor.type || !editor.visible)
	{
		return null;
	}

	switch(editor.type)
	{
	case 'date':
		return (
			<CellDateEdit
				onChange={handleChange} 
				onSubmit={handleSubmit} 
				value={data.value && new Date(data.value)}
				title={editor.title}
				visible
			/>
		);

	case 'list':
		return (
			<CellListEdit
				onChange={handleChange} 
				onSubmit={handleSubmit} 
				value={data.value || ''}
				list={data.list}
				title={editor.title}
				visible
			/>
		);
		
	case 'grid':
		return (
			<GridEditor
				onChange={handleChange} 
				onSubmit={handleSubmit}
				container={props.container}
				value={data.value || ''}
				list={data.list}
				title={editor.title}
				visible
			/>
		);
		
	case 'array':
		return (
			<ArrayEditor
				onChange={handleChange} 
				onSubmit={handleSubmit} 
				value={data.value || []}
				title={editor.title}
				visible
			/>
		);
		
	case 'rating':
		return (
			<RatingEditor
				onChange={handleChange} 
				onSubmit={handleSubmit}
				value={data.value || 0}
				title={editor.title}
				visible
			/>
		);
		
	case 'group':
		return (
			<GroupEditor
				onChange={handleChange} 
				onSubmit={handleSubmit}
				container={props.container}
				user={session.user}
				store={store}
				viewId={data.viewId}
				fieldName={data.fieldName}
				row={data.row}
				item={data.item}
				fields={data.list}
				isCreator={props.isCreator}
				visible
			/>
		);
		
	case 'rename':
		return (
			<TextEditor 
				onChange={handleChange} 
				onSubmit={handleSubmit}
				value={data.value || ''}
				title={editor.title}
				visible
			/>
		);
		
	case 'auto':
		return (
			<AutoEditor
				onChange={handleAutoChange} 
				onSubmit={handleAutoSubmit}
				onFilter={handleAutoFilter}
				renderOptions={renderAutoOverlayOptions}
				value={data.value || ''}
				title={editor.title}
				isMultiple={editor.extra.isMultiple}
				display={editor.extra.display}
				suggestions={data.suggestions}
				visible
			/>
		);
		
	case 'bool':
		return (
			<BoolEditor 
				onChange={handleChange} 
				onSubmit={handleSubmit} 
				value={data.value || false}
				title={editor.title}
				visible
			/>
		);

	case 'textarea':
		return (
			<TextareaEditor
				onChange={handleChange} 
				onSubmit={handleSubmit} 
				value={data.value || ''}
				title={editor.title}
				visible
			/>
		);

	default:
		return (
			<TextEditor 
				onChange={handleChange} 
				onSubmit={handleSubmit} 
				value={data.value || ''}
				title={editor.title}
				visible
			/>
		);
	}
});

Editor.propTypes = {
	api: PropTypes.object,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	editor: PropTypes.object.isRequired,
	isCreator: PropTypes.bool.isRequired,
	onTransform: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
};

export default Editor;