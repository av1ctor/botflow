package com.robotikflow.core.services.activities;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.interfaces.IActivityService;
import com.robotikflow.core.interfaces.IGenericApiMethodProviderService;
import com.robotikflow.core.interfaces.IObjService;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;
import com.robotikflow.core.services.formula.eval.EvalContext;

import org.springframework.beans.factory.annotation.Autowired;

import javafx.util.Pair;

public class GenericApiMethodActivity 
    implements IActivityService
{
    public static final String name = "genericApiMethodActivity";

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
        final Map<String, Object> item,
        final EvalContext scriptContext) 
        throws Exception 
    {
        var fields = activity.getFields();
        
        var provider = providerRepo.findByPubIdAndWorkspace(
            fields.getString("provider"), activity.getWorkspace());
        
        var providerService = (IGenericApiMethodProviderService)providerServiceFactory.build(
            provider.getSchema().getName(), provider);

        @SuppressWarnings("unchecked")
        var params = ((Map<String, Object>)fields.get("params"));
            
        @SuppressWarnings("unchecked")
        var args = params != null? 
            params.entrySet().stream()
                .map(e -> new Pair<String, ScriptOrFunctionOrValue>(
                    e.getKey(), 
                    IObjService.mapToScriptOrFunctionOrValue((Map<String, Object>)e.getValue()))
                )
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())):
            new HashMap<String, ScriptOrFunctionOrValue>();

        var state = new HashMap<String, Object>();

        scriptContext.put(
			"cols", 
			item != null? 
				item: 
				new HashMap<String, Object>());

        return providerService.call(
            providerService.getApiProvider().getFields().getString("baseUrl"),
            args,
            state,
            scriptContext,
            activity.getWorkspace());
    }
    
}
