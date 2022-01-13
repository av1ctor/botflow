package com.robotikflow.core.services.providers.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.http.StandardHttpRequestor.Config;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.interfaces.props.FileProps;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.util.DocumentUtil;

public class DropboxProvider 
	extends StorageBaseProvider
{
    public static final String name = "dropboxProvider";

    private ICredentialService credentialService;
    private DbxClientV2 _client;
	private String accessToken;
	private DbxRequestConfig requestConfig;
    
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		super.initialize(provider);
		
        credentialService = credentialServiceFactory.buildByPubId(
            provider.getFields().getString("credential"), 
            provider.getWorkspace());

		requestConfig = DbxRequestConfig.newBuilder("robotikflow-client")
            .withHttpRequestor(new StandardHttpRequestor(
				StandardHttpRequestor.Config.DEFAULT_INSTANCE))
            .build();		

		accessToken = credentialService.getAccessToken();
		_client = new DbxClientV2(requestConfig, accessToken);
    }

	private DbxClientV2 getCustomClient(
		Config config) 
		throws Exception
	{
		var customRequestConfig = DbxRequestConfig.newBuilder("robotikflow-client")
            .withHttpRequestor(new StandardHttpRequestor(
				config != null?
					config:
					StandardHttpRequestor.Config.DEFAULT_INSTANCE))
            .build();		
       
		accessToken = credentialService.getAccessToken();
		_client = new DbxClientV2(customRequestConfig, accessToken);
		return _client;
	}

    private DbxClientV2 getClient() 
        throws Exception
    {
        var curAccessToken = credentialService.getAccessToken();
        if(!curAccessToken.equals(accessToken))
        {
            accessToken = curAccessToken;
            _client = new DbxClientV2(requestConfig, accessToken);
        }

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
		var items = new ArrayList<Map<String, Object>>();

		var longpollTimeoutSecs = TimeUnit.MINUTES.toSeconds(2);

		var dbConfig = StandardHttpRequestor.Config.DEFAULT_INSTANCE;
		var dbLongpollConfig = dbConfig.copy()
			.withReadTimeout(5, TimeUnit.MINUTES)
			.build();
		
		var client = getCustomClient(dbConfig);
		var longpollClient = getCustomClient(dbLongpollConfig);
	
		var cleanPath = path.replace("\\", "/").replace("'", "\\'");
		if(cleanPath.charAt(0) != '/')
		{
			cleanPath = "/" + cleanPath;
		}

		var cursor = state.get("cursor") == null? 
			getLatestCursor(client, cleanPath):
			(String)state.get("cursor");

		if(cursor != null) 
		{
			var res = longpollClient.files()
				.listFolderLongpoll(cursor, longpollTimeoutSecs);

			if(res.getChanges()) 
			{
				while (true) 
				{
					var result = client.files()
						.listFolderContinue(cursor);
					for(var meta : result.getEntries()) 
					{
						if (meta instanceof FileMetadata) 
						{
							var data = (FileMetadata)meta;
							var id = data.getId();
							var extension = DocumentUtil.getExtension(data.getName(), DocumentType.FILE);
							var size = data.getSize();
							var createdTime = data.getClientModified();
							var modifiedTime = data.getServerModified();

							var fileProps = new FileProps(
								provider,
								meta.getName(), 
								id, 
								meta.getPathDisplay(), 
								extension, 
								null, 
								size, 
								ZonedDateTime.ofInstant(createdTime.toInstant(), ZoneId.of("Z")), 
								modifiedTime != null && modifiedTime != createdTime? 
									ZonedDateTime.ofInstant(modifiedTime.toInstant(), ZoneId.of("Z")): 
									null, 
								null, 
								null);
	
							var files = new ArrayList<FileProps>() {{
								add(fileProps);
							}};
						
							var props = new HashMap<String, Object>() {{
								put("name", fileProps.name);
								put("id", fileProps.id);
								put("path", fileProps.path);
								put("extension", fileProps.extension);
								put("mimeType", fileProps.mimeType);
								put("size", fileProps.size);
								put("createdAt", fileProps.createdAt);
								put("modifiedAt", fileProps.modifiedAt);
								put("creator", fileProps.creator);
								put("url", fileProps.url);
								put("files", files);
							}};								

							items.add(props);
						}
					}
					
					cursor = result.getCursor();
		
					if (!result.getHasMore()) 
					{
						break;
					}
				}					
			}

			var backoff = res.getBackoff();
			if (backoff != null) 
			{
				try 
				{
					Thread.sleep(TimeUnit.SECONDS.toMillis(backoff));
				} 
				catch (InterruptedException ex) 
				{
				}
			}
		}
		
		state.put("cursor", cursor);

        return items;
	}

	private String getLatestCursor(
		final DbxClientV2 client, 
		final String path)
		throws Exception
	{
		return client.files()
			.listFolderGetLatestCursorBuilder(path)
				.withIncludeDeleted(false)
				.withIncludeMediaInfo(false)
				.withRecursive(false)
				.withIncludeNonDownloadableFiles(false)
			.start()
			.getCursor();
	}

	@Override
	public void createWorkspace(
		String idWorkspace, 
		boolean isPublic) 
	{
		try 
		{
			createFolder(idWorkspace, (String)null);
		} 
		catch(Exception e) 
		{
			logger.error("Workspace creation failed", e);
		}
	}

	@Override
	public DocProps createFolder(
		String name, 
		DocProps parent) 
	{
		try 
		{
			var res = createFolder(name, parent != null? parent.getPath(): null);

			return new DocProps(
				res.getId(), res.getPathDisplay(), "text/folder", null);
		} 
		catch (Exception e) 
		{
			logger.error("Folder creation failed", e);
			return null;
		}
	}

	private FolderMetadata createFolder(
		final String name, 
		final String parentPath) 
		throws Exception 
	{
		var path = (parentPath == null? "": parentPath) + "/" + name;
		return getClient()
			.files()
			.createFolder(path, true);
	}

	@Override
	public void destroyWorkspace(
		String idWorkspace) 
	{
	}

	@Override
	public void sendContents(
		Path fromPath, 
		String idWorkspace, 
		String idDocument, 
		String extension)
		throws StorageException 
	{
	}

	@Override
	public void sendContents(
		BufferedImage img, 
		String format, 
		String idWorkspace, 
		String idDocument)
		throws StorageException 
	{
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
		try 
		{
			var path = (parent != null? parent.getPath(): "") + "/" + idDocument;
			var res = getClient()
				.files()
				.uploadBuilder(path)
				.uploadAndFinish(stream);

			return new DocProps(
				res.getId(), res.getPathDisplay(), extensionToMimeType(extension), null);
		} 
		catch (Exception e) 
		{
			logger.error("File creation failed", e);
			return null;
		}
	}

	@Override
	public void copyContents(
		String idSourceWorkspace, 
		String idSourceDocument, 
		String idTargetWorkspace,
		String idTargetDocument) 
	{
	}

	@Override
	public String getDownloadUrl(
		String idWorkspace, 
		String idDocument, 
		String extension, 
		int expiresInSecs) 
	{
		return null;
	}

	@Override
	public String getDownloadUrl(
		DocumentExt doc, 
		int expiresInSecs) 
	{
		try {
			var res = getClient()
				.files()
				.getTemporaryLink(doc.getFileId());
			return res.getLink();
		} 
		catch (Exception e) 
		{
			return "";
		}
	}

	@Override
	public void deleteContents(
		String idWorkspace, 
		String idDocument) 
	{
	}

	@Override
	public byte[] receiveContents(
		String idWorkspace, 
		String idDocument) 
		throws StorageException 
	{
		return null;
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
		var path = Paths.get(doc.getFilePath());
		return String.format(
			"https://www.dropbox.com/home%s?preview=%s", 
			path.getParent().toString().replace("\\", "/"), 
			path.getFileName().toString());
	}
}