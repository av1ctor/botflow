package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;

public class AuthAction 
{
	private AuthActionWhen when; 
	@Valid
	private AuthUser user;
	@Valid
	private AuthColumn column;
	
	public AuthActionWhen getWhen() {
		return when;
	}
	public void setWhen(AuthActionWhen when) {
		this.when = when;
	}
	public AuthUser getUser() {
		return user;
	}
	public void setUser(AuthUser user) {
		this.user = user;
	}
	public AuthColumn getColumn() {
		return column;
	}
	public void setColumn(AuthColumn column) {
		this.column = column;
	}
}
