import React from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import {AutoComplete} from 'primereact/autocomplete';

const AutoEditor = props =>
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
							<AutoComplete 
								multiple={props.isMultiple}
								field={props.display}
								value={props.value} 
								onChange={props.onChange} 
								itemTemplate={props.renderOptions}
								suggestions={props.suggestions}
								completeMethod={props.onFilter}
								minLength={1}
								disabled={props.disabled}
								dropdown
								dropdownMode="current"
								tabIndex="1"
								data-autofocus />
						</div>
						<div className="col-auto pl-0 pr-0">
							<button 
								className="btn btn-primary" 
								type="submit" 
								tabIndex="2"
								disabled={props.disabled}>
								OK
							</button>
						</div>
					</div>
				</div>
			</form>
		</FocusLock>
	);
};

AutoEditor.propTypes = 
{
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
	onFilter: PropTypes.func.isRequired,
	renderOptions: PropTypes.func.isRequired,
	suggestions: PropTypes.array,
	title: PropTypes.string,
	value: PropTypes.any,
	isMultiple: PropTypes.bool,
	display: PropTypes.string,
	disabled: PropTypes.bool,
	visible: PropTypes.bool,
	focus: PropTypes.bool,
};

AutoEditor.defaultProps = {
	focus: true
};

export default AutoEditor;