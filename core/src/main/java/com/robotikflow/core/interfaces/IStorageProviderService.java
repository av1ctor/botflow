package com.robotikflow.core.interfaces;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.Workspace;

public interface IStorageProviderService
	extends IProviderService
{
    List<Map<String, Object>> sync(
		final String path,
		final DocumentOperationType op,
		final Workspace workspace,
		final Map<String, Object> state) 
		throws Exception;
	
	void createWorkspace(
		final String idWorkspace, 
		final boolean isPublic);
	
	DocProps createFolder(
		final String name, 
		final DocProps parent);
	
	void destroyWorkspace(
		final String idWorkspace);
	
	void sendContents(
		final Path fromPath, 
		final String idWorkspace, 
		final String idDocument, 
		final String extension) 
		throws StorageException;
	
	void sendContents(
		final BufferedImage img, 
		final String format, 
		final String idWorkspace, 
		final String idDocument) 
		throws StorageException;
	
	DocProps createFile(
		final byte[] contents, 
		final String idWorkspace, 
		final String idDocument, 
		final String extension, 
		final DocProps parent);
	
	DocProps createFile(
		final InputStream stream, 
		final long size,
		final String idWorkspace, 
		final String idDocument, 
		final String extension, 
		final DocProps parent);
	
	void copyContents(
		final String idSourceWorkspace, 
		final String idSourceDocument, 
		final String idTargetWorkspace,
		final String idTargetDocument);
	
	String getDownloadUrl(
		final String idWorkspace, 
		final String idDocument, 
		final String extension, 
		final int expiresInSecs);

	String getDownloadUrl(
		final DocumentExt doc, 
		final int expiresInSecs);
		
	void deleteContents(
		final String idWorkspace, 
		final String idDocument);
	
	byte[] receiveContents(
		final String idWorkspace, 
		final String idDocument) 
		throws StorageException;

	String getPreviewUrl(
		final String pubId, 
		final String blobId, 
		final String extension);

	String getPreviewUrl(
		final DocumentExt doc);

    Document getRootDoc();
}
