package com.robotikflow.core.services.providers.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.interfaces.props.FileProps;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.util.DocumentUtil;

public class DriveProvider 
	extends StorageBaseProvider
{
    public static final String name = "driveProvider";

    private ICredentialService credentialService;
    private Drive _client;
    
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		super.initialize(provider);
		
        credentialService = credentialServiceFactory.buildByPubId(
            provider.getFields().getString("credential"), 
            provider.getWorkspace());

        var googleCredential = (GoogleCredential)credentialService.getClient();
       
        _client = new Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), 
            JacksonFactory.getDefaultInstance(), 
            googleCredential)
			.setApplicationName("RobotikFlow")
            .build();		
    }

    private Drive getClient() 
        throws Exception
    {
       credentialService.authenticate();
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
		var client = getClient();
		
		if(!checkParentId(path, client, state))
		{
			return null;
		}

		var items = new ArrayList<Map<String, Object>>();

		var lastModifiedTime = (String)state.get("lastModifiedTime");
		var iniTime = lastModifiedTime != null?
			ZonedDateTime.parse(lastModifiedTime):
			null;
		var endTime = ZonedDateTime.now();
		if(iniTime != null)
		{
			var time = op == DocumentOperationType.CREATED? 
				"createdTime": 
				"modifiedTime";
			String pageToken = null;
			do
			{
				var query = String.format(
					"mimeType != 'application/vnd.google-apps.folder' and (%s >= '%s' and %s < '%s') and trashed = false and '%s' in parents", 
					time, 
					iniTime.format(DateTimeFormatter.ISO_INSTANT), 
					time, 
					endTime.format(DateTimeFormatter.ISO_INSTANT), 
					state.get("parentId"));
				
				var res = client
					.files().list()
					.setSpaces("drive")
					.setQ(query)
					.setPageToken(pageToken)
					.setFields("nextPageToken, files(id, name, fileExtension, createdTime, modifiedTime, size, owners, webContentLink, mimeType)")
					.execute();

				if(res.getFiles() != null)
				{
					for(var file : res.getFiles())
					{
						var fileProps = new FileProps(
							provider,
							file.getName(), 
							file.getId(), 
							null, 
							file.getFileExtension(), 
							file.getMimeType(), 
							file.getSize(), 
							ZonedDateTime.parse(file.getCreatedTime().toStringRfc3339()), 
							file.getModifiedTime() != null? 
								ZonedDateTime.parse(file.getModifiedTime().toStringRfc3339()): 
								null, 
							file.getOwners().get(0).getEmailAddress(), 
							file.getWebContentLink());

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
				
				pageToken = res.getNextPageToken();
			} while(pageToken != null);
		}

		state.put("lastModifiedTime", endTime.toString());

        return items;
	}

	private boolean checkParentId(
		final String path, 
        final Drive client,
		final Map<String, Object> state) 
		throws Exception
	{
		if(state.get("parentId") != null)
		{
			return true;
		}

		var dirs = path.replace("\\", "/").replace("'", "\\'").split("/");

		var parentId = "root";
		for(var dir : dirs)
		{
			if(dir.length() == 0)
			{
				continue;
			}
			
			var res = client
				.files().list()
				.setSpaces("drive")
				.setQ(String.format("mimeType = 'application/vnd.google-apps.folder' and name = '%s' and trashed = false and '%s' in parents", dir, parentId))
				.setFields("nextPageToken, files(id, name, parents)")
				.execute();

			var files = res.getFiles();
			if(files == null || files.size() == 0)
			{
				return false;
			}
			parentId = files.get(0).getId();
		}

		state.put("parentId", parentId);
		return true;
	}

	@Override
	public void createWorkspace(
		String idWorkspace, 
		boolean isPublic) 
	{
		try 
		{
			var file = new File();
			file.setName(idWorkspace);
			file.setMimeType("application/vnd.google-apps.folder");
			
			getClient()
				.files()
					.create(file)
				.execute();
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
		try 
		{
			var file = new File();
			file.setName(name);
			file.setMimeType("application/vnd.google-apps.folder");
			if(target != null)
			{
				file.setParents(Arrays.asList(target.getId()));
			}
			
			var res = getClient()
				.files()
					.create(file)
				.setFields("id, webContentLink, mimeType")
				.execute();
			
			return new DocProps(res.getId(), res.getMimeType(), res.getWebViewLink());
		} 
		catch (Exception e) 
		{
			logger.error("Folder creation failed", e);
			return null;
		}
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
			var mimeType = extensionToMimeType(extension);

			var file = new File();
			file.setName(idDocument);
			file.setMimeType(mimeType);
			if(parent != null)
			{
				file.setParents(Arrays.asList(parent.getId()));
			}

			var contents = new InputStreamContent(mimeType, stream);
			
			var res = getClient()
				.files()
					.create(file, contents)
				.setFields("id, webContentLink, mimeType")
				.execute();
			
			return new DocProps(res.getId(), res.getMimeType(), res.getWebContentLink());
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
		return doc.getUrlDownload();
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
		return DocumentUtil.getPreviewUrl(doc);
	}
}