package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.util.DocumentUtil;

public class DocumentBaseResponse 
{
	private final String id;
	private final DocumentType type;
	private final ProviderBaseResponse provider;
	private final String name;

	public DocumentBaseResponse(
		final Document document, 
		final DocumentUtil docUtil)
	{
		id = document.getPubId();
		type = document.getType();
		name = document.getName();
		
		if(DocumentUtil.isInterno(document))
		{
			provider = new ProviderBaseResponse(
				getInternalProvider(docUtil, document.getWorkspace()));
		}
		else
		{
			var doc = DocumentUtil.toExterno(document);
			provider = new ProviderBaseResponse(doc.getProvider());
		}
	}

	public String getId() {
		return id;
	}

	public DocumentType getType() {
		return type;
	}

	public ProviderBaseResponse getProvider() {
		return provider;
	}

	public String getName() {
		return name;
	}

	private Provider getInternalProvider(
		final DocumentUtil docUtil,
		final Workspace workspace) 
	{
		var prov = docUtil.getInternalProvider(workspace);

		return prov;
	}

}
