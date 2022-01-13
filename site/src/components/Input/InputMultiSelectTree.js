import React, {useCallback} from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import MultiSelect from 'react-multi-select-component';

const InputMultiSelectTree = (props) =>
{
	const renderItem = useCallback(({checked, option, onClick, disabled}) =>
	{
		return (
			<div 
				className={classNames('item-renderer', disabled && 'disabled')}
				style={{paddingLeft: (0.5 + option.level) + 'rem'}}>
				<input
					type="checkbox"
					onChange={onClick}
					checked={checked}
					tabIndex={-1}
					disabled={disabled} />
				<span style={{paddingLeft:'0.5rem'}}>{option.label}</span>
			</div>
		);
	}, []);

	const renderValues = useCallback((selected) =>
	{
		return selected.length === 0?
			'Selecionar...':
			selected.map(option => option.label).join(', ');
	}, []);

	const handleChange = useCallback((value) =>
	{
		const {name, id, onChange} = props;

		const values = value?
			value
				.sort((a, b) => a.label.localeCompare(b.label))
				.sort((a, b) => a.level - b.level):
			[];

		return onChange({target: {name: name, id: id, value: values}});
	}, [props.name, props.id, props.onChange]);

	const {options, values, ...other} = props;
	
	return (
		<MultiSelect
			hasSelectAll={false}
			disableSearch
			{...other}
			options={options}
			value={values}
			onChange={handleChange}
			valueRenderer={renderValues}
			ItemRenderer={renderItem}
		/>
	);
};

InputMultiSelectTree.propTypes = 
{
	options: PropTypes.array.isRequired,
	values: PropTypes.array.isRequired,
	name: PropTypes.string,
	id: PropTypes.string,
	onChange: PropTypes.func,
};

export default InputMultiSelectTree;