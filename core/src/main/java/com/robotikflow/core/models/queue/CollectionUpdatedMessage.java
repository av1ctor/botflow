package com.robotikflow.core.models.queue;

import java.time.ZonedDateTime;

public class CollectionUpdatedMessage 
    extends Message 
{
    private final String event;
    private final ZonedDateTime date;
    private final String origin;
    private final String schema;
    private final Object extra;

    public CollectionUpdatedMessage(
        CollectionUpdatedEvent event, 
        String origin, 
        String schema, 
        Object extra) 
    {
        this.type = MessageType.COLLECTION_UPDATED;
        this.event = event.toString();
        this.date = ZonedDateTime.now();
        this.origin = origin;
        this.schema = schema;
        this.extra = extra;
    }

    public String getEvent() {
        return event;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public String getOrigin() {
        return origin;
    }

    public String getSchema() {
        return schema;
    }

    public Object getExtra() {
        return extra;
    }
}