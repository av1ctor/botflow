import React from 'react';
import PropTypes from 'prop-types';
import {Col, Row} from 'reactstrap';
import Text from './Elements/Text';
import Form from './Elements/Form';

import './Screen.css';

const Screen = (props) =>
{
	const renderElement = (elm, parent, index) =>
	{
		if(elm.type === 'block')
		{
			return (
				<Row className="rf-screen-block">
					{renderElements(elm.elements, elm)}
				</Row>
			);
		}
		
		let compo = null;
		switch(elm.type)
		{
		case 'text':
			compo =
				<Text 
					element={elm} 
				/>;
			break;

		case 'form':
			compo =
				<Form
					api={props.api}
					container={props.container}
					element={elm} 
				/>;
			break;

		case 'horzline':
			compo = 
				<div 
					className="rf-screen-horzline" 
				/>
			;	
			break;

		case 'empty':
			compo = 
				<div 
					className="rf-screen-empty"
					style={{
						height: elm.height,
					}}>
					<div>&nbsp;</div>
				</div>;
			break;
		}

		return (
			<Col 
				key={index}
				xs={elm.widths.xs}
				md={elm.widths.md}>
				{compo}
			</Col>
		);
	};

	const renderElements = (elements, parent) =>
	{
		return (
			elements.map((elm, index) => renderElement(elm, parent, index))
		);
	};

	return (
		<div className="rf-app-screen">
			{renderElements(props.screen.elements, props.screen)}
		</div>
	);
};

Screen.propTypes = {
	api: PropTypes.object,
	container: PropTypes.object,
	app: PropTypes.object.isRequired,
	screen: PropTypes.object.isRequired,
};

export default Screen;