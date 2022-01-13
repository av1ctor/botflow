import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, Button} from 'reactstrap';
import Misc from '../../../../libs/Misc';
import ExtensibleList from '../../../../components/List/ExtensibleList';
import CollectionUtil from '../../../../libs/CollectionUtil';
import InputMultiSelectObject from '../../../../components/Input/InputMultiSelectObject';
import InputSelectObject from '../../../../components/Input/InputSelectObject';

const columns = [
	{width: 3, label: 'Coleção auxiliar', name: 'collection'},
	{width: 3, label: 'Filtros', name: 'filters'},
	{width: 4, label: 'Prévia', name: 'preview'},
];

const transformForEditing = (schema) =>
{
	const references = schema.refs;
	
	if(!references)
	{
		return [];
	}

	return Object.entries(references).map(([k, ref]) => (
		Object.assign({name: k}, Misc.cloneObject(ref))
	)).filter((ref, index, self) => 
		index === self.findIndex(t => t.collection === ref.collection));
};

const References = observer(props =>
{
	const store = props.store;
	
	const [references, setReferences] = useState(() => 
		transformForEditing(store.collection.schemaObj));
	const [collections, setCollections] = useState(null);
	const [errors, setErrors] = useState([]);
	const [loading, setLoading]	= useState(false);

	useEffect(() =>
	{
		setReferences(transformForEditing(store.collection.schemaObj));
	}, [store.collection]);

	useEffect(() =>
	{
		(async () =>
		{
			const collections = props.visible? 
				await loadAuxCollections(): 
				null;
			
			setCollections(collections);
		})();
	}, []);
	
	useEffect(() =>
	{
		(async () =>
		{
			if(props.visible && !collections)
			{
				const collections = await loadAuxCollections();
				setCollections(collections);
			}

			setReferences(transformForEditing(store.collection.schemaObj));
		})();
	}, [props.visible, store.collection]);

	const loadAuxCollections = async () =>
	{
		const res = await props.api.get(`collections/${store.collection.id}/auxs?page=0&size=32`);
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return null;
		}
			
		return res.data.map(col => ({
			id: col.id,
			name: col.name, 
			schema: JSON.parse(col.schema)
		}));
	};
	
	const handleChange = (event, index) =>
	{
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;
		
		const to = Array.from(to);
		
		to[index][event.target.name] = value;
		
		setReferences(to);
	};

	const handleSubmit = async (event) =>
	{
		event.preventDefault();

		try 
		{
			setLoading(true);
			
			const toSchema = Misc.cloneObject(store.collection.schemaObj);
			const from = transformForEditing(toSchema);
			const to = references;

			const errors = [];
			let changeCnt = 0;
			if(!Misc.compareArrays(from, to))
			{
				const refs = {};
				for(let i = 0; i < to.length; i++)
				{
					const ref = to[i];
					const col = getCollection(ref.collection);
					if(!ref.collection || !col.id)
					{
						errors.push(`Referências[${1+i}]: Coleção deve ser definida`);
					}
					else if(ref.filters.length === 0)
					{
						errors.push(`Referências[${1+i}]: Filtros deve ser definido`);
					}
					else if(ref.preview.columns.length === 0)
					{
						errors.push(`Referências[${1+i}]: Prévia deve ser definida`);
					}
					else
					{
						refs[ref.name || ref.collection] = {
							collection: col.id,
							filters: ref.filters.map(f => ({column: f.column})),
							preview: ref.preview
						};
					}
				}
				toSchema.refs = refs;
				
				setErrors(errors);
				++changeCnt;
			}

			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await store.updateSchema(toSchema);
				if(res.errors)
				{
					setErrors(res.errors);
					return false;
				}
				else
				{
					props.container.showMessage('Atualização de referências realizada!', 'success');
				}
			}
		} 
		finally 
		{
			setLoading(false);
		}

		return false;
	};

	const onAddItem = (items) =>
	{
		return items.length < 10;
	};

	const addItem = (items) =>
	{
		const res = {
			name: '',
			collection: '',
			filters: [],
			preview: {
				columns: []
			}
		};
		
		setReferences(references =>
		{
			const to = Array.from(references);
		
			to.push(res);
			
			return to;
		});

		return res;
	};
	
	const onDelItem = ({item, index, callback}) =>
	{
		props.container.showUserConfirmation(callback, {item, index}, 'Remover referência?');
	};

	const delItem = (item, index) =>
	{
		setReferences(references => references.filter((c, i) => i !== index));
	};

	const renderHeader = ({col}) =>
	{
		return (col.label);
	};

	const handleCollectionChange = useCallback((event, index) => 
	{
		const key = event.target.value.id? CollectionUtil.ID_NAME: '';
		const name = event.target.name;
		const value = event.target.value.id || event.target.value;

		setReferences(references =>
		{
			const to = Array.from(references);
			
			Misc.setFieldValue(to[index], name, value);

			to[index].key = key;
			to[index].filters = [];
			to[index].preview.columns = [];
			
			return to;
		});
	}, []);

	const getCollection = (id) =>
	{
		const col = (collections || []).find(col => col.id === id);
		return col? col: {id: '', name: id, schema: {columns: {}}};
	}

	const getCollectionColumns = (collection) =>
	{
		return collection? 
			getCollection(collection).schema.columns:
			{};
	};

	const getColumnLabel = (columns, name) =>
	{
		return (columns[name] && columns[name].label) || name;
	};

	const buildFilterValues = (columns, item) =>
	{
		if(!item.collection || !item.filters || item.filters.length === 0)
		{
			return [];
		}

		return item.filters
			.map(f => ({
				column: f.column, 
				label: getColumnLabel(columns, f.column)
			}));
	};

	const handleFilterChange = useCallback((event, index) => 
	{
		const name = event.target.name;
		const value = event.target.value.map(v => ({column: v.column}));
		
		setReferences(references =>
		{
			const to = Array.from(references);
		
			Misc.setFieldValue(to[index], name, value);
			
			return to;
		});
	}, []);

	const buildPreviewValues = (columns, item) =>
	{
		if(!item.collection || !item.preview || item.preview.columns.length === 0)
		{
			return [];
		}

		return item.preview.columns
			.map(col => ({
				column: col, 
				label: getColumnLabel(columns, col)
			}));
	};

	const handlePreviewChange = useCallback((event, index) => 
	{
		const name = event.target.name;
		const value = event.target.value.map(v => v.column);

		setReferences(references =>
		{
			const to = Array.from(references);
		
			Misc.setFieldValue(to[index], name, value);
			
			return to;
		});
	}, []);

	const renderItem = ({item, index, col, isPreview}) => 
	{
		const collection = getCollection(item.collection);
		const columns = getCollectionColumns(item.collection);

		const suggestions = Object.entries(columns)
			.map(([key, col]) => ({
				column: key, 
				label: col.label || key
			}));
		
		if(isPreview)
		{
			switch(col.name)
			{
			case 'collection':
				return collection.name;
			case 'filters':
				return item.filters.map(f => getColumnLabel(columns, f.column)).join(', ');
			default:
				return item[col.name];
			}
		}
		
		switch(col.name)
		{
		case 'collection':
			return (
				<InputSelectObject
					required
					name="collection"
					options={collections || []} 
					value={{name: collection.name, id: item.collection}} 
					objectKey="id"
					objectLabel="name"
					onChange={e => handleCollectionChange(e, index)} 
				/>);
		
		case 'key':
			return (
				<Input 
					required
					disabled
					type="text" 
					name={'key'}
					value={item.key} 
					readOnly>
				</Input>);

		case 'filters':
			return (
				<InputMultiSelectObject
					required
					name="filters"
					options={suggestions} 
					values={buildFilterValues(columns, item)} 
					objectKey="column"
					objectLabel="label"
					onChange={e => handleFilterChange(e, index)} 
				/>
			);
		
		case 'preview':
			return (
				<InputMultiSelectObject
					required
					name="preview.columns"
					options={suggestions} 
					values={buildPreviewValues(columns, item)} 
					objectKey="column"
					objectLabel="label"
					onChange={e => handlePreviewChange(e, index)} 
					disabled={!collection.id} />);
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

	if(!references)
	{
		return null;
	}

	return (
		<form>
			<Row>
				<Col sm="12">
					<ExtensibleList 
						columns={columns}
						totalPreviewCols={1}
						items={references}
						collections={collections} 
						renderHeader={renderHeader}
						renderItem={renderItem}
						onAdditem={onAddItem}
						addItem={addItem}
						onDeleteItem={onDelItem}
						deleteItem={delItem} />
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
						className="mt-4"
					>Atualizar</Button>
				</Col>
			</Row>
		</form>
	);
});

References.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
};

export default References;