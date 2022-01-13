import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import {Container, Col, Row, Label, Input, Button} from 'reactstrap';
import {SortableContainer, SortableElement, SortableHandle} from 'react-sortable-hoc';
import CollectionUtil from '../../../../../libs/CollectionUtil';
import Misc from '../../../../../libs/Misc';
import Inspector from './Inspector';
import { transformElementsForEditing } from './Panel';

export class ElementHelper
{
	static buildEmpty(md = 6, type = 'empty')
	{
		return {
			type: type,
			id: Misc.genId(),
			widths: {
				xs: 12,
				md: md
			},
			height: {value: 4, unit: 'rem'}
		};
	}

	static buildRowProps(md = 12)
	{
		return {
			elements: [this.buildEmpty(md)]
		};
	}

	static buildRow(md = 12)
	{
		return {
			type: 'row',
			id: Misc.genId(),
			...this.buildRowProps(md)
		};
	}

	static buildTextProps()
	{
		return {
			text: '',
			justify: 'center',
			align: 'center',
		};
	}

	static buildText(md = 6)
	{
		return {
			...this.buildEmpty(md, 'text'),
			...this.buildTextProps()
		};
	}

	static buildFieldProps()
	{
		return {
			name: '',
			label: null,
			desc: '',
			default_: null,
			placeholder: '',
		};
	}

	static buildField(md = 6)
	{
		return {
			...this.buildEmpty(md, 'field'),
			...this.buildFieldProps()
		};
	}

	static buildElementProps(type)
	{
		switch(type)
		{
		case 'row':
			return this.buildRowProps();
		case 'field':
			return this.buildFieldProps();
		case 'text':
			return this.buildTextProps();
		default:
			return {};
		}
	}

	static buildRowOrEmptyElement(parent, md = 12)
	{
		return !parent || parent.type !== 'row'? 
			this.buildRow(md):
			this.buildEmpty(md);
	}

	static addElement(node, index, proto, parent)
	{
		if(node === parent)
		{
			return Misc.addToArray(node.elements, index+1, [proto]);
		}

		for(let i = 0; i < node.elements.length; i++)
		{
			const elm = node.elements[i];
			if(elm === parent)
			{
				elm.elements = Misc.addToArray(elm.elements, index+1, [proto]);

				if(proto.type !== 'row')
				{
					elm.elements[index].widths.md = Math.ceil(elm.elements[index].widths.md / 2);
				}
				break;
			}
			else
			{
				if(elm.elements)
				{
					elm.elements = this.addElement(elm, index, proto, node);
				}
			}
		}

		return Array.from(node.elements);
	}

	static updateElements(main, nodes, values)
	{
		for(let i = 0; i < main.elements.length; i++)
		{
			const elm = main.elements[i];
			const index = nodes.indexOf(elm);
			if(index >= 0)
			{
				main.elements[i] = values[index];
			}
			else
			{
				if(elm.elements)
				{
					elm.elements = this.updateElements(elm, nodes, values);
				}
			}
		}

		return Array.from(main.elements);
	}	

	static delElement(node, index, parent)
	{
		if(node === parent)
		{
			const elements = Misc.deleteFromArray(node.elements, index, 1);

			return elements.length === 0?
				[ElementHelper.buildRowOrEmptyElement(parent, 12)]:
				elements;
		}

		for(let i = 0; i < node.elements.length; i++)
		{
			const elm = node.elements[i];
			if(elm === parent)
			{
				if(index > 0)
				{
					elm.elements[index-1].widths.md += elm.elements[index].widths.md;
				}
				else if(elm.elements.length > 1)
				{
					elm.elements[1].widths.md += elm.elements[0].widths.md;
				}
				
				elm.elements = Misc.deleteFromArray(elm.elements, index, 1);

				if(elm.elements.length === 0)
				{
					node.elements = Misc.deleteFromArray(node.elements, i, 1);
					if(node.elements.length === 0)
					{
						node.elements = [ElementHelper.buildRowOrEmptyElement(node, 12)];
					}
				}

				break;
			}
			else
			{
				if(elm.elements)
				{
					elm.elements = this.delElement(elm, index, node);
				}
			}
		}

		return Array.from(node.elements);
	}

	static moveElement(node, oldIndex, newIndex, parent)
	{
		if(node === parent)
		{
			return Misc.moveArray(node.elements, oldIndex, newIndex);
		}

		for(let i = 0; i < node.elements.length; i++)
		{
			const element = node.elements[i];
			if(element === parent)
			{
				element.elements = Misc.moveArray(element.elements, oldIndex, newIndex);
				break;
			}
			else
			{
				if(element.elements)
				{
					element.elements = this.moveElement(element, oldIndex, newIndex, node);
				}
			}
		}

		return Array.from(node.elements);
	}

	static findElementOnTreeById(elements, id)
	{
		const res = elements.find(e => e.id === id);
		if(res)
		{
			return res;
		}

		for(let i = 0; i < elements.length; i++)
		{
			const elm = elements[i];
			if(elm.elements)
			{
				const res = this.findElementOnTreeById(elm.elements, id);
				if(res)
				{
					return res;
				}
			}
		}

		return null;
	}

	static findElementById(id, steps)
	{
		if(id === null)
		{
			return null;
		}
		
		for(let i = 0; i < steps.length; i++)
		{
			const res = this.findElementOnTreeById(steps[i].elements, id);
			if(res)
			{
				return res;
			}
		}

		return null;
	}

	static findElementFieldsOnTree(elements)
	{
		let res = [];
		for(let i = 0; i < elements.length; i++)
		{
			const elm = elements[i];
			if(elm.type === 'field')
			{
				res.push(elm.name);
			}

			if(elm.elements)
			{
				res = res.concat(this.findElementFieldsOnTree(elm.elements));
			}
		}

		return res;
	}

	static findElementFields(steps)
	{
		let res = [];
		for(let i = 0; i < steps.length; i++)
		{
			res = res.concat(this.findElementFieldsOnTree(steps[i].elements));
		}

		return res;
	}

}

const SortHandle = SortableHandle(({children}) => children);

const SortContainer = SortableContainer(({children}) => children);

// eslint-disable-next-line react/prop-types
const ElementControls = ({index, parent, className, onDelete, children}) =>
{
	return (
		<div className={className || 'rf-formeditor-element-controls'}>
			{children}
			<div 
				className="rf-formeditor-controls-button"
				onClick={() => onDelete(index, parent)}>
				<i className="la la-trash" title="Delete" />
			</div>
			{parent.elements.length > 1 &&
				<SortHandle>
					<div className="rf-formeditor-controls-button grab">
						<i className="la la-arrows-alt" title="Move" />
					</div>					
				</SortHandle>
			}
		</div>
	);
};

const SortElement = SortableElement(({children}) =>
	<div className="rf-formeditor-element">
		{children}
	</div>
);

const Form = observer(props =>
{
	const store = props.store;
	const collection = store.collection;

	const [step, setStep] = useState(0);
	const [selected, setSelected] = useState(null);

	useEffect(() =>
	{
		setStep(0);
		setSelected(null);
	}, [collection]);
	
	const handleAddElement = useCallback((index, parent) =>
	{
		props.onChange({
			target: {
				name: `steps[${step}].elements`,
				value: ElementHelper.addElement(
					props.form.steps[step], 
					index, 
					ElementHelper.buildRowOrEmptyElement(parent), 
					parent)
			}
		});
	}, [props.form, step]);

	const handleSplitElement = useCallback((elm, index, parent) =>
	{
		const half =  Math.floor(elm.widths.md / 2);
		
		props.onChange({
			target: {
				name: `steps[${step}].elements`,
				value: ElementHelper.addElement(
					props.form.steps[step], 
					index, 
					ElementHelper.buildRowOrEmptyElement(parent, half), 
					parent)
			}
		});
	}, [props.form, step]);

	const handleDelElement = useCallback((index, parent) =>
	{
		props.onChange({
			target: {
				name: `steps[${step}].elements`,
				value: ElementHelper.delElement(
					props.form.steps[step], 
					index, 
					parent)
			}
		});
	}, [props.form, step]);

	const handleMoveElement = useCallback((oldIndex, newIndex, parent) =>
	{
		props.onChange({
			target: {
				name: `steps[${step}].elements`,
				value: ElementHelper.moveElement(
					props.form.steps[step],  
					oldIndex, 
					newIndex, 
					parent)
			}
		});
	}, [props.form, step]);

	const handleResizeElement = useCallback((dir, elm, index, parent) =>
	{
		const nodes = [elm, parent.elements[dir === 'left'? index-1: index+1]];
		const values = [{...elm}, {...nodes[1]}];
		
		values[0].widths.md += 1;
		values[1].widths.md -= 1;

		props.onChange({
			target: {
				name: `steps[${step}].elements`,
				value: ElementHelper.updateElements(
					props.form.steps[step], 
					nodes, 
					values)
			}
		});
	}, [props.form, step]);

	const handleChangeStep = useCallback((index, e) =>
	{
		e.stopPropagation();
		setSelected(null);
		setStep(index);
	}, []);

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
			selected, props.form.steps);
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
				name: `steps[${step}].elements`,
				value: ElementHelper.updateElements(
					props.form.steps[step], 
					[element], 
					[clone])
			}
		});

	}, [props.form, selected, step]);

	const handleShowPreview = useCallback( () =>
	{
		const url = CollectionUtil.genFormUrl(collection.id, props.form.id);
		window.open(url, '_blank');
	}, [props.form]);

	const showPopupMenu = (event, menu) =>
	{
		props.container.showPopupMenu(event, menu);
	};

	const handleHidePopupMenu = () =>
	{
		props.container.hidePopupMenu();
	};

	const handleShowStepMenu = (step_, index, event) =>
	{
		showPopupMenu(event, buildStepMenu(step_, index, event.target));
	};	

	const createStep = (index) =>
	{
		const steps = store.createFormStep(props.form.steps, index);
		
		steps[index+1].elements = transformElementsForEditing(steps[index+1].elements);

		props.onChange({
			target: {
				name: 'steps',
				value: steps
			}
		});

		setStep(index+1);
	};

	const duplicateStep = (index) =>
	{
		const steps = store.duplicateFormStep(props.form.steps, index);

		steps[index+1].elements = transformElementsForEditing(steps[index+1].elements);

		props.onChange({
			target: {
				name: 'steps',
				value: steps
			}
		});
		
		setStep(index+1);
	};

	const deleteStep = (index) =>
	{
		const steps = store.deleteFormStep(props.form.steps, index);

		props.onChange({
			target: {
				name: 'steps',
				value: steps
			}
		});

		setStep(step => step === index? 
			(index > 0? index - 1: 0):
			step);
	};
	
	const moveStep = (from, to) =>
	{
		const steps = store.moveFormStep(props.form.steps, from, to);

		props.onChange({
			target: {
				name: 'steps',
				value: steps
			}
		});

		setStep(to);
	};
	
	const renderField = (elm) =>
	{
		// eslint-disable-next-line react/prop-types
		const field = CollectionUtil.getColumn(collection.schemaObj, elm.name, store.references) || {};
		
		return (
			<div title={elm.desc}>
				<Label className="rf-formeditor-label rf-form-label">
					{elm.label || field.label || elm.name}
				</Label>
				<Input 
					className="rf-form-input" 
					type="text"
					placeholder={elm.placeholder}
					value={elm.default_ || ''}
					readOnly>
				</Input>
			</div>
		);
	};

	const renderElement = (elm, parent, index) =>
	{
		if(elm.type === 'row')
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
						helperClass="rf-formeditor-sortable">
						<div className="rf-formeditor-row">
							<Row className="rf-form-row">
								{renderElements(elm.elements, elm)}
							</Row>
						</div>
						<ElementControls
							className="rf-formeditor-row-controls"
							index={index}
							parent={parent}
							onDelete={handleDelElement}>
							<div 
								className="rf-formeditor-controls-button"
								title="Add row"
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
				<div 
					className="rf-formeditor-text rf-form-text"
					style={{
						height: elm.height.value + elm.height.unit,
						fontSize: elm.size + 'px', 
						fontWeight: elm.bold? 'bold': null, 
						color: elm.color,
						backgroundColor: elm.backgroundColor,
						textAlign: elm.justify, 
						justifyContent: elm.align}}>
					{elm.text.split('\n').map((text, index) => 
						<div key={index}>{text}</div>
					)}
				</div>
			;
			break;

		case 'field':
			compo = 
				renderField(elm, parent, index)
			;
			break;

		case 'horzline':
			compo = 
				<div 
					className="rf-formeditor-horzline rf-form-horzline" 
				/>
			;	
			break;

		case 'empty':
			compo = 
				<div className="rf-formeditor-empty"
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
						className={classNames('rf-formeditor-element-wrapper', {'selected': elm.id === selected})}
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
								className="rf-formeditor-controls-button"
								title="Expand left"
								onClick={() => handleResizeElement('left', elm, index, parent)}>
								<i className="la la-arrow-circle-left" />
							</div>
						}
						{elm.widths.md < 12 && relm && relm.widths.md > 1 &&
							<div 
								className="rf-formeditor-controls-button"
								title="Expand right"
								onClick={() => handleResizeElement('right', elm, index, parent)}>
								<i className="la la-arrow-circle-right" />
							</div>
						}
						{elm.widths.md > 1 && 
							<div 
								className="rf-formeditor-controls-button"
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
			elements.map((elm, index) => renderElement(elm, parent, index))
		);
	};

	const renderSteps = (steps) =>
	{
		return (
			<div className="rf-formeditor-step-container">
				<div className="rf-formeditor-step-tabs">
					{steps.map((s, index) => 
						<div key={index} className={`rf-formeditor-step-elements ${index === step? 'active': ''}`}>
							<SortContainer
								axis="y"
								lockAxis="y"
								onSortEnd={({oldIndex, newIndex}) => handleMoveElement(oldIndex, newIndex, s)} 
								useDragHandle 
								helperClass="rf-formeditor-sortable">
								<Container fluid className="rf-formeditor-elements-list">
									{renderElements(s.elements, s)}
								</Container>
							</SortContainer>
						</div>
					)}
				</div>
				<div className="rf-formeditor-step-controls">
					{steps.map((s, index) => 
						<div 
							key={index} 
							className={`rf-formeditor-step-button ${index === step? 'active': ''}`}
							title={`Step ${1+index}`}
							onClick={(e) => handleChangeStep(index, e)}>
							{1+index}
							<div className="rf-menu-handle">
								<i 
									className="la la-caret-down" 
									onClick={(e) => handleShowStepMenu(s, index, e)} />
							</div>
						</div>
					)}
				</div>
			</div>
		);
	};

	const buildStepMenu = (step, index) =>
	{
		const hideMenu = () => handleHidePopupMenu();

		const steps = props.form.steps;
		
		return [
			{
				label: 'Create',
				icon: 'la la-plus-circle',
				command: () => 
				{
					hideMenu();
					createStep(index);
				}
			},
			{
				label: 'Duplicate',
				icon: 'la la-copy',
				command: () => 
				{
					hideMenu();
					duplicateStep(index);
				}
			},
			{
				label: 'Delete',
				icon: 'la la-trash',
				disabled: steps.length === 0,
				command: () => 
				{
					hideMenu();
					deleteStep(index);
				}
			},
			{
				separator: true
			},
			{
				label: 'Move',
				icon: 'la la-arrows',
				items: [
					{
						label: 'Begin',
						icon: 'la la-angle-double-left',
						disabled: index === 0,
						command: () => 
						{
							hideMenu();
							moveStep(index, 0);
						}
					},
					{
						label: 'Left',
						icon: 'la la-angle-left',
						disabled: index === 0,
						command: () => 
						{
							hideMenu();
							moveStep(index, index-1);
						}
					},
					{
						label: 'Right',
						icon: 'la la-angle-right',
						disabled: index === steps.length - 1,
						command: () => 
						{
							hideMenu();
							moveStep(index, index+1);
						}
					},
					{
						label: 'Final',
						icon: 'la la-angle-double-right',
						disabled: index === steps.length - 1,
						command: () => 
						{
							hideMenu();
							moveStep(index, steps.length - 1);
						}
					}				
				]
			},
		];
	};

	if(!props.visible)
	{
		return null;
	}
	
	return (
		<div 
			className="rf-formeditor-form pt-2"
			onClick={handleDeselectElement}>
			{renderSteps(props.form.steps)}
			
			<Container fluid className="mt-2">
				<Row>
					<Col xs="12">
						{props.form.id &&
							<Button
								color="secondary"
								type="submit"
								onClick={handleShowPreview}
								className="mr-2">
							View
							</Button>
						}
						<Button
							color="primary"
							type="submit"
							onClick={props.onSubmit}>
							{props.form.id? 'Update': 'Create'}
						</Button>
					</Col>
				</Row>
			</Container>

			<Inspector 
				store={store}
				form={props.form}
				step={step}
				element={ElementHelper.findElementById(selected, props.form.steps)}
				onChange={handleElementChanged}
				onHide={handleDeselectElement}
			/>
		</div>
	);
});

Form.propTypes = 
{
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
	form: PropTypes.object.isRequired,
	onChange: PropTypes.func.isRequired,
	onSubmit: PropTypes.func.isRequired,
};

export default Form;