import React from 'react';
import PropTypes from 'prop-types';
import {Row, Col, Label, Input} from 'reactstrap';

const BlockElm = (props) =>
{
	const elm = props.element;

	return (
		<>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Align items</Label>
					<Input 
						type="select"
						name="align"
						value={elm.align || ''}
						onChange={props.onChange}
					>
						<option value=""></option>
						<option value="flex-start">Left/Top</option>
						<option value="center">Center</option>
						<option value="flex-end">Right/Bottom</option>
					</Input>
				</Col>
			</Row>
		</>
	);	
};

BlockElm.propTypes = 
{
	element: PropTypes.object,
	onChange: PropTypes.func.isRequired,
};

export default BlockElm;