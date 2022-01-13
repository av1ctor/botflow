import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, Button, InputGroup, InputGroupAddon} from 'reactstrap';
import Misc from '../../../../libs/Misc';
import ExtensibleList from '../../../../components/List/ExtensibleList';
import CollectionUtil from '../../../../libs/CollectionUtil';

const columns = [
	{width: 0, label: 'Id', name: 'id'},
	{width: 7, label: 'Nome', name: 'label'},
	{width: 0, label: 'Tipo', name: 'type'}
];

const transformForEditing = (collection) =>
{
	if(!collection)
	{
		return null;
	}

	const schema = collection.schemaObj;
	
	return schema.constants?
		Object.entries(schema.constants).map(([key, c]) => ({
			name: key,
			label: c.label,
			type: c.value.script? 'script': c.value.function? 'function': 'value',
			value: c.value.script || c.value.function || c.value.value
		})):
		[];
};

const Constants = observer(props =>
{
	const store = props.store;
	
	const [consts, setConsts] = useState(() => transformForEditing(store.collection));
	const [errors, setErrors] = useState([]);
	const [loading, setLoading]	= useState(false);

	useEffect(() =>
	{
		if(props.visible)
		{
			setConsts(transformForEditing(store.collection));
		}
	}, [store.collection]);
	
	const handleChange = useCallback((event) =>
	{
		const name = event.target.name || event.target.id;
		const value = event.target.type !== 'checkbox' ?
			event.target.value :
			event.target.checked;
		
		setConsts(consts =>
		{
			const to = Array.from(consts);
		
			Misc.setFieldValue(to, name, value);
		
			return to;
		});
	}, 90);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setLoading(true);
			
			const from = transformForEditing(store.collection);
			const to = consts;

			let changeCnt = 0;
			const errors = [];
			const constants = {};
			if(!Misc.compareArrays(from, to))
			{
				for(let i = 0; i < to.length; i++)
				{
					const constant = to[i];
					if(!constant.label)
					{
						errors.push(`Constantes[${1+i}]: Nome deve ser definido`);
					}
					else
					{
						if(!constant.name)
						{
							constant.name = CollectionUtil.labelToKey(constant.label);
						}

						constants[constant.name] = {
							label: constant.label,
							value: {[constant.type]: constant.value},
							name: undefined
						};
					}
				}
				
				++changeCnt;
			}

			setErrors(errors);

			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await store.updateConstants(constants);
				setErrors(res.errors || []);
				if(!res.errors)
				{
					props.container.showMessage('Atualização de constantes realizada!', 'success');
				}
			}
		} 
		finally 
		{
			setLoading(false);
		}

		return false;
	}, [consts]);

	const onAddItem = (items) =>
	{
		return items.length < 10;
	};

	const addItem = useCallback((items) =>
	{
		const res = {
			label: '',
			type: '',
			value: null
		};
		
		setConsts(consts =>
		{		
			const to = Array.from(consts);
		
			to.push(res);
		
			return to;
		});

		return res;
	}, []);
	
	const onDelItem = ({item, index, callback}) =>
	{
		props.container.showUserConfirmation(callback, {item, index}, 'Remover constante?');
	};

	const delItem = (item, index) =>
	{
		setConsts(consts => consts.filter((c, i) => i !== index));
	};

	const renderHeader = ({col}) =>
	{
		return (col.label);
	};

	const renderItem = ({item, index, col, isPreview}) => 
	{
		const renderTypes = () =>
			Object.entries(CollectionUtil.getColumnDefaultTypes())
				.map(([key, opt], index) => <option key={index} value={key}>{opt.name}</option>);
		
		const renderFunctionNames = () =>
			CollectionUtil.getColumnDefaultFunctionNames()
				.map((opt, index) => <option key={index} value={opt.value}>{opt.label}</option>);
		
		if(isPreview)
		{
			return item[col.name];
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
		
		case 'type':
		{
			let compo = null;

			switch(item.type || 'value')
			{
			case 'value':
				compo = () =>
					<Input 
						type="text"
						name={`[${index}].value`}
						value={item.value || ''} 
						onChange={handleChange}>
					</Input>;
				break;

			case 'function':
				compo = () =>
					<Input 
						type="select"
						name={`[${index}].value`}
						value={item.value || ''} 
						onChange={handleChange}>
						{renderFunctionNames()}
					</Input>;
				break;
			
			case 'script':
				compo = () =>
					<Input 
						type="text"
						name={`[${index}].value`}
						value={item.value || ''} 
						onChange={handleChange}>
					</Input>;
				break;
			}

			return (
				<InputGroup>
					<InputGroupAddon addonType="prepend">
						<Input 
							type="select"
							name={`[${index}].type`}
							value={item.type} 
							onChange={handleChange}>
							{renderTypes()}
						</Input>
					</InputGroupAddon>
					{compo()}
				</InputGroup>
			);
		}
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

	if(!consts)
	{
		return null;
	}

	return (
		<form>
			<Row>
				<Col sm="12">
					<ExtensibleList 
						columns={columns}
						items={consts}
						alwaysMobile
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

Constants.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
};

export default Constants;