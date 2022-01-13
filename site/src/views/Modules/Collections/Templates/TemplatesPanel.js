import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import ModalScrollable from '../../../../components/Modal/ModalScrollable';

const TemplatesPanel = (props) =>
{
	const [templates, setTemplates] = useState(null);

	useEffect(() =>
	{
		loadTemplates();
	}, []);
	
	const loadTemplates = async () =>
	{
		const res = await props.api.get('templates/collections');
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		setTemplates(res.data);
	};

	const handleCreateCollection = async (template) =>
	{
		if(props.onSelected)
		{
			props.onHide();
			props.onSelected(template.id);
			return;
		}

		const res = await props.api.post(
			`collections/${props.parentId}/gen/${template.id}?published=${props.published}`);
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		if(props.published)
		{
			await props.container.reloadCollectionsMenu(props.parentId);
		}

		props.onHide();
		
		props.container.redirect(`/c/${res.data.id}`);
	};

	const handleCreateFolder = async () =>
	{
		if(props.onSelected)
		{
			props.onHide();
			props.onSelected('0');
			return;
		}

		const res = await props.api.post(`collections/${props.parentId}/gen/0?published=${props.published}`);
		if(res.errors !== null)
		{
			props.container.showMessage(res.errors, 'error');
			return;
		}

		if(props.published)
		{
			await props.container.reloadCollectionsMenu(props.parentId);
		}

		props.onHide();
	};

	if(!templates || templates.length === 0)
	{
		return null;
	}

	return (
		<ModalScrollable 
			isOpen={props.visible} 
			onHide={props.onHide} 
			icon="th-large"
			title="Modelos de coleção"
			className="rf-col-templates-modal">
			<div className="rf-col-templates-body">
				<div>
					{props.allowFolder && 
					<div 
						className="rf-col-templates-template" 
						onClick={handleCreateFolder}>
						<div>
							<h1><i className="la la-folder"/></h1>
						</div>
						<div>
							<strong>Pasta</strong>
						</div>
					</div>}
					{templates.map((t, index) => 
						<div 
							key={index} 
							className="rf-col-templates-template" 
							onClick={() => handleCreateCollection(t)}>
							<div>
								<h1><i className={`la la-${t.icon}`}/></h1>
							</div>
							<div>
								<strong>{t.name}</strong>
							</div>
						</div> 
					)}
				</div>
			</div>
		</ModalScrollable>
	);
};

TemplatesPanel.propTypes = 
{
	api: PropTypes.object.isRequired,
	container: PropTypes.object.isRequired,
	parentId: PropTypes.string,
	visible: PropTypes.bool, 
	published: PropTypes.bool,
	allowFolder: PropTypes.bool,
	onSelected: PropTypes.func,
	onHide: PropTypes.func.isRequired,
};

export default TemplatesPanel;

