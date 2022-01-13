package com.robotikflow.api.server.models.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = DocumentAcaoRenomearRequest.class, name = "RENOMEAR"),
        @JsonSubTypes.Type(value = DocumentAcaoMoverRequest.class, name = "MOVER"),
        @JsonSubTypes.Type(value = DocumentAcaoCopiarRequest.class, name = "COPIAR"),
        @JsonSubTypes.Type(value = DocumentAcaoCriarDiretorioRequest.class, name = "CRIAR_DIRETORIO")
})
public class DocumentAcaoRequest 
{
	@NotNull
	private DocumentAcaoTipo type;

	public DocumentAcaoTipo getType() {
		return type;
	}

	public void setType(DocumentAcaoTipo type) {
		this.type = type;
	}
}
