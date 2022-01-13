package com.robotikflow.core.services.log;

import java.time.ZonedDateTime;
import java.util.Map;

import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionLog;
import com.robotikflow.core.models.entities.WorkspaceLogType;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.repositories.CollectionLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class CollectionLogger 
{
	public final Logger logger = LoggerFactory.getLogger(CollectionLogger.class);
    @Autowired
    private CollectionLogRepository logRepo;

	private void log(
		final Collection collection, 
		final WorkspaceLogType type,
		final User user,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex) 
	{
		try
		{
			var l = new CollectionLog();
			l.setWorkspace(collection.getWorkspace());
			l.setCollection(collection);
			l.setType(type);
			var msg = ex == null || ex.getMessage() == null?
				message:
				String.format("%s. Cause: %s", message, ex.getMessage());
			l.setMessage(msg);
			l.setExtra(extra);
			l.setDate(ZonedDateTime.now());
			l.setUser(user);
			logRepo.save(l);
		}
		catch(Exception e)
		{
			logger.error("Falha ao grvar log da coleção {}", e);
		}
	}

	public void error(
		final Collection collection,
		final User user,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex)
	{
		log(collection, WorkspaceLogType.ERROR, user, message, extra, ex);
	}

	public void error(
		final Collection collection,
		final User user,
		final String message,
		final Exception ex)
	{
		log(collection, WorkspaceLogType.ERROR, user, message, null, ex);
	}

	public void error(
		final Collection collection,
		final User user,
		final String format,
		final Object... args)
	{
		log(collection, WorkspaceLogType.ERROR, user, String.format(format, args), null, null);
	}	

	public void error(
		final Collection collection,
		final User user,
		final String message)
	{
		log(collection, WorkspaceLogType.ERROR, user, message, null, null);
	}

	public void info(
		final Collection collection,
		final User user,
		final String message, 
		final Map<String, Object> extra)
	{
		log(collection, WorkspaceLogType.INFO, user, message, extra, null);
	}

	public void info(
		final Collection collection,
		final User user,
		final String format,
		final Object... args)
	{
		log(collection, WorkspaceLogType.INFO, user, String.format(format, args), null, null);
	}	

	public void info(
		final Collection collection,
		final User user,
		final String message)
	{
		log(collection, WorkspaceLogType.INFO, user, message, null, null);
	}    
}