package com.robotikflow.api.server.models.request;

public class DocumentAcaoCopiarRequest extends DocumentAcaoRequest 
{
	private String destino;

	public String getDestino() {
		return destino;
	}

	public void setDestino(String destino) {
		this.destino = destino;
	}
}
