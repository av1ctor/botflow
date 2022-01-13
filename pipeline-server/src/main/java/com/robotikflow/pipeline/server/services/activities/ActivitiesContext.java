package com.robotikflow.pipeline.server.services.activities;

import com.robotikflow.core.factories.ActivityServiceFactory;
import com.robotikflow.core.models.repositories.ActivityRepository;
import com.robotikflow.core.models.repositories.ActivitySchemaRepository;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.core.services.log.CollectionLogger;
import com.robotikflow.core.services.queue.QueueService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivitiesContext 
{
    @Autowired
    private QueueService activitiesQueueService;
	@Autowired
	private ActivityServiceFactory activityServiceFactory;
	@Autowired
	private ActivityRepository activityRepository;
	@Autowired
	private ActivitySchemaRepository activitySchemaRepository;
	@Autowired
	private CollectionService collectionService;
	@Autowired
	private CollectionLogger collectionLogger;

	public QueueService getActivitiesQueueService() {
		return activitiesQueueService;
	}

	public CollectionLogger getCollectionLogger() {
		return collectionLogger;
	}

	public CollectionService getCollectionService() {
		return collectionService;
	}

	public ActivityServiceFactory getActivityServiceFactory() {
		return activityServiceFactory;
	}

	public ActivityRepository getActivityRepository() {
		return activityRepository;
	}

	public ActivitySchemaRepository getActivitySchemaRepository() {
		return activitySchemaRepository;
	}
}
