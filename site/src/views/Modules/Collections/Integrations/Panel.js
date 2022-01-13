import React from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../components/Modal/ModalScrollable';
import IntegrationList from './IntegrationList';

const Panel = observer(props =>
{
	const store = props.store;
	
	if(!props.visible)
	{
		return null;
	}

	const collection = store.collection;

	return (
		<ModalScrollable 
			isOpen={props.visible || false}
			onHide={props.onHide}
			icon="chain"
			title={`Collection "${collection.name}" > Integrations`} 
		>
			<IntegrationList
				api={props.api}
				container={props.container}
				store={props.store}
				visible={props.visible || false}
			/>
		</ModalScrollable>
	);
});

Panel.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default Panel;
