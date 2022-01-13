package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Role;

public class RoleResponse 
{
    private final Long id;
    private final String name;
    
    public RoleResponse(
		final Role role) 
	{
    	id = role.getId();
    	name = role.getName().name();
    }

    public Long getId() {
        return this.id;
    }

	public String getName() {
		return name;
	}
}

