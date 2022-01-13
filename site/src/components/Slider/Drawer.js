import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Button} from 'reactstrap';

export default class Drawer extends PureComponent
{
	static propTypes = {
		visible: PropTypes.bool.isRequired,
		onReturn: PropTypes.func.isRequired,
		children: PropTypes.any
	};

	render()
	{
		const {children, visible, onReturn} = this.props;

		return (
			<div 
				className={`rf-drawer-container ${visible? 'visible': 'hidden'}`}>
				<div>
					<div className="rf-drawer-body">
						{children}
					</div>
					<Button
						size="xs"
						outline
						className="base-list-button rf-drawer-button" 
						color="none"
						onClick={() => onReturn()}>
						<span className="base-list-button-add-title">Voltar</span><i className="base-list-button-add-icon las la-caret-left"/>
					</Button>
				</div>
			</div>);
	}
}