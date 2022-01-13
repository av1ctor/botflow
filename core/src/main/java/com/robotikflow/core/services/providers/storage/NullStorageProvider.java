package com.robotikflow.core.services.providers.storage;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;

public class NullStorageProvider 
    extends StorageBaseProvider
{
    public static final String name = "nullStorageProvider";

    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		super.initialize(provider);
	}

	@Override
	public List<Map<String, Object>> sync(
		final String path, 
		final DocumentOperationType op, 
		final Workspace workspace,
		final Map<String, Object> state) 
		throws Exception 
 	{
		return null;
	}

	@Override
	public void createWorkspace(
        String idWorkspace, 
		boolean isPublic) {
	}

	@Override
	public DocProps createFolder(
        String name, 
		DocProps parent) {
		return null;
	}

	@Override
	public void destroyWorkspace(
        String idWorkspace) {
	}

	@Override
	public void sendContents(
        Path fromPath, 
		String idWorkspace, 
		String idDocument, 
		String extension)
		throws StorageException {
	}

	@Override
	public void sendContents(
        BufferedImage img, 
		String format, 
		String idWorkspace, 
		String idDocument)
		throws StorageException {
	}

	@Override
	public DocProps createFile(
        byte[] contents, 
		String idWorkspace, 
		String idDocument, 
		String extension,
		DocProps parent) {
		return null;
	}

	@Override
	public DocProps createFile(
        InputStream stream, 
		long size, 
		String idWorkspace, 
		String idDocument,
		String extension, 
		DocProps parent) {
		return null;
	}

	@Override
	public void copyContents(
        String idSourceWorkspace, 
		String idSourceDocument, 
		String idTargetWorkspace,
		String idTargetDocument) {
	}

	@Override
	public String getDownloadUrl(
        String idWorkspace, 
		String idDocument, 
		String extension, 
		int expiresInSecs) {
		return null;
	}

	@Override
	public String getDownloadUrl(
        DocumentExt doc, int expiresInSecs) {
		return null;
	}

	@Override
	public void deleteContents(
        String idWorkspace, String idDocument) {
	}

	@Override
	public byte[] receiveContents(
        String idWorkspace, String idDocument) throws 
        StorageException {
		return null;
	}

	@Override
	public String getPreviewUrl(
        String pubId, String blobId, String extension) {
		return null;
	}

	@Override
	public String getPreviewUrl(
        DocumentExt doc) {
		return null;
	}
    
}
