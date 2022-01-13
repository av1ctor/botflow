import React, {useEffect, useRef} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {useParams} from 'react-router-dom';
import Collection from './Collection';
import CollectionStore from '../../../stores/CollectionStore';
import CollectionLogic from '../../../business/CollectionLogic';

const loadCollection = async (props, store, id) =>
{
	try
	{
		props.container.showSpinner();

		const res = await store.load(id);
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return false;
		}
	}
	finally
	{
		props.container.hideSpinner();
	}

	return true;
};

const CollectionWrapper = observer(props =>
{
	const storeRef = useRef(null);

	const params = useParams();
	
	const store = storeRef.current?
		storeRef.current:
		props.store || 
			new CollectionStore(
				new CollectionLogic(props.childProps.api), 
				null, 
				props.parent || null
			);

	storeRef.current = store;
	
	useEffect(() =>
	{
		const id = props.id || params.id;
		(async () =>
		{
			if(await loadCollection(props.childProps, store, id))
			{
				store.subscribe();
				return () => store.unsubscribe();
			}
		})();
	}, []);

	return (
		<Collection 
			{...props.childProps}
			store={store}
			visible={props.visible || true}
		/>
	);
});

CollectionWrapper.propTypes = {
	id: PropTypes.string,
	parent: PropTypes.object,
	store: PropTypes.object,
	childProps: PropTypes.object,
};

export default CollectionWrapper;