import React from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../../components/Modal/ModalScrollable';
import Form from './Form';
import CSV from './CSV';

const lut = {
	'form': Form,
	'csv': CSV,
	'file': () => <></>,
};

const Panel = observer(props =>
{
	const store = props.store;

	if(!props.visible)
	{
		return null;
	}

	const collection = store.collection;

	const Compo = lut[props.compo];

	return (
		<ModalScrollable 
			isOpen={props.visible || false}
			onHide={props.onHide}
			icon={props.icon}
			title={`Coleção "${collection.name}" > Importar > ${props.title}`} 
		>
			<Compo 
				{...props}
			/>
		</ModalScrollable>
	);
});

Panel.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
	icon: PropTypes.string,
	title: PropTypes.string,
	compo: PropTypes.string,
	visible: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default Panel;
