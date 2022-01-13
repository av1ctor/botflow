import React from 'react';
import PropTypes from 'prop-types';
import {Row, Col, Label, Input} from 'reactstrap';

const Form = (props) =>
{
	const elm = props.element;

	return (
		<>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Collection</Label>
					<Input 
						type="text"
						name="collection"
						value={elm.collection || ''}
						placeholder="Select a collection..."
						onChange={props.onChange}
					/>
				</Col>
			</Row>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Form</Label>
					<Input 
						type="text"
						name="form"
						value={elm.form || ''}
						disabled={!elm.collection}
						placeholder="Select a form..."
						onChange={props.onChange}
					/>
				</Col>
			</Row>
		</>
	);	
};

Form.propTypes = 
{
	element: PropTypes.object,
	onChange: PropTypes.func.isRequired,
};

export default Form;