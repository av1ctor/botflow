package com.robotikflow.api.server.models.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class DocumentAcaoRenomearRequest extends DocumentAcaoRequest 
{
	@NotNull
	@Size(min=1, max=128)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
