package com.robotikflow.api.server.models.response;

import com.robotikflow.core.exception.CollectionException;
import com.robotikflow.core.models.entities.CollectionAutomation;
import com.robotikflow.core.models.entities.CollectionAutomationField;
import com.robotikflow.core.models.entities.CollectionAutomationDate;
import com.robotikflow.core.models.entities.CollectionAutomationItem;

public class CollectionAutomationResponseFactory 
{
	public static CollectionAutomationResponse create
		(CollectionAutomation automacao) 
	{
		switch(automacao.getType()) 
		{
			case FIELD:
				return new CollectionAutomationFieldResponse((CollectionAutomationField)automacao);

			case ITEM:
				return new CollectionAutomationItemResponse((CollectionAutomationItem)automacao);

			case DATE:
				return new CollectionAutomationDateResponse((CollectionAutomationDate)automacao);

			default:
				throw new CollectionException("Não implementado");
		}
	}
}
