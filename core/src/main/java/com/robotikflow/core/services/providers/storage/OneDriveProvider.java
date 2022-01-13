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
import java.util.concurrent.CompletableFuture;

import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.models.extensions.Folder;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.DriveItemDeltaCollectionPage;
import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.interfaces.props.FileProps;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.util.DocumentUtil;

import org.springframework.web.util.UriComponentsBuilder;

public class OneDriveProvider 
	extends StorageBaseProvider
{
    public static final String name = "oneDriveProvider";

    private IGraphServiceClient client;
    
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		super.initialize(provider);

		var credentialService = credentialServiceFactory.buildByPubId(
            provider.getFields().getString("credential"), 
            provider.getWorkspace());

        client = (IGraphServiceClient)credentialService.getClient();
    }

    @Override
	public List<Map<String, Object>> sync(
		final String path, 
		final DocumentOperationType op, 
		final Workspace workspace,
		final Map<String, Object> state) 
		throws Exception 
	{
		if(!checkParentId(path, state))
		{
			return null;
		}

		if(state.get("cursor") == null)
		{
			var res = client.me()
				.drive()
					.root()
						.delta()
				.buildRequest(Arrays.asList(new QueryOption("token", "latest")))
				.get();
			
			state.put("cursor", getTokenFromDeltaLink(res.deltaLink()));

			return null;
		}

		var items = new ArrayList<Map<String, Object>>();

		var page = client.me()
			.drive()
				.root()
					.delta()
			.buildRequest(Arrays.asList(new QueryOption("token", state.get("cursor"))))
			.get();

		while(page != null)
		{
			var files = page.getCurrentPage();
			if(files != null)
			{
				for(var file : files)
				{
					if(file.file != null &&
						file.parentReference != null &&
						file.parentReference.id.equals(state.get("parentId")))
					{
						var fileProps = new FileProps(
							provider,
							file.name, 
							file.id, 
							null, 
							DocumentUtil.getExtension(file.name, DocumentType.FILE), 
							file.file.mimeType, 
							file.size, 
							ZonedDateTime.ofInstant(file.createdDateTime.toInstant(), ZoneId.of("Z")), 
							file.lastModifiedDateTime != null? 
								ZonedDateTime.ofInstant(file.lastModifiedDateTime.toInstant(), ZoneId.of("Z")): 
								null, 
							null, 
							null);

						var files_ = new ArrayList<FileProps>() {{
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
							put("files", files_);
						}};								

						items.add(props);					
					}
				}
			}

			var delta = page.deltaLink();
			if(delta != null)
			{
				state.put("cursor", getTokenFromDeltaLink(delta));
				break;
			}
				
			page = (DriveItemDeltaCollectionPage)page
				.getNextPage()
				.buildRequest()
				.get();
		}

        return items;
	}

	private boolean checkParentId(
		final String path, 
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

		String parentId = null;
		var dirs = cleanPath.split("/");
		for(var dir : dirs)
		{
			if(dir.length() == 0)
			{
				continue;
			}
			
			var options = dir.length() > 0? 
				Arrays.asList(
					new QueryOption(
						"$filter", 
						String.format("name eq '%s'", dir)
					)):
				null;

			var res = parentId == null?
				client.me()
					.drive()
						.root()
					.children()
					.buildRequest(options)
					.get():
				client.me()
					.drive()
						.items(parentId)
					.children()
					.buildRequest(options)
					.get();

			var files = res.getCurrentPage();
			if(files == null || files.size() == 0)
			{
				return false;
			}
			parentId = files.get(0).id;
		}

		state.put("parentId", parentId);
		return true;
	}

	private String getTokenFromDeltaLink(
		final String url) 
	{
		return UriComponentsBuilder
			.fromUriString(url).build().getQueryParams().get("token").get(0);
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

			return new DocProps(res.id, "text/folder");
		} 
		catch (Exception e) 
		{
			logger.error("Folder creation failed", e);
			return null;
		}
	}

	private DriveItem createFolder(
		final String name, 
		final String parent) 
	{
		var req = new DriveItem();
		req.name = name;
		req.folder = new Folder();

		var drive = parent == null?
			client
				.me()
					.drive()
						.root():
			client
				.me()
					.drive()
						.items(parent);

		var res = drive
			.children()
			.buildRequest()
			//.select("id") -- the SDK has a bug since 2018...
			.post(req);        
		
		return res;
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
			var props = new DriveItemUploadableProperties();
			//props.description = "";
			props.name = idDocument;
			props.oDataType = "microsoft.graph.driveItemUploadableProperties";			

			var drive = parent == null?
				client.me().drive().root():
				client.me().drive().items(parent.getId());

			var session = drive
				.itemWithPath(idDocument)
				.createUploadSession(props)
				.buildRequest()
				.post();

			var chunkedUploadProvider = new ChunkedUploadProvider<DriveItem>(
				session, 
				client, 
				stream, 
				size, 
				DriveItem.class);			

			var future = new CompletableFuture<DriveItem>();

			var callback = new IProgressCallback<DriveItem> () 
			{
				@Override
				public void progress(final long current, final long max) {
				}
				@Override
				public void success(final DriveItem item) {
					future.complete(item);
				}
		
				@Override
				public void failure(final ClientException ex) {
				}
			};				

			chunkedUploadProvider.upload(callback);

			var res = future.join();

			return new DocProps(res.id, res.file.mimeType);		
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
		var res = client
			.me()
				.drive()
					.items(doc.getFileId())
			.buildRequest()
			.select("@content.downloadUrl")
			.get();

		return res.additionalDataManager()
			.get("@microsoft.graph.downloadUrl")
				.getAsString();
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
		var res = client
			.me()
			.drive()
				.items(doc.getFileId())
					.createLink("view", "anonymous")
			.buildRequest()
			.post();

		return res.link.webUrl;
	}
}