package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionAutomationDate;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionAutomationDateResponse 
	extends CollectionAutomationResponse 
{
	private final String repeat;
	private final String start;

	public CollectionAutomationDateResponse
		(CollectionAutomationDate automacao)
	{
		super(automacao);

		repeat = automacao.getRepeat().toString();
		start = automacao.getStart().format(DocumentUtil.datePattern);
	}

	public String getRepeat() {
		return repeat;
	}
	public String getStart() {
		return start;
	}
}
