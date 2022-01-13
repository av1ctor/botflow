package com.robotikflow.core.services.activities;

import java.util.HashMap;
import java.util.Map;

import com.robotikflow.core.interfaces.IActivityService;
import com.robotikflow.core.interfaces.IObjService;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.core.services.formula.eval.EvalContext;

import org.springframework.beans.factory.annotation.Autowired;

public class UpdateCurrentItemActivity 
    implements IActivityService
{
    public static final String name = "updateCurrentItemActivity";

	@Autowired
	private CollectionService collectionService;

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
        var values = new HashMap<String, Object>();
        
        @SuppressWarnings("unchecked")
        var fields = (Map<String, Object>)
            activity.getFields().get("fields");
        if(fields != null)
        {
            for(var entry : fields.entrySet())
            {
                @SuppressWarnings("unchecked")
                var value = (Map<String, Object>)entry.getValue();

                var evaluated = collectionService
                    .evalValueOrScriptOrFunction(
                        IObjService.mapToScriptOrFunctionOrValue(value), 
                        scriptContext);

                values.put(entry.getKey(), evaluated);
            }
        }

        var user = collection.getCreatedBy();
        
        var ctx = collectionService.reconfigScriptContext(
            scriptContext, collection, user);
        
        collectionService
            .atualizarItem(collection, (String)item.get("_id"), item, ctx, values, user, true, null, true);

        return values;
    }
}
