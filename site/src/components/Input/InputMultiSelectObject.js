import React, {useCallback, useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import MultiSelect from 'react-multi-select-component';

const transform = (arr, key, value) =>
	arr.map(o => ({value: o[key], label: o[value]}));

const InputMultiSelectObject = (props) =>
{
	const {objectKey, objectLabel, ...rest} = props;
	
	const [options, setOptions] = useState(() => 
		transform(props.options, objectKey, objectLabel));
	const [values, setValues] = useState(() => 
		transform(props.values, objectKey, objectLabel));

	useEffect(() =>
	{
		setOptions(
			transform(props.options, objectKey, objectLabel));
	}, [props.options]);
	
	useEffect(() =>
	{
		setValues(
			transform(props.values, objectKey, objectLabel));
	}, [props.values]);
	
	const handleChange = useCallback((value) =>
	{
		const {options, name, id, onChange} = props;

		return onChange({
			target: {
				name: name, 
				id: id, 
				value: value?
					options.filter(o => value
						.findIndex(v => v.value === o[objectKey]) >= 0):
					[]
			}
		});
	}, [objectKey, props.options, props.name, props.id, props.onChange]);

	const renderValues = useCallback((selected) =>
	{
		return selected.length === 0?
			'Selecionar...':
			selected.map(option => option.label).join(', ');
	}, []);

	return (
		<MultiSelect
			hasSelectAll={false}
			disableSearch
			{...rest}
			options={options}
			value={values}
			valueRenderer={renderValues}
			onChange={handleChange}
		/>
	);
};

InputMultiSelectObject.propTypes = 
{
	options: PropTypes.array.isRequired,
	values: PropTypes.array.isRequired,
	objectKey: PropTypes.string,
	objectLabel: PropTypes.string,
	name: PropTypes.string,
	id: PropTypes.string,
	onChange: PropTypes.func
};

InputMultiSelectObject.defaultProps = 
{
	objectKey: 'value',
	objectLabel: 'label',
};

export default InputMultiSelectObject;