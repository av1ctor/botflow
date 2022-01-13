package com.robotikflow.core.models.response;

import java.util.Set;
import java.util.stream.Collectors;

import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentAuthType;
import com.robotikflow.core.util.DocumentUtil;

public class DocumentResponse
	extends DocumentBaseResponse
{
	private final UserBaseResponse owner;
	private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;
	private final Long size;
	private final DocumentAuthType ownerAuth;
	private final DocumentAuthType groupAuth;
	private final DocumentAuthType othersAuth;
	private final boolean locked;
	private final UserBaseResponse lockedBy;
	private final String lockedSince;
	private final boolean checked;
	private final UserBaseResponse checkedBy;
	private final String checkedSince;
	private final Short version;
	private final Set<Short> versions;
	private final Set<String> tags;
	private final String thumbUrl;
	private final String previewUrl;
	private final String downloadUrl;
	private final String highlightText;
	private final WorkspaceBaseResponse workspace;

	public DocumentResponse(
		final Document document, 
		final String highlightText,
		final DocumentUtil docUtil)
	{
		super(document, docUtil);

		createdBy = new UserBaseResponse(document.getCreatedBy());
		createdAt = document.getCreatedAt().format(DocumentUtil.datePattern);
		updatedBy = document.getUpdatedBy() != null? 
			new UserBaseResponse(document.getUpdatedBy()): 
			null;
		updatedAt = document.getUpdatedAt() != null? 
			document.getUpdatedAt().format(DocumentUtil.datePattern): 
			null;
		size = document.getSize();
		version = document.getVersion();
		workspace = new WorkspaceBaseResponse(document.getWorkspace());
		owner = new UserBaseResponse(
			document.getOwner() != null?
				document.getOwner(): 
				document.getCreatedBy());
		ownerAuth = document.getOwnerAuth();
		groupAuth = document.getGroupAuth();
		othersAuth = document.getOthersAuth();
		this.highlightText = highlightText;
		
		if(DocumentUtil.isInterno(document))
		{
			var doc = DocumentUtil.toInterno(document);
			locked = doc.isLocked();
			lockedBy = locked? 
				new UserBaseResponse(doc.getLockedBy()): 
				null;
			lockedSince = doc.getLockedSince() != null? 
				doc.getLockedSince().format(DocumentUtil.datePattern): 
				null;
			checked = doc.getChecked();
			checkedBy = checked? 
				new UserBaseResponse(doc.getCheckedBy()): 
				null;
			checkedSince = doc.getCheckedSince() != null? 
				doc.getCheckedSince().format(DocumentUtil.datePattern): 
				null;
			versions = doc.getVersions().stream().map(v -> v.getVersion()).collect(Collectors.toSet());
			tags = doc.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet());
			downloadUrl = null;
			thumbUrl = DocumentUtil.getThumbUrl(doc);
			previewUrl = DocumentUtil.getPreviewUrl(doc);
		}
		else
		{
			var doc = DocumentUtil.toExterno(document);
			locked = false;
			lockedBy = null;
			lockedSince = null;
			checked = false;
			checkedBy = null;
			checkedSince = null;
			versions = null;
			tags = null;
			downloadUrl = doc.getUrlDownload();
			thumbUrl = null;
			previewUrl = DocumentUtil.getPreviewUrl(doc);
		}
	}

	public DocumentResponse(
		final Document document,
		final DocumentUtil docUtil)
	{
		this(document, null, docUtil);
	}

	public UserBaseResponse getOwner() {
		return owner;
	}

	public UserBaseResponse getCreatedBy() {
		return createdBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public UserBaseResponse getUpdatedBy() {
		return updatedBy;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public Long getSize() {
		return size;
	}

	public DocumentAuthType getOwnerAuth() {
		return ownerAuth;
	}

	public DocumentAuthType getGroupAuth() {
		return groupAuth;
	}

	public DocumentAuthType getOthersAuth() {
		return othersAuth;
	}

	public boolean isLocked() {
		return locked;
	}

	public UserBaseResponse getLockedBy() {
		return lockedBy;
	}

	public String getLockedSince() {
		return lockedSince;
	}

	public boolean isChecked() {
		return checked;
	}

	public UserBaseResponse getCheckedBy() {
		return checkedBy;
	}

	public String getCheckedSince() {
		return checkedSince;
	}

	public Short getVersion() {
		return version;
	}

	public Set<Short> getVersions() {
		return versions;
	}

	public Set<String> getTags() {
		return tags;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getHighlightText() {
		return highlightText;
	}

	public WorkspaceBaseResponse getWorkspace() {
		return workspace;
	}

}
