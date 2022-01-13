import React, { useCallback } from 'react';
import PropTypes from 'prop-types';
import {Container, Row, Col, Label, Input, Button} from 'reactstrap';
import CollectionUtil from '../../../../../../../libs/CollectionUtil';
import ElementHelper from '../Elements/Helper';
import BlockElm from './Elements/Block';
import ButtonElm from './Elements/Button';
import MenuElm from './Elements/Menu';

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

	const renderFields = (elm, onChange) =>
	{
		switch(elm.type)
		{
		case 'block':
			return (
				<>
					{renderWidthWidget(elm, onChange)}
					<BlockElm
						element={elm}
						onChange={onChange} 
					/>
				</>
			);

		case 'button':
			return (
				<>
					<ButtonElm
						screens={props.screens}
						element={elm}
						onChange={onChange} 
					/>
				</>
			);

		case 'menu':
			return (
				<>
					<MenuElm
						screens={props.screens}
						element={elm}
						onChange={onChange} 
					/>
				</>
			);

		}

		return null;
	};
	
	const renderElementTypeOptions = (current) =>
	{
		const types = CollectionUtil.getAppBarElementTypes();
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
			className="rf-bareditor-inspector visible" 
			onClick={(e) => e.stopPropagation()}>
			<div className="rf-bareditor-inspector-title"><i className="la la-search" /> Inspector</div>
			<div 
				className="rf-bareditor-inspector-close"
				onClick={props.onHide}>
				<i className="la la-times-circle" />
			</div>
			<Container fluid>
				{props.element.type !== 'block' &&
					<Row>
						<Col xs="12">
							<Label className="rf-form-label">Type</Label>
							{renderElementTypeOptions(props.element.type || '')}
						</Col>
					</Row>
				}
				{renderFields(props.element, props.onChange)}
			</Container>
		</div>
	);
};

Inspector.propTypes = 
{
	screens: PropTypes.array.isRequired,
	bar: PropTypes.object.isRequired,
	element: PropTypes.object,
	onHide: PropTypes.func.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Inspector;