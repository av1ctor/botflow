package com.robotikflow.core.services.log;

import java.time.ZonedDateTime;
import java.util.Map;

import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.WorkspaceLog;
import com.robotikflow.core.models.entities.WorkspaceLogType;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.repositories.WorkspaceLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class WorkspaceLogger 
{
	public final Logger logger = LoggerFactory.getLogger(WorkspaceLogger.class);
    @Autowired
    private WorkspaceLogRepository logRepo;

	private void log(
		final Workspace workspace, 
		final WorkspaceLogType type,
		final User user,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex) 
	{
		try
		{
			var l = new WorkspaceLog();
			l.setWorkspace(workspace);
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
			logger.error("Falha ao gravar log do área de trabalho {}", e);
		}
	}

	public void error(
		final Workspace workspace,
		final User user,
		final String message, 
		final Map<String, Object> extra,
		final Exception ex)
	{
		log(workspace, WorkspaceLogType.ERROR, user, message, extra, ex);
	}

	public void error(
		final Workspace workspace,
		final User user,
		final String message,
		final Exception ex)
	{
		log(workspace, WorkspaceLogType.ERROR, user, message, null, ex);
	}

	public void error(
		final Workspace workspace,
		final User user,
		final String format,
		final Object... args)
	{
		log(workspace, WorkspaceLogType.ERROR, user, String.format(format, args), null, null);
	}	

	public void error(
		final Workspace workspace,
		final User user,
		final String message)
	{
		log(workspace, WorkspaceLogType.ERROR, user, message, null, null);
	}

	public void info(
		final Workspace workspace,
		final User user,
		final String message, 
		final Map<String, Object> extra)
	{
		log(workspace, WorkspaceLogType.INFO, user, message, extra, null);
	}

	public void info(
		final Workspace workspace,
		final User user,
		final String format,
		final Object... args)
	{
		log(workspace, WorkspaceLogType.INFO, user, String.format(format, args), null, null);
	}	

	public void info(
		final Workspace workspace,
		final User user,
		final String message)
	{
		log(workspace, WorkspaceLogType.INFO, user, message, null, null);
	}    
}