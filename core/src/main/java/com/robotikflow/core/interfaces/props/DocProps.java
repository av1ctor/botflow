package com.robotikflow.core.interfaces.props;

public class DocProps 
{
    private final String id;
    private final String path;
    private final String mimeType;
    private final String urlDownload;
    
    public DocProps(
        final String id) 
    {
        this(id, null, null, null);
    }
    
	public DocProps(
        final String id, 
        final String mimeType) 
    {
        this(id, null, mimeType, null);
    }

	public DocProps(
        final String id, 
        final String mimeType, 
        final String urlDownload) 
    {
        this(id, null, mimeType, urlDownload);
    }

    public DocProps(
        final String id, 
        final String path, 
        final String mimeType, 
        final String urlDownload) 
    {
        this.id = id;
        this.path = path;
        this.mimeType = mimeType;
        this.urlDownload = urlDownload;
    }    

	public String getId() {
		return id;
	}
    public String getPath() {
		return path;
	}
    public String getMimeType() {
        return mimeType;
    }
    public String getUrlDownload() {
        return urlDownload;
    }
}