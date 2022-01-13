package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.robotikflow.core.util.DocumentUtil;

@Entity
@Table(name = "documents_exts")
public class DocumentExt 
	extends Document
{
	private String fileId;
	
	private String filePath;
	
	private String mimeType;
	
	private String urlDownload;
	
	public DocumentExt()
	{
		super();
	}

	public DocumentExt(
		Provider provider,
		String name, 
		DocumentType type,
		Long tamanho,
		Document parent,
		User user,
		ZonedDateTime createdAt,
		ZonedDateTime updatedAt,
		String fileId,
		String filePath,
		String extensao, 
		String mimeType,
		String urlDownload,
		Workspace workspace
		)
	{
		super(
			provider,
			name, 
			type, 
			tamanho, 
			parent, 
			user, 
			null, 
			DocumentAuthType.MODIFY, 
			DocumentAuthType.READ, 
			DocumentAuthType.NONE, 
			workspace);
		this.extension = extensao;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.updatedBy = updatedAt != null? user: null;
		this.fileId = fileId;
		this.filePath = filePath;
		this.mimeType = mimeType;
		this.urlDownload = urlDownload;
	}

	public DocumentExt(
		Provider provider,
		String name, 
		DocumentType type,
		Long tamanho,
		Document parent,
		User user,
		String fileId,
		String filePath,
		String mimeType,
		String urlDownload,
		Workspace workspace
		)
	{
		this(
			provider, 
			name, 
			type, 
			tamanho, 
			parent, 
			user, 
			ZonedDateTime.now(), 
			null, 
			fileId, 
			filePath,
			DocumentUtil.getExtension(name, type), 
			mimeType, 
			urlDownload, 
			workspace);
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getUrlDownload() {
		return urlDownload;
	}

	public void setUrlDownload(String urlDownload) {
		this.urlDownload = urlDownload;
	}
}
