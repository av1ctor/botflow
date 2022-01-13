package com.robotikflow.core.services.providers.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;

public class LocalStorageProvider 
	extends StorageBaseProvider
{
    public static final String name = "localStorageProvider";
    
	private String baseUrl;
	private Path rootPath;

    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		super.initialize(provider);
		
		var fields = provider.getFields();
		
		baseUrl = (String)fields.get("baseUrl");
		
		var baseDir = (String)fields.get("basePath");
		if(baseDir == null)
		{
			rootPath = null;
			return;
		}
		
		rootPath = Paths.get(baseDir).toAbsolutePath().normalize();
		
		try
		{
			Files.createDirectories(rootPath);
		}
		catch (Exception e) 
		{
			throw new StorageException("Não foi possível criar o diretório para armazenar os arquivos", e);
		}
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
		if(rootPath == null)
		{
			return;
		}
		
		try 
		{
			var target = rootPath.resolve(idWorkspace);
			Files.createDirectories(target);		
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
		var path = rootPath.resolve(idWorkspace);
		
		try 
		{
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() 
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException 
				{
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException 
				{
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} 
		catch (IOException e) 
		{
			throw new StorageException(String.format("Workspace destruction failed: %s", idWorkspace), e);
		}	
	}

	@Override
	public void sendContents(
		Path fromPath, 
		String idWorkspace, 
		String idDocument, 
		String extension)
		throws StorageException 
	{
		var target = rootPath.resolve(String.format("%s/%s", idWorkspace, idDocument));
		try 
		{
			Files.createDirectories(target.getParent());
			copy(fromPath, target);
		} 
		catch (IOException e) 
		{
			throw new StorageException(String.format("Blob creation failed: %s", idDocument), e);
		}
	}

	private void copy(
		final Path from, 
		final Path to) 
		throws IOException 
	{
		var src = from.normalize();
		var dst = to.normalize();
		
		Files.copy(src, dst);
	}

	@Override
	public void sendContents(
		BufferedImage img, 
		String format, 
		String idWorkspace, 
		String idDocument)
		throws StorageException 
	{
		var target = rootPath.resolve(String.format("%s/%s", idWorkspace, idDocument));
		
		try 
		{
			Files.createDirectories(target.getParent());
			var file = Files.createFile(target);
			ImageIO.write(img, format, file.toFile());
		} 
		catch (IOException e) 
		{
			throw new StorageException(String.format("Blob creation failed: %s", idDocument), e);
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
		try
		{
			copy(
				rootPath.resolve(String.format("%s/%s", idSourceWorkspace, idSourceDocument)), 
				rootPath.resolve(String.format("%s/%s", idTargetWorkspace, idTargetDocument)));
		}
		catch(Exception e)
		{
			throw new StorageException(String.format("File copy failed: %s", idSourceDocument, e));
		}
	}

	@Override
	public String getDownloadUrl(
		String idWorkspace, 
		String idDocument, 
		String extension, 
		int expiresInSecs) 
	{
		return String.format("%s/%s/%s", baseUrl, idWorkspace, idDocument); 
	}

	@Override
	public String getDownloadUrl(
		DocumentExt doc, 
		int expiresInSecs) 
	{
		return null;
	}

	@Override
	public void deleteContents(
		String idWorkspace, 
		String idDocument) 
	{
		try 
		{
			delete(rootPath.resolve(String.format("%s/%s", idWorkspace, idDocument)));
		} 
		catch (Exception e) 
		{
			throw new StorageException(String.format("File deletion failed: %s", idDocument), e);
		}
	}

	private void delete(
		final Path path) 
		throws IOException
	{
		var src = path.normalize();
		
		Files.deleteIfExists(src);
	}

	@Override
	public byte[] receiveContents(
		String idWorkspace, 
		String idDocument) 
		throws StorageException 
	{
		try 
		{
			return Files.readAllBytes(rootPath.resolve(String.format("%s/%s", idWorkspace, idDocument)));
		} 
		catch (Exception e) 
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
		return null;
	}
}