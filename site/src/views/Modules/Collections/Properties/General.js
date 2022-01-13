import React, {useCallback, useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import {Col, Row, Label, Input, InputGroup, InputGroupAddon, Button, CustomInput} from 'reactstrap';
import AutoSizer from 'react-virtualized-auto-sizer';
import CollectionUtil from '../../../../libs/CollectionUtil';
import IconPicker from '../../../../components/Picker/IconPicker';

const General = observer(props =>
{
	const store = props.store;
	
	const [collection, setCollection] = useState(() => Object.assign({}, store.collection));
	const [providers, setProviders] = useState([]);
	const [errors, setErrors] = useState([]);
	const [loading, setLoading]	= useState(false);

	const loadStorages = async () =>
	{
		const res = await props.api.get('config/storages');
		if(res.errors)
		{
			return [];
		}

		return res.data;
	};

	useEffect(() =>
	{
		(async () =>
		{
			const storages = await loadStorages();
			setProviders(
				[{id: '', name: '', user: '', type: ''}]
					.concat(storages));
		})();
	}, []);

	useEffect(() =>
	{
		setCollection(Object.assign({}, store.collection));
	}, [store.collection]);

	const handleChange = useCallback((event) =>
	{
		const name = event.target.name;
		const value = event.target.type !== 'checkbox'? 
			event.target.value: 
			event.target.checked;

		setCollection(collection =>
		{
			const to = Object.assign({}, collection);

			to[name] = value;

			return to;
		});
	}, []);

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();
		event.stopPropagation();

		try 
		{
			setLoading(true);
			
			const from = store.collection;
			const to = collection;

			let changeCnt = 0;
			let errors = [];
			
			if(from.name !== to.name)
			{
				++changeCnt;
				if(!to.name)
				{
					errors.push('Campo Nome não pode estar vazio');
				}
			}
			
			if(from.desc !== to.desc)
			{
				++changeCnt;
				if(!to.desc)
				{
					errors.push('Campo Descrição não pode estar vazio');
				}
			}
	
			if(from.provider !== to.provider)
			{
				++changeCnt;
				if(!to.provider.id)
				{
					errors.push('Campo Provedor não pode estar vazio');
				}
			}

			if(from.publishedAt !== to.publishedAt)
			{
				++changeCnt;
			}

			if(from.icon !== to.icon)
			{
				++changeCnt;
				if(!to.icon)
				{
					errors.push('Ícone de ser definido');
				}
			}

			setErrors(errors);
		
			if(changeCnt > 0 && errors.length === 0)
			{
				const res = await store.update(to);
				if(res.errors)
				{
					props.container.showMessage(res.errors, 'error');
					return;
				}
			}
		} 
		finally 
		{
			setLoading(false);
		}

		return false;
	}, [collection]);

	const renderProviderOptions = (providers) =>
	{
		return (
			providers
				.map((provider, index) => 
					<option 
						value={provider.id} 
						key={index}>
						{provider.user? `${provider.user}@${provider.name}`: provider.name}
					</option>)
		);
	};

	const handleProviderChange = useCallback((e) =>
	{
		handleChange({
			target: {
				type: 'object',
				name: 'provider',
				value: providers.find(h => h.id === e.target.value)
			}
		});
	}, []);

	const getIcon = (provider) =>
	{
		return '';
	};

	const renderErrors = () =>
	{
		return errors && errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	};
	
	if(!collection || !collection.id || !props.visible)
	{
		return null;
	}

	const hasSchema = collection.type === 'SCHEMA';

	return (
		<form>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Id</Label>
					<Input  
						required 
						disabled
						readOnly
						type="text" 
						value={collection.id} />
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Nome</Label>
					<Input 
						required 
						name="name" 
						type="text" 
						value={collection.name || ''} 
						onChange={handleChange} />
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Descrição</Label>
					<Input 
						required 
						name="desc" 
						type="text" 
						value={collection.desc || ''} 
						onChange={handleChange} />
				</Col>
			</Row>
			<Row>
				<Col sm="12" className="pt-2">
					<CustomInput 
						type="checkbox" 
						label="Publicada" 
						id="publishedAt" 
						checked={!!collection.publishedAt} 
						onChange={e => handleChange({target: {...e.target, name: 'publishedAt', type: 'checkbox'}})} />
				</Col>
			</Row>
			{hasSchema && 
				<Row style={{height:130}}>
					<Col sm="12" className="pt-2 w-100">
						<Label className="base-form-label">Ícone</Label>
						<AutoSizer>
							{({width}) => 
								<IconPicker
									width={width}
									dir="horz"
									selected={collection.icon}
									onChange={(icon) => handleChange({target: {name: 'icon', value: icon}})} />
							}
						</AutoSizer>
					</Col>
				</Row>
			}
			{hasSchema && 
				<Row>
					<Col sm="12" className="pt-2">
						<Label className="base-form-label">Provedor de armazenamento</Label>
						<InputGroup>
							<InputGroupAddon addonType="prepend">
								<div className="image-valigned-container mr-1" style={{width: 24}}>
									<img className="image-valigned" src={`icons/${getIcon(collection.provider)}.svg`} width="24" height="24" />
								</div>
							</InputGroupAddon>
							<Input 
								required 
								name="provider.id" 
								var-type="string" 
								type="select" 
								value={collection.provider? collection.provider.id: ''} 
								onChange={handleProviderChange}>
								{renderProviderOptions(providers)}
							</Input>
						</InputGroup>
					</Col>
				</Row>
			}
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

General.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
};

export default General;

