package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;

public class FieldDisabledFor 
{
	private FieldDisabledWhen when;
	@Valid
	private FieldDisabledUser user;
	@Valid
	private FieldDisabledValue value;

	public FieldDisabledWhen getWhen() {
		return when;
	}
	public void setWhen(FieldDisabledWhen when) {
		this.when = when;
	}
	public FieldDisabledUser getUser() {
		return user;
	}
	public void setUser(FieldDisabledUser user) {
		this.user = user;
	}
	public FieldDisabledValue getValue() {
		return value;
	}
	public void setValue(FieldDisabledValue value) {
		this.value = value;
	}
}
