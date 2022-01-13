package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Group;

public class GroupBaseResponse 
{
    private final String id;
	private final String name;

    public GroupBaseResponse(
		final Group group) 
	{
		id = group.getPubId();
		name = group.getName();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}

