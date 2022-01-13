import React, {useCallback, useState} from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import {Container, Col, Row, Button} from 'reactstrap';
import {SortableContainer, SortableElement, SortableHandle} from 'react-sortable-hoc';
import Misc from '../../../../../../libs/Misc';
import CollectionUtil from '../../../../../../libs/CollectionUtil';
import Inspector from './Inspector/Inspector';
import Text from './Elements/Text';
import Form from './Elements/Form';
import ElementHelper from './Elements/Helper';

const SortHandle = SortableHandle(({children}) => children);

const SortContainer = SortableContainer(({children}) => children);

// eslint-disable-next-line react/prop-types
const ElementControls = ({index, parent, className, onDelete, children}) =>
{
	return (
		<div className={className || 'rf-screeneditor-element-controls'}>
			{children}
			<div 
				className="rf-screeneditor-controls-button"
				onClick={() => onDelete(index, parent)}>
				<i className="la la-trash" title="Delete" />
			</div>
			{parent.elements.length > 1 &&
				<SortHandle>
					<div className="rf-screeneditor-controls-button grab">
						<i className="la la-arrows-alt" title="Move" />
					</div>					
				</SortHandle>
			}
		</div>
	);
};

const SortElement = SortableElement(({children}) =>
	<div className="rf-screeneditor-element">
		{children}
	</div>
);

const Screen = (props) =>
{
	const [selected, setSelected] = useState(null);

	const handleShowPreview = useCallback( () =>
	{
		const url = CollectionUtil.genAppScreenUrl(props.app.id, props.screen.id);
		window.open(url, '_blank');
	}, [props.app, props.screen]);

	const handleAddElement = useCallback((index, parent) =>
	{
		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.addElement(
					props.screen, 
					index, 
					ElementHelper.buildBlockOrEmptyElement(parent), 
					parent)
			}
		});
	}, [props.screen]);

	const handleSplitElement = useCallback((elm, index, parent) =>
	{
		const half =  Math.floor(elm.widths.md / 2);
		
		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.addElement(
					props.screen, 
					index, 
					ElementHelper.buildBlockOrEmptyElement(parent, half), 
					parent)
			}
		});
	}, [props.screen]);

	const handleDelElement = useCallback((index, parent) =>
	{
		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.delElement(
					props.screen, 
					index, 
					parent)
			}
		});
	}, [props.screen]);

	const handleMoveElement = useCallback((oldIndex, newIndex, parent) =>
	{
		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.moveElement(
					props.screen,  
					oldIndex, 
					newIndex, 
					parent)
			}
		});
	}, [props.screen]);

	const handleResizeElement = useCallback((dir, elm, index, parent) =>
	{
		const nodes = [elm, parent.elements[dir === 'left'? index-1: index+1]];
		const values = [{...elm}, {...nodes[1]}];
		
		values[0].widths.md += 1;
		values[1].widths.md -= 1;

		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.updateElements(
					props.screen, 
					nodes, 
					values)
			}
		});
	}, [props.screen]);

	const handleSelectElement = useCallback((elm, e) =>
	{
		e.stopPropagation();
		setSelected(elm.id);
	}, []);

	const handleDeselectElement = useCallback(() =>
	{
		setSelected(null);
	}, []);

	const handleElementChanged = useCallback(e =>
	{
		const name = e.target.name || e.target.id;
		const value = e.target.type !== 'checkbox'? 
			e.target.value: 
			e.target.checked;

		const element = ElementHelper.findElementById(
			selected, props.screen.elements);
		if(!element)
		{
			return;
		}

		const clone = name?
			Object.assign({}, element):
			value;
		if(name)
		{
			Misc.setFieldValue(clone, name, value);
		}

		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.updateElements(
					props.screen, 
					[element], 
					[clone])
			}
		});

	}, [props.screen, selected]);

	const renderElement = (elm, index, parent) =>
	{
		if(elm.type === 'block')
		{
			return (
				<SortElement
					key={index}
					index={index}
					elm={elm}>
					<SortContainer
						axis="x"
						lockAxis="x"
						onSortEnd={({oldIndex, newIndex}) => handleMoveElement(oldIndex, newIndex, elm)} 
						useDragHandle
						helperClass="rf-screeneditor-sortable">
						<div className="rf-screeneditor-block">
							<Row className="rf-screen-block">
								{renderElements(elm.elements, elm)}
							</Row>
						</div>
						<ElementControls
							className="rf-screeneditor-block-controls"
							index={index}
							parent={parent}
							onDelete={handleDelElement}>
							<div 
								className="rf-screeneditor-controls-button"
								title="Add block"
								onClick={() => handleAddElement(index, parent)}>
								<i className="la la-plus-circle"/>
							</div>
						</ElementControls>
					</SortContainer>
				</SortElement>
			);
		}
		
		let compo = null;
		switch(elm.type)
		{
		case 'text':
			compo =
				<Text 
					element={elm} 
				/>;
			break;

		case 'form':
			compo =
				<Form
					api={props.api}
					container={props.container}
					element={elm} 
				/>;
			break;

		case 'horzline':
			compo = 
				<div 
					className="rf-screeneditor-horzline rf-screen-horzline" 
				/>
			;	
			break;

		case 'empty':
			compo = 
				<div 
					className="rf-screeneditor-empty rf-screen-empty"
					style={{
						height: elm.height.value + elm.height.unit,
					}}>
					<div>Column {1+index}</div>
				</div>;
			break;
		}

		const lelm = index > 0?
			parent.elements[index-1]:
			null;

		const relm = index < parent.elements.length-1?
			parent.elements[index+1]:
			null;

		return (
			<Col 
				key={index}
				xs={elm.widths.xs}
				md={elm.widths.md}
				className={classNames({'last': index === parent.elements.length -1})}>
				<SortElement
					key={index}
					index={index}>
					<div
						className={classNames('rf-screeneditor-element-wrapper', {'selected': elm.id === selected})}
						onClick={(e) => handleSelectElement(elm, e)}>					
						{compo}
					</div>
					<ElementControls
						index={index}
						parent={parent}
						onDelete={handleDelElement}
					>
						{elm.widths.md < 12 && lelm && lelm.widths.md > 1 &&
							<div 
								className="rf-screeneditor-controls-button"
								title="Expand left"
								onClick={() => handleResizeElement('left', elm, index, parent)}>
								<i className="la la-arrow-circle-left" />
							</div>
						}
						{elm.widths.md < 12 && relm && relm.widths.md > 1 &&
							<div 
								className="rf-screeneditor-controls-button"
								title="Expand right"
								onClick={() => handleResizeElement('right', elm, index, parent)}>
								<i className="la la-arrow-circle-right" />
							</div>
						}
						{elm.widths.md > 1 && 
							<div 
								className="rf-screeneditor-controls-button"
								title="Split"
								onClick={() => handleSplitElement(elm, index, parent)}>
								<i className="la la-cut" />
							</div>
						}
					</ElementControls>
				</SortElement>
			</Col>
		);
	};

	const renderElements = (elements, parent) =>
	{
		return (
			elements.map((elm, index) => renderElement(elm, index, parent))
		);
	};

	const renderScreenElements = (screen) =>
	{
		return (
			<SortContainer
				axis="y"
				lockAxis="y"
				onSortEnd={({oldIndex, newIndex}) => handleMoveElement(oldIndex, newIndex, screen)} 
				useDragHandle 
				helperClass="rf-screeneditor-sortable">
				<Container fluid className="rf-screeneditor-elements-list">
					{renderElements(screen.elements, screen)}
				</Container>
			</SortContainer>
		);
	};

	if(!props.visible)
	{
		return null;
	}
	
	return (
		<div 
			className="rf-screeneditor-screen pt-2"
			onClick={handleDeselectElement}>
			{renderScreenElements(props.screen)}

			<Container fluid className="mt-2">
				<Row>
					<Col xs="12">
						{props.app.id &&
							<Button
								color="secondary"
								type="submit"
								onClick={handleShowPreview}
								className="mr-2">
							View
							</Button>
						}
					</Col>
				</Row>
			</Container>

			<Inspector 
				screen={props.screen}
				element={ElementHelper.findElementById(selected, props.screen.elements)}
				onChange={handleElementChanged}
				onHide={handleDeselectElement}
			/>
		</div>
	);
};

Screen.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
	app: PropTypes.object.isRequired,
	screen: PropTypes.object.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Screen;