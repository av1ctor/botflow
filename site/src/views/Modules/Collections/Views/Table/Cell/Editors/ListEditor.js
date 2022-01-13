import React from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';

const ListEditor = props =>
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
						<select 
							tabIndex="1"
							data-autofocus
							value={props.value}
							onChange={props.onChange}>
							<option key={-1} value=""></option>
							{props.list && props.list.map((o, index) =>
								<option key={index} value={o.value}>{o.name}</option>
							)}
						</select>
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

ListEditor.propTypes = 
{
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
	title: PropTypes.string,
	value: PropTypes.any,
	disabled: PropTypes.bool,
	visible: PropTypes.bool,
	list: PropTypes.array,
	focus: PropTypes.bool,
};

ListEditor.defaultProps = {
	focus: true
};

export default ListEditor;
