import React, {useState} from 'react';
import {useHistory} from 'react-router-dom';
import PropTypes from 'prop-types';
import {Dialog} from 'primereact/dialog';
import {Button, Row, Col, Input, InputGroup, InputGroupAddon, InputGroupText, Card, CardBody, CardGroup, Container} from 'reactstrap';
import LoaderButton from '../../components/Button/LoaderButton';

const Signup = ({api}) =>
{
	const history = useHistory();
	
	const [form, setForm] = useState({
		email: '',
		nick: '',
		password: '',
		workspace: '',
		lembrar: true,
	});
	const [isLoading, setLoading] = useState(false);
	const [errors, setErrors] = useState([]);
	const [dialogVisible, setDialogVisible] = useState(false);

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

			const res = await api.post(
				'auth/join', 
				{
					email: form.email,
					nick: form.nick, 
					password: form.password, 
					workspace: form.workspace
				});
			if(res.errors === null)
			{
				setDialogVisible(true);
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

	const handleDialogClosed = async (event) =>
	{
		setDialogVisible(false);
		
		setTimeout(() => history.push('/'), 500);
	};

	const renderErrors = () =>
	{
		return errors && errors.map((erro, index) =>
			<span key={index} className="badge badge-danger">{erro}</span>
		);

	};

	const dialogFooter = (
		<Row className="justify-content-center">
			<Button color="primary" className="px-4" block onClick={handleDialogClosed}>OK</Button>
		</Row>
	);

	return (
		<div className="app flex-row align-items-center">
			<Container>
				<Row className="justify-content-center">
					<CardGroup>
						<Card className="p-4">
							<CardBody>
								<form onSubmit={handleSubmit}>
									<h1>Account creation</h1>
									<p className="text-muted">Inform e-mail, nick name, password and the workspace name</p>
									<InputGroup className="mb-3">
										<InputGroupAddon addonType="prepend">
											<InputGroupText>
												<span className="la la-user"></span>
											</InputGroupText>
										</InputGroupAddon>
										<Input 
											id="email" 
											type="email" 
											placeholder="your@email.com" 
											value={form.email} 
											onChange={handleChange} />
									</InputGroup>
									<InputGroup className="mb-3">
										<InputGroupAddon addonType="prepend">
											<InputGroupText>
												<span className="la la-list"></span>
											</InputGroupText>
										</InputGroupAddon>
										<Input 
											id="nick" 
											type="text" 
											placeholder="nick name" 
											value={form.nick} 
											onChange={handleChange} />
									</InputGroup>
									<InputGroup className="mb-4">
										<InputGroupAddon addonType="prepend">
											<InputGroupText>
												<span className="la la-lock"></span>
											</InputGroupText>
										</InputGroupAddon>
										<Input 
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
											id="workspace" 
											type="text" 
											placeholder="workspace name" 
											value={form.workspace} 
											onChange={handleChange} />
									</InputGroup>
									<Row>
										<LoaderButton 
											text="Signup"
											loadingText="Signing up..."
											color="primary" 
											className="px-4"
											block 
											disabled={!validateForm()} 
											isLoading={isLoading} 
											type="submit" />
									</Row>
									<Row>
										<Col md="12">
											{renderErrors()}
										</Col>
									</Row>
								</form>
							</CardBody>
						</Card>
					</CardGroup>
				</Row>
				
				<Dialog 
					header="Sucesso!" 
					visible={dialogVisible} 
					footer={dialogFooter} 
					width="400px" 
					modal 
					onHide={handleDialogClosed}
				>
					<Row className="justify-content-center">
						<div>Account created!</div>
						<div>Check your e-mail to confirm your account.</div>
					</Row>
				</Dialog>					
			</Container>
		</div>
	);
};

Signup.propTypes = 
{
	api: PropTypes.object,
};

export default Signup;

