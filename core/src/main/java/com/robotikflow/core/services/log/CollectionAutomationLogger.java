package com.robotikflow.core.services.log;

import java.time.ZonedDateTime;
import java.util.Map;

import com.robotikflow.core.models.entities.CollectionAutomation;
import com.robotikflow.core.models.entities.CollectionAutomationLog;
import com.robotikflow.core.models.entities.WorkspaceLogType;
import com.robotikflow.core.models.repositories.CollectionAutomationLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class CollectionAutomationLogger 
{
	public final Logger logger = LoggerFactory.getLogger(CollectionAutomationLogger.class);
    @Autowired
    private CollectionAutomationLogRepository logRepo;

	private void log(
		final CollectionAutomation auto, 
		final WorkspaceLogType type,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex) 
	{
		try
		{
			var l = new CollectionAutomationLog();
			l.setWorkspace(auto.getCollection().getWorkspace());
			l.setCollection(auto.getCollection());
			l.setAutomation(auto);
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
			logger.error("Falha ao gravar log de automação de coleção {}", e);
		}
	}

	public void error(
		final CollectionAutomation auto,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex)
	{
		log(auto, WorkspaceLogType.ERROR, message, extra, ex);
	}

	public void error(
		final CollectionAutomation auto,
		final String message,
		final Exception ex)
	{
		log(auto, WorkspaceLogType.ERROR, message, null, ex);
	}

	public void error(
		final CollectionAutomation auto,
		final String format,
		final Object... args)
	{
		log(auto, WorkspaceLogType.ERROR, String.format(format, args), null, null);
	}	

	public void error(
		final CollectionAutomation auto,
		final String message)
	{
		log(auto, WorkspaceLogType.ERROR, message, null, null);
	}

	public void info(
		final CollectionAutomation auto,
		final String message, 
		final Map<String, Object> extra)
	{
		log(auto, WorkspaceLogType.INFO, message, extra, null);
	}

	public void info(
		final CollectionAutomation auto,
		final String format,
		final Object... args)
	{
		log(auto, WorkspaceLogType.INFO, String.format(format, args), null, null);
	}	

	public void info(
		final CollectionAutomation auto,
		final String message)
	{
		log(auto, WorkspaceLogType.INFO, message, null, null);
	}    
}