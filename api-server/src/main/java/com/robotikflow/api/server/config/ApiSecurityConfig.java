package com.robotikflow.api.server.config;

import com.robotikflow.core.web.security.JwtAuthenticationEntryPoint;
import com.robotikflow.core.web.security.JwtAuthorizationTokenFilter;
import com.robotikflow.core.web.security.JwtCorsFilter;
import com.robotikflow.core.web.security.JwtUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter 
{
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtAuthorizationTokenFilter authenticationTokenFilter;
    @Autowired
    private JwtCorsFilter corsFilter;

    @Value("${jwt.route.authentication.path}/**")
    private String logonPathMask;
    @Value("${websocket.path}/**")
    private String websocketPathMask;
    @Value("${public.path.mask}")
    private String publicPathMask;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception 
    {
        auth
            .userDetailsService(jwtUserDetailsService)
            .passwordEncoder(passwordEncoderBean());
    }

    @Bean
    public PasswordEncoder passwordEncoderBean() 
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception 
    {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception 
    {
        http
            .csrf()
                .disable()
            .anonymous()
                .disable()
            .logout().
                disable()
            .formLogin()
                .disable()
            .httpBasic()
                .disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler).and()
            .authorizeRequests()
                .antMatchers(logonPathMask).permitAll()
                .antMatchers(websocketPathMask).permitAll()
                .antMatchers(publicPathMask).permitAll()
	            .anyRequest().authenticated();

	   http
	   		.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)     
	   		.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
	
	    // disable page caching
	    http
	        .headers()
	            .frameOptions().sameOrigin()  // required to set for H2 else H2 Console will be blank.
	            .cacheControl();
    }

    @Override
    public void configure(WebSecurity web) throws Exception 
    {
        // AuthenticationTokenFilter will ignore the below paths
        web
        	.ignoring()
                .antMatchers(logonPathMask)
                .antMatchers(websocketPathMask)
                .antMatchers(publicPathMask)
                .antMatchers("/swagger-ui/index.html");
            
    }
}
