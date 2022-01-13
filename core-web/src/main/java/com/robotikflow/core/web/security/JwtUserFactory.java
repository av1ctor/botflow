package com.robotikflow.core.web.security;

import com.robotikflow.core.models.UserSession;

public final class JwtUserFactory 
{
    private JwtUserFactory() 
	{
    }

    public static JwtUserDetails create(UserSession userSession) 
	{
        return new JwtUserDetails(userSession);
    }
}
