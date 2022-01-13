import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import ReactModal from 'react-modal';
import FocusLock from 'react-focus-lock';

import './Modal.css';

const Modal = props =>
{
	return (
		<FocusLock disabled={!props.isOpen} returnFocus>
			<ReactModal 
				isOpen={props.isOpen} 
				onRequestClose={props.onHide} 
				className={classNames('simple-modal', props.className)} 
				ariaHideApp={false} 
				shouldCloseOnEsc>
				<div 
					className="simple-modal-close-handle" 
					onClick={props.onHide}>
					<strong><i className="la la-times-circle" /></strong>
				</div>
				{props.children}
			</ReactModal>
		</FocusLock>
	);
};

Modal.propTypes = 
{
	isOpen: PropTypes.bool.isRequired,
	onHide: PropTypes.func.isRequired,
	className: PropTypes.string,
	children: PropTypes.any
};

export default Modal;