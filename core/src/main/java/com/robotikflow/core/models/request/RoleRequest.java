package com.robotikflow.core.models.request;

import javax.validation.constraints.NotBlank;

import com.robotikflow.core.models.entities.Role;
import com.robotikflow.core.models.entities.RoleType;

public class RoleRequest 
{
	private Long id;
	@NotBlank
    private RoleType name;
    
    public RoleRequest()
    {
    }

    public RoleRequest(Role role) 
	{
    	id = role.getId();
    	name = role.getName();
    }

    public Long getId() 
	{
        return this.id;
    }

	public RoleType getName() {
		return name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(RoleType name) {
		this.name = name;
	}
}

