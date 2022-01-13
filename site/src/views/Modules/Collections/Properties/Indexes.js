import React, { useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, Button, CustomInput} from 'reactstrap';
import Misc from '../../../../libs/Misc';
import ExtensibleList from '../../../../components/List/ExtensibleList';
import CollectionUtil from '../../../../libs/CollectionUtil';
import InputMultiSelectObject from '../../../../components/Input/InputMultiSelectObject';

const columns = [
	{width: 6, label: 'Campos', name: 'columns'},
	{width: 2, label: 'Direção', name: 'dir'},
	{width: 2, label: 'Único', name: 'unique'},
];

const transformForEditing = (schema) =>
{
	return schema.indexes?
		Misc.cloneObject(schema.indexes):
		[];
};

const Indexes = observer(props =>
{
	const store = props.store;
	
	const [indexes, setIndexes] = useState(() => transformForEditing(store.collection.schemaObj));
	const [errors, setErrors] = useState([]);
	const [loading, setLoading]	= useState(false);

	useEffect(() =>
	{
		setIndexes(transformForEditing(store.collection.schemaObj));
	}, [store.collection]);

	const handleChange = (event) =>
	{
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;
		
		const ind = Array.from(indexes);
		
		Misc.setFieldValue(ind, event.target.name || event.target.id, value);
		
		setIndexes(ind);
	};

	const handleSubmit = async (event) =>
	{
		event.preventDefault();

		try 
		{
			setLoading(true);
			
			const schema = Misc.cloneObject(store.collection.schemaObj);
			const from = transformForEditing(schema);
			const to = indexes;

			const errors = [];
			let changeCnt = 0;
			if(!Misc.compareArrays(from, to))
			{
				for(let i = 0; i < to.length; i++)
				{
					const index = to[i];
					if(!index.columns || index.columns.length === 0)
					{
						errors.push(`Índices[${1+i}]: Ao menos um campo deve ser definido`);
					}
				}
				++changeCnt;
			}

			setErrors(errors);

			if(changeCnt > 0 && errors.length === 0)
			{
				const res = store.updateIndexes(to);
				setErrors(res.errors || []);
				if(!res.errors)
				{
					props.container.showMessage('Atualização de índices realizada!', 'success');
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

	const addItem = useCallback((items) =>
	{
		const res = {
			columns: [],
			dir: 'asc',
			unique: false
		};
		
		setIndexes(indexes =>
		{
			const to = Array.from(indexes);
		
			to.push(res);
			
			return to;
		});

		return res;
	}, []);
	
	const onDelItem = ({item, index, callback}) =>
	{
		const columns = item.columns;
		if(columns)
		{
			if(columns.length === 1)
			{
				const name = columns[0];
				const column = CollectionUtil.getColumn(store.collection.schemaObj, name, null);
				if(column && (column.unique || column.sortable || column.positional))
				{
					if(!indexes.find((ind, i) => 
						i !== index && 
						Misc.compareArrays(ind.columns, columns) && 
						(ind.dir !== item.dir || ind.unique !== item.unique)))
					{
						props.container.showMessage('Não é possível remover o índice porque ele é usado por um campo indexável, única ou posicional', 'error');
						return;
					}
				}
			}
		}
		
		props.container.showUserConfirmation(callback, {item, index}, 'Remover índice?');
	};

	const delItem = (item, index) =>
	{
		setIndexes(indexes => indexes.filter((c, i) => i !== index));
	};

	const renderHeader = ({col}) =>
	{
		return (col.label);
	};

	const buildColumnValues = (columns, item) =>
	{
		if(!item.columns || item.columns.length === 0)
		{
			return [];
		}

		return item.columns
			.map(name => {
				const column = columns.find(c => c.name === name);
				return {
					name: name, 
					label: (column && column.label) || name
				};
			});
	};

	const handleColumnChange = useCallback((event, index) => 
	{
		const name = event.target.name;
		const value = event.target.value.map(v => v.name);
		
		setIndexes(indexes =>
		{
			const to = Array.from(indexes);
		
			Misc.setFieldValue(to[index], name, value);
			
			return to;
		});
	}, []);

	const renderItem = ({item, index, col, isPreview}) => 
	{
		if(isPreview)
		{
			switch(col.name)
			{
			case 'columns':
				return item.columns.join(',');
			case 'dir':
				return item.dir;
			default:
				return item[col.name];
			}
		}

		const schema = store.collection.schemaObj;
		const columns = CollectionUtil.transformColumns(schema, schema.columns);
		
		switch(col.name)
		{
		case 'columns':
			return (
				<InputMultiSelectObject 
					required
					name="columns"
					options={columns || []} 
					values={buildColumnValues(columns, item)} 
					objectKey="name"
					objectLabel="label"
					onChange={e => handleColumnChange(e, index)} 
				/>
			);
		
		case 'dir':
			return (
				<Input
					required
					name={`[${index}].dir`}
					type="select"
					value={item.dir || 'asc'}
					onChange={handleChange}>
					<option key="1" value="asc">Asc</option>
					<option key="2" value="desc">Desc</option>
				</Input>
			);
		
		case 'unique':
			return (
				<CustomInput
					id={`[${index}].unique`}
					type="switch" 
					label="Sim"
					checked={item.unique? true: false}
					onChange={handleChange} />
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

	if(!indexes)
	{
		return null;
	}

	return (
		<form>
			<Row>
				<Col sm="12">
					<ExtensibleList 
						columns={columns}
						items={indexes}
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

Indexes.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
};

export default Indexes;