package com.robotikflow.core.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;

import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.repositories.WorkspaceRepository;
import com.robotikflow.core.models.repositories.UserRepository;
import com.robotikflow.core.web.services.UserService;

import io.jsonwebtoken.Claims;

@Service
public class JwtUserDetailsService 
    implements UserDetailsService 
{
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private WorkspaceRepository workspaceRepo;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

	@Override
	public UserDetails loadUserByUsername(
        String username) 
        throws UsernameNotFoundException 
	{
		var user = userRepo.findByEmail(username);
        if (user == null) 
        {
            throw new UsernameNotFoundException("Usuário inexistente");
        } 
        
        return JwtUserFactory.create(new UserSession(user, null));
	}

	public UserDetails loadUser(
        String username, 
        Claims claims) 
        throws ServletException 
	{
        var idWorkspace = jwtTokenUtil.getClaim(claims, "workspaceId", Long.class);

        var workspace = workspaceRepo
            .findById(idWorkspace)
                .orElseThrow(() -> new ServletException("Área de trabalho inexistente"));
        
		var user = userRepo.findByEmail(username);
        if (user == null) 
        {
            throw new ServletException("Usuário inexistente");
        }

        UserService.validarLogon(user, workspace);
        
        return JwtUserFactory.create(new UserSession(user, workspace));
	}
}
