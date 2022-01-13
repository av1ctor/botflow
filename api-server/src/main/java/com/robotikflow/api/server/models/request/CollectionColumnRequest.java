package com.robotikflow.api.server.models.request;

import com.robotikflow.core.models.schemas.collection.Field;

public class CollectionColumnRequest 
{
    private Field column;

    public Field getColumn() {
        return column;
    }
    public void setColumn(Field column) {
        this.column = column;
    }
}