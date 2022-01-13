package com.robotikflow.core.models.entities;

public enum DocumentOperationType 
{
	// NOTA: não alterar order, porque EnumType.ORDINAL está sendo usado
	CREATED,
	UPDATED,
	MOVED,
	RENAMED,
	REMOVED;
	
	private static final DocumentOperationType[] values = DocumentOperationType.values();
	
    public static DocumentOperationType from(short x) 
	{
        return values[x];
    }	
}
