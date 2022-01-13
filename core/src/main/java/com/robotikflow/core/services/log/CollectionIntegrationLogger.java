package com.robotikflow.core.services.log;

import java.time.ZonedDateTime;
import java.util.Map;

import com.robotikflow.core.models.entities.CollectionIntegration;
import com.robotikflow.core.models.entities.CollectionIntegrationLog;
import com.robotikflow.core.models.entities.WorkspaceLogType;
import com.robotikflow.core.models.repositories.CollectionIntegrationLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class CollectionIntegrationLogger 
{
    private static Logger logger = LoggerFactory.getLogger(CollectionIntegrationLogger.class);
    @Autowired
    private CollectionIntegrationLogRepository logRepo;

	private void log(
		final CollectionIntegration integ, 
		final WorkspaceLogType type,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex) 
	{
		try
		{
			var l = new CollectionIntegrationLog();
			l.setWorkspace(integ.getCollection().getWorkspace());
			l.setCollection(integ.getCollection());
			l.setIntegration(integ);
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
			logger.error("Falha ao gravar log de integração de coleção {}", e);
		}
	}

	public void error(
		final CollectionIntegration integ,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex)
	{
		log(integ, WorkspaceLogType.ERROR, message, extra, ex);
	}

	public void error(
		final CollectionIntegration integ,
		final String message,
		final Exception ex)
	{
		log(integ, WorkspaceLogType.ERROR, message, null, ex);
	}

	public void error(
		final CollectionIntegration integ,
		final String format,
		final Object... args)
	{
		log(integ, WorkspaceLogType.ERROR, String.format(format, args), null, null);
	}	

	public void error(
		final CollectionIntegration integ,
		final String message)
	{
		log(integ, WorkspaceLogType.ERROR, message, null, null);
	}

	public void info(
		final CollectionIntegration integ,
		final String message, 
		final Map<String, Object> extra)
	{
		log(integ, WorkspaceLogType.INFO, message, extra, null);
	}

	public void info(
		final CollectionIntegration integ,
		final String format,
		final Object... args)
	{
		log(integ, WorkspaceLogType.INFO, String.format(format, args), null, null);
	}	

	public void info(
		final CollectionIntegration integ,
		final String message)
	{
		log(integ, WorkspaceLogType.INFO, message, null, null);
	}    
}