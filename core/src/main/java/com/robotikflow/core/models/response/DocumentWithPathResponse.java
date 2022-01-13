package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.DocumentWithPath;
import com.robotikflow.core.util.DocumentUtil;

public class DocumentWithPathResponse 
	extends DocumentResponse
{
	private final String path;
	
	public DocumentWithPathResponse(
		final DocumentWithPath document,
		final DocumentUtil documentoUtil)
	{
		super(document.getDocument(), documentoUtil);
		this.path = document.getPath();
	}

	public String getPath() {
		return path;
	}
}
