package com.robotikflow.api.server.controllers;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.robotikflow.api.server.models.request.OffsetLimitRequest;
import com.robotikflow.core.exception.AuthenticationException;
import com.robotikflow.core.exception.BadRequestException;
import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.entities.RoleType;
import com.robotikflow.core.services.WorkspaceService;
import com.robotikflow.core.util.DocumentUtil;
import com.robotikflow.core.web.services.UserService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

abstract public class BaseController 
{
	@Autowired
	protected WorkspaceService workspaceService;
	@Autowired
	protected UserService userService;
	@Autowired
	protected DocumentUtil documentoUtil;
	
	protected ObjectMapper objMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL);
	
    @Value("${jwt.header}")
    private String tokenHeader;
	
    /**
	 * Cria um área de trabalho e adiciona o usuário, criando-o se este não existir ainda
	 * @param email
	 * @param password
	 * @param role
	 * @param workspaceName
	 * @return
	 * @throws BadRequestException
	 */
	protected UserSession criarUserAndWorkspace(
		final String email, 
		final String password, 
		final String nick,
		final RoleType role, 
		final String workspaceName) 
		throws BadRequestException
	{
		// criar área de trabalho
		var workspace = workspaceService
			.create(workspaceName);
		
		// criar usuário
		var user = userService
			.create(email, password, nick, role, workspace);
		
		//
		var provider = workspaceService
			.createDefaultStorageProvider(workspace, user);
		workspace.setProvider(provider);
		
		// criar diretório raiz
		var rootDoc = workspaceService
			.createRootDoc(workspace, user);
		workspace.setRootDoc(rootDoc);

		workspaceService.changeDefaultStorageProviderRoot(provider, rootDoc);
		
		// create root collection 
		var rootCollection = workspaceService
			.createRootCollection(workspace, user);
		workspace.setRootCollection(rootCollection);
			
		//
		workspace.setOwner(user);
		
		//
		workspace = workspaceService.save(workspace);
				
		return new UserSession(user, workspace);
	}

	/**
	 * Adiciona um usuário, criando-o se necessário, a um área de trabalho existente
	 * @param email
	 * @param password
	 * @param role
	 * @param workspaceName
	 * @return
	 * @throws BadRequestException
	 */
	protected UserSession addUserAoWorkspace(
		final String email, 
		final String password, 
		final String nick,
		final RoleType role, 
		final String workspaceName) 
		throws BadRequestException
	{
		// iniciar área de trabalho
		var workspace = workspaceService
			.findByNome(workspaceName);
		
		// criar usuário se não existir
		var user = userService
			.create(email, password, nick, role, workspace);
		
		return new UserSession(user, workspace);
	}	
	
	/**
	 * Autentica um usuário no área de trabalho, utilizando email e password
	 * @param email
	 * @param password
	 * @param workspaceName
	 * @return
	 * @throws AuthenticationException
	 */
    protected UserSession autenticarUser(
		final String email, 
		final String password, 
		final String workspaceName) 
		throws AuthenticationException
	{
		return userService
			.autenticar(email, password, workspaceName);
    }
	
    /**
     * Recupera a sessão do usuário no área de trabalho em que foi autenticado previamente
     * @return
     */
    protected UserSession getUserSession()
    {
        return userService.getUserSession();
    }

    /**
     * Recupera o token do cabeçalho da requisição HTTP
     * @param req
     * @return
     */
    protected String getToken(
		final HttpServletRequest req)
    {
        var authToken = req.getHeader(tokenHeader);
        return getToken(authToken);
    }
	
	/**
	 * 
	 * @param authToken
	 * @return
	 */
	protected String getToken(
		final String authToken) 
	{
		return UserService.getToken(authToken);
	}

	/**
	 * 
	 * @param filtersStr
	 * @param filtersClass
	 * @return
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	protected <T> T buildFilters(
		final String filtersStr, 
		final Class<T> filtersClass) 
		throws IOException, JsonParseException, JsonMappingException 
	{
		return filtersStr == null || filtersStr.isEmpty()? 
			null: 
			objMapper.readValue('{' + filtersStr + '}', filtersClass);
	}
    
	protected Pageable validateSorting(
		final Pageable pageable, 
		final Set<String> validColumns, 
		final String defaultColumn, 
		final Direction defaultDir)
	{
		if(pageable.getSort().equals(Sort.unsorted()) ||
			!validColumns.containsAll(
				pageable.getSort().get()
					.map(s -> s.getProperty())
						.collect(Collectors.toSet())))
		{
			return PageRequest.of(
				pageable.getPageNumber(), 
				pageable.getPageSize(), 
				Sort.by(defaultDir, defaultColumn)
			);
		}
		
		return pageable;
	}

	protected OffsetLimitRequest verificarOrdenacaoOffsetLimit(
		final Pageable pageable, 
		final Set<String> colunasValidas, 
		final String columnaDefault, 
		final Direction direcaoDefault)
	{
		if(!colunasValidas.containsAll(
			pageable.getSort().get()
				.map(s -> s.getProperty())
					.collect(Collectors.toSet())))
		{
			return OffsetLimitRequest.of(
				pageable.getPageNumber(), 
				pageable.getPageSize(), 
				Sort.by(direcaoDefault, columnaDefault)
			);
		}
		
		return OffsetLimitRequest.of(
			pageable.getPageNumber(), 
			pageable.getPageSize(),
			pageable.getSort() 			
		);
	}

	protected boolean userIsAdmin(
		final UserSession userSession) 
	{
		return userSession.getUser()
			.isAdmin(userSession.getWorkspace().getId());
	}
}
