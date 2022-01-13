import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import FocusLock from 'react-focus-lock';
import {Nav, NavItem, NavLink, TabContent, TabPane} from 'reactstrap';
import SidebarResizeable from '../../../../../components/Bar/SidebarResizeable';
import PanelHorzSlider from '../../../../../components/Slider/PanelHorzSlider';
import Misc from '../../../../../libs/Misc';
import Form from './Form';

import '../Viewer/Form.css';
import './Editor.css';

const transformHeight = (height) =>
{
	if(!height)
	{
		return {value: 1, unit: 'rem'};
	}

	const m = height.match(/(\d+)(.*)/);
	return {value: m[1]|0, unit: m[2]};
};

const transformElementForEditing = (elm) =>
{
	return {
		...elm,
		id: Misc.genId(),
		height: elm.height?
			transformHeight(elm.height):
			undefined,
	};
};

export const transformElementsForEditing = (elements) =>
{
	return elements.map(elm => ({
		...transformElementForEditing(elm), 
		elements: elm.elements? 
			transformElementsForEditing(elm.elements): 
			undefined
	}));
};

const transformForEditing = (form) =>
{
	return {
		...form,
		steps: form.steps.map(step => ({
			elements: transformElementsForEditing(step.elements)
		}))
	};
};

const transformFormsForEditing = (forms) =>
{
	return forms.map((form) => 
		transformForEditing(form)
	);
};

const transformElementForSubmitting = (elm) =>
{
	return {
		...elm,
		height: elm.height && elm.height.value? 
			elm.height.value + elm.height.unit:
			undefined,
		id: undefined,
	};
};

const transformElementsForSubmitting = (elements) =>
{
	return elements.map(elm => ({
		...transformElementForSubmitting(elm), 
		elements: elm.elements? 
			transformElementsForSubmitting(elm.elements): 
			undefined, 
	}));
};

export const transformForSubmitting = (form) =>
{
	return {
		...form,
		steps: form.steps.map(step => ({
			elements: transformElementsForSubmitting(step.elements)
		}))
	};

};

const Forms = observer(props =>
{
	const store = props.store;
	const collection = store.collection;

	const [activeTab, setActiveTab] = useState(0);
	const [forms, setForms] = useState(null);

	const initializeForms = (schema) =>
	{
		const forms = schema.forms && schema.forms.length > 0?
			schema.forms:
			[store.generateForm()];

		setForms(transformFormsForEditing(forms));
	};

	useEffect(() =>
	{
		if(props.visible)
		{
			initializeForms(collection.schemaObj);
		}
	}, []);

	useEffect(() =>
	{
		if(props.visible)
		{
			initializeForms(collection.schemaObj);
		}
	}, [props.visible, collection]);	

	const showForm = (index) =>
	{
		setActiveTab(index);
	};

	const showPopupMenu = (event, menu) =>
	{
		props.container.showPopupMenu(event, menu);
	};

	const handleHidePopupMenu = () =>
	{
		props.container.hidePopupMenu();
	};

	const handleShowMenu = (event, form, index) =>
	{
		showPopupMenu(event, buildMenu(form, index, event.target));
	};

	const handleShowProps = (form, index) =>
	{
		props.container.showPanel('formProps', {
			form: form,
			index: index,
			title: 'General',
			icon: 'file',
			compo: 'general'
		});
	};

	const handleChange = useCallback((id, e) =>
	{
		const name = e.target.name || e.target.id;
		const value = e.target.value;
		
		setForms(forms =>
		{
			const to = Array.from(forms);
			const form = id? 
				to.find(f => f.id === id):
				to[0];
			Misc.setFieldValue(form, name, value);
			return to;
		});
	}, []);

	const handleSubmit = useCallback(async (id, index, e) =>
	{
		e.preventDefault();
		
		const form = transformForSubmitting(id? 
			forms.find(f => f.id === id):
			forms[0]);

		const res = await (id? 
			store.updateForm(index, form):
			store.createForm(form, index));

		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
		}
		else
		{
			props.container.showMessage(`Form ${id? 'updated': 'created'}!`, 'success');
		}
	}, [forms]);

	const createForm = async (index) =>
	{
		store.createForm(store.generateForm(), index);
	};

	const duplicateForm = async (index) =>
	{
		const res = await store.duplicateForm(index);
		if(!res.errors)
		{
			setActiveTab(forms.length);
		}

	};

	const deleteForm = async (data) =>
	{
		const res = await store.deleteForm(data.index);
		if(!res.errors)
		{
			setActiveTab(activeTab => activeTab === data.index? 
				(data.index > 0? data.index - 1: 0):
				activeTab);
		}
	};

	const handleDeleteForm = (form, index) =>
	{
		props.container.showUserConfirmation(deleteForm, {form, index}, 'Delete form?');
	};	

	const moveForm = async (from, to) =>
	{
		const res = await store.moveForm(from, to);
		if(!res.errors)
		{
			setActiveTab(to);
		}
	};

	const renderForm = (form, visible, index) =>
	{
		return (
			<Form 
				key={index}
				container={props.container}
				store={store}
				collection={collection}
				visible={visible}
				form={form}
				onChange={(e) => handleChange(form.id, e)}
				onSubmit={(e) => handleSubmit(form.id, index, e)}
			/>
		);
	};

	const buildMenu = (form, index) =>
	{
		const hideMenu = () => handleHidePopupMenu();
		
		return [
			{
				label: 'Properties',
				icon: 'la la-cog',
				command: () => 
				{
					hideMenu();
					handleShowProps(form, index);
				}
			},
			{
				separator: true
			},
			{
				label: 'Create',
				icon: 'la la-plus-circle',
				disabled: !form.id,
				command: async () => 
				{
					hideMenu();
					await createForm(index);
				}
			},
			{
				label: 'Duplicate',
				icon: 'la la-copy',
				disabled: !form.id,
				command: async () => 
				{
					hideMenu();
					await duplicateForm(index);
				}
			},
			{
				label: 'Delete',
				icon: 'la la-trash',
				disabled: !form.id || forms.length === 0,
				command: async () => 
				{
					hideMenu();
					await handleDeleteForm(form, index);
				}
			},
			{
				separator: true
			},
			{
				label: 'Move',
				icon: 'la la-arrows',
				disabled: !form.id,
				items: [
					{
						label: 'Begin',
						icon: 'la la-angle-double-left',
						disabled: index === 0,
						command: async () => 
						{
							hideMenu();
							await moveForm(index, 0);
						}
					},
					{
						label: 'Left',
						icon: 'la la-angle-left',
						disabled: index === 0,
						command: async () => 
						{
							hideMenu();
							await moveForm(index, index-1);
						}
					},
					{
						label: 'Right',
						icon: 'la la-angle-right',
						disabled: index === forms.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveForm(index, index+1);
						}
					},
					{
						label: 'Final',
						icon: 'la la-angle-double-right',
						disabled: index === forms.length - 1,
						command: async () => 
						{
							hideMenu();
							await moveForm(index, forms.length - 1);
						}
					}				
				]
			},
		];
	};

	if(!collection || !forms || !props.visible)
	{
		return null;
	}
	
	return (
		<SidebarResizeable 
			visible={props.visible} 
			position="right" 
			onHide={props.onHide} 
			fullScreen={props.fullScreen}>
			<div className="w-100 h-100">
				<FocusLock disabled={!props.visible} returnFocus className="rf-flex-100">
					<div className="rf-col-forms-tabs w-100">
						<h4><i className="la la-wpforms" /> Forms</h4>
						<PanelHorzSlider>
							<Nav tabs>
								{forms.map((form, index) =>
									<NavItem key={index}>
										<NavLink
											className={classnames({active: activeTab === index})}
											onClick={() => showForm(index)}>
											{form.name}
											<div className="rf-menu-handle">
												<i 
													className="la la-caret-down" 
													onClick={(e) => handleShowMenu(e, form, index)} />
											</div>

										</NavLink>
									</NavItem>
								)}
							</Nav>
						</PanelHorzSlider>
						<TabContent activeTab={activeTab}>
							{forms.map((form, index) =>
								<TabPane key={index} tabId={index} className="rf-col-forms-tab-pane">
									{renderForm(form, activeTab === index, index)}
								</TabPane>
							)}
						</TabContent>
					</div>
				</FocusLock>
			</div>
		</SidebarResizeable>
	);
});

Forms.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
	fullScreen: PropTypes.bool,
	onHide: PropTypes.func.isRequired,
};

export default Forms;