package com.robotikflow.core.models.entities;

public class DocumentWithPath
{
	private Document document;
	private String path;

	public DocumentWithPath(Document document, String path) 
	{
		this.document = document;
		this.path = path;
	}

	public Document getDocument() {
		return document;
	}

	public String getPath()
	{
		return path;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setCaminho(String caminho) {
		this.path = caminho;
	}
}

