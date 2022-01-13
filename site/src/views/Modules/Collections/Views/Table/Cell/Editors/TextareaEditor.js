import React from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';

const TextareaEditor = props =>
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
						<div className="col-12">
							<textarea
								className="form-control" 
								rows="4"
								tabIndex="1"
								data-autofocus
								onChange={props.onChange} 
								value={props.value} />
						</div>
						<div className="col-12">
							<button 
								className="btn btn-primary" 
								type="submit" 
								tabIndex="2"
								disabled={props.disabled}>OK</button>
						</div>
					</div>
				</div>
			</form>
		</FocusLock>
	);
};

TextareaEditor.propTypes = 
{
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
	title: PropTypes.string,
	value: PropTypes.any,
	disabled: PropTypes.bool,
	visible: PropTypes.bool,
	focus: PropTypes.bool,
};

TextareaEditor.defaultProps = {
	focus: true
};

export default TextareaEditor;