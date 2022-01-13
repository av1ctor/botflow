import React from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Container, Button} from 'reactstrap';
import CollectionUtil from '../../../../../../../libs/CollectionUtil';

const General = (props) =>
{
	const screen = props.screen;

	return (
		<form onSubmit={props.onSubmit}>
			<Container fluid>
				<Row>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Id</Label>
						<Input 
							type="text"
							name="id"
							value={screen.id || ''}
							disabled
						/>
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Title</Label>
						<Input 
							type="text"
							name="title"
							value={screen.title || ''}
							onChange={props.onChange}
						/>
					</Col>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Description</Label>
						<Input 
							type="text"
							name="desc"
							value={screen.desc || ''}
							onChange={props.onChange}
						/>
					</Col>
				</Row>
				{props.app.id &&
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">URL</Label>
							<Input 
								type="text"
								value={CollectionUtil.genAppScreenUrl(props.app.id, screen.id)}
								readOnly
							/>
						</Col>
					</Row>
				}
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
	screen: PropTypes.object.isRequired,
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
};

export default General;

