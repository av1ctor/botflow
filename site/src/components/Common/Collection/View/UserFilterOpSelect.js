import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import {Dropdown} from 'primereact/dropdown';
import CollectionUtil from '../../../../libs/CollectionUtil';

export default class UserFilterOpSelect extends PureComponent
{
	static propTypes = 
	{
		op: PropTypes.string.isRequired,
		disabled: PropTypes.bool,
		onChange: PropTypes.func.isRequired,
		name: PropTypes.string,
		className: PropTypes.string,
		isModal: PropTypes.bool,
		showLabel: PropTypes.bool
	};

	render()
	{
		const {op, name, className, disabled, onChange, isModal, showLabel} = this.props;

		return (
			<Dropdown 
				className={className}
				panelClassName={isModal? 'rf-over-the-modal': null}
				placeholder=""
				value={op}
				options={CollectionUtil.getUserFilterOps()}
				optionLabel={showLabel? 'label': 'symbol'}
				optionValue="value"
				onChange={(e) => onChange({target: {name: name, value: e.value}}, 'op')}
				itemTemplate={(option) => option.value? <div>{option.symbol} {option.label}</div>: ''}
				disabled={disabled}
				appendTo={document.body} />
		);
	}
}