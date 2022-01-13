package com.robotikflow.core.services.triggers;

import java.util.List;
import java.util.Map;

import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.interfaces.ITriggerService;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.entities.Trigger;

import org.springframework.beans.factory.annotation.Autowired;

public class StorageTrigger 
    implements ITriggerService
{
    public static final String name = "storageTrigger";

	@Autowired
	private ProviderServiceFactory providerFactory;

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
        
        var provider = (IStorageProviderService)providerFactory.buildByPubId(
            (String)fields.get("provider"), trigger.getWorkspace());

        return provider.sync(
            (String)fields.get("path"),
            DocumentOperationType.valueOf((String)fields.get("op")),
            trigger.getWorkspace(),
            state);
    }
    
}
