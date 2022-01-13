import React, {useCallback, useEffect, useState} from 'react';
import {observer} from 'mobx-react-lite';
import PropTypes from 'prop-types';
import {Col, Row, Input, Button} from 'reactstrap';
import ExtensibleList from '../../../../components/List/ExtensibleList';
import Misc from '../../../../libs/Misc';

const columns = [
	{width: 3, label: 'Descrição', name: 'desc'},
	{width: 5, label: 'Data', name: 'createdAt'},
	{width: 0, label: 'Patches', name: 'diff'},
];

const Versions = observer(props =>
{
	const store = props.store;
	
	const [versions, setVersions] = useState(null);
	const [errors, setErrors] = useState([]);
	const [loading, setLoading]	= useState(false);

	useEffect(() =>
	{
		(async () =>
		{
			const versions = props.visible? 
				await loadVersions(): 
				null;
			
			setVersions(versions);
		})();
	}, []);

	useEffect(() =>
	{
		(async () =>
		{
			if(props.visible)
			{
				const versions = await loadVersions();
				setVersions(versions);
			}
		})();
	}, [props.visible, store.collection]);

	const loadVersions = async () =>
	{
		const res = await store.loadVersions();
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return null;
		}
			
		return res.data;
	};

	const handleSubmit = useCallback(async (event) =>
	{
		event.preventDefault();

		try 
		{
			setLoading(true);
			
			if(versions.length !== store.versions.length)
			{
				const ids = [];
				
				store.versions.filter(v => !versions.find(c => c.id === v.id)).forEach(version => 
				{
					ids.push(version.id);
				});

				const res = await store.deleteVersions(ids);
				setErrors(res.errors || []);
				if(!res.errors)
				{
					props.container.showMessage('Atualização de versões realizada!', 'success');
				}
			}
		} 
		finally 
		{
			setLoading(false);
		}

		return false;
	}, [versions]);

	const restore = useCallback(async ({item}) => 
	{
		const res = await store.revertToVersion(item.id);
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return null;
		}

		props.container.showMessage('Versão revertida!', 'success');
	}, []);

	const renderHeader = ({col}) =>
	{
		return (col.label);
	};

	const renderItem = ({item, index, col, isPreview}) => 
	{
		if(isPreview)
		{
			switch(col.name)
			{
			case 'createdAt':
				return Misc.dateToString(item.createdAt);
			default:
				return item[col.name];
			}
		}

		switch(col.name)
		{
		case 'desc':
			return (
				<Input 
					type="text" 
					name="desc"
					value={item.desc} 
					readOnly>
				</Input>);

		case 'createdAt':
			return (
				<Input 
					type="text" 
					name="createdAt"
					value={Misc.dateToString(item.createdAt)} 
					readOnly>
				</Input>);

		case 'diff':
			return (
				<Input 
					type="textarea" 
					rows="8"
					name="diff"
					value={item.diff} 
					readOnly>
				</Input>);
		}
	};

	const onRestore = useCallback((event, item) =>
	{
		props.container.showUserConfirmation(restore, {item}, 'Reverter para essa versão?');
	}, []);
	
	const onDelItem = useCallback(({item, index, callback}) =>
	{
		props.container.showUserConfirmation(callback, {item, index}, 'Remover versão?');
	}, []);

	const delItem = useCallback((item, index) =>
	{
		setVersions(versions => versions.filter((c, i) => i !== index));
	}, []);

	const renderActions = ({item}) =>
	{
		return (
			<>
				<Button 
					size="xs" 
					outline 
					className="rf-listbox-button" 
					onClick={(e) => onRestore(e, item)} 
					title="Restaurar">
					<i className="rf-listbox-button-icon la la-undo-alt"></i>
				</Button>
			</>
		);
	};

	const renderErrors = () =>
	{
		return errors.map((error, index) =>
			<div key={index}>
				<span className="badge badge-danger">{error}</span>
			</div>
		);
	};

	if(!versions)
	{
		return null;
	}

	return (
		<form>
			<Row>
				<Col sm="12">
					<ExtensibleList 
						columns={columns}
						totalPreviewCols={2}
						items={versions}
						renderHeader={renderHeader}
						renderItem={renderItem}
						renderActions={renderActions}
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

Versions.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	store: PropTypes.object.isRequired,
	visible: PropTypes.bool.isRequired,
};

export default Versions;