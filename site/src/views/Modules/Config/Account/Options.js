import React, {useContext, useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input} from 'reactstrap';
import AutoSizer from 'react-virtualized-auto-sizer';
import CollectionUtil from '../../../../libs/CollectionUtil';
import AvatarPicker from '../../../../components/Picker/AvatarPicker';
import BaseForm from '../../../../components/BaseForm';
import {SessionContext } from '../../../../contexts/SessionContext';

const Options = ({api, history, container}) =>
{
	const sessionStore = useContext(SessionContext);

	const [form, setForm] = useState({});
	const [timezones, setTimezones] = useState([]);

	const loadUser = async () =>
	{
		const res = await api.get(`users/${sessionStore.user.id}`);
		if(res.errors)
		{
			container.showMessage(res.errors, 'error');
			return {};
		}

		return res.data;
	};

	const loadTimezones = async () =>
	{
		const res = await api.get('misc/timezones');
		if(res.errors)
		{
			container.showMessage(res.errors, 'error');
			return {};
		}

		return res.data;
	};

	useEffect(() =>
	{
		(async () =>
		{
			const form = await loadUser();
			const timezones = await loadTimezones();

			setForm(form);
			setTimezones(timezones);
		})();
	}, []);

	const handleUpdated = (prev, user) =>
	{
		setForm(user);

		sessionStore.setUser(user);

		return user;
	};

	const renderLangOptions = () =>
	{
		return (
			CollectionUtil.getUserLangs().map((lang, index) => 
				<option value={lang.value} key={index}>{lang.label}</option>
			)
		);
	};

	const renderTimezoneOptions = () =>
	{
		return (
			(timezones || []).map((timezone, index) => 
				<option value={timezone.id} key={index}>{timezone.id} ({timezone.diff})</option>
			)
		);
	};

	const renderThemeOptions = () =>
	{
		return (
			CollectionUtil.getUserThemes().map((theme, index) => 
				<option value={theme.value} key={index}>{theme.label}</option>
			)
		);
	};

	const renderFields = (item, onChange) =>
	{
		return (
			<>
				<Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Id</Label>
						<Input 
							disabled
							required 
							name="" 
							type="text" 
							value={item.id || ''} 
							readOnly />
					</Col>
					<Col xs="12" md="6">
						<Label className="base-form-label">E-mail</Label>
						<Input 
							disabled
							required 
							name="email" 
							type="text" 
							value={item.email || ''} 
							readOnly />
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="6">
						<Label className="base-form-label">Apelido</Label>
						<Input 
							required
							name="nick" 
							type="text" 
							value={item.nick || ''}
							onChange={onChange} />
					</Col>
					<Col xs="12" md="6">
						<Label className="base-form-label">Nome</Label>
						<Input 
							required
							name="name" 
							type="text" 
							value={item.name || ''}
							onChange={onChange} />
					</Col>
				</Row>
				<Row>
					<Col xs="12" md="4">
						<Label className="base-form-label">Idioma</Label>
						<Input 
							required
							name="lang" 
							type="select" 
							value={item.lang || ''}
							onChange={onChange}>
							{renderLangOptions()}
						</Input>
					</Col>
					<Col xs="12" md="4">
						<Label className="base-form-label">Timezone</Label>
						<Input 
							required
							name="timezone" 
							type="select" 
							value={item.timezone || ''}
							onChange={onChange}>
							{renderTimezoneOptions()}
						</Input>
					</Col>
					<Col xs="12" md="4">
						<Label className="base-form-label">Tema</Label>
						<Input 
							required
							name="theme" 
							type="select" 
							value={item.theme || 'default'}
							onChange={onChange}>
							{renderThemeOptions()}
						</Input>
					</Col>
				</Row>
				<Row>
					<Col xs="12">
						<Label className="base-form-label">Avatar</Label>
						<div style={{width: '100%', height: 92}}>
							<AutoSizer>
								{({width}) => 
									<AvatarPicker 
										dir="horz"
										width={width}
										selected={item.icon}
										onChange={(icon) => onChange({target: {name: 'icon', value: icon}})}
									/>
								}
							</AutoSizer>
						</div>
					</Col>
				</Row>
			</>
		);
	};

	return (
		<div className="rf-page-contents">
			<div className="rf-page-header pt-2">
				<div>
					<div className="rf-page-title-icon">
						<i className="la la-user-edit" />
					</div>
					<div className="rf-page-title-other">
						<div className="row">
							<div className="col-auto rf-page-title-container">
								<div className="rf-page-title">
									Configurações / Conta / Dados
								</div>
							</div>
						</div>
						<div className="row">
							<div className="col">
								<small>Dados da conta</small>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div className="rf-page-main-form">
				<BaseForm 
					api={api}
					history={history}
					parent={container}
					controller="users"
					item={form}
					render={renderFields}
					onUpdated={handleUpdated}
				/>
			</div>
		</div>
	);
};

Options.propTypes = 
{
	api: PropTypes.object.isRequired,
	history: PropTypes.object,
	container: PropTypes.object.isRequired
};

export default Options;