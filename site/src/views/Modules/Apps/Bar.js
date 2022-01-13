import React from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Button} from 'reactstrap';

import './Bar.css';

const Bar = (props) =>
{
	const isVertical = !props.bar.type === 'top' && !props.bar.type === 'bottom';

	const renderElement = (elm, parent, index) =>
	{
		if(elm.type === 'block')
		{
			return (
				<Col 
					className={`rf-bar-block ${isVertical? 'vert': 'horz'}`}
					style={{justifyContent: elm.align}}
					xs={elm.widths.xs}
					md={elm.widths.md}>
					{renderElements(elm.elements, elm)}
				</Col>
			);
		}
		
		let compo = null;
		switch(elm.type)
		{
		case 'button':
			compo =
				<Button
					className="rf-bar-button"
					title={elm.title}
					color={elm.color}
					size={elm.size}
					onClick={(e) => props.onClick(elm, e)}>
					<i className={`inter-${elm.icon}`}/>
					{elm.size === 'lg' && <div>{elm.title}</div>}
				</Button>;
			break;

		case 'menu':
			compo = 
				<div 
					className="rf-bar-menu"
					onClick={(e) => props.onShowMenu(elm, e)}>
					<i className="la la-bars" />
				</div>
			;	    
			break;  

		case 'empty':
			compo = 
				<div 
					index={index}
					className="rf-bar-empty">
					<div>&nbsp;</div>
				</div>;
			break;
		}

		return (
			compo
		);
	};

	const renderElements = (elements, parent) =>
	{
		return (
			elements.map((elm, index) => renderElement(elm, parent, index))
		);
	};

	const bar = props.bar;

	return (
		<Row 
			className={`rf-app-bar ${isVertical? 'vert': 'horz'} ${bar.type}`}
			style={{
				backgroundColor: bar.bgColor,
				color: bar.color,
				minWidth: isVertical? 
					bar.size? 
						bar.size + 'px': 
						undefined:
					undefined,
				minHeight: isVertical? 
					undefined: 
					bar.size?
						bar.size + 'px':
						undefined
			}}>
			{renderElements(bar.elements, bar)}
		</Row>
	);
};

Bar.propTypes = {
	api: PropTypes.object,
	container: PropTypes.object,
	app: PropTypes.object.isRequired,
	bar: PropTypes.object.isRequired,
	onClick: PropTypes.func.isRequired,
	onShowMenu: PropTypes.func.isRequired,
};

export default Bar;