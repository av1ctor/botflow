import React from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../components/Modal/ModalScrollable';
import General from './General';
import FieldsPanel from './FieldsPanel';
import ClassesPanel from './ClassesPanel';
import Constants from './Constants';
import Indexes from './Indexes';
import References from './References';
import Versions from './Versions';
import Schema from './Schema';
import Logs from './Logs';

const lut = {
	'general': General,
	'fields': FieldsPanel,
	'classes': ClassesPanel,
	'constants': Constants,
	'indexes': Indexes,
	'references': References,
	'versions': Versions,
	'schema': Schema,
	'logs': Logs,
};

const Panel = observer(props =>
{	
	const store = props.store;

	if(!props.visible)
	{
		return null;
	}
	
	const collection = store.collection;
	if(!collection)
	{
		return null;
	}

	const hasSchema = collection.type === 'SCHEMA';

	const Compo = lut[props.compo];

	return (
		<ModalScrollable
			isOpen={props.visible || false}
			onHide={props.onHide}
			icon={props.icon} 
			title={`${hasSchema? 'Collection': 'Folder'} "${collection.name}" > Props > ${props.title}`} 
		>
			<Compo
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
	title: PropTypes.string,
	icon: PropTypes.string,
	compo: PropTypes.string,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

Panel.defaultProps = {
	title: 'Propriedades',
	icon: 'cog'
};

export default Panel;

