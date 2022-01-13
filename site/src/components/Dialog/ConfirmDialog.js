import React from 'react';
import PropTypes from 'prop-types';
import {Dialog} from 'primereact/dialog';
import {Button} from 'primereact/button';
import FocusLock from 'react-focus-lock';

const ConfirmDialog = (props) =>
{
	const areYouSureFooter = (
		<div>
			<Button 
				label="Sim" 
				icon="la la-check" 
				onClick={props.onConfirmed} 
				className="p-button-danger" />
			<Button 
				label="Não" 
				icon="la la-times" 
				onClick={props.onHide} 
				className="p-button-secondary" />
		</div>
	);

	return (
		<FocusLock 
			disabled={!props.visible} 
			returnFocus>
			<Dialog 
				header={<span><span className="la la-exclamation-triangle"></span><b>Alerta</b></span>} 
				visible={props.visible} 
				modal
				responsive
				footer={areYouSureFooter} 
				onHide={props.onHide} >
				{props.text}
			</Dialog>
		</FocusLock>
	);
};

ConfirmDialog.propTypes = 
{
	visible: PropTypes.bool,
	text: PropTypes.any,
	onConfirmed: PropTypes.func,
	onHide: PropTypes.func
};

export default ConfirmDialog;
