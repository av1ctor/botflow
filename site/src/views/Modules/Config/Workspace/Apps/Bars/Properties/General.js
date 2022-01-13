import React from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Container, Button} from 'reactstrap';

const General = (props) =>
{
	const bar = props.bar;

	return (
		<form onSubmit={props.onSubmit}>
			<Container fluid>
				<Row>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Id</Label>
						<Input 
							type="text"
							name="id"
							value={bar.id || ''}
							disabled
						/>
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Size (px)</Label>
						<Input 
							type="number"
							name="size"
							value={bar.size || ''}
							onChange={props.onChange}
						/>
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Text color</Label>
						<Input 
							type="text"
							name="color"
							value={bar.color || ''}
							onChange={props.onChange}
						/>
					</Col>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Background color</Label>
						<Input 
							type="text"
							name="bgColor"
							value={bar.bgColor || ''}
							onChange={props.onChange}
						/>
					</Col>
				</Row>
				<Row>
					<Col sm="12">
						<Button
							color="primary"
							type="submit"
							onClick={props.onSubmit}
							className="mt-4">
							Update
						</Button>
					</Col>
				</Row>
			</Container>
		</form>	
	);
};

General.propTypes = 
{
	app: PropTypes.object.isRequired,
	bar: PropTypes.object.isRequired,
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
};

export default General;

