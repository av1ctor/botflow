package com.robotikflow.core.models.queue;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = DocIntCreatedMessage.class, name = "DOC_INT_CREATED"),
        @JsonSubTypes.Type(value = DocIntDeletedMessage.class, name = "DOC_INT_DELETED"),
        @JsonSubTypes.Type(value = DocIntUpdatedMessage.class, name = "DOC_INT_UPDATED"),
        @JsonSubTypes.Type(value = DocIntCopiedMessage.class, name = "DOC_INT_COPIED"),
		@JsonSubTypes.Type(value = DocExtCreatedMessage.class, name = "DOC_EXT_CREATED"),
		@JsonSubTypes.Type(value = IntegrationTriggeredMessage.class, name = "INTEGRATION_TRIGGERED"),
		@JsonSubTypes.Type(value = ActivityTriggeredMessage.class, name = "ACTIVITY_TRIGGERED"),
		@JsonSubTypes.Type(value = CollectionUpdatedMessage.class, name = "COLLECTION_UPDATED"),
		@JsonSubTypes.Type(value = EmailMessengerMessage.class, name = "MESSENGER_EMAIL"),
})
public abstract class Message 
{
	@NotNull
	protected MessageType type;
	
	public Message()
	{
	}
	
	public Message(MessageType type)
	{
		this.type = type;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}
}
