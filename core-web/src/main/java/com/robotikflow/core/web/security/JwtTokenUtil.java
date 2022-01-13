package com.robotikflow.core.web.security;

import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.robotikflow.core.exception.AuthenticationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javafx.util.Pair;

@Component
public class JwtTokenUtil
{
    static final String CLAIM_KEY_USERNAME = "sub";
    static final String CLAIM_KEY_CREATED = "iat";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String getUsername(String token) 
	{
        return getClaim(token, Claims::getSubject);
    }

    public String getUsername(Claims claims) 
	{
        return getClaim(claims, Claims::getSubject);
    }

    public Date getIssuedAt(String token) 
	{
        return getClaim(token, Claims::getIssuedAt);
    }

    public Date getIssuedAt(Claims claims) 
	{
        return getClaim(claims, Claims::getIssuedAt);
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) 
	{
        var claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public <T> T getClaim(Claims claims, Function<Claims, T> claimsResolver) 
	{
        return claimsResolver.apply(claims);
    }

    public <T> T getClaim(Claims claims, String key, Class<T> requiredType) 
	{
        return claims.get(key, requiredType);
    }
    
    public Claims getAllClaims(String token) throws AuthenticationException
	{
        try
        {
            return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        }
        catch(Exception e)
        {
            throw new AuthenticationException("Token inválido", e);
        }
    }

    private Boolean isTokenExpired(Date issuedAt) 
	{
        return calcExpirationDate(issuedAt).isBefore(Instant.now());
    }

    public Pair<String, Instant> generateToken(UserDetails userDetails) 
	{
        var claims = ((JwtUserDetails)userDetails).getClaims();
        
        var issuedAt = Date.from(Instant.now());

        var token = Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(issuedAt)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();

        return new Pair<>(token, calcExpirationDate(issuedAt));
    }

    private boolean shouldTokenBeRefreshed(String token) 
	{
        return calcExpirationDate(getIssuedAt(token)).isBefore(Instant.now().plusSeconds(60*30));
    }

    public Pair<String, Instant> refreshToken(String token) 
	{
        var claims = getAllClaims(token);
        
        if (!shouldTokenBeRefreshed(token)) 
		{
        	return new Pair<>(token, calcExpirationDate(getIssuedAt(claims)));
		}
        
        var issuedAt = Date.from(Instant.now());

        claims.setIssuedAt(issuedAt);

        var refreshedToken = Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();

        return new Pair<>(refreshedToken, calcExpirationDate(issuedAt));
    }

    public boolean validateToken(String token) 
	{
        var claims = getAllClaims(token);
        
        return !isTokenExpired(getIssuedAt(claims));
    }

    private Instant calcExpirationDate(Date issuedAt) 
	{
        return Instant.from(issuedAt.toInstant()).plusSeconds(expiration);
    }
}
