import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button, Card, CustomInput} from 'reactstrap';
import {CirclePicker} from 'react-color';
import Misc from '../../../../../libs/Misc.js';
import CollectionUtil from '../../../../../libs/CollectionUtil.js';

const rgbToName = {
	'#666666': 'black', 
	'#f7f7f7': 'white', 
	'#f0f0f0': 'grey', 
	'#e9f0ef': 'cyan', 
	'#f0e9ef': 'red', 
	'#eff0e9': 'green'
};

const nameToRGB = Object.entries(rgbToName)
	.reduce((obj, [rgb, name]) => ({...obj, [name]: rgb}), {});

const General = observer(props =>
{
	const store = props.store;
	
	const [isLoading, setIsLoading] = useState(false);
	const [view, setView] = useState(() => Misc.cloneObject(store.getViewOnSchemaById(props.viewId)));

	useEffect(() =>
	{
		setView(Misc.cloneObject(store.getViewOnSchemaById(props.viewId)));
	}, [store.views.get(props.viewId)]);

	const handleChange = useCallback((event) =>
	{
		const key = event.target.name || event.target.id;
		const value = event.target.type !== 'checkbox'? 
			event.target.value: 
			event.target.checked;
		
		setView(view =>
		{
			const to = Object.assign({}, view);

			Misc.setFieldValue(to, key, value);

			return to;
		});
	}, []);

	const toRGB = (name) =>
	{
		return nameToRGB[name];
	};

	const toName = (color) =>
	{
		return rgbToName[color];
	};
		
	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setIsLoading(true);
			
			const from = store.views.get(props.viewId);
			const to = view;

			let changeCnt = 0;
			if(from.type !== to.type)
			{
				++changeCnt;
			}

			if(from.name !== to.name)
			{
				if(store.collection.schemaObj.views.find(v => v.name.toLowerCase() === to.name.toLowerCase()))
				{
					props.container.showMessage('Já existe uma view com o mesmo name na coleção', 'error');
					return false;
				}

				++changeCnt;
			}

			if(!Misc.compareObjects(from.style, to.style))
			{
				++changeCnt;
			}

			if(changeCnt > 0)
			{
				const res = await store.updateView(props.viewId, to);
				if(res.errors)
				{
					props.container.showMessage(res.errors, 'error');
				}
				else
				{
					props.container.showMessage('Atualização de view realizada!', 'success');
				}
			}

			return false;
		} 
		finally 
		{
			setIsLoading(false);
		}
	}, [view]);

	const renderTypeOptions = () =>
	{
		const options = [
			{value: '', title: ''},
			{value: 'grid', title: 'Grid'},
			{value: 'kanban', title: 'Kanban'},
			{value: 'table', title: 'Table'},
		];

		return options.map((opt, index) => <option key={index} value={opt.value}>{opt.title}</option>);
	};

	const style = view.style || {};
	const border = style.border || {};

	return (
		<>
			<Row>
				<Col xs="12" md="4">
					<Label className="base-form-label">Type</Label>
					<Input 
						required 
						name="type" 
						type="select" 
						value={view.type} 
						onChange={handleChange} 
					>
						{renderTypeOptions()}
					</Input>
				</Col>
				<Col xs="12" md="8">
					<Label className="base-form-label">Nome</Label>
					<Input 
						required 
						name="name" 
						type="text" 
						value={view.name} 
						onChange={handleChange} 
					/>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Label className="base-form-label">Altura da linha</Label>
					<Input 
						required 
						name="style.height" 
						type="number" 
						value={style.height || 40} 
						onChange={handleChange} 
					/>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Label className="base-form-label pt-1">Cor de fundo</Label>
					<CirclePicker 
						color={toRGB(style.color || CollectionUtil.DEFAULT_BG_COLOR)}
						colors={['#f0f0f0', '#e9f0ef', '#f0e9ef', '#eff0e9']}
						onChange={(color) => handleChange({
							target: {
								name: 'style.color', 
								type: 'text', 
								value: toName(color.hex)
							}
						})} />
				</Col>
			</Row>
			<Row>
				<Col sm="12" md="6">
					<CustomInput
						type="checkbox"
						label="Listrada"
						id="style.striped"
						checked={!!style.striped}
						onChange={handleChange} 
					/>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Label className="base-form-label pt-1">Borda</Label>
					<Card>
						<Row>
							<Col sm="12">
								<Label className="base-form-label">Cor</Label>
								<CirclePicker 
									color={toRGB(border.color || CollectionUtil.DEFAULT_BORDER_COLOR)}
									colors={['#f7f7f7', '#666666']}
									onChange={(color) => handleChange({
										target: {
											name: 'style.border.color', 
											type: 'text', 
											value: toName(color.hex)
										}
									})} />
							</Col>
						</Row>
						<Row>
							<Col sm="12" md="6">
								<Label className="base-form-label">Estilo</Label>
								<Input
									type="select"
									name="style.border.style"
									value={border.style || ''}
									onChange={handleChange}>
									{CollectionUtil.getCellBorderStyles().map((style, index) => 
										<option key={index} value={style.value}>{style.label}</option>
									)}
								</Input>
							</Col>
							<Col sm="12" md="6">
								<CustomInput
									type="checkbox"
									label="Aredondada"
									id="style.border.rounded"
									checked={!!border.rounded}
									onChange={handleChange} />
							</Col>
						</Row>
					</Card>
				</Col>
			</Row>
			<Row>
				<Col sm="12">
					<Button
						color="primary"
						disabled={isLoading}
						type="submit"
						onClick={handleSubmit}
						className="mt-4"
					>Atualizar</Button>
				</Col>
			</Row>
		</>
	);
});

General.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	viewId: PropTypes.string.isRequired,
};

export default General;