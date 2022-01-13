import React, { useCallback, useEffect, useState } from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../../components/Modal/ModalScrollable';
import Visual from './Visual';
import General from './General';
import CollectionUtil from '../../../../../libs/CollectionUtil';

const lut = {
	'general': General,
	'visual': Visual,
	'fields': () => <></>,
};

const Panel = observer(props =>
{
	const store = props.store;
	
	const [name, setName] = useState(props.name);
	const [column, setColumn] = useState(Object.assign({}, props.column));
	const [field, setField] = useState(Object.assign({}, props.field));

	const config = (name) =>
	{
		const schema = store.collection.schemaObj;
		const view = store.getViewOnSchemaById(props.viewId, schema);

		setName(name);
		setColumn(CollectionUtil.getColumn(schema, name, store.references));
		setField(Object.assign({}, view.fields[name]));
	};
	
	useEffect(() =>
	{
		if(props.visible)
		{
			config(props.name);
		}
	}, [store.collection, props.name]);
	
	const handleNameChanged = useCallback((to) =>
	{
		config(to);
	}, [column, field, name]);
	
	if(!props.visible || !column)
	{
		return null;
	}

	const Compo = lut[props.compo];

	return (
		<ModalScrollable 
			isOpen={props.visible || false}
			onHide={props.onHide}
			icon={props.icon} 
			title={`Campo "${column.label}" > Propriedades > ${props.title}`} 
		>
			<Compo
				{...props}
				name={name}
				column={column}
				field={field}
				onNameChanged={handleNameChanged}
			/>
		</ModalScrollable>
	);
});

Panel.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
	title: PropTypes.string,
	icon: PropTypes.string,
	compo: PropTypes.string,
	visible: PropTypes.bool,
	viewId: PropTypes.string,
	name: PropTypes.string,
	column: PropTypes.object,
	field: PropTypes.object,
	isProp: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default Panel;