package com.robotikflow.core.services.log;

import java.time.ZonedDateTime;
import java.util.Map;

import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentLog;
import com.robotikflow.core.models.entities.WorkspaceLogType;
import com.robotikflow.core.models.repositories.DocumentLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class DocumentLogger 
{
	public final Logger logger = LoggerFactory.getLogger(DocumentLogger.class);
    @Autowired
    private DocumentLogRepository logRepo;

	private void log(
		final Document document, 
		final WorkspaceLogType type,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex) 
	{
		try
		{
			var l = new DocumentLog();
			l.setWorkspace(document.getWorkspace());
			l.setDocument(document);
			l.setType(type);
			var msg = ex == null || ex.getMessage() == null?
				message:
				String.format("%s. Cause: %s", message, ex.getMessage());
			l.setMessage(msg);
			l.setExtra(extra);
			l.setDate(ZonedDateTime.now());
			logRepo.save(l);
		}
		catch(Exception e)
		{
			logger.error("Falha ao gravar log do document {}", e);
		}
	}

	public void error(
		final Document document,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex)
	{
		log(document, WorkspaceLogType.ERROR, message, extra, ex);
	}

	public void error(
		final Document document,
		final String message,
		final Exception ex)
	{
		log(document, WorkspaceLogType.ERROR, message, null, ex);
	}

	public void error(
		final Document document,
		final String format,
		final Object... args)
	{
		log(document, WorkspaceLogType.ERROR, String.format(format, args), null, null);
	}	

	public void error(
		final Document document,
		final String message)
	{
		log(document, WorkspaceLogType.ERROR, message, null, null);
	}

	public void info(
		final Document document,
		final String message, 
		final Map<String, Object> extra)
	{
		log(document, WorkspaceLogType.INFO, message, extra, null);
	}

	public void info(
		final Document document,
		final String format,
		final Object... args)
	{
		log(document, WorkspaceLogType.INFO, String.format(format, args), null, null);
	}	

	public void info(
		final Document document,
		final String message)
	{
		log(document, WorkspaceLogType.INFO, message, null, null);
	}    
}