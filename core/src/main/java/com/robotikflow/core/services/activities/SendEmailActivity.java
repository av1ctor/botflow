package com.robotikflow.core.services.activities;

import java.util.Map;

import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.interfaces.IActivityService;
import com.robotikflow.core.interfaces.IEmailProviderService;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.services.formula.eval.EvalContext;

import org.springframework.beans.factory.annotation.Autowired;

public class SendEmailActivity 
    implements IActivityService
{
    public static final String name = "sendEmailActivity";

	@Autowired
	private ProviderServiceFactory providerServiceFactory;
    @Autowired
    private ProviderRepository providerRepo;

    private Activity activity;

    @Override
    public void initialize(
        final Activity activity)
    {
        this.activity = activity;        
    }

    @Override
    public Object run(
        final CollectionWithSchema collection,
        final Map<String, Object> values,
        final EvalContext scriptContext) 
        throws Exception 
    {
        var fields = activity.getFields();
        
        var provider = providerRepo.findByPubIdAndWorkspace(
            fields.getString("provider"), activity.getWorkspace());
        
        var providerService = (IEmailProviderService)providerServiceFactory.buildById(
            provider.getId());

        return providerService.sendMessage(
            fields.getString("from") != null?
                fields.getString("from"):
                collection.getCreatedBy().getEmail(),
            fields.getString("to"),
            fields.getString("subject"),
            fields.getString("body"));
    }
    
}
