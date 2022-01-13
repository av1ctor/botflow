package com.robotikflow.core.services.providers.storage;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Cors;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.CopyRequest;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.util.DocumentUtil;

public class GcloudStorageProvider 
	extends StorageBaseProvider
{
    public static final String name = "gcloudStorageProvider";

    private Storage _client;
	private long MAX_FILE_SIZE_TO_LOAD_IN_MEMORY = 100*1024*1024l;
	private FileNameMap fileNameMap;
	private String corsAllowedOrigin;
	private String location;
    
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		super.initialize(provider);
		
        var credentialService = credentialServiceFactory.buildByPubId(
            provider.getFields().getString("credential"), 
            provider.getWorkspace());

        var resource = (InputStream)credentialService.getClient();

		_client = StorageOptions.newBuilder()
		    .setCredentials(ServiceAccountCredentials.fromStream(resource))
		    .build()
		    .getService();
		
		var fields = provider.getFields();
       
		fileNameMap = URLConnection.getFileNameMap();
		corsAllowedOrigin = (String)fields.get("corsOrigin");
		location = (String)fields.get("location");
    }

    private Storage getClient() 
    {
       return _client;
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
		boolean isPublic) 
	{
		try 
		{
			var cors = Cors
				.newBuilder()
					.setMaxAgeSeconds(3600)
					.setMethods(Arrays.asList(HttpMethod.GET, HttpMethod.HEAD))
					.setOrigins(Arrays.asList(Cors.Origin.of(corsAllowedOrigin)))
					.setResponseHeaders(Arrays.asList("Content-Type"))
				.build();
			
			Bucket bucket = getClient().create(
				BucketInfo.newBuilder(idWorkspace)
					.setStorageClass(StorageClass.REGIONAL)
					.setLocation(location)
					.setVersioningEnabled(false)
					.setCors(Arrays.asList(cors))
				.build());
			
			if(isPublic)
			{
				bucket.createDefaultAcl(Acl.of(User.ofAllUsers(), Acl.Role.READER));
			}
			else
			{
				bucket.createDefaultAcl(Acl.of(bucket.getOwner(), Acl.Role.OWNER));
			}
		} 
		catch(Exception e) 
		{
			logger.error("Workspace creation failed", e);
		}
	}

	@Override
	public DocProps createFolder(
		String name, 
		DocProps target) 
	{
		return null;
	}

	@Override
	public void destroyWorkspace(
		String idWorkspace) 
	{
		getClient().delete(idWorkspace);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void sendContents(
		Path fromPath, 
		String idWorkspace, 
		String idDocument, 
		String extension)
		throws StorageException 
	{
		var contentType = fileNameMap.getContentTypeFor("f." + extension);
		
		var blobId = BlobId.of(idWorkspace, idDocument);
		var blobInfo = BlobInfo
			.newBuilder(blobId)
				.setContentType(contentType)
			.build();
		var file = new File(fromPath.toUri());
		 
		Blob blob = null;
		try 
		{
			var is = new BufferedInputStream(new FileInputStream(file));
		
			if(file.length() <= MAX_FILE_SIZE_TO_LOAD_IN_MEMORY)
			{
				blob = getClient().create(blobInfo, is.readAllBytes());
			}
			else
			{
				blob = getClient().create(blobInfo, is);
			}
		} 
		catch (IOException e) 
		{
			blob = null;
		}
		
		if(blob == null)
		{
			throw new StorageException(String.format("Blob creation failed: %s", idDocument));
		}
	}

	@Override
	public void sendContents(
		BufferedImage img, 
		String format, 
		String idWorkspace, 
		String idDocument)
		throws StorageException 
	{
		var contentType = fileNameMap.getContentTypeFor("f." + format);
		
		var blobId = BlobId.of(idWorkspace, idDocument);
		var blobInfo = BlobInfo
			.newBuilder(blobId)
				.setContentType(contentType)
			.build();
		
		var os = new ByteArrayOutputStream();
		try 
		{
			ImageIO.write(img, format, os);
		} 
		catch (IOException e) 
		{
			throw new StorageException(String.format("Blob creation failed: %s", idDocument), e);
		}
		
		if(getClient().create(blobInfo, os.toByteArray()) == null)
		{
			throw new StorageException(String.format("Blob creation failed: %s", idDocument));
		}
	}

	@Override
	public DocProps createFile(
		byte[] contents, 
		String idWorkspace, 
		String idDocument, 
		String extension,
		DocProps parent) 
	{
		return createFile(
			new ByteArrayInputStream(contents), 
			contents.length, 
			idWorkspace, 
			idDocument, 
			extension, 
			parent);
	}

	@Override
	public DocProps createFile(
		InputStream stream, 
		long size, 
		String idWorkspace, 
		String idDocument,
		String extension, 
		DocProps parent) 
	{
		return null;
	}

	@Override
	public void copyContents(
		String idSourceWorkspace, 
		String idSourceDocument, 
		String idTargetWorkspace,
		String idTargetDocument) 
	{
		var srcBlobId = BlobId.of(idSourceWorkspace, idSourceDocument);
		var destBlobId = BlobId.of(idTargetWorkspace, idTargetDocument);
		
		var req = CopyRequest.newBuilder()
				.setSource(srcBlobId)
				.setTarget(destBlobId)
				.build();
		
		if(getClient().copy(req).getResult() == null)
		{
			throw new StorageException(String.format("Blob copy failed: %s", idSourceDocument));
		}
	}

	@Override
	public String getDownloadUrl(
		String idWorkspace, 
		String idDocument, 
		String extension, 
		int expiresInSecs) 
	{
		var blobId = BlobInfo.newBuilder(idWorkspace, idDocument).build();
		
		var url = getClient().signUrl(blobId, expiresInSecs, TimeUnit.SECONDS);
		
		return url.toString();
	}

	@Override
	public String getDownloadUrl(
		DocumentExt doc, 
		int expiresInSecs) 
	{
		return doc.getUrlDownload();
	}

	@Override
	public void deleteContents(
		String idWorkspace, 
		String idDocument) 
	{
		var blobId = BlobId.of(idWorkspace, idDocument);
		if(!getClient().delete(blobId))
		{
			throw new StorageException(String.format("Blob deletion failed: %s", idDocument));
		}
	}

	@Override
	public byte[] receiveContents(
		String idWorkspace, 
		String idDocument) 
		throws StorageException 
	{
		var blobId = BlobId.of(idWorkspace, idDocument);
		try
		{
			return getClient().readAllBytes(blobId);
		}
		catch(com.google.cloud.storage.StorageException e)
		{
			throw new StorageException(String.format("Blob download failed: %s", idDocument), e);
		}
	}

	@Override
	public String getPreviewUrl(
		String pubId, 
		String blobId, 
		String extension) 
	{
		return null;
	}

	@Override
	public String getPreviewUrl(
		DocumentExt doc) 
	{
		return DocumentUtil.getPreviewUrl(doc);
	}
}