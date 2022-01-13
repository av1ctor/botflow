package com.robotikflow.core.services.log;

import java.time.ZonedDateTime;
import java.util.Map;

import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionItemLog;
import com.robotikflow.core.models.entities.WorkspaceLogType;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.repositories.CollectionItemLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class CollectionItemLogger 
{
	public final Logger logger = LoggerFactory.getLogger(CollectionItemLogger.class);
    @Autowired
    private CollectionItemLogRepository logRepo;

	private void log(
		final Collection collection,
		final Object itemId,
		final WorkspaceLogType type,
		final User user,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex) 
	{
		try
		{
			var l = new CollectionItemLog();
			l.setWorkspace(collection.getWorkspace());
			l.setCollection(collection);
			l.setItemId((String)itemId);
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
			logger.error("Falha ao gravar log de item de coleção {}", e);
		}
	}

	public void error(
		final Collection collection,
		final Object itemId,
		final User user,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex)
	{
		log(collection, itemId, WorkspaceLogType.ERROR, user, message, extra, ex);
	}

	public void error(
		final Collection collection,
		final Object itemId,
		final User user,
		final String message,
		final Exception ex)
	{
		log(collection, itemId, WorkspaceLogType.ERROR, user, message, null, ex);
	}

	public void error(
		final Collection collection,
		final Object itemId,
		final User user,
		final String format,
		final Object... args)
	{
		log(collection, itemId, WorkspaceLogType.ERROR, user, String.format(format, args), null, null);
	}	

	public void error(
		final Collection collection,
		final Object itemId,
		final User user,
		final String message)
	{
		log(collection, itemId, WorkspaceLogType.ERROR, user, message, null, null);
	}

	public void info(
		final Collection collection,
		final Object itemId,
		final User user,
		final String message, 
		final Map<String, Object> extra)
	{
		log(collection, itemId, WorkspaceLogType.INFO, user, message, extra, null);
	}

	public void info(
		final Collection collection,
		final Object itemId,
		final User user,
		final String format,
		final Object... args)
	{
		log(collection, itemId, WorkspaceLogType.INFO, user, String.format(format, args), null, null);
	}	

	public void info(
		final Collection collection,
		final Object itemId,
		final User user,
		final String message)
	{
		log(collection, itemId, WorkspaceLogType.INFO, user, message, null, null);
	}    
}