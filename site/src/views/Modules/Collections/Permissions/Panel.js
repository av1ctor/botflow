import React, {useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../components/Modal/ModalScrollable';
import General from './General';
import Itemwise from './Itemwise';
import CollectionUtil from '../../../../libs/CollectionUtil';

const lut = {
	'general': General,
	'itemwise': Itemwise
};

const Panel = observer(props =>
{
	const store = props.store;

	const [columns, setColumns] = useState([]);

	useEffect(() =>
	{
		if(props.visible)
		{
			const collection = store.collection;
			if(collection.schemaObj)
			{
				setColumns(CollectionUtil
					.transformColumns(collection.schemaObj, collection.schemaObj.columns));
			}
		}
	}, [props.visible]);

	if(!props.visible)
	{
		return null;
	}

	const collection = store.collection;
	if(!collection)
	{
		return null;
	}
	
	const Compo = lut[props.compo];

	return (
		<ModalScrollable 
			isOpen={props.visible || false}
			onHide={props.onHide}
			icon={props.icon} 
			title={`Coleção "${collection.name}" > Permissões > ${props.title}`} 
		>
			<Compo
				api={props.api}
				container={props.container}
				store={props.store}
				visible={props.visible || false}
				columns={columns}
			/>
		</ModalScrollable>
	);
});

Panel.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
	title: PropTypes.string,
	icon: PropTypes.string,
	compo: PropTypes.string,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default Panel;