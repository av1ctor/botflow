import React, {useCallback} from 'react';
import PropTypes from 'prop-types';
import MultiSelect from 'react-multi-select-component';

const InputMultiSelect = props =>
{
	const handleChange = useCallback((value) =>
	{
		const {name, id, onChange} = props;

		const values = value?
			value.sort((a, b) => a.label.localeCompare(b.label)):
			[];

		return onChange({target: {name: name, id: id, value: values}});
	}, [props.name, props.id, props.onChange]);

	const renderValues = useCallback((selected) =>
	{
		return selected.length === 0?
			'Selecionar...':
			selected.map(option => option.label).join(', ');
	}, []);

	const {options, values, ...other} = props;
	
	return (
		<MultiSelect
			{...other}
			options={options}
			value={values}
			onChange={handleChange}
			valueRenderer={renderValues}
		/>
	);
};

InputMultiSelect.propTypes = 
{
	options: PropTypes.array.isRequired,
	values: PropTypes.array.isRequired,
	name: PropTypes.string,
	id: PropTypes.string,
	onChange: PropTypes.func
};

export default InputMultiSelect;