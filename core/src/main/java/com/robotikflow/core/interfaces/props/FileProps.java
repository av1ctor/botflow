package com.robotikflow.core.interfaces.props;

import java.time.ZonedDateTime;

import com.robotikflow.core.models.entities.Provider;

public class FileProps 
{
    public final Provider provider;
    public final String name;
    public final String id;
    public final String path;
    public final String extension;
    public final String mimeType;
    public final Long size;
    public final ZonedDateTime createdAt;
    public final ZonedDateTime modifiedAt;
    public final String creator;
    public final String url;
    public final byte[] contents;

	public FileProps(
        final Provider provider,
        final String name, 
        final String id, 
        final String path, 
        final String extension, 
        final String mimeType, 
        final Long size,
		final ZonedDateTime createdAt, 
        final ZonedDateTime modifiedAt, 
        final String creator, 
        final String url) 
    {
		this.provider = provider;
        this.name = name;
		this.id = id;
		this.path = path;
		this.extension = extension;
		this.mimeType = mimeType;
		this.size = size;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
		this.creator = creator;
		this.url = url;
        this.contents = null;
	}

}
