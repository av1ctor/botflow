import React, { useCallback } from 'react';
import PropTypes from 'prop-types';
import {Container, Row, Col, Label, Input, Button, InputGroup, InputGroupAddon} from 'reactstrap';
import CollectionUtil from '../../../../../../../libs/CollectionUtil';
import ElementHelper from '../Elements/Helper';
import Text from './Elements/Text';
import Form from './Elements/Form';

const Inspector = (props) =>
{
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
					<Text
						element={elm}
						onChange={onChange} 
					/>
				</>
			);

		case 'form':
			return (
				<>
					{renderCommonFields(elm, onChange)}
					<Form
						element={elm}
						onChange={onChange} 
					/>
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
		const types = CollectionUtil.getAppScreenElementTypes();
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
			className="rf-screeneditor-inspector visible" 
			onClick={(e) => e.stopPropagation()}>
			<div className="rf-screeneditor-inspector-title"><i className="la la-search" /> Inspector</div>
			<div 
				className="rf-bareditor-inspector-close"
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
};

Inspector.propTypes = 
{
	screen: PropTypes.object.isRequired,
	element: PropTypes.object,
	onHide: PropTypes.func.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Inspector;