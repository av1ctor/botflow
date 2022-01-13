import React from 'react';
import PropTypes from 'prop-types';
import {Row, Col, Label, Input, InputGroup, InputGroupAddon} from 'reactstrap';
import AutoSizer from 'react-virtualized-auto-sizer';
import IconPicker from '../../../../../../../../components/Picker/IconPicker';

const ButtonElm = (props) =>
{
	const elm = props.element;

	const renderScreenOptions = (screens) =>
	{
		return [{id: '', title: ''}].concat(screens).map((screen, index) =>
			<option key={index} value={screen.id}>{screen.title}</option>);
	};

	return (
		<>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Title</Label>
					<Input 
						type="text"
						name="title"
						value={elm.title || ''}
						required
						onChange={props.onChange}
					/>
				</Col>
			</Row>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Icon</Label>
					<div style={{width: '100%', height: 92}}>
						<AutoSizer>
							{({width}) => 
								<IconPicker
									dir="horz"
									width={width}
									selected={elm.icon}
									onChange={(icon) => props.onChange({target: {name: 'icon', value: icon}})}
								/>
							}
						</AutoSizer>
					</div>
				</Col>
			</Row>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Color</Label>
					<Input 
						type="select"
						name="color"
						value={elm.color || ''}
						onChange={props.onChange}>
						<option value=""></option>
						<option value="primary">Primary</option>
						<option value="secondary">Secondary</option>
						<option value="info">Info</option>
						<option value="success">Success</option>
						<option value="danger">Danger</option>
						<option value="warning">Warning</option>
						<option value="light">Light</option>
						<option value="dark">Dark</option>
					</Input>
				</Col>
			</Row>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Size</Label>
					<Input 
						type="select"
						name="size"
						value={elm.size || ''}
						onChange={props.onChange}>
						<option value="sm">Small</option>
						<option value="">Normal</option>
						<option value="lg">Large</option>
					</Input>
				</Col>
			</Row>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">On click</Label>
					<InputGroup>
						<Input 
							type="select"
							name="onClick.action"
							value={elm.onClick.action || ''}
							required
							onChange={props.onChange}>
							<option value=""></option>
							<option value="show">Show screen</option>
							<option value="logout">Logout user</option>
						</Input>
						<InputGroupAddon addonType="append">
							<Input 
								type="select"
								name="onClick.target"
								value={elm.onClick.target || ''}
								disabled={elm.onClick.action !== 'show'}
								required
								onChange={props.onChange}>
								{renderScreenOptions(props.screens)}
							</Input>
						</InputGroupAddon>
					</InputGroup>
				</Col>
			</Row>
		</>
	);	
};

ButtonElm.propTypes = 
{
	element: PropTypes.object,
	screens: PropTypes.array.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default ButtonElm;