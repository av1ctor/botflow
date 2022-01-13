import React from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Container, Button} from 'reactstrap';
import CollectionUtil from '../../../../../../libs/CollectionUtil';

const General = (props) =>
{
	const store = props.store;
	const collection = store.collection;
	
	const renderFormUsageOptions = () =>
	{
		return CollectionUtil.getFormUses()
			.map((type, index)=> 
				<option key={index} value={type.name}>{type.desc}</option>
			);
	};

	const {form} = props;

	return (
		<form onSubmit={props.onSubmit}>
			<Container fluid>
				<Row>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Id</Label>
						<Input 
							type="text"
							name="id"
							value={form.id || ''}
							disabled
						/>
					</Col>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Usage</Label>
						<Input 
							type="select"
							name="use"
							value={form.use || ''}
							onChange={props.onChange}>
							{renderFormUsageOptions()}
						</Input>
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Name</Label>
						<Input 
							type="text"
							name="name"
							value={form.name || ''}
							onChange={props.onChange}
						/>
					</Col>
					<Col xs="12" md="6">
						<Label className="rf-form-label">Description</Label>
						<Input 
							type="text"
							name="desc"
							value={form.desc || ''}
							onChange={props.onChange}
						/>
					</Col>
				</Row>
				<Row>
					<Col xs="6">
						<Label className="rf-form-label">Active</Label>
						<Input 
							className="rf-form-checkbox"
							type="checkbox"
							name="active"
							checked={form.active? true: false}
							onChange={props.onChange}
						/>
					</Col>
					<Col xs="6">
						<Label className="rf-form-label">Public</Label>
						<Input 
							className="rf-form-checkbox"
							type="checkbox"
							name="public"
							checked={form.public? true: false}
							onChange={props.onChange}
						/>
					</Col>
				</Row>		
				{form.id &&
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">URL</Label>
							<Input 
								type="text"
								value={CollectionUtil.genFormUrl(collection.id, form.id)}
								readOnly
							/>
						</Col>
					</Row>
				}
				<Row>
					<Col sm="12">
						<Button
							color="primary"
							disabled={props.isLoading}
							type="submit"
							onClick={props.onSubmit}
							className="mt-4"
						>{form.id? 'Update': 'Create'}</Button>
					</Col>
				</Row>
			</Container>
		</form>	
	);
};

General.propTypes = 
{
	store: PropTypes.object.isRequired,
	form: PropTypes.object.isRequired,
	isLoading: PropTypes.bool,
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
};

export default General;

