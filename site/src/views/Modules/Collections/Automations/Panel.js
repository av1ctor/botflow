import React, {useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../components/Modal/ModalScrollable';
import CollectionUtil from '../../../../libs/CollectionUtil';
import AutomationList from './AutomationList';

const Panel = observer(props =>
{
	const store = props.store;
	
	const [columns, setColumns] = useState([]);

	useEffect(() =>
	{
		setColumns(CollectionUtil.transformColumns(
			store.collection.schemaObj, store.collection.schemaObj.columns));
	}, [store.collection]);

	if(!props.visible)
	{
		return null;
	}
	
	const collection = store.collection;

	return (
		<ModalScrollable 
			isOpen={props.visible || false}
			onHide={props.onHide}
			icon="cogs" 
			title={`Collection "${collection.name}" > Automations`} 
		>
			<AutomationList
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
