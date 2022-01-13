package com.robotikflow.core.web.services;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.robotikflow.core.exception.AuthenticationException;
import com.robotikflow.core.exception.UserException;
import com.robotikflow.core.models.entities.RoleType;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.UserGroupWorkspace;
import com.robotikflow.core.models.entities.UserRoleWorkspace;
import com.robotikflow.core.models.entities.UserPropsWorkspace;
import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.filters.UserFilter;
import com.robotikflow.core.models.repositories.GroupRepository;
import com.robotikflow.core.models.repositories.RoleRepository;
import com.robotikflow.core.models.repositories.WorkspaceRepository;
import com.robotikflow.core.models.repositories.UserRepository;
import com.robotikflow.core.models.request.AccessType;
import com.robotikflow.core.models.request.UserRequest;
import com.robotikflow.core.web.security.JwtTokenUtil;
import com.robotikflow.core.web.security.JwtUserDetails;
import com.google.common.collect.ImmutableMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Lazy
public class UserService 
{
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private WorkspaceRepository workspaceRepo;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private GroupRepository groupRepo;
	@Autowired
    protected JwtTokenUtil jwtTokenUtil;

	private static final long INVITATION_EXPIRES_IN_SECS = 60L*60L*24L*30L;

	// mapeamento de role para group de usuário
	private final static Map<RoleType, String> roleToGroup = ImmutableMap.of(
			RoleType.ROLE_USER_PARTNER, "Partners",
			RoleType.ROLE_USER_CONTRIBUTOR, "Users",
			RoleType.ROLE_USER_ADMIN, "Administrators"
	);
	
    /**
     * Recupera a sessão do usuário no área de trabalho em que foi autenticado previamente
     * @return
     * @throws AuthenticationException
     */
    public UserSession getUserSession()
    {
    	var authentication = (UsernamePasswordAuthenticationToken)SecurityContextHolder
			.getContext().getAuthentication();
    	var userDetails = (JwtUserDetails)authentication.getPrincipal();
    	
        var user = userDetails.getUser();
		if(user == null)
		{
			throw new AuthenticationException("Usuário não autenticado");
		}
    	
        return new UserSession(user, userDetails.getWorkspace());
    }
	
	public List<User> findAllByWorkspace(
		final Long idWorkspace, 
		final Pageable pageable) 
	{
		return userRepo.findAllByIdWorkspace(idWorkspace, pageable);
	}

	public List<User> findAllByWorkspace(
		final Long idWorkspace, 
		final Pageable pageable, 
		final UserFilter filtros) 
	{
		if(filtros.getEmail() != null)
		{
			return userRepo
				.findAllByEmailContainingAndWorkspace(
					filtros.getEmail(), idWorkspace, pageable);
		}
		
		if(filtros.getPubId() != null)
		{
			var user = userRepo
				.findByPubIdAndIdWorkspace(filtros.getPubId(), idWorkspace);
			return user != null? List.of(user): List.of();
		}

		return List.of();
	}

	public User findByPubId(
		final String id) 
	{
		return userRepo.findByPubId(id)
			.orElseThrow(() -> new UserException("Usuário inexistente"));
	}

	public User findByPubIdAndWorkspace(
		final String id,
		final Workspace workspace) 
	{
		return userRepo.findByPubIdAndIdWorkspace(id, workspace.getId());
	}

	/**
	 * 
	 * @param email
	 * @param workspace
	 * @return
	 */
	public User findByEmailAndWorkspace(
		final String email, 
		final Workspace workspace)
	{
		var user = userRepo.findByEmailAndIdWorkspace(email, workspace.getId());
		if(user == null)
		{
			throw new UserException("Usuário inexistente");
		}
				
		return user;
	}

	public User findByEmail(String email) 
	{
		return userRepo.findByEmail(email);
	}

	public static String getToken(
		final String authToken) 
	{
		return authToken != null && 
			authToken.startsWith("Bearer ") && 
			authToken.length() > 7? 
				authToken.substring(7): 
				null;
	}
	
	/**
	 * 
	 * @param email
	 * @param password
	 * @param nomeWorkspace
	 * @return
	 * @throws AuthenticationException
	 */
    public UserSession autenticar(
		final String email, 
		final String password, 
		final String nomeWorkspace) 
		throws AuthenticationException
	{
        try 
		{
            authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(email, password));
            
            var user = userRepo.findByEmail(email);
            if(user == null)
            {
            	throw new AuthenticationException("Usuário inexistente");
            }
            
            var workspace = user.getWorkspace(nomeWorkspace);

			validarLogon(user, workspace);			
            
            return new UserSession(user, workspace);
        } 
		catch (DisabledException e) 
		{
            throw new AuthenticationException("Usuário inactive", e);
        } 
		catch (BadCredentialsException e) 
		{
            throw new AuthenticationException("Usuário inexistente ou e-mail e password incorretos", e);
        }
	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	public UserSession autenticar(
		final String token) 
	{
		var claims = jwtTokenUtil.getAllClaims(token);
		if(claims == null || !jwtTokenUtil.validateToken(token))
		{
			throw new AuthenticationException("Token inválido");
		}

        var idWorkspace = jwtTokenUtil.getClaim(claims, "workspaceId", Long.class);

        var workspace = workspaceRepo.findById(idWorkspace).get();
		var user = userRepo.findByEmailAndIdWorkspace(
			jwtTokenUtil.getUsername(claims), workspace.getId());
		if(user == null)
		{
			throw new AuthenticationException("Usuário inexistente", null);
		}

		validarLogon(user, workspace);

		return new UserSession(user, workspace);
	}	

	/**
	 * 
	 * @param user
	 * @param workspace
	 */
	public static void validarLogon(
		final User user, 
		final Workspace workspace) 
	{
		if(!user.isSuperAdmin())
		{
			if(user.getConfirmedAt() == null)
			{
				throw new AuthenticationException("Confirmação pendente");
			}

			var props = user.getProps(workspace.getId());
			if(props == null || !props.isActive())
			{
				throw new AuthenticationException("Usuário inexistente ou inactive na área de trabalho");
			}
			
			if(!user.isAdmin(workspace.getId()))
			{
				if(props.getInvitedAt() == null)
				{
					throw new AuthenticationException("Usuário não é membro da área de trabalho");
				}

				if(props.getAcceptedAt() == null)
				{
					throw new AuthenticationException("Usuário não aceitou convite para ser membro da área de trabalho");
				}

				if(!props.isActive())
				{
					throw new AuthenticationException("Usuário não está active como membro da área de trabalho");
				}
			}
		}
	}
	
	private void adicionarAoGroupPorRole(
		final User user, 
		final Workspace workspace, 
		final RoleType nomeRole) 
	{
		var name = roleToGroup.get(nomeRole);
		var group = groupRepo.findByNameAndWorkspace(name, workspace.getId());
		var urg = new UserGroupWorkspace(group, user, workspace);
		user.addGroupNoWorkspace(urg);
	}

	private void adicionarAosGroups(
		final User user, 
		final Workspace workspace, 
		final List<String> groups) 
	{
		for(var id : groups)
		{
			var group = groupRepo
				.findByPubIdAndWorkspace(id, workspace.getId())
					.orElseThrow();
			
			var urg = new UserGroupWorkspace(group, user, workspace);
			user.addGroupNoWorkspace(urg);
		}
	}

	private void adicionarRole(
		final User user, 
		final RoleType nomeRole, 
		final Workspace workspace) 
	{
		var role = roleRepo.findByNome(nomeRole);
		do
		{
			var urp = new UserRoleWorkspace(user, workspace, role);
			user.addRoleNoWorkspace(urp);
			
			role = role.getAboveOf();
		} while(role != null);
	}

	private void removerPerfisNoWorkspace(
		final User user, 
		final Workspace workspace) 
	{
		var toRemove = new HashSet<UserRoleWorkspace>();
		for(var role : user.getRoles())
		{
			if(role.getWorkspace().getId() == workspace.getId())
			{
				toRemove.add(role);
			}
		}

		user.getRoles().removeAll(toRemove);
	}

	private void removerPropsNoWorkspace(
		final User user, 
		final Workspace workspace) 
	{
		var toRemove = new HashSet<UserPropsWorkspace>();
		
		for(var props : user.getProps())
		{
			if(props.getWorkspace().getId() == workspace.getId())
			{
				toRemove.add(props);
			}
		}

		user.getProps().removeAll(toRemove);
	}

	private void removerGroupsNoWorkspace(
		final User user, 
		final Workspace workspace) 
	{
		var toRemove = new HashSet<UserGroupWorkspace>();
		
		for(var group : user.getGroups())
		{
			if(group.getWorkspace().getId() == workspace.getId())
			{
				toRemove.add(group);
			}
		}

		user.getGroups().removeAll(toRemove);
	}

	/**
	 * 
	 * @param email
	 * @param password
	 * @param role
	 * @param workspace
	 * @return
	 */
	public User create(
		final String email, 
		final String password, 
		final String nick,
		final RoleType role, 
		final Workspace workspace) 
	{
		var now = ZonedDateTime.now();
		
		// criar usuário se não existir
		var user = userRepo.findByEmail(email);
		if(user == null)
		{
			user = new User(
				email, 
				passwordEncoder.encode(password), 
				nick,
				"gothic",
				ZoneId.systemDefault().getId(),
				Locale.getDefault().toString(),
				true, 
				now);
		}

		// adicionar usuário ao área de trabalho
		if(user.getRole(workspace.getId()) == null)
		{
			// adicionar usuário ao group a partir do role
			adicionarAoGroupPorRole(user, workspace, role);
		
			// vincular usuário ao área de trabalho
			adicionarRole(user, role, workspace);

			// props
			var props = new UserPropsWorkspace(
				user, workspace, true, now, now);
			user.getProps().add(props);
		}
		
		return userRepo.save(user);
	}

	/**
	 * 
	 * @param req
	 * @param workspace
	 * @param criador
	 * @return
	 */
	public User adicionar(
		final UserRequest req, 
		final Workspace workspace, 
		final User criador) 
	{
		var user = userRepo.findByEmail(req.getEmail());
		if(user == null)
		{
			throw new UserException("Usuário inexistente");
		}

		if(user.getRole(workspace.getId()) != null)
		{
			throw new UserException("Usuário já existente no área de trabalho");
		}

		// groups
		adicionarAosGroups(
			user, 
			workspace, 
			req.getGroups().stream()
				.map(g -> g.getId())
					.collect(Collectors.toList()));
	
		// roles
		adicionarRole(user, req.getRole().getName(), workspace);

		// props
		var props = new UserPropsWorkspace(
			user, workspace, true, ZonedDateTime.now(), null);
		user.getProps().add(props);

		//TODO: enviar e-mail de convite

		return userRepo.save(user);
	}

	/**
	 * 
	 * @param user
	 * @param req
	 * @param workspace
	 * @param isSelf
	 * @return
	 */
	public User atualizar(
		final User user, 
		final UserRequest req,
		final Workspace workspace, 
		final boolean isAdmin,
		final boolean isSelf) 
	{
		if(isSelf)
		{
			if(req.getPassword() != null)
			{
				user.setPassword(passwordEncoder.encode(req.getPassword()));
			}
			if(req.getName() != null)
			{
				user.setName(req.getName());
			}
			if(req.getNick() != null)
			{
				user.setNick(req.getNick());
			}
			if(req.getIcon() != null)
			{
				user.setIcon(req.getIcon());
			}
			if(req.getLang() != null)
			{
				user.setLang(req.getLang());
			}
			if(req.getTimezone() != null)
			{
				user.setTimezone(req.getTimezone());
			}
			if(req.getTheme() != null)
			{
				user.setTheme(req.getTheme());
			}

			user.setUpdatedAt(ZonedDateTime.now());
		}

		if(req.getRole() != null)
		{
			removerPerfisNoWorkspace(user, workspace);

			adicionarRole(user, req.getRole().getName(), workspace);
		}

		if(isAdmin)
		{
			if(req.getGroups() != null && req.getGroups().size() > 0)
			{
				removerGroupsNoWorkspace(user, workspace);

				adicionarAosGroups(
					user, 
					workspace, 
					req.getGroups().stream()
						.map(g -> g.getId())
							.collect(Collectors.toList()));
			}

			if(req.getProps() != null)
			{
				var props = user.getProps(workspace.getId());
				var invitedAt = props.getInvitedAt();
				var acceptedAt = props.getAcceptedAt();

				removerPropsNoWorkspace(user, workspace);

				user.getProps().add(
					new UserPropsWorkspace(
						user, 
						workspace, 
						req.getProps().isActive(),
						invitedAt,
						acceptedAt
					)
				);
			}
		}
		
		return userRepo.save(user);
	}

	/**
	 * 
	 * @param user
	 * @param deletedBy
	 * @return
	 */
	public User remover(
		final User user, 
		final User deletedBy) 
	{
		user.setActive(false);
		user.setDeletedAt(ZonedDateTime.now());
		user.setDeletedBy(deletedBy);
		return userRepo.save(user);
	}

	/**
	 * 
	 * @param user
	 * @param workspace
	 * @return
	 */
	public User remover(
		final User user, 
		final Workspace workspace) 
	{
		removerGroupsNoWorkspace(user, workspace);

		removerPropsNoWorkspace(user, workspace);

		removerPerfisNoWorkspace(user, workspace);

		return userRepo.save(user);
	}

	/**
	 * 
	 * @param user
	 * @param workspace
	 * @return
	 */
	public UserSession aceitarConvite(
		final User user, 
		final Workspace workspace) 
	{
		if(!user.getWorkspaces().stream()
			.anyMatch(r -> r.getId().equals(workspace.getId())))
		{
			throw new UserException("Área de trabalho inexistente ou usuário não é membro dela");
		}

		var props = user.getProps(workspace.getId());
		if(props == null || props.getInvitedAt() == null)
		{
			throw new UserException("Convite inexistente");
		}

		if((ZonedDateTime.now().toEpochSecond() - props.getInvitedAt().toEpochSecond()) > INVITATION_EXPIRES_IN_SECS)
		{
			throw new UserException("Convite expirado");
		}
		
		if(props.getAcceptedAt() != null)
		{
			throw new UserException("Convite já aceito");
		}
		
		props.setAcceptedAt(ZonedDateTime.now());
		
		return new UserSession(userRepo.save(user), workspace);
	}

	/**
	 * 
	 * @param user
	 * @param idWorkspace
	 * @return
	 */
	public UserSession aceitarConvite(
		final User user, 
		final String idWorkspace) 
	{
		var workspace = user.getWorkspaces().stream()
			.filter(r -> r.getPubId().equals(idWorkspace))
				.findFirst()
					.orElseThrow(() -> new UserException("Área de trabalho inexistente ou usuário não é membro dela"));

		return aceitarConvite(user, workspace);
	}

	/**
	 * 
	 * @param user
	 * @param workspace
	 * @return
	 */
	public User recusarConvite(
		final User user, 
		final Workspace workspace) 
	{
		if(!user.getWorkspaces().stream()
			.anyMatch(r -> r.getId().equals(workspace.getId())))
		{
			throw new UserException("Área de trabalho inexistente ou usuário não é membro dela");
		}

		var props = user.getProps(workspace.getId());
		if(props == null || props.getInvitedAt() == null)
		{
			throw new UserException("Convite inexistente");
		}

		if(props.getAcceptedAt() != null)
		{
			throw new UserException("Convite já aceito");
		}

		return remover(user, workspace);
	}

	/**
	 * 
	 * @param user
	 * @param workspace
	 * @return
	 */
	public User abandonar(
		final User user, 
		final Workspace workspace) 
	{
		if(!user.getWorkspaces().stream()
			.anyMatch(r -> r.getId().equals(workspace.getId())))
		{
			throw new UserException("Área de trabalho inexistente ou usuário não é membro dela");
		}

		return remover(user, workspace);
	}

	/**
	 * 
	 * @param acesso
	 * @param user
	 * @param workspace
	 */
	public void validarAcesso(
		final AccessType acesso, 
		final User user, 
		final Workspace workspace) 
	{
		//TODO: implement validation
	}

	/**
	 * 
	 * @param acesso
	 * @param entity
	 * @param user
	 * @param workspace
	 */
	public void validarAcesso(
		final AccessType acesso, 
		final User entity,
		final User user, 
		final Workspace workspace) 
	{
		switch(acesso)
		{
			case DELETE:
			case UPDATE:
				if(entity.getId() != user.getId())
				{
					if(!user.isAdmin(workspace.getId()))
					{
						throw new AuthenticationException("Acesso não permitido");
					}
				}
				break;

			case READ:
				if(!user.isSuperAdmin())
				{
					if(entity.getId() != user.getId())
					{
						if(!user.isAdmin(workspace.getId()))
						{
							throw new AuthenticationException("Acesso não permitido");
						}
					}
				}
				break;

			default:
				break;
			
		}
	}
}
