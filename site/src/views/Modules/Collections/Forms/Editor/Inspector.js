import React, { useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Container, Row, Col, Label, Input, InputGroup, InputGroupAddon, Button} from 'reactstrap';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import {ElementHelper} from './Form';

const Inspector = observer((props) =>
{
	const store = props.store;
	const collection = store.collection;
	
	const handleChangeType = useCallback((type) =>
	{
		const elm = props.element;
		
		props.onChange({
			target: {
				name: null,
				value: {
					id: elm.id,
					type: type,
					widths: elm.widths?
						elm.widths:
						undefined,
					height: elm.height?
						elm.height:
						undefined,
					...ElementHelper.buildElementProps(type),
				},
			}
		});
	}, [props.element]);

	const filterUsedFields = (fields, steps, current) =>
	{
		const elementFields = ElementHelper.findElementFields(steps);
		return fields.filter(([name, ]) => name === current || elementFields.indexOf(name) < 0);
	};

	const renderFieldOptions = (current) =>
	{
		const collectionFields = Object.entries(collection.schemaObj.columns);
		const fields = props.form.use !== 'edit'? 
			filterUsedFields(collectionFields, props.form.steps, current):
			collectionFields;

		return [['', {label: ''}]].concat(fields)
			.map(([name, field], index) =>
				<option key={index} value={name}>{field.label}</option>);
	};

	const renderWidthWidget = (elm, onChange) =>
	{
		return (
			<Row>
				<Col xs="6">
					<Label className="rf-form-label">Width mobile</Label>
					<Input
						type="number"
						max="12"
						min="1"
						name="widths.xs"
						value={elm.widths.xs || 6}
						onChange={onChange}
					/>
				</Col>
				<Col xs="6">
					<Label className="rf-form-label">Width desktop</Label>
					<Input
						type="number"
						max="12"
						min="1"
						name="widths.md"
						value={elm.widths.md || 12}
						onChange={onChange}
					/>
				</Col>
			</Row>
		);
	};
	
	const renderCommonFields = (elm, onChange) =>
	{
		return (
			<>
				{renderWidthWidget(elm, onChange)}
				<Row>
					<Col xs="12">
						<Label className="rf-form-label">Height</Label>
						<InputGroup>
							<Input 
								type="number"
								min="1"
								name="height.value"
								value={elm.height.value || ''}
								onChange={props.onChange}
							/>
							<InputGroupAddon addonType="append">
								<Input 
									type="select"
									name="height.unit"
									value={elm.height.unit || ''}
									onChange={props.onChange}>
									<option value=""></option>
									<option value="px">px</option>
									<option value="rem">rem</option>
									<option value="vh">vh</option>
								</Input>
							</InputGroupAddon>
						</InputGroup>
					</Col>
				</Row>
			</>
		);
	};	
	const renderFields = (elm, onChange) =>
	{
		switch(elm.type)
		{
		case 'text':
			return (
				<>
					{renderCommonFields(elm, onChange)}
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Text</Label>
							<Input 
								type="textarea"
								rows="4"
								name="text"
								value={elm.text || ''}
								onChange={onChange}
							/>
						</Col>
					</Row>
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Size</Label>
							<Input 
								type="number"
								min="8"
								max="512"
								name="size"
								value={elm.size || 14}
								onChange={onChange}
							/>
						</Col>
					</Row>
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Bold</Label>
							<Input 
								className="rf-form-checkbox"
								type="checkbox"
								name="bold"
								checked={elm.bold? true: false}
								onChange={onChange}
							/>
						</Col>
					</Row>
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Color</Label>
							<Input 
								type="text"
								name="color"
								value={elm.color || ''}
								onChange={onChange}
							/>
						</Col>
					</Row>
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Background Color</Label>
							<Input 
								type="text"
								name="backgroundColor"
								value={elm.backgroundColor || ''}
								onChange={onChange}
							/>
						</Col>
					</Row>
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Justify</Label>
							<Input 
								type="select"
								name="justify"
								value={elm.justify || 'left'}
								onChange={onChange}>
								<option value="left">Left</option>
								<option value="center">Center</option>
								<option value="right">Right</option>
							</Input>
						</Col>
					</Row>
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Align</Label>
							<Input 
								type="select"
								name="align"
								value={elm.align || 'top'}
								onChange={onChange}>
								<option value="flex-start">Top</option>
								<option value="center">Middle</option>
								<option value="flex-end">Bottom</option>
							</Input>
						</Col>
					</Row>
				</>
			);

		case 'field':
			return (
				<>
					{renderWidthWidget(elm, onChange)}
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Field</Label>
							<Input 
								type="select"
								name="name"
								value={elm.name || ''}
								onChange={onChange}>
								{renderFieldOptions(elm.name)}
							</Input>
						</Col>
					</Row>
					{props.form.use !== 'edit' || props.step > 0?
						<Row>
							<Col xs="12">
								<Label className="rf-form-label">Disabled</Label>
								<Input 
									className="rf-form-checkbox"
									type="checkbox"
									name="disabled"
									checked={!elm.name || elm.disabled? true: false}
									onChange={onChange}
								/>
							</Col>
						</Row>:
						<Row>
							<Col xs="12">
								<Label className="rf-form-label">Required</Label>
								<Input 
									className="rf-form-checkbox"
									type="checkbox"
									name="required"
									checked={elm.required? true: false}
									onChange={onChange}
								/>
							</Col>
						</Row>
					}
					{!elm.name &&
						<Row>
							<Col xs="12">
								<Label className="rf-form-label">Label</Label>
								<Input 
									type="text"
									name="label"
									value={elm.label || ''}
									required
									onChange={onChange}
								/>
							</Col>
						</Row>
					}
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Description</Label>
							<Input 
								type="textarea"
								rows="3"
								name="desc"
								value={elm.desc || ''}
								onChange={onChange}
							/>
						</Col>
					</Row>
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Placeholder</Label>
							<Input 
								type="text"
								name="placeholder"
								value={elm.placeholder || ''}
								onChange={onChange}
							/>
						</Col>
					</Row>
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Default value</Label>
							<Input 
								type="text"
								name="default_"
								value={elm.default_ || ''}
								onChange={onChange}
							/>
						</Col>
					</Row>
				</>
			);
		
		case 'empty':
			return (
				<>
					{renderCommonFields(elm, onChange)}
				</>
			);
		}

		return null;
	};
	
	const renderElementTypeOptions = (current) =>
	{
		const types = CollectionUtil.getFormElementTypes();
		return (
			<div className="rf-form-options-container">
				{types.map((type, index) => 
					<Button 
						key={index}
						outline
						color="primary"
						className={`rf-form-options-button ${current === type.name? 'active': ''}`}
						onClick={() => handleChangeType(type.name)}>
						<i className={`la la-${type.icon}`} />
						{type.label}
					</Button>
				)}
			</div>
		);
	};
	
	if(!props.element)
	{
		return null;
	}

	return (
		<div 
			className="rf-formeditor-inspector visible" 
			onClick={(e) => e.stopPropagation()}>
			<div>Inspector</div>
			<div 
				className="rf-formeditor-inspector-close"
				onClick={props.onHide}>
				<i className="la la-times-circle" />
			</div>
			<Container fluid>
				<Row>
					<Col xs="12">
						<Label className="rf-form-label">Type</Label>
						{renderElementTypeOptions(props.element.type || '')}
					</Col>
				</Row>
				{renderFields(props.element, props.onChange)}
			</Container>
		</div>
	);
});

Inspector.propTypes = 
{
	store: PropTypes.object.isRequired,
	form: PropTypes.object.isRequired,
	step: PropTypes.number.isRequired,
	element: PropTypes.object,
	onHide: PropTypes.func.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Inspector;