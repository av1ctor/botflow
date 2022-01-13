package com.robotikflow.api.server.controllers;

import javax.validation.Valid;

import com.robotikflow.api.server.models.request.AuthLoginRequest;
import com.robotikflow.api.server.models.request.UserCreateRequest;
import com.robotikflow.api.server.models.response.AuthLoginResponse;
import com.robotikflow.core.exception.AuthenticationException;
import com.robotikflow.core.exception.BadRequestException;
import com.robotikflow.core.models.entities.RoleType;
import com.robotikflow.core.models.response.UserResponse;
import com.robotikflow.core.web.security.JwtTokenUtil;
import com.robotikflow.core.web.security.JwtUserFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class AuthController extends BaseController 
{
	@Autowired
    private JwtTokenUtil jwtTokenUtil;

	@PostMapping("${jwt.route.authentication.path}/join")
	@ApiOperation(value = "Criar uma área de trabalho e registar o usuário administrador")
	public UserResponse criarUserAdminAndWorkspace(
		@Valid @RequestBody UserCreateRequest req) 
		throws Exception
	{
		var userSession = criarUserAndWorkspace(
				req.getEmail(), 
				req.getPassword(),
				req.getNick(), 
				RoleType.ROLE_USER_ADMIN, 
				req.getWorkspace());
		
		return new UserResponse(userSession);
	}
	
    @PostMapping(value = "${jwt.route.authentication.path}/login")
    @ApiOperation(value = "Autenticar usuário")
    public AuthLoginResponse autenticarUser(
		@Valid @RequestBody AuthLoginRequest req) 
		throws AuthenticationException 
	{
    	var userSession = autenticarUser(req.getEmail(), req.getPassword(), req.getWorkspace());
    	
    	var jwtUser = JwtUserFactory.create(userSession);

        var pair = jwtTokenUtil.generateToken(jwtUser);

        return new AuthLoginResponse(userSession, pair.getKey(), pair.getValue());
    }

    @GetMapping(value = "${jwt.route.authentication.path}/refresh/{token}")
    @ApiOperation(value = "Atualizar token de usuário autenticado")
    public AuthLoginResponse refreshAndGetAuthenticationToken(
		@PathVariable String token) 
		throws AuthenticationException 
	{
		var pair = jwtTokenUtil.refreshToken(token);
		
        return new AuthLoginResponse(null, pair.getKey(), pair.getValue());
    }    

	@PostMapping("${jwt.route.authentication.path}/accept")
	@ApiOperation(value = "Aceitar convite como usuário de um área de trabalho existente")
	public UserResponse addConvidadoAoWorkspace(
		@Valid @RequestBody AuthLoginRequest req) 
		throws Exception
	{
		var user = userService.findByEmail(req.getEmail());
		if(user == null)
		{
			throw new BadRequestException("Usuário inexistente");
		}
		
		var ua = userService.aceitarConvite(user, req.getWorkspace());
		
		return new UserResponse(ua);
	}
}
