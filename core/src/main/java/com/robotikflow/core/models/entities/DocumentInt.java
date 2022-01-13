package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "documents_ints")
public class DocumentInt 
	extends Document
{
	protected String blobId;
	
	protected String thumbId;
	
	protected String previewId;
	
	@NotNull
	protected boolean locked;

	@ManyToOne(fetch = FetchType.EAGER)
	protected User lockedBy;
	
	protected ZonedDateTime lockedSince;

	@NotNull
	protected boolean checked;
	
	@ManyToOne(fetch = FetchType.EAGER)
	protected User checkedBy;
	
	protected ZonedDateTime checkedSince;
	
	//FIXME: melhor que uma versão seja só um pointer para o document anterior, 
	//		 até chegar na versão 0 (anterior_id = null). A query precisa ser otimizada para usar CTE...
	@OneToMany(mappedBy="document", fetch = FetchType.LAZY)
	protected Set<DocumentVersion> versions = new HashSet<>();
	
	@ManyToMany(fetch = FetchType.LAZY, 
		cascade = {CascadeType.MERGE, CascadeType.PERSIST})
	@JoinTable(name="documents_tags", 
		joinColumns = @JoinColumn(name = "document_id"), 
		inverseJoinColumns = @JoinColumn(name = "tag_id"))
	protected Set<Tag> tags = new HashSet<>();

	@OneToMany(mappedBy = "document", 
		fetch = FetchType.LAZY, 
		cascade = {CascadeType.MERGE, CascadeType.PERSIST}, 
		orphanRemoval = true)
	protected Set<DocumentCollectionIntegration> integrations = new HashSet<>();
	
	@OneToMany(mappedBy="document", fetch = FetchType.LAZY)
	protected Set<DocumentAuth> auths = new HashSet<>();
	
	public DocumentInt()
	{
		super();
	}

	public DocumentInt(
		String name, 
		DocumentType type,
		Long tamanho,
		Document parent, 
		DocumentAuthType ownerAuth, 
		DocumentAuthType groupAuth,
		DocumentAuthType othersAuth,
		User user,
		Group group,
		Workspace workspace)
	{
		super(
			 workspace.getProvider(), 
			 name, 
			 type, 
			 tamanho, 
			 parent, 
			 user, 
			 group, 
			 ownerAuth, 
			 groupAuth, 
			 othersAuth, 
			 workspace);
		this.locked = this.checked = false;
		this.version = 0;
	}	

	public DocumentInt(
		String name, 
		DocumentType type, 
		Long tamanho, 
		Document parent, 
		User user, 
		Group group, 
		Workspace workspace)
	{
		super(
			workspace.getProvider(),
			name, 
			type, 
			tamanho, 
			parent, 
			user, 
			group, 
			parent.getOwnerAuth(), 
			parent.getGroupAuth(), 
			parent.getOthersAuth(), 
			workspace);
	}	

	public DocumentInt(
		DocumentInt doc)
	{
		super(
			doc.getWorkspace().getProvider(),
			doc.getName(), 
			doc.getType(), 
			doc.getSize(), 
			doc.getParent(),
			doc.getCreatedBy(),
			doc.getGroup(),
			doc.getOwnerAuth(),
			doc.getGroupAuth(),
			doc.getOthersAuth(),
			doc.getCreatedAt(),
			doc.getWorkspace());
		
		this.updatedBy = doc.updatedBy;
		this.updatedAt = doc.updatedAt;
		
		copyFields(doc);
	}

	private void copyFields(DocumentInt doc) 
	{
		blobId = doc.blobId;
		previewId = doc.previewId;
		thumbId = doc.thumbId;
		locked = doc.locked;
		lockedBy = doc.lockedBy;
		lockedSince = doc.lockedSince;
		checked = doc.checked;
		checkedBy = doc.checkedBy;
		checkedSince = doc.checkedSince;
	}

	public DocumentInt(
		Document doc)
	{
		super(
			doc.getWorkspace().getProvider(),
			doc.getName(), 
			doc.getType(), 
			doc.getSize(), 
			doc.getParent(),
			doc.getCreatedBy(),
			doc.getGroup(),
			doc.getOwnerAuth(),
			doc.getGroupAuth(),
			doc.getOthersAuth(),
			doc.getCreatedAt(),
			doc.getWorkspace());
			
		this.updatedBy = doc.updatedBy;
		this.updatedAt = doc.updatedAt;
	}
	
	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean bloqueado) {
		this.locked = bloqueado;
	}

	public boolean getChecked() {
		return checked;
	}

	public void setChecked(boolean checkado) {
		this.checked = checkado;
	}

	public Set<DocumentVersion> getVersions() {
		return versions;
	}

	public void setVersions(Set<DocumentVersion> versoes) {
		this.versions = versoes;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}
	
	public String getBlobId() {
		return blobId;
	}

	public void setBlobId(String blobId) {
		this.blobId = blobId;
	}

	public User getLockedBy() {
		return lockedBy;
	}

	public void setLockedPor(User bloqueadoPor) {
		this.lockedBy = bloqueadoPor;
	}

	public ZonedDateTime getLockedSince() {
		return lockedSince;
	}

	public void setLockedSince(ZonedDateTime bloqueadoDesde) {
		this.lockedSince = bloqueadoDesde;
	}

	public User getCheckedBy() {
		return checkedBy;
	}

	public void setCheckedBy(User checkadoPor) {
		this.checkedBy = checkadoPor;
	}

	public ZonedDateTime getCheckedSince() {
		return checkedSince;
	}

	public void setCheckedSince(ZonedDateTime checkadoDesde) {
		this.checkedSince = checkadoDesde;
	}

	public String getThumbId() {
		return thumbId;
	}

	public void setThumbId(String thumbId) {
		this.thumbId = thumbId;
	}

	public String getPreviewId() {
		return previewId;
	}

	public void setPreviewId(String previewId) {
		this.previewId = previewId;
	}
	
	public Set<DocumentCollectionIntegration> getIntegrations() {
		return integrations;
	}

	public void setIntegrations(Set<DocumentCollectionIntegration> integrations) {
		this.integrations = integrations;
	}
	
	public void addTag(Tag tag)
	{
		tags.add(tag);
	}
	
	public void delTag(Tag tag)
	{
		tags.remove(tag);
	}
}
