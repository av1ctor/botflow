import React from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';

const TextEditor = props =>
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
						<div className="col-9 pr-0">
							<input 
								className="form-control" 
								type="text"
								tabIndex="1"
								data-autofocus
								value={props.value}
								onChange={props.onChange} />
						</div>
						<div className="col-auto pl-0 pr-0">
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

TextEditor.propTypes = 
{
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
	title: PropTypes.string,
	value: PropTypes.any,
	disabled: PropTypes.bool,
	visible: PropTypes.bool,
	focus: PropTypes.bool,
};

TextEditor.defaultProps = {
	focus: true
};

export default TextEditor;