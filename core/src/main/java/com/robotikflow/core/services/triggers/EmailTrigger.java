package com.robotikflow.core.services.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.interfaces.IEmailProviderService;
import com.robotikflow.core.interfaces.ITriggerService;
import com.robotikflow.core.models.entities.Trigger;
import com.robotikflow.core.models.schemas.expr.LogicalExpr;
import com.robotikflow.core.util.expr.Evaluator;

import org.springframework.beans.factory.annotation.Autowired;

public class EmailTrigger 
    implements ITriggerService
{
    public static final String name = "emailTrigger";

    private Trigger trigger;
    private static final ObjectMapper objectMapper = 
        new ObjectMapper();

    @Override
    public void initialize(
        final Trigger trigger) 
        throws Exception
    {
        this.trigger = trigger;
    }

    @Autowired
	private ProviderServiceFactory providerServiceFactory;

    @Override
    public List<Map<String, Object>> sync(
        final Map<String, Object> state) 
        throws Exception 
    {
        var fields = trigger.getFields();
        
        var providerService = (IEmailProviderService)providerServiceFactory.buildByPubId(
            (String)fields.get("provider"), trigger.getWorkspace());

        var items = providerService.sync(trigger.getFields(), state);

        return filter(trigger, items);
    }

    private List<Map<String, Object>> filter(
        final Trigger trigger, 
        final List<Map<String, Object>> items) 
        throws Exception
    {
        var toRemove = new ArrayList<Map<String, Object>>();

        if(items != null)
        {
            for(var item : items)        
            {
                if(!evalConditions(trigger, item))
                {
                    toRemove.add(item);
                }
            }

            items.removeAll(toRemove);
        }

        return items;
    }

	private boolean evalConditions(
		final Trigger trigger, 
		final Map<String, Object> item)
        throws Exception
	{
        var conditions = objectMapper.readValue(
            trigger.getFields().getString("conditions"), 
            new TypeReference<LogicalExpr<Map<String, Object>>>() {});
		        
        return Evaluator.logicalEval(
			conditions, cond ->
			{
				String field = null;
				switch((String)cond.get("field"))
				{
				case "BODY":
					field = (String)item.get("body");
					break;
				case "SENDER":
					field = (String)item.get("sender");
					break;
				case "SUBJECT":
					field = (String)item.get("subject");
					break;
				default:
					return false;
				}

				return field != null? 
					Pattern.matches((String)cond.get("value"), field):
					false;
			});
	}	

    
}
