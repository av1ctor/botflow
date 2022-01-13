package com.robotikflow.api.server.models.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CollectionAdicionarItensRequest
{
	@NotNull
	@Size(min=3, max=65536)
    private String csv;
	@NotNull
	private boolean substituirSeExistir;

	public String getCsv() {
		return csv;
	}

	public void setCsv(String csv) {
		this.csv = csv;
	}

	public boolean isSubstituirSeExistir() {
		return substituirSeExistir;
	}

	public void setSubstituirSeExistir(boolean substituirSeExistir) {
		this.substituirSeExistir = substituirSeExistir;
	}
	
}
