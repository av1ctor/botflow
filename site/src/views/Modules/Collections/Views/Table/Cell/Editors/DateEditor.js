import React from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import {Calendar} from 'primereact/calendar';

const DateEditor = props =>
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
				{props.title &&
					<div className="row">
						<div className="col-12">
							<strong>{props.title}</strong>
						</div>
					</div>				
				}	
				<div className="row">
					<div className="col-12">
						<Calendar 
							inline 
							showTime 
							dateFormat="yy-mm-dd" 
							hourFormat="24"
							showSeconds
							value={props.value}
							tabIndex="1" 
							data-autofocus
							onChange={e => props.onChange({target: {value: e.value}})} />
					</div>
				</div>
				<div className="row">
					<div className="col-12 pt-2">
						<button 
							className="btn btn-primary right" 
							type="submit" 
							tabIndex="2"
							disabled={props.disabled}>
							OK
						</button>
					</div>
				</div>
			</form>
		</FocusLock>
	);
};

DateEditor.propTypes = 
{
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
	title: PropTypes.string,
	value: PropTypes.any,
	disabled: PropTypes.bool,
	visible: PropTypes.bool,
	focus: PropTypes.bool,
};

DateEditor.defaultProps = {
	focus: true
};

export default DateEditor;

