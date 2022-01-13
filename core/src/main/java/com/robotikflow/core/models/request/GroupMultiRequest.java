package com.robotikflow.core.models.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GroupMultiRequest 
{
	@NotNull
	private GroupMultiRequestOp op;
	private String id;
	@Valid
	private GroupRequest group;

	public GroupMultiRequestOp getOp() {
		return op;
	}

	public void setOp(GroupMultiRequestOp op) {
		this.op = op;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GroupRequest getGroup() {
		return group;
	}

	public void setGroup(GroupRequest group) {
		this.group = group;
	}
}

