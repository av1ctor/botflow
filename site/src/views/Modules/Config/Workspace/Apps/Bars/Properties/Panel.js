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
	const [bar, setBar] = useState(null);
	
	useEffect(() =>
	{
		if(props.visible)
		{
			setBar(props.bars? 
				Object.assign({}, props.bars[props.index]): 
				null);
		}
		
	}, [props.visible, props.bars, props.index]);

	const handleChange = useCallback((e) =>
	{
		const name = e.target.name || e.target.id;
		const value = e.target.type !== 'checkbox'? 
			e.target.value: 
			e.target.checked;

		setBar(bar =>
		{
			const to = Object.assign({}, bar);

			Misc.setFieldValue(to, name, value);

			return to;
		});
	}, []);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		const errors = [];

		const bars = Array.from(props.bars);
		bars[props.index] = bar;
		props.onChange(bars);
		
	}, [props.bars, props.index, bar]);	

	if(!props.visible || !props.compo || !bar)
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
			title={`Bar "${bar.title}" > Props > ${compo.title}`} 
		>
			<Compo
				app={props.app}
				bar={bar}
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
	bars: PropTypes.array,
	index: PropTypes.number,
	onHide: PropTypes.func.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Panel;