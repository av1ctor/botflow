import React, { useCallback } from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Button} from 'reactstrap';
import CollectionUtil from '../../../../libs/CollectionUtil';
import Misc from '../../../../libs/Misc';
import ExtensibleListCollapsed from '../../../../components/List/ExtensibleListCollapsed';
import FieldsForm, {transformForEditing as FieldsForm_transformForEditing} from './FieldsForm';

export const transformForEditing = (schema, columns, references) =>
{
	return Object.entries(columns)
		.map(([k, col]) => FieldsForm_transformForEditing(schema, k, col, references));
};

const FieldsList = observer((props) =>
{
	const store = props.store;

	const handleChange = useCallback((event, index, extra = null) =>
	{
		const {target} = event;
		props.onChange({
			target: {
				type: target.type,
				name: target.name? 
					`[${index}].${target.name}`:
					null,
				id: target.id,
				value: target.value,
				checked: target.checked,
			}
		}, extra);
	}, []);

	const addItem = () =>
	{
		const item = {
			label: '',
			type: '',
			subtype: null,
			component: null,
			options: null,
			nullable: false,
			sortable: false,
			unique: false,
			mask: null,
			default: null,
			ref: null,
			depends: null,
			disabled: null,
			methods: null
		};

		const prep = FieldsForm_transformForEditing(store.collection, '', item, store.references);

		props.onCreate(prep);

		return prep;
	};
	
	const delItem = (item, index) =>
	{
		props.onDelete(item, index);
	};

	const onDelItem = ({item, index, callback}) =>
	{
		if(!item.name)
		{
			callback({item, index});
		}
		else
		{
			props.container.showUserConfirmation(callback, {item, index}, 'Remover campo?');
		}
	};

	const dupItem = (event, item) =>
	{
		event.stopPropagation();

		const dup = Misc.cloneObject(item);

		const {columns} = props;
		dup.label = Misc.genUniqueName(dup.label, '_', (label) => !columns.find(column => column.label === label));
		dup.name = CollectionUtil.labelToKey(dup.label);
		
		props.onCreate(dup);
	};

	const renderPreview = ({item, index}) => 
	{
		return (
			<div key={`prev_${index}`}>
				{item.label}
			</div>
		);
	};

	const renderHeader = () =>
	{
		return 'Nome';
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

	const {columns} = props;

	if(!columns)
	{
		return null;
	}

	return (
		<ExtensibleListCollapsed 
			items={columns} 
			renderHeader={renderHeader}
			renderPreview={renderPreview}
			renderActions={renderActions}
			onAddItem={() => true}
			addItem={addItem}
			onDeleteItem={onDelItem}
			deleteItem={delItem}
			extraAddButtonText="campo">
			{({item, index}) => 
				<FieldsForm
					key={index} 
					store={store}
					column={item}
					idPrefix={`${props.idPrefix || ''}[${index}].`}
					onChange={(e, extra) => handleChange(e, index, extra)}
				/>
			}
		</ExtensibleListCollapsed>
	);
});

FieldsList.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
};

export default FieldsList;