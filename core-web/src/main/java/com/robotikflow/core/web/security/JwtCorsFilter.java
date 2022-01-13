package com.robotikflow.core.web.security;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtCorsFilter extends OncePerRequestFilter 
{
	private final Pattern origins;
	@Value("${cors.allowed-headers}")
	private String headers;
	@Value("${cors.exposed-headers}")
	private String exposedHeaders;
	
	@Autowired
	public JwtCorsFilter(Environment env)
	{
		origins = Pattern.compile(env.getProperty("cors.allowed-origins"));
	}
	
	protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain) throws ServletException, IOException 
    {
		var origin = request.getHeader("Origin");
		
		if(origin != null && origins.matcher(origin).matches())
		{
			response.setHeader("Access-Control-Allow-Origin", origin);
	        response.setHeader("Access-Control-Allow-Credentials", "true");
	        response.setHeader("Access-Control-Allow-Methods", "PUT, POST, PATCH, GET, OPTIONS, DELETE");
	        response.setHeader("Access-Control-Max-Age", "3600");
	        response.setHeader("Access-Control-Allow-Headers", headers);
	        
	        if(exposedHeaders.trim().length() > 0) 
	        {
	        	response.setHeader("Access-Control-Expose-Headers", exposedHeaders);
	        }
		}
        
        if (request.getMethod().equals("OPTIONS"))
        {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        else
        {
            filterChain.doFilter(request, response);
        }
    }
}

