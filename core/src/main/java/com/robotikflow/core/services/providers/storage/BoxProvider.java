package com.robotikflow.core.services.providers.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxEvent;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxFolder.Info;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxSearch;
import com.box.sdk.BoxSearchParameters;
import com.box.sdk.BoxSharedLink;
import com.box.sdk.EventListener;
import com.box.sdk.EventStream;
import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.factories.CredentialServiceFactory;
import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.interfaces.props.FileProps;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;

import org.springframework.beans.factory.annotation.Autowired;

public class BoxProvider 
	extends StorageBaseProvider
{
    public static final String name = "boxProvider";

    @Autowired
    private CredentialServiceFactory credentialFactory;

    private ICredentialService credentialService;
    private BoxAPIConnection _client;
    
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		super.initialize(provider);
		
        credentialService = credentialFactory.buildByPubId(
            provider.getFields().getString("credential"), 
            provider.getWorkspace());

        _client = new BoxAPIConnection(credentialService.getAccessToken());
        _client.setAutoRefresh(false);
    }

    private BoxAPIConnection getClient() 
        throws Exception
    {
		_client.setAccessToken(credentialService.getAccessToken());
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

		var pos = state.get("lastId") == null? 
			-1L: //STREAM_POSITION_NOW
			Long.parseLong((String)state.get("lastId"));

		var latch = new CountDownLatch(1);

		var stream = new EventStream(client, pos);
		stream.addListener(new EventListener() 
		{
			@Override
			public void onEvent(BoxEvent event) 
			{
				switch(event.getType())
				{
				case ITEM_CREATE:
				case ITEM_UPLOAD:
				{
					var resource = event.getSourceInfo().getResource();
					if(resource instanceof BoxFile)
					{
						var file = new BoxFile(client, resource.getID());
						var info = file.getInfo();
						if(info.getParent().getID().equals(state.get("parentId")))
						{
							var name = info.getName();
							var createdAt = info.getCreatedAt().toInstant();
							var modifiedAt = info.getModifiedAt() != null?
								info.getModifiedAt().toInstant():
								null;

							var fileProps = new FileProps(
								provider,
								name, 
								file.getID(), 
								null, 
								info.getExtension(), 
								null, 
								info.getSize(), 
								ZonedDateTime.ofInstant(createdAt, ZoneId.of("Z")), 
								modifiedAt != null?
									ZonedDateTime.ofInstant(modifiedAt, ZoneId.of("Z")):
									null, 
								info.getCreatedBy().getLogin(), 
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
					break;
				}

				default:
					break;
				}
			}

			@Override
			public void onNextPosition(long position) 
			{
				state.put("lastId", Long.toString(position));
			}

			@Override
			public boolean onException(Throwable e) 
			{
				latch.countDown();
				return true;
			}
		});

		stream.start();
		latch.await();

        return items;
	}

	private boolean checkParentId(
		final String path, 
        final BoxAPIConnection client,
		final Map<String, Object> state) 
		throws Exception
	{
		if(state.get("parentId") != null)
		{
			return true;
		}
			
		var cleanPath = path
			.replace("\\", "/")
			.replace("'", "\\'");

		var infos = new BoxItem.Info[1];
		String parentId = null;
		var dirs = cleanPath.split("/");
		for(var dir : dirs)
		{
			var bsp = new BoxSearchParameters();
			bsp.setAncestorFolderIds(Arrays.asList(
				parentId == null? BoxFolder.getRootFolder(client).getID(): parentId));
			bsp.setContentTypes(Arrays.asList("name"));
			bsp.setQuery(String.format("\"%s\"", dir));
			bsp.setType("folder");
			bsp.setTrashContent("non_trashed_only");
			bsp.setFields(Arrays.asList("id"));
			
			var res = new BoxSearch(client).searchRange(0, 1, bsp);
			if(res == null || res.size() != 1)
			{
				return false;
			}

			parentId = res.toArray(infos)[0].getID();
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
			var res = createFolder(name, parent != null? parent.getId(): null);

			return new DocProps(
				res.getID(), "text/folder");
		} 
		catch (Exception e) 
		{
			logger.error("Folder creation failed", e);
			return null;
		}
	}

	private Info createFolder(
		final String name, 
		final String parentId) 
	{
		try 
		{
			var parentFolder = parentId == null? 
				BoxFolder.getRootFolder(getClient()): 
				new BoxFolder(getClient(), parentId);

			return parentFolder.createFolder(name);
		} 
		catch (Exception e) {
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
			var parentFolder = parent == null? 
				BoxFolder.getRootFolder(getClient()): 
				new BoxFolder(getClient(), parent.getId());

			var res = size <= 5*1024*1024? 
				parentFolder.uploadFile(stream, idDocument):
				parentFolder.uploadLargeFile(stream, idDocument, size);

			return new DocProps(
				res.getID(), null, extensionToMimeType(extension), null);
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
		try
		{
			var file = new BoxFile(getClient(), doc.getFileId());
			var permissions = new BoxSharedLink.Permissions();
			permissions.setCanDownload(true);
			permissions.setCanPreview(false);
			var res = file.createSharedLink(
				BoxSharedLink.Access.OPEN, 
				//FIXME: SDK bug: unshare_date is being passed as toString()
				null /*Date.from(ZonedDateTime.now().plusMinutes(15).toInstant())*/, 
				permissions);
			return res.getDownloadURL();
		}
		catch(Exception e)
		{
			logger.error("URL generation failed", e);
			return null;
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
		try
		{
			var file = new BoxFile(getClient(), doc.getFileId());
			var res = file.getPreviewLink();
			return res.toString();
		}
		catch(Exception e)
		{
			logger.error("Preview generation failed", e);
			return null;
		}
	}
}