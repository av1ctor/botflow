package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.List;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.factories.CredentialServiceFactory;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.CredentialSchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.filters.CredentialFilter;
import com.robotikflow.core.models.filters.WorkspaceFilter;
import com.robotikflow.core.models.misc.ObjFields;
import com.robotikflow.core.models.repositories.CredentialRepository;
import com.robotikflow.core.models.repositories.CredentialSchemaRepository;
import com.robotikflow.core.models.request.CredentialRequest;
import com.robotikflow.core.models.oauth2.OAuth2Props;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CredentialService 
	extends ObjBaseService<CredentialSchema>
{
	@Autowired
	private CredentialRepository credentialRepo;
    @Autowired
    private CredentialSchemaRepository credentialSchemaRepo;
	@Autowired
	private CredentialServiceFactory credentialFactory;

	private ExampleMatcher credMatcher = ExampleMatcher.matching()
		.withIgnoreNullValues()
		.withIgnoreCase();

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Credential> findAllByWorkspace(
		final Workspace workspace, 
		final Pageable pageable) 
	{
		return credentialRepo
			.findAllByWorkspace(workspace, pageable);
	}

	/**
	 * 
	 * @param workspace
	 * @param filters
	 * @param pageable
	 * @return
	 */
	public List<Credential> findAllByWorkspace(
		final Workspace workspace, 
		final CredentialFilter filters,
		final Pageable pageable) 
	{
		filters.setWorkspace(new WorkspaceFilter(workspace.getId()));
		var example = Example.of(new Credential(filters), credMatcher);
		return credentialRepo
			.findAll(example, pageable)
				.getContent();
	}

	/**
	 * 
	 * @param pubId
	 * @param workspace
	 * @return
	 */
	public Credential findByPubIdAndWorkspace(
		final String pubId, 
		final Workspace workspace) 
	{
		return credentialRepo
			.findByPubIdAndWorkspace(pubId, workspace);
	}

	/**
	 * 
	 * @param id
	 * @param type
	 * @param code
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public Credential redeemTokens(
		final String pubId, 
		final String code, 
		final Workspace workspace) 
		throws Exception 
	{
		if(code == null)
		{
			return null;
		}

		var credential = findByPubIdAndWorkspace(pubId, workspace);
		
		var handler = credentialFactory.build(
			credential.getSchema().getName(), credential);

		var tokens = handler.redeemTokens(code);

		var fields = credential.getFields();
		fields.put("authorizationCode", code);
		fields.put("accessToken", tokens.getAccessToken());
		fields.put("refreshToken", tokens.getRefreshToken());
		fields.put("tokenExpiration", tokens.getTokenExpiration());

		return credentialRepo.save(credential);
	}

	/**
	 * 
	 * @param pubId
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public OAuth2Props getOAuth2Props(
		final String pubId, 
		final Workspace workspace) 
		throws Exception 
	{
		var handler = credentialFactory.buildByPubId(pubId, workspace);

		return handler.getOAuth2Props();
	}

	/**
	 * 
	 * @param pubId
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public String getOAuth2AuthorizationUrl(
		final String pubId, 
		final Workspace workspace) 
		throws Exception 
	{
		var handler = credentialFactory.buildByPubId(pubId, workspace);

		return handler.getOAuth2AuthUrl();
	}

	private void setCommonFields(
		final Credential cred,
		final CredentialSchema schema,
		final CredentialRequest req) 
	{
		super.setFields(cred, schema, req);
	}

	/**
	 * 
	 * @param id
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public Credential update(
		final String id, 
		final CredentialRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var cred = credentialRepo.findByPubIdAndWorkspace(id, workspace);
		if(cred == null)
		{
			throw new ObjException("Credential not found");
		}

		var schema = cred.getSchema();

		cred.setUpdatedBy(user);
		cred.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(cred, schema, req);

		return credentialRepo.save(cred);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public Credential create(
		final CredentialRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var cred = new Credential();

		var schema = findSchemaByPubId(req.getSchemaId());
		validate(schema, req.getFields());

		cred.setWorkspace(workspace);
		cred.setCreatedBy(user);
		cred.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(cred, schema, req);

		return credentialRepo.save(cred);
	}
	
	/**
	 * 
	 * @param schemaId
	 * @param fields
	 * @throws Exception
	 */
	public void validate(
		final String schemaId,
		final ObjFields fields) 
		throws Exception
	{
		var schema = findSchemaByPubId(schemaId);
		super.validate(schema, fields);
	}

	protected CredentialSchema findSchemaByPubId(
        final String pubId
    )
    {
        return credentialSchemaRepo.findByPubId(pubId);
    }
}
