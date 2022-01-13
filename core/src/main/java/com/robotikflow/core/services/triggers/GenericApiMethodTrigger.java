package com.robotikflow.core.services.triggers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.interfaces.IGenericApiMethodProviderService;
import com.robotikflow.core.interfaces.IObjService;
import com.robotikflow.core.interfaces.ITriggerService;
import com.robotikflow.core.models.entities.Trigger;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;
import com.robotikflow.core.services.collections.CollectionService;

import org.springframework.beans.factory.annotation.Autowired;

import javafx.util.Pair;

public class GenericApiMethodTrigger 
    implements ITriggerService
{
    public static final String name = "genericApiMethodTrigger";

	@Autowired
	private ProviderServiceFactory providerServiceFactory;
    @Autowired
    private ProviderRepository providerRepo;
    @Autowired
    private CollectionService collectionService;

    private Trigger trigger;

    @Override
    public void initialize(
        final Trigger trigger) 
        throws Exception
    {
        this.trigger = trigger;        
    }

    @Override
    public List<Map<String, Object>> sync(
        final Map<String, Object> state) 
        throws Exception 
    {
        var fields = trigger.getFields();
        
        var provider = providerRepo.findByPubIdAndWorkspace(
            (String)fields.get("provider"), trigger.getWorkspace());
        
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

        var scriptContext = collectionService.configScriptContext(null, null);

        scriptContext.put("credential", providerService.getCredential().getFields());

        return providerService.call(
            providerService.getApiProvider().getFields().getString("baseUrl"),
            args,
            state,
            scriptContext,
            trigger.getWorkspace());
    }
    
}
