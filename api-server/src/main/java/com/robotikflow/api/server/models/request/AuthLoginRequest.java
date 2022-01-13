package com.robotikflow.api.server.models.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AuthLoginRequest
{
    @NotNull
	@Email
    private String email;

    @NotNull
	@Size(min=6, max=64)
    private String password;
    
    @NotNull
	@Size(min=4, max=32)
    private String workspace;

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

	public String getWorkspace() {
		return workspace;
	}
}
