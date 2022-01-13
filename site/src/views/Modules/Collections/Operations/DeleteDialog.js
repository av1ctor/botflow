import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {Col, Row, Label, Input, Button} from 'reactstrap';
import ModalScrollable from '../../../../components/Modal/ModalScrollable';

const DeleteDialog = (props) =>
{
	const store = props.store;
	
	const [name, setName] = useState('');

	const handleDelete = async () =>
	{
		const collection = store.collection;
		if(name.toLowerCase() !== collection.name.toLowerCase())
		{
			return;
		}

		const res = await store.delete();
		if(res.errors)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		if(!store.isAux)
		{
			await props.container.reloadCollectionsMenu(collection.parent.id);

			if(collection.type === 'SCHEMA')
			{
				props.container.redirect(null);
			}
		}

		props.onDeleted(props.position);
	};

	const handleChange = (e) => 
	{
		setName(e.target.value);
	};

	if(!props.visible)
	{
		return null;
	}

	const collection = store.collection;
	
	return (
		<ModalScrollable 
			isOpen={props.visible || false} 
			onHide={props.onHide} 
			icon='trash'
			title={`Apagar ${collection.type === 'SCHEMA'? 'coleção': 'pasta'} "${collection.name}"`}
			className="rf-col-dialog-modal">
			<div className="rf-col-props-body-content">
				<div>
					<Row>
						<Col sm="12">
							<Label className="base-form-label">
								Confirme o name da {collection.type === 'SCHEMA'? 'coleção': 'pasta'} e pressione &quot;Apagar&quot; para prosseguir
							</Label>
							<Input 
								required 
								name="name" 
								type="text" 
								value={name} 
								onChange={handleChange} 
							/>
						</Col>
					</Row>
					<Row>
						<Col sm="12">
							<Button 
								color="danger" 
								type="submit" 
								className="mt-4" 
								disabled={name.toLowerCase() !== collection.name.toLowerCase()}
								onClick={handleDelete}>Apagar</Button>
						</Col>
					</Row>
				</div>
			</div>
		</ModalScrollable>
	);
};

DeleteDialog.propTypes = 
{
	container: PropTypes.object.isRequired,
	store: PropTypes.object,	
	visible: PropTypes.bool,
	position: PropTypes.any,
	onHide: PropTypes.func.isRequired,
	onDeleted: PropTypes.func.isRequired,
};

export default DeleteDialog;

