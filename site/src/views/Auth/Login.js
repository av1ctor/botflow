import React, {useContext, useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import PropTypes from 'prop-types';
import {Button, CustomInput, Row, Col, InputGroup, InputGroupAddon, InputGroupText, Input, Card, CardBody, CardGroup, Container} from 'reactstrap';
import LoaderButton from '../../components/Button/LoaderButton';
import {SessionContext} from '../../contexts/SessionContext';

const subdomain = window.location.hostname
	.replace(new RegExp(process.env.REACT_APP_ROBOTIKFLOW_DOMAIN), '')
	.replace('.', '');

const Login = ({container}) =>
{
	const session = useContext(SessionContext);
	const params = useParams();

	const [form, setForm] = useState({
		email: '',
		password: '',
		workspace: subdomain,
		rememberMe: true,
	});
	const [isLoading, setLoading] = useState(false);
	const [errors, setErrors] = useState([]);

	const validateForm = () =>
	{
		return form.email.length > 5 && form.password.length > 5 && form.workspace.length >= 5;
	};

	const handleChange = (event) =>
	{
		const key = event.target.id;
		const value = event.target.type !== 'checkbox'? 
			event.target.value: 
			event.target.checked;

		setForm(form => ({
			...form,
			[key]: value
		}));
	};

	const handleSubmit = async (event) =>
	{
		event.preventDefault();

		try
		{
			setLoading(true);
			
			const res = await session.logonUser(
				form.email, form.password, form.workspace, form.rememberMe);
			if(!res.errors)
			{
				setErrors([]);
				if(params.redirect)
				{
					container.redirect(params.redirect);
				}
			}
			else
			{
				setErrors(res.errors);
			}
		}
		finally
		{
			setLoading(false);
		}
	};

	const handleEsqueceuSenha = async (event) =>
	{
		event.preventDefault();
	};

	const renderErrors = (props) =>
	{
		return errors && errors.map((error, index) =>
			<span key={index} className="badge badge-danger">{error}</span>
		);

	};

	return (
		<div className="app flex-row align-items-center">
			<Container>
				<Row className="justify-content-center">
					<CardGroup>
						<Card className="p-4">
							<CardBody>
								<form onSubmit={handleSubmit}>
									<h1>Login</h1>
									<p className="text-muted">Informe seu e-mail, password e área de trabalho</p>
									<InputGroup className="mb-3">
										<InputGroupAddon addonType="prepend">
											<InputGroupText>
												<span className="la la-user"></span>
											</InputGroupText>
										</InputGroupAddon>
										<Input
											required
											id="email" 
											type="email" 
											placeholder="user@email.com" 
											value={form.email} 
											onChange={handleChange} />
									</InputGroup>
									<InputGroup className="mb-3">
										<InputGroupAddon addonType="prepend">
											<InputGroupText>
												<span className="la la-lock"></span>
											</InputGroupText>
										</InputGroupAddon>
										<Input 
											required
											id="password"
											type="password"
											placeholder="password"  
											value={form.password} 
											onChange={handleChange} />
									</InputGroup>
									<InputGroup className="mb-3">
										<InputGroupAddon addonType="prepend">
											<InputGroupText>
												<span className="la la-desktop"></span>
											</InputGroupText>
										</InputGroupAddon>
										<Input 
											required
											disabled={subdomain !== ''} 
											id="workspace" 
											type="text" 
											placeholder="área de trabalho" 
											value={form.workspace} 
											onChange={handleChange} />
									</InputGroup>
									<InputGroup className="mb-3">
										<CustomInput 
											id="rememberMe" 
											type="checkbox" 
											label="Lembrar" 
											checked={form.rememberMe} 
											onChange={handleChange} />
									</InputGroup>
									<Row>
										<Button 
											color="link" 
											className="px-0" 
											onClick={handleEsqueceuSenha}>Esqueceu a password?
										</Button>
									</Row>
									<Row>
										<LoaderButton 
											text="Entrar"
											loadingText="Aguarde..."
											color="primary" 
											className="px-4"
											block 
											disabled={!validateForm()} 
											isLoading={isLoading} 
											type="submit" />
									</Row>
									<Row className="justify-content-center">
										<Col md="12">
											{renderErrors()}
										</Col>
									</Row>
									<hr />
									<Row className="justify-content-center">
										<Link 
											to="/auth/signup" 
											color="link" 
											className="px-0">
											Não tem conta ainda? Registre-se!
										</Link>
									</Row>
								</form>
							</CardBody>
						</Card>
					</CardGroup>
				</Row>
			</Container>
		</div>
	);
};

Login.propTypes = 
{
	api: PropTypes.object,
	container: PropTypes.object,
};

export default Login;

