package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.UserPropsWorkspace;

public class UserPropsWorkspaceResponse
{
    private final boolean active;
    
    public UserPropsWorkspaceResponse(UserPropsWorkspace props) 
    {
		active = props != null && props.isActive();
    }

    public boolean isActive() 
    {
		return active;
	}
}

