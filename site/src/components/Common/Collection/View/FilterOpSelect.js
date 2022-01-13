import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Dropdown} from 'primereact/dropdown';
import CollectionUtil from '../../../../libs/CollectionUtil';

export default class FilterOpSelect extends PureComponent
{
	static propTypes = 
	{
		op: PropTypes.string.isRequired,
		scalarType: PropTypes.string.isRequired,
		disabled: PropTypes.bool,
		onChange: PropTypes.func.isRequired,
		name: PropTypes.string,
		className: PropTypes.string,
		isModal: PropTypes.bool,
		showLabel: PropTypes.bool,
		allowExists: PropTypes.bool,
	};

	static getFilterOps(type = null, allowExists = false)
	{
		return CollectionUtil.getFilterOps(type, allowExists);
	}

	render()
	{
		const {op, scalarType, name, className, disabled, onChange, isModal, showLabel, allowExists} = this.props;

		return (
			<Dropdown 
				className={className}
				panelClassName={isModal? 'rf-over-the-modal': null}
				placeholder=""
				value={op}
				options={FilterOpSelect.getFilterOps(scalarType, allowExists)}
				optionLabel={showLabel? 'label': 'symbol'}
				optionValue="value"
				onChange={(e) => onChange({target: {name: name, value: e.value}}, 'op')}
				itemTemplate={(option) => option.value? <div>{option.symbol} {option.label}</div>: ''}
				disabled={disabled}
				appendTo={document.body} />
		);
	}
}