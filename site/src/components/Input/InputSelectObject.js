import React from 'react';
import PropTypes from 'prop-types';
import {Input} from 'reactstrap';

const InputSelectObject = (props) =>
{
	const {objectKey, objectLabel, value, options, onChange, ...rest} = props;
	
	return (
		<Input
			{...rest}
			type="select"
			value={value[objectKey]}
			onChange={onChange}>
			{options.map((option, index) => 
				<option 
					key={index} 
					value={option[objectKey]}>
					{option[objectLabel]}
				</option>
			)}
		</Input>
	);
};

InputSelectObject.propTypes = 
{
	options: PropTypes.array.isRequired,
	value: PropTypes.object.isRequired,
	objectKey: PropTypes.string,
	objectLabel: PropTypes.string,
	name: PropTypes.string,
	id: PropTypes.string,
	onChange: PropTypes.func
};

InputSelectObject.defaultProps = 
{
	objectKey: 'value',
	objectLabel: 'label',
};

export default InputSelectObject;