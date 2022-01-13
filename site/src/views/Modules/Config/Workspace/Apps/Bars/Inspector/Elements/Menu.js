import React, { useCallback } from 'react';
import PropTypes from 'prop-types';
import {Button, Row, Col, Label, Input, InputGroup, InputGroupAddon} from 'reactstrap';
import AutoSizer from 'react-virtualized-auto-sizer';
import IconPicker from '../../../../../../../../components/Picker/IconPicker';
import ExtensibleListCollapsed from '../../../../../../../../components/List/ExtensibleListCollapsed';
import Misc from '../../../../../../../../libs/Misc';

const MenuElm = (props) =>
{
	const elm = props.element;

	const handleAddItem = useCallback(() =>
	{
		const item = {
			type: 'menuitem',
			id: Misc.genId(),
			icon: '',
			title: '',
			onClick: {action: '', target: ''}
		};

		const elements = Array.from(props.element.elements);
		elements.push(item);

		props.onChange({
			target: {
				name: 'elements',
				value: elements
			}
		});
	}, [props.element]);
	
	const handleDelItem = useCallback((item, index) =>
	{
		const elements = Array.from(props.element.elements);
		elements.splice(index, 1);

		props.onChange({
			target: {
				name: 'elements',
				value: elements
			}
		});
	}, [props.element]);

	const handleMoveItem = useCallback((item, index, dir, e) =>
	{
		e.stopPropagation();
		
		const elements = Misc.moveArray(props.element.elements, index, dir === 'up'? index-1: index+1);

		props.onChange({
			target: {
				name: 'elements',
				value: elements
			}
		});
	}, [props.element]);

	const renderPreview = ({item, index}) => 
	{
		return (
			<div key={`prev_${index}`}>
				{item.title}
			</div>
		);
	};

	const renderActions = ({item, index}) =>
	{
		return (
			<>
				<Button
					size="xs" 
					outline
					className="rf-listbox-button" 
					title="Move up"
					disabled={index === 0} 
					onClick={(e) => handleMoveItem(item, index, 'up', e)}>
					<i className="rf-listbox-button-icon la la-arrow-circle-up"/>
				</Button>
				<Button
					size="xs" 
					outline
					className="rf-listbox-button" 
					title="Move down" 
					disabled={index === props.element.elements.length - 1} 
					onClick={(e) => handleMoveItem(item, index, 'down', e)}>
					<i className="rf-listbox-button-icon la la-arrow-circle-down"/>
				</Button>
			</>
		);
	};

	const renderScreenOptions = (screens) =>
	{
		return [{id: '', title: ''}].concat(screens).map((screen, index) =>
			<option key={index} value={screen.id}>{screen.title}</option>);
	};

	const renderFields = (item, index) =>
	{
		const base = `elements[${index}]`;
		
		return (
			<>
				<Row>
					<Col xs="12">
						<Label className="rf-form-label">Title</Label>
						<Input
							type="text"
							name={`${base}.title`}
							value={item.title || ''}
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
										selected={item.icon}
										onChange={(icon) => props.onChange({target: {name: `${base}.icon`, value: icon}})}
									/>
								}
							</AutoSizer>
						</div>
					</Col>
				</Row>
				<Row>
					<Col xs="12">
						<Label className="rf-form-label">On click</Label>
						<InputGroup>
							<Input 
								type="select"
								name={`${base}.onClick.action`}
								value={item.onClick.action || ''}
								required
								onChange={props.onChange}>
								<option value=""></option>
								<option value="show">Show screen</option>
								<option value="logout">Logout user</option>
							</Input>
							<InputGroupAddon addonType="append">
								<Input 
									type="select"
									name={`${base}.onClick.target`}
									value={item.onClick.target || ''}
									disabled={item.onClick.action !== 'show'}
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

	return (
		<>
			<Row>
				<Col xs="12">
					<Label className="rf-form-label">Items</Label>
					<ExtensibleListCollapsed
						items={elm.elements}
						useIndex
						renderPreview={renderPreview}
						renderActions={renderActions}
						addItem={handleAddItem}
						deleteItem={handleDelItem}>
						{({item, index}) =>
							renderFields(item, index)}
					</ExtensibleListCollapsed>
				</Col>
			</Row>
		</>
	);	
};

MenuElm.propTypes = 
{
	element: PropTypes.object,
	screens: PropTypes.array.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default MenuElm;