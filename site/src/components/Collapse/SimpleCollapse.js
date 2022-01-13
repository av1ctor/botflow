import React, {useCallback, useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {Collapse} from 'reactstrap';

import './SimpleCollapse.css';

const SimpleCollapse = (props) =>
{
	const [expanded, setExpanded] = useState(props.isOpen);

	useEffect(() =>
	{
		setExpanded(props.isOpen);
	}, [props.isOpen]);

	const onToggle = useCallback(() =>
	{
		setExpanded(expanded => !expanded);
	}, []);

	const {title, style, children, ...rest} = props;

	return(
		<div 
			className={`rf-collapse-container ${expanded? 'expanded': 'collapsed'}`}
			style={style}>
			<div 
				className="rf-collapse-header" 
				onClick={onToggle}>
				<strong>
					<i 
						className={`la la-caret-${!expanded? 'right': 'down'}`}/> 
					{title.constructor === Function? title(): title}
				</strong>
			</div>
			
			<Collapse 
				timeout={{enter: 200, exit: 100}}
				{...rest} 
				isOpen={expanded}>
				{children}
			</Collapse>
		</div>
	);
};

SimpleCollapse.propTypes = {
	isOpen: PropTypes.bool.isRequired,
	title: PropTypes.any.isRequired,
	style: PropTypes.object,
	children: PropTypes.any
};

SimpleCollapse.defaultProps = {
	title: ''
};

export default SimpleCollapse;