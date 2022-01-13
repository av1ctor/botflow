package com.robotikflow.api.server.models.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class DocumentAcaoCriarDiretorioRequest extends DocumentAcaoRequest
{
	@NotNull
	@Size(min=1, max=64)
	private String diretorio;

	public String getDiretorio() {
		return diretorio;
	}

	public void setDiretorio(String diretorio) {
		this.diretorio = diretorio;
	}
}
