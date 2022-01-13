package com.robotikflow.core.interfaces;

import java.util.Map;

import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.services.formula.eval.EvalContext;

public interface IActivityService 
    extends IObjService<Activity>
{
    Object run(
        final CollectionWithSchema collection,
        final Map<String, Object> item,
        final EvalContext scriptContext) 
        throws Exception;
}
