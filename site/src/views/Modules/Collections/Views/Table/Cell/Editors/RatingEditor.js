import React from 'react';
import PropTypes from 'prop-types';
import FocusLock from 'react-focus-lock';
import Rating from 'react-rating';

const RatingEditor = props =>
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
						<div className="rf-col-edit-rating col-12">
							<Rating 
								fractions={2}
								emptySymbol="la la-star-o"
								fullSymbol="la la-star"
								initialRating={props.value}
								tabIndex="1"
								data-autofocus
								onClick={(value) => props.onChange({target: {value: value}})}
							/>
						</div>
					</div>
					<div className="row">
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

RatingEditor.propTypes = 
{
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
	title: PropTypes.string,
	value: PropTypes.any,
	disabled: PropTypes.bool,
	visible: PropTypes.bool,
	focus: PropTypes.bool,
};

RatingEditor.defaultProps = {
	focus: true
};

export default RatingEditor;
