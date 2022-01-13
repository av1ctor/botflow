import React, {useMemo} from 'react';
import PropTypes from 'prop-types';
import {Container, Col, Row, Label, Input, Button} from 'reactstrap';
import {AutoComplete} from 'primereact/autocomplete';

const Steps = (props) =>
{
	const renderRefField = (value, index) =>
	{
		return <div className="rf-form-auto-item-field" key={index}>{value}</div>;
	};

	const renderRefItem = (item, ref) =>
	{
		return (
			<div className="rf-form-auto-item">
				{ref.preview.columns.map((name, index) => renderRefField(item[name], index))}
			</div>
		);
	};

	const fieldsValues = useMemo(() =>
	{
		const res = {};

		if(props.steps) 
		{
			props.steps.forEach(fields =>
				Object.entries(fields)
					.map(([key, field]) => ({key, value: field.value}))
					.reduce((res, item) => 
					{
						res[item.key] = item.value;
						return res;
					}, 
					res)
			);
		}

		return res;
	}, [props.steps, props.step]);

	const renderField = (elm) =>
	{
		if(!props.steps)
		{
			return null;
		}

		const field = 
			props.steps[props.step][elm.name] || 
			{type: 'text', fake: true, disabled: true, field: {}};
		
		const refName = field.field.ref && field.field.ref.name;
		const ref = refName && props.refs[refName];
		const value = field.fake && elm.default_? 
			elm.default_.func?
				elm.default_.func({fields: fieldsValues}):
				elm.default_.value:
			field.value;

		return (
			<div title={elm.desc}>
				<Label className="rf-form-label">
					{elm.label || field.label}
				</Label>
				{field.type === 'auto'? 
					<AutoComplete 
						multiple={field.field.type === 'array'}
						field={field.field.ref.display}
						value={value} 
						onChange={props.onChange} 
						itemTemplate={item => renderRefItem(item, ref)}
						suggestions={props.refSuggestions[elm.name]} 
						completeMethod={e => props.onRefFilter(e, elm.name, ref)}
						minLength={1}
						className="rf-over-the-modal"
						appendTo={document.body}
						disabled={field.disabled}
						dropdown
						dropdownMode="current" />:				
					<Input 
						className="rf-form-input" 
						type={field.type}
						name={elm.name}
						value={value || ''}
						placeholder={elm.placeholder}
						disabled={field.disabled}
						required={field.required || !field.field.nullable}
						onChange={field.disabled? 
							null: 
							props.onChange}>
						{
							field.options && [{value: '', name: ''}].concat(field.options).map((option, index) => 
								<option key={index} value={option.value}>{option.name}</option>
							)
						}
					</Input>
				}
			</div>
		);
	};

	const renderElement = (elm, parent, index) =>
	{
		if(elm.type === 'row')
		{
			return (
				<Row key={index} className="rf-form-row">
					{renderElements(elm.elements, elm)}
				</Row>
			);
		}
		
		let compo = null;
		switch(elm.type)
		{
		case 'text':
			compo =
				<div 
					className="rf-form-text"
					style={{
						fontSize: elm.size + 'px', 
						fontWeight: elm.bold? 'bold': null, 
						color: elm.color,
						backgroundColor: elm.backgroundColor,
						textAlign: elm.justify, 
						justifyContent: elm.align}}>
					{elm.text.split('\n').map((text, index) => 
						<div key={index}>{text}</div>
					)}
				</div>
			;
			break;

		case 'field':
			compo = 
				renderField(elm)
			;
			break;

		case 'horzline':
			compo = 
				<div 
					className="rf-form-horzline" 
				/>
			;	
			break;

		case 'empty':
			compo = 
				<div className="rf-form-empty">
				</div>;
			break;
		}

		return (
			<Col 
				key={index}
				xs={elm.widths.xs}
				md={elm.widths.md}>
				{compo}
			</Col>
		);
	};

	const renderElements = (elements, parent) =>
	{
		return (
			elements.map((elm, index) => renderElement(elm, parent, index))
		);
	};

	const {form} = props;
		
	return (
		<div className="rf-form-steps">
			<div className="rf-form-step-tabs">
				{form.steps.map((s, index) => 
					<div 
						key={index} 
						className={`rf-form-step-elements ${index === props.step? 'active': ''}`}>
						{index === props.step && 
							<Container fluid className="rf-form-elements-list">
								{renderElements(s.elements, s)}
							</Container>
						}
					</div>
				)}
			</div>
			<div className="rf-form-step-controls">
				<div className="rf-form-step-buttons">
					{props.step > 0 && 
						<Button 
							className="rf-form-step-button"
							onClick={props.onPrevStep}>
							{'Prev'}
						</Button>
					}
					{form.use == 'create'?
						<Button 
							className="rf-form-step-button"
							type="submit"
							onClick={props.step === form.steps.length-1? 
								props.onSubmit:
								props.onNextStep}>
							{props.step === form.steps.length-1? 'Create': 'Next'}
						</Button>:
						form.use === 'edit'?
							<Button 
								className="rf-form-step-button"
								type="submit"
								onClick={props.step === 0?
									props.onSearch:
									props.step === form.steps.length-1? 
										props.onSubmit:
										props.onNextStep}>
								{props.step === 0?
									'Search':
									props.step === form.steps.length-1? 
										'Update': 
										'Next'}
							</Button>:
							props.step < form.steps.length-1 && 
							<Button 
								className="rf-form-step-button"
								type="submit"
								onClick={props.onNextStep}>
								Next
							</Button>
					}
				</div>
				{form.steps.length > 1 && 
					<div className="rf-form-step-indicators">
						{form.steps.map((s, index) => 
							<div 
								key={index} 
								className={`rf-form-step-indicator ${index === props.step? 'active': ''}`}
								title={`Step ${1+index}`}>
							</div>
						)}
					</div>
				}
			</div>
		</div>
	);
};

Steps.propTypes = {
	container: PropTypes.object,
	form: PropTypes.object,
	steps: PropTypes.array,
	step: PropTypes.number,
	refs: PropTypes.object,
	refSuggestions: PropTypes.object,
	onChange: PropTypes.func,
	onRefFilter: PropTypes.func,
	onPrevStep: PropTypes.func,
	onNextStep: PropTypes.func,
	onSearch: PropTypes.func,
	onSubmit: PropTypes.func,
};

export default Steps;