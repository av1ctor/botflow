package com.robotikflow.core.models.queue;

public enum MessageType
{
	// docs
	DOC_INT_CREATED,
	DOC_INT_UPDATED,
	DOC_INT_DELETED,
	DOC_INT_COPIED,
	DOC_EXT_CREATED,
	// collections
	COLLECTION_UPDATED,
	// activities
	ACTIVITY_TRIGGERED,
	// integrations
	INTEGRATION_TRIGGERED,
	// messengers
	MESSENGER_EMAIL,
	MESSENGER_SMS,
	MESSENGER_WHATSAPP,
}

