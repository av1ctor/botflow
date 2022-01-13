import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import ReactModal from 'react-modal';
import {Scrollbars} from 'react-custom-scrollbars';
import ReactFocusLock from 'react-focus-lock';

import './Modal.css';

const Focus = ({lock, children, ...rest}) =>
{
	return (
		lock?
			<ReactFocusLock
				{...rest}
				autoFocus
				returnFocus
				persistentFocus={false}>
				{children}
			</ReactFocusLock>:
			<div {...rest}>
				{children}
			</div>
	);
};

Focus.propTypes = {
	lock: PropTypes.bool.isRequired,
	children: PropTypes.any,
};

const ModalScrollable = (props) =>
{
	return (
		<ReactModal 
			isOpen={props.isOpen} 
			onRequestClose={props.onHide} 
			className={classNames('simple-modal', 'scrollable-modal', props.className)} 
			ariaHideApp={false} 
			shouldCloseOnEsc>
			<div 
				className="simple-modal-close-handle" 
				onClick={props.onHide}>
				<strong><i className="la la-times-circle" /></strong>
			</div>
			<div className="rf-dialog-title"><i className={`la la-${props.icon}`}/> {props.title}</div>
			<Scrollbars style={{flex: '1 1 auto'}}>
				<Focus 
					disabled={!props.isOpen}
					lock={props.lockFocus || false}
					className="simple-modal-contents container-fluid">
					{props.children}
				</Focus>
			</Scrollbars>
		</ReactModal>
	);
};

ModalScrollable.propTypes = 
{
	isOpen: PropTypes.bool.isRequired,
	title: PropTypes.string.isRequired,
	icon: PropTypes.string.isRequired,
	lockFocus: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
	className: PropTypes.string,
	children: PropTypes.any
};

export default ModalScrollable;