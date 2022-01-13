import React, {useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Label, CustomInput, InputGroup, Button} from 'reactstrap';
import ModalScrollable from '../../../../components/Modal/ModalScrollable';

const DuplicateDialog = observer(props =>
{
	const store = props.store;
	
	const [dup, setDup]	= useState({
		data: false,
		permissions: true, 
		integrations: true, 
		automations: true,
		auxs: true,
	});
	const [loading, setLoading]	= useState(false);

	const handleDuplicate = async () =>
	{
		try
		{
			setLoading(true);

			const res = await store.duplicate({
				...dup, 
				position: props.position
			});
			if(res.errors !== null)
			{
				props.container.showMessage(res.errors, 'error');
				return;
			}
	
			if(!store.isAux)
			{
				props.container.reloadCollectionsMenu();
				props.container.redirect(res.data.id);
			}
	
			if(props.onDuplicated)
			{
				props.onDuplicated(res.data, props.position);
			}
		}
		finally
		{
			setLoading(false);
		}
	};

	const handleChange = (e) =>
	{
		const key = e.target.id;
		const value = e.target.checked;
		setDup(state => ({
			...state,
			[key]: value
		}));
	};

	if(!props.visible)
	{
		return null;
	}

	const collection = store.collection;

	return (
		<ModalScrollable 
			isOpen={props.visible || false} 
			icon='copy'
			title={`Duplicar coleção "${collection.name}"`}
			onHide={props.onHide} 
			className="rf-col-dialog-modal">
			<div className="rf-col-props-body-content">
				<div>
					<Row>
						<Col sm="12">
							<InputGroup>
								<CustomInput 
									id="data" 
									type="checkbox" 
									checked={dup.data} 
									onChange={handleChange} />
								<Label>Incluir dados</Label>
							</InputGroup>
						</Col>
					</Row>
					<Row>
						<Col sm="12">
							<InputGroup>
								<CustomInput 
									id="permissions" 
									type="checkbox" 
									checked={dup.permissions} 
									onChange={handleChange} />
								<Label>Incluir permissões</Label>
							</InputGroup>
						</Col>
					</Row>
					<Row>
						<Col sm="12">
							<InputGroup>
								<CustomInput 
									id="integrations" 
									type="checkbox" 
									checked={dup.integrations} 
									onChange={handleChange} />
								<Label>Incluir integrações</Label>
							</InputGroup>
						</Col>
					</Row>
					<Row>
						<Col sm="12">
							<InputGroup>
								<CustomInput 
									id="automations" 
									type="checkbox" 
									checked={dup.automations} 
									onChange={handleChange} />
								<Label>Incluir automações</Label>
							</InputGroup>
						</Col>
					</Row>
					<Row>
						<Col sm="12">
							<InputGroup>
								<CustomInput 
									id="auxs" 
									type="checkbox" 
									checked={dup.auxs} 
									onChange={handleChange} />
								<Label>Incluir auxiliares</Label>
							</InputGroup>
						</Col>
					</Row>
					<Row>
						<Col sm="12">
							<Button 
								disabled={loading}
								color="primary" 
								type="submit" 
								className="mt-4" 
								onClick={handleDuplicate}>Duplicar</Button>
						</Col>
					</Row>
				</div>
			</div>
		</ModalScrollable>
	);
});

DuplicateDialog.propTypes = {
	container: PropTypes.object.isRequired,
	store: PropTypes.object,
	visible: PropTypes.bool,
	position: PropTypes.number,
	onHide: PropTypes.func,
	onDuplicated: PropTypes.func,
};

export default DuplicateDialog;

