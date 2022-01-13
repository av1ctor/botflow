package com.robotikflow.core.models.request;

import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.DocumentAuthType;

public class DocumentRequest
	extends DocumentBaseRequest
{
	@NotNull
	private DocumentType type;
	@NotNull
	@Size(min = 1, max = 128)
	private String name;
	private UserBaseRequest owner;
	private DocumentAuthType ownerAuth;
	private DocumentAuthType groupAuth;
	private DocumentAuthType othersAuth;
	private Set<String> tags;
	private ProviderBaseRequest provider;

	public DocumentType getType() {
		return type;
	}
	public void setType(DocumentType type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public UserBaseRequest getOwner() {
		return owner;
	}
	public void setOwner(UserBaseRequest owner) {
		this.owner = owner;
	}
	public DocumentAuthType getOwnerAuth() {
		return ownerAuth;
	}
	public void setAuthDono(DocumentAuthType ownerAuth) {
		this.ownerAuth = ownerAuth;
	}
	public DocumentAuthType getGroupAuth() {
		return groupAuth;
	}
	public void setAuthGroup(DocumentAuthType groupAuth) {
		this.groupAuth = groupAuth;
	}
	public DocumentAuthType getOthersAuth() {
		return othersAuth;
	}
	public void setAuthOutros(DocumentAuthType othersAuth) {
		this.othersAuth = othersAuth;
	}
	public Set<String> getTags() {
		return tags;
	}
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	public ProviderBaseRequest getProvider() {
		return provider;
	}
	public void setProvider(ProviderBaseRequest provider) {
		this.provider = provider;
	}
}
