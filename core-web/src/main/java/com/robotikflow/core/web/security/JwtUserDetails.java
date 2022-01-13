package com.robotikflow.core.web.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.entities.Role;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JwtUserDetails 
    implements UserDetails 
{
	private static final long serialVersionUID = -3270146910756157812L;
    private final UserSession userSession;
    private final Collection<? extends GrantedAuthority> roles;

    public JwtUserDetails(UserSession userSession) 
	{
        this.userSession = userSession;
        this.roles = mapToGrantedAuthorities(userSession.getRoles());
    }
    
    private static Set<GrantedAuthority> mapToGrantedAuthorities(Set<Role> roles) 
	{
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());
    }
    
    @JsonIgnore
    public Map<String, Object> getClaims()
    {
    	var claims = new HashMap<String, Object>();
    	
    	claims.put("workspaceId", userSession.getWorkspace().getId());
    	
    	return claims;
    }

    @Override
    public String getUsername() 
	{
        return userSession.getUser().getEmail();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() 
	{
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() 
	{
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() 
	{
        return true;
    }

    @JsonIgnore
    @Override
    public String getPassword() 
	{
        return userSession.getUser().getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() 
	{
        return roles;
    }

    @Override
    public boolean isEnabled() 
	{
        return userSession.getUser().isActive();
    }

    public Workspace getWorkspace() 
	{
        return userSession.getWorkspace();
    }

	public User getUser() 
    {
		return userSession.getUser();
	}
}
