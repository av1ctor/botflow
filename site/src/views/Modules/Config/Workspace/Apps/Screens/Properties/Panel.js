import React, {useCallback, useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../../../../components/Modal/ModalScrollable';
import Misc from '../../../../../../../libs/Misc';
import General from './General';

const lut = {
	'general': {compo: General, icon: 'cog', title: 'General'},
}; 

const Panel = (props) =>
{
	const [screen, setScreen] = useState(null);
	
	useEffect(() =>
	{
		if(props.visible)
		{
			setScreen(props.screens? 
				Object.assign({}, props.screens[props.index]): 
				null);
		}
		
	}, [props.visible, props.screens, props.index]);

	const handleChange = useCallback((e) =>
	{
		const name = e.target.name || e.target.id;
		const value = e.target.type !== 'checkbox'? 
			e.target.value: 
			e.target.checked;

		setScreen(screen =>
		{
			const to = Object.assign({}, screen);

			Misc.setFieldValue(to, name, value);

			return to;
		});
	}, []);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		const errors = [];

		const screens = Array.from(props.screens);
		screens[props.index] = screen;
		props.onChange(screens);
		
	}, [props.screens, props.index, screen]);	

	if(!props.visible || !props.compo || !screen)
	{
		return null;
	}

	const compo = lut[props.compo];
	const Compo = compo.compo;

	return (
		<ModalScrollable 
			isOpen={props.visible}
			onHide={props.onHide}
			lockFocus
			icon={compo.icon} 
			title={`Screen "${screen.title}" > Props > ${compo.title}`} 
		>
			<Compo
				app={props.app}
				screen={screen}
				onChange={handleChange}
				onSubmit={handleSubmit}
			/>
		</ModalScrollable>
	);
};

Panel.propTypes = 
{
	visible: PropTypes.bool,
	app: PropTypes.object,
	screens: PropTypes.array,
	index: PropTypes.number,
	onHide: PropTypes.func.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Panel;