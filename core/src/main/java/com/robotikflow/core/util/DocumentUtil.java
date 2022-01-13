package com.robotikflow.core.util;

import java.time.format.DateTimeFormatter;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentInt;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.ProviderRepository;

@Component
public class DocumentUtil 
{
	private static String thumbAddrFormat;
	private static String previewAddrFormat;
	public static DateTimeFormatter datePattern = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	@Autowired
	private ProviderRepository providerRepo;

	@Autowired
	public DocumentUtil(
		Environment env)
	{
		var hostAddr = env.getProperty("cloud.host-addr");
		if(hostAddr != null)
		{
			thumbAddrFormat = hostAddr + "/%s-pub/%s.jpg";
			previewAddrFormat = hostAddr + "/%s-pub/%s.jpg";
		}
	}
	
	public static String getExtension(
		final String name, 
		final DocumentType type) 
	{
		if(type == DocumentType.FOLDER)
		{
			return null;
		}
		
		return name.indexOf('.') < 0? 
			null: 
			name.split("\\.(?=[^\\.]+$)")[1];
	}
	
	public static String getThumbUrl(
		final DocumentInt document)
	{
		if(document.getThumbId() == null)
		{
			return "";
		}
		
		return String.format(
			thumbAddrFormat, 
			document.getWorkspace().getPubId(), 
			document.getThumbId());
	}

	public static String getPreviewUrl(
		final DocumentInt document)
	{
		if(document.getPreviewId() == null)
		{
			return "";
		}
		
		return String.format(
			previewAddrFormat, 
			document.getWorkspace().getPubId(), 
			document.getPreviewId());
	}

	public static String getBlobId(
		final Document document) 
	{
		return DocumentUtil.toInterno(document).getBlobId();
	}

	public static boolean isInterno(
		final Document document) 
	{
		if(document instanceof DocumentInt)
			return true;
		
		if(document instanceof HibernateProxy && 
			DocumentInt.class.isAssignableFrom(Hibernate.getClass(document)))
			return true;

		return false;
	}

	public static DocumentInt toInterno(
		final Document document)
	{
		if(document == null)
			return null;

		if(document instanceof DocumentInt)
			return (DocumentInt)document;

		return (DocumentInt)Hibernate.unproxy(document);
	}

	public static DocumentExt toExterno(
		final Document document)
	{
		if(document == null)
			return null;
			
		if(document instanceof DocumentExt)
			return (DocumentExt)document;

		return (DocumentExt)Hibernate.unproxy(document);
	}

	public static String getPreviewUrl(
		final DocumentExt document) 
	{
		switch(document.getProvider().getSchema().getName())
		{
		case "driveProvider":
			return String.format("https://drive.google.com/file/d/%s/preview", document.getFileId());
		default:
			return null;
		}
	}

	@Cacheable(value = "internalStorageProviders", key = "{#workspace.id}")
	public Provider getInternalProvider(
		final Workspace workspace
	)
	{
		return providerRepo.findInternalStorage(workspace);
	}
}