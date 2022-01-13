import React, {useCallback, useState} from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import {Col, Row, Button} from 'reactstrap';
import {SortableContainer, SortableElement, SortableHandle} from 'react-sortable-hoc';
import Misc from '../../../../../../libs/Misc';
import ElementHelper from './Elements/Helper';
import Inspector from './Inspector/Inspector';

const SortHandle = SortableHandle(({children}) => children);

const SortContainer = SortableContainer(({children}) => children);

const SortElement = SortableElement(({children}) => children);

// eslint-disable-next-line react/prop-types
const ElementControls = ({className, parent, children}) =>
{
	return (
		<div className={className || 'rf-bareditor-element-controls'}>
			{children}
			{parent.elements.length > 1 &&
				<SortHandle>
					<div className="rf-bareditor-controls-button grab">
						<i className="la la-arrows-alt" title="Move" />
					</div>					
				</SortHandle>
			}
		</div>
	);
};

const Bar = (props) =>
{
	const [selected, setSelected] = useState(null);

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
			selected, props.bar.elements);
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
					props.bar, 
					[element], 
					[clone])
			}
		});

	}, [props.bar, selected]);
		
	const handleAddElement = useCallback((index, parent) =>
	{
		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.addElement(
					props.bar, 
					index, 
					ElementHelper.buildBlockOrEmptyElement(parent), 
					parent)
			}
		});
	}, [props.bar]);
	
	const handleDelElement = useCallback((index, parent) =>
	{
		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.delElement(
					props.bar, 
					index, 
					parent)
			}
		});
	}, [props.bar]);

	const handleMoveElement = useCallback((oldIndex, newIndex, parent) =>
	{
		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.moveElement(
					props.bar,  
					oldIndex, 
					newIndex, 
					parent)
			}
		});
	}, [props.bar]);

	const handleSplitElement = useCallback((elm, index, parent) =>
	{
		const half =  Math.floor(elm.widths.md / 2);
		
		props.onChange({
			target: {
				name: 'elements',
				value: ElementHelper.addElement(
					props.bar, 
					index, 
					ElementHelper.buildBlockOrEmptyElement(parent, half), 
					parent)
			}
		});
	}, [props.bar]);

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
					props.bar, 
					nodes, 
					values)
			}
		});
	}, [props.bar]);

	const renderElement = (elm, index, parent) =>
	{
		if(elm.type === 'block')
		{
			const axis = isVertical?
				'y':
				'x';
			
			return (
				<SortElement
					key={index}
					index={index}
					elm={elm}>
					<SortContainer
						axis={axis}
						lockAxis={axis}
						onSortEnd={({oldIndex, newIndex}) => 
							handleMoveElement(oldIndex, newIndex, elm)} 
						useDragHandle
						helperClass="rf-bareditor-sortable">
						<Col 
							className={classNames(
								'rf-bareditor-element', 
								'rf-bareditor-block', 
								'rf-bar-block', {
									'vert': isVertical, 
									'horz': !isVertical, 
									'last': index === parent.elements.length - 1
								})}
							style={{justifyContent: elm.align}}
							xs={elm.widths.xs}
							md={elm.widths.md}>
							{renderElements(elm.elements, elm)}
							<ElementControls
								className="rf-bareditor-block-controls"
								parent={parent}>
								<div className="rf-bareditor-controls-title">Block {1+index}</div>
								<div 
									className="rf-bareditor-controls-button"
									title="Edit block"
									onClick={(e) => handleSelectElement(elm, e)}>
									<i className="la la-pen"/>
								</div>
								{elm.widths.md > 1 && 
									<div 
										className="rf-bareditor-controls-button"
										title="Split block"
										onClick={() => handleSplitElement(elm, index, parent)}>
										<i className="la la-cut"/>
									</div>
								}
								<div 
									className="rf-bareditor-controls-button"
									onClick={() => handleDelElement(index, parent)}>
									<i className="la la-trash" title="Delete block" />
								</div>
							</ElementControls>
						</Col>
					</SortContainer>
				</SortElement>
			);
		}
		
		let compo = null;
		switch(elm.type)
		{
		case 'button':
			compo =
				<Button
					className="rf-bareditor-button rf-bar-button"
					title={elm.title}
					color={elm.color}
					size={elm.size}>
					<i className={`inter-${elm.icon}`}/>
					{elm.size === 'lg' && <div>{elm.title}</div>}
				</Button>;
			break;

		case 'menu':
			compo = 
				<div className="rf-bareditor-menu rf-bar-menu">
					<i className="la la-bars" />
				</div>
			;	    
			break;  

		case 'empty':
			compo = 
				<div 
					className="rf-bareditor-empty rf-bar-empty">
					<div>{1+index}</div>
				</div>;
			break;

		default:
			compo = 
				<div>
					Unknown
				</div>;
		}

		return (
			<SortElement
				key={index}
				index={index}
				elm={elm}>
				<div className="rf-bareditor-element">
					<div
						className={classNames('rf-bareditor-element-wrapper', {'selected': elm.id === selected})}
						onClick={(e) => handleSelectElement(elm, e)}>
						{compo}
						<ElementControls
							parent={parent}>
							<div className="rf-bareditor-controls-title">Element {1+index}</div>
							<div 
								className="rf-bareditor-controls-button"
								title="Add"
								onClick={() => handleAddElement(index, parent)}>
								<i className="la la-plus-circle"/>
							</div>
							<div 
								className="rf-bareditor-controls-button"
								onClick={() => handleDelElement(index, parent)}>
								<i className="la la-trash" title="Delete" />
							</div>
						</ElementControls>
					</div>
				</div>
			</SortElement>
		);
	};

	const renderElements = (elements, parent) =>
	{
		return (
			elements.map((elm, index) => renderElement(elm, index, parent))
		);
	};

	const renderBarElements = (bar) =>
	{
		const axis = isVertical?
			'y':
			'x';

		return (
			<SortContainer
				axis={axis}
				lockAxis={axis}
				onSortEnd={({oldIndex, newIndex}) => handleMoveElement(oldIndex, newIndex, bar)} 
				useDragHandle 
				helperClass="rf-bareditor-sortable">
				<Row 
					className={`rf-bareditor-bar ${isVertical? 'vert': 'horz'}`}
					style={{
						backgroundColor: bar.bgColor,
						color: bar.color,
						minWidth: isVertical? 
							bar.size? 
								bar.size + 'px': 
								undefined:
							undefined,
						minHeight: isVertical? 
							undefined: 
							bar.size?
								bar.size + 'px':
								undefined
					}}>
					{renderElements(bar.elements, bar)}
				</Row>
			</SortContainer>
		);
	};

	if(!props.visible)
	{
		return null;
	}

	const isVertical = props.bar.type === 'left' || props.bar.type === 'right';
	
	return (
		<div
			className={`rf-bareditor-${props.bar.type}`}>
			{renderBarElements(props.bar)}
		
			<Inspector 
				screens={props.screens}
				bar={props.bar}
				element={ElementHelper.findElementById(selected, props.bar.elements)}
				onChange={handleElementChanged}
				onHide={handleDeselectElement}
			/>
		</div>
	);
};

Bar.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
	app: PropTypes.object.isRequired,
	screens: PropTypes.array.isRequired,
	bar: PropTypes.object.isRequired,
	onChange: PropTypes.func.isRequired,
};

export default Bar;