package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class FieldHidden 
{
	@NotNull
	private FieldHiddenType type;
	@Valid
	private HiddenUser user;

	public FieldHiddenType getType() {
		return type;
	}
	public void setType(FieldHiddenType type) {
		this.type = type;
	}
	public HiddenUser getUser() {
		return user;
	}
	public void setUser(HiddenUser user) {
		this.user = user;
	}
}
