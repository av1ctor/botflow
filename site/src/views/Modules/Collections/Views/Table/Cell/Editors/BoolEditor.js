import React from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';

const BoolEditor = props =>
{
	const {focus} = props;

	return(
		<FocusLock 
			group="cell-editors" 
			disabled={!props.visible} 
			autoFocus={focus}
			returnFocus={focus} 
			persistentFocus={focus}>
			<form className="form-horizontal" onSubmit={props.onSubmit}>
				<div className="form-group">
					{props.title &&
						<div className="row">
							<div className="col-12">
								<strong>{props.title}</strong>
							</div>
						</div>					
					}
					<div className="row">
						<div className="col-6">
							<button 
								className="btn btn-primary"
								tabIndex="1"
								data-autofocus
								disabled={props.disabled}
								onClick={(e) => !props.value? props.onChange({target: {value: true}}): e.preventDefault()}>
								{props.value === true? <u>Sim</u>: 'Sim'}
							</button>
						</div>
						<div className="col-6">
							<button 
								className="btn btn-danger"
								tabIndex="2"
								disabled={props.disabled}
								onClick={(e) => props.value || props.value === undefined? props.onChange({target: {value: false}}): e.preventDefault()}>
								{props.value === false? <u>Não</u>: 'Não'}
							</button>
						</div>
					</div>
				</div>
			</form>
		</FocusLock>
	);
};

BoolEditor.propTypes = 
{
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
	title: PropTypes.string,
	value: PropTypes.any,
	disabled: PropTypes.bool,
	visible: PropTypes.bool,
	focus: PropTypes.bool,
};

BoolEditor.defaultProps = {
	focus: true
};

export default BoolEditor;
