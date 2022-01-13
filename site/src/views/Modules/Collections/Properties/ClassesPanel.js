import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, Button} from 'reactstrap';
import Misc from '../../../../libs/Misc';
import ExtensibleList from '../../../../components/List/ExtensibleList';
import CollectionUtil from '../../../../libs/CollectionUtil';
import AutoSizer from 'react-virtualized-auto-sizer';
import FieldsList, {transformForEditing as FieldsList_transformForEditing} from './FieldsList';
import {transformForSubmitting as FieldsForm_transformForSubmitting} from './FieldsForm';
import LineAwesomePicker from '../../../../components/Picker/LineAwesomePicker';

const columns = [
	{width: 0, label: 'Id', name: 'id'},
	{width: 7, label: 'Nome', name: 'label'},
	{width: 0, label: 'Ícone', name: 'icon'},
	{width: 0, label: 'Campos', name: 'props'},
	{width: 0, label: 'Métodos', name: 'methods'},
];

const methodsColumns  = [
	{width: 7, label: 'Nome', name: 'name'},
	{width: 0, label: 'Script', name: 'script'},
];

const transformForEditing = (schema) =>
{
	const classes = Misc.cloneObject(schema.classes);
	
	const transformMethods = (methods) => methods? 
		Object.entries(methods).map(([k, method]) => ({name: k, script: method.script})): 
		[];

	if(!classes)
	{
		return [];
	}

	return Object.entries(classes).map(([k, klass]) => ({
		name: k,
		...klass,
		props: FieldsList_transformForEditing(schema, klass.props, null), 
		methods: transformMethods(klass.methods) 
	}));
};

const ClassesPanel = observer(props =>
{
	const store = props.store;

	const [classes, setClasses] = useState(() => transformForEditing(store.collection.schemaObj, store.references));
	const [errors, setErrors] = useState([]);
	const [loading, setLoading]	= useState(false);

	useEffect(() =>
	{
		setClasses(transformForEditing(store.collection.schemaObj, store.references));
	}, [store.collection]);
	
	const handleChange = useCallback((event, relatedInputToCleanup = null) =>
	{
		const key = event.target.name || event.target.id;
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;
		
		setClasses(classes =>
		{
			const to = Array.from(classes);
		
			Misc.setFieldValue(to, key, value);

			if(relatedInputToCleanup)
			{
				Misc.setFieldValue(to, relatedInputToCleanup, null);
			}
			
			return to;
		});
	}, []);

	const handleChangeColumn = useCallback((event, index, extra = null) =>
	{
		const {target} = event;

		handleChange({
			target: {
				type: target.type,
				name: target.name? 
					`[${index}].props${target.name}`:
					null,
				id: target.id,
				value: target.value,
				checked: target.checked,
			}
		},
		extra);
	}, []);

	const handleCreateColumn = useCallback((column, index) =>
	{
		setClasses(classes =>
		{
			const to = Array.from(classes);

			to[index].props.push(column);
		
			return to;
		});
	}, []);

	const handleDeleteColumn = useCallback((column, colIndex, index) =>
	{
		setClasses(classes =>
		{
			const to = Array.from(classes);

			to[index].props = to[index].props.filter((c, i) => i !== colIndex);

			return to;
		});
	}, []);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setLoading(true);
			
			const from = transformForEditing(store.collection.schemaObj);
			const to = classes;
			
			const klasses = {};
			const errors = [];
			let changeCnt = 0;

			if(!Misc.compareArrays(from, to))
			{
				++changeCnt;

				for(let i = 0; i < to.length; i++)
				{
					const klass = to[i];

					if(!klass.label)
					{
						errors.push(`Classes[${1+i}]: Nome deve ser definido`);
					}
					else if(!klass.props || klass.props.length === 0)
					{
						errors.push(`Classes[${1+i}]: Ao menos uma propriedade deve ser definida`);
					}
					else
					{
						const props = {};
						for(let j = 0; j < klass.props.length; j++)
						{
							const {name, column} = FieldsForm_transformForSubmitting(klass.props[j], errors);
							if(errors.length > 0)
							{
								errors.forEach((err, index) => 
									errors[index] = `Classes[${1+i}].Propriedades[${1+j}].${err}`);
							}
							else
							{
								props[name] = column;
							}			
						}

						const methods = klass.methods && klass.methods.length > 0? {}: null;
						if(klass.methods)
						{
							for(let j = 0; j < klass.methods.length; j++)
							{
								const meth = klass.methods[j];
								if(!meth.name)
								{
									errors.push(`Classes[${1+i}].Métodos[${1+j}]: Nome deve ser definido`);
								}
								else if(!meth.script)
								{
									errors.push(`Classes[${1+i}].Métodos[${1+j}]: Script deve ser definido`);
								}
								else
								{
									methods[meth.name] = {
										script: meth.script
									};
								}
							}
						}

						if(!klass.name)
						{
							klass.name = CollectionUtil.labelToKey(klass.label);
						}

						klasses[klass.name] = {
							...klass,
							name: undefined,
							props: props,
							methods: methods,
						};
					}
				}
			}

			setErrors(errors);
			
			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await store.updateClasses(klasses);
				if(!res.errors)
				{
					props.container.showMessage('Atualização de classes realizada!', 'success');
				}
				else
				{
					setErrors(res.errors);
				}
			}
		} 
		finally 
		{
			setLoading(false);
		}

		return false;
	}, [classes]);

	const addMethod = useCallback((items, index) =>
	{
		const res = {
			script: ''
		};
		
		setClasses(classes =>
		{
			const to = Array.from(classes);
		
			to[index].methods.push(res);
			
			return to;
		});

		return res;
	}, []);
	
	const delMethod = useCallback((item, index, methIndex) =>
	{
		setClasses(classes =>
		{
			const to = Array.from(classes);

			to[index].methods = to[index].methods.filter((c, i) => i !== methIndex);
		
			return to;
		});
	}, []);

	const renderMethod = ({item, index, propIndex = null, methIndex, parent, col, isPreview}) => 
	{
		const renderMethodNames = () =>
			CollectionUtil.getClassMethodNames()
				.filter(opt => opt.value === item.name || !parent.methods.find(meth => meth.name === opt.value))
				.map((opt, index) => <option key={index} value={opt.value}>{opt.label}</option>);

		if(isPreview)
		{
			switch(col.name)
			{
			default:
				return item[col.name];
			}
		}

		switch(col.name)
		{
		case 'name':
			return (
				<Input 
					required
					type="select" 
					name={`[${index}].${propIndex !== null? 'props[' + propIndex + '].': ''}methods[${methIndex}].name`}
					value={item.name} 
					onChange={handleChange}>
					{renderMethodNames()}
				</Input>);
		
		case 'script':
			return (
				<Input 
					required
					type="textarea" 
					rows="4"
					name={`[${index}].${propIndex !== null? 'props[' + propIndex + '].': ''}methods[${methIndex}].script`}
					value={item.script} 
					onChange={handleChange}>
				</Input>);
		}
	};
	
	const addItem = useCallback((items) =>
	{
		const res = {
			name: '',
			label: '',
			icon: '',
			props: [],
			methods: []
		};
		
		setClasses(classes =>
		{
			const to = Array.from(classes);
		
			to.push(res);
		
			return to;
		});

		return res;
	}, []);

	const dupItem = useCallback((event, item) =>
	{
		event.stopPropagation();

		const dup = Misc.cloneObject(item);

		setClasses(classes =>
		{
			const to = Array.from(classes);

			dup.label = Misc.genUniqueName(dup.label, '_', (label) => !to.find(klass => klass.label === label));
			dup.name = CollectionUtil.labelToKey(dup.label);
		
			to.push(dup);
		
			return to;
		});
	}, []);

	const onDelItem = ({item, index, callback}) =>
	{
		if(!item.name)
		{
			callback({item, index});
		}
		else
		{
			props.container.showUserConfirmation(callback, {item, index}, 'Remover classe?');
		}
	};
	
	const delItem = useCallback((item, index) =>
	{
		setClasses(classes => classes.filter((c, i) => i !== index));
	}, []);

	const renderHeader = ({col}) =>
	{
		return (col.label);
	};

	const renderActions = ({item}) =>
	{
		return (
			<>
				<Button 
					size="xs" 
					outline 
					className="rf-listbox-button" 
					onClick={(e) => dupItem(e, item)} 
					title="Duplicar">
					<i className="rf-listbox-button-icon la la-copy"></i>
				</Button>
			</>
		);
	};

	const renderItem = ({item, index, col, isPreview}) => 
	{
		if(isPreview)
		{
			switch(col.name)
			{
			case 'label':
				return item.label;
			case 'props':
				return item.props.length;
			case 'methods':
				return item.methods.length;
			default:
				return item[col.name];
			}
		}
		
		switch(col.name)
		{
		case 'id':
			return (
				<Input 
					required
					disabled
					readOnly
					type="text" 
					value={item.name}>
				</Input>);			

		case 'label':
			return (
				<Input 
					required
					type="text" 
					name={`[${index}].label`}
					value={item.label} 
					onChange={handleChange}>
				</Input>);
		
		case 'icon':
			return (
				<div className="w-100">
					<LineAwesomePicker
						api={props.api}
						width={800}
						dir="horz"
						selected={item.icon || ''}
						onChange={(icon) => handleChange({target: {name: `[${index}].icon`, value: icon}})} />
				</div>
			);
	
		case 'props':
			return (
				<FieldsList 
					{...props}
					columns={item.props}
					idPrefix={`[${index}].props`}
					onChange={(e, extra) => handleChangeColumn(e, index, extra)}
					onCreate={(col) => handleCreateColumn(col, index)}
					onDelete={(col, colIndex) => handleDeleteColumn(col, colIndex, index)} />
			);
		
		case 'methods':
			return (
				<ExtensibleList 
					columns={methodsColumns}
					items={item.methods} 
					alwaysMobile
					totalPreviewCols={1}
					renderHeader={({col}) => col.label}
					renderItem={(args) => renderMethod({...args, index: index, methIndex: args.index, parent: item})}
					onAddItem={() => true}
					addItem={(items) => addMethod(items, index)}
					deleteItem={(item, methIndex) => delMethod(item, index, methIndex)}
					extraAddButtonText="método" />
			);
		}
	};
	
	const renderErrors = () =>
	{
		return errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	};

	if(!classes)
	{
		return null;
	}

	return (
		<form>
			<Row>
				<Col sm="12">
					<ExtensibleList 
						columns={columns}
						items={classes} 
						alwaysMobile
						totalPreviewCols={1}
						renderHeader={renderHeader}
						renderItem={renderItem}
						renderActions={renderActions}
						onAddItem={() => true}
						addItem={addItem}
						onDeleteItem={onDelItem}
						deleteItem={delItem}
						extraAddButtonText="classe" />
				</Col>
			</Row>
			<Row>
				<Col md="12">
					{renderErrors()}
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Button
						color="primary"
						disabled={loading}
						type="submit"
						onClick={handleSubmit}
						className="mt-4">
							Atualizar
					</Button>
				</Col>
			</Row>
		</form>
	);
});

ClassesPanel.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
};

export default ClassesPanel;