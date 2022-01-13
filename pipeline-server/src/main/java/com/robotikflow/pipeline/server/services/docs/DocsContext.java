package com.robotikflow.pipeline.server.services.docs;

import com.robotikflow.core.services.queue.QueueService;
import com.robotikflow.pipeline.server.services.docs.external.DocExtContext;
import com.robotikflow.pipeline.server.services.docs.internal.DocIntContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DocsContext 
{
	@Autowired
	private QueueService docsQueueService;
	@Autowired
	private DocIntContext docIntContext;
	@Autowired
	private DocExtContext docExtContext;

	public QueueService getDocsQueueService() {
		return docsQueueService;
	}

	public DocIntContext getDocIntContext() {
		return docIntContext;
	}

	public DocExtContext getDocExtContext() {
		return docExtContext;
	}
}
