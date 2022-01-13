import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Collapse} from 'reactstrap';

export default class CollapseUnmounted extends PureComponent
{
	static propTypes = 
	{
		isOpen: PropTypes.bool.isRequired,
		children: PropTypes.any,
	};

	constructor(props)
	{
		super(props);
		
		this.state = {
			closed: !props.isOpen
		};
	}

	render()
	{
		const {children, isOpen, ...props} = this.props;

		return(
			<Collapse 
				timeout={{enter: 200, exit: 100}}
				{...props} 
				isOpen={isOpen} 
				onEntering={() => this.setState({closed: false})}
				onExited={() => this.setState({closed: true})}>
				{!isOpen && this.state.closed? null: children}
			</Collapse>
		);
	}
}