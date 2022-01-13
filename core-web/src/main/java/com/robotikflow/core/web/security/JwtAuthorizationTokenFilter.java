package com.robotikflow.core.web.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

@Component
public class JwtAuthorizationTokenFilter 
    extends OncePerRequestFilter 
{
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.header}")
    private String tokenHeader;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain chain) 
        throws ServletException, IOException 
	{
    	final String requestHeader = request.getHeader(this.tokenHeader);

    	Claims claims = null;
        String authToken = null;
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) 
		{
            authToken = requestHeader.substring(7);
			claims = jwtTokenUtil.getAllClaims(authToken);
        } 

        if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) 
		{
            if (jwtTokenUtil.validateToken(authToken)) 
			{
                var userDetails = ((JwtUserDetailsService)userDetailsService)
                    .loadUser(jwtTokenUtil.getUsername(claims), claims);

                var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}
