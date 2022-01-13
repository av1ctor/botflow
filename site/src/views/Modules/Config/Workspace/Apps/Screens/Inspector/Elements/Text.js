import React from 'react';
import PropTypes from 'prop-types';
import {Row, Col, Label, Input} from 'reactstrap';

const Text = (props) =>
{
	const elm = props.element;

	return (
		<>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Text</Label>
					<Input 
						type="textarea"
						rows="4"
						name="text"
						value={elm.text || ''}
						placeholder="Type your text..."
						onChange={props.onChange}
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
						onChange={props.onChange}
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
						onChange={props.onChange}
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
						onChange={props.onChange}
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
						onChange={props.onChange}
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
						onChange={props.onChange}>
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
						onChange={props.onChange}>
						<option value="flex-start">Top</option>
						<option value="center">Middle</option>
						<option value="flex-end">Bottom</option>
					</Input>
				</Col>
			</Row>
		</>
	);	
};

Text.propTypes = 
{
	element: PropTypes.object,
	onChange: PropTypes.func.isRequired,
};

export default Text;