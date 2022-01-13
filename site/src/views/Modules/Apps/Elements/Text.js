import React from 'react';
import PropTypes from 'prop-types';

const Text = (props) =>
{
	const elm = props.element;

	return (
		<div 
			className="rf-screen-text"
			style={{
				height: elm.height,
				fontSize: elm.size + 'px', 
				fontWeight: elm.bold? 'bold': null, 
				color: elm.color,
				backgroundColor: elm.backgroundColor,
				textAlign: elm.justify, 
				justifyContent: elm.align}}>
			{elm.text.split('\n').map((text, index) => 
				<div key={index}>{text}</div>
			)}
		</div>
	);	
};

Text.propTypes = 
{
	element: PropTypes.object,
};

export default Text;