import React from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../../components/Modal/ModalScrollable';
import General from './General';
import Filters from './Filters';
import Visuals from './Visuals';

const lut = {
	'general': General,
	'filters': Filters,
	'visuals': Visuals
};

const Panel = observer(props =>
{
	const store = props.store;
	
	if(!props.visible || !props.viewId)
	{
		return null;
	}

	const view = store.views.get(props.viewId);

	const Compo = lut[props.compo];

	return (
		<ModalScrollable 
			isOpen={props.visible || false}
			onHide={props.onHide}
			icon={props.icon} 
			title={`View "${view.name}" > Propriedades > ${props.title}`} 
		>
			<Compo
				api={props.api}
				container={props.container}
				store={props.store}
				visible={props.visible || false}
				viewId={props.viewId}
			/>
		</ModalScrollable>
	);
});

Panel.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	title: PropTypes.string,
	icon: PropTypes.string,
	compo: PropTypes.string,
	viewId: PropTypes.string,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default Panel;