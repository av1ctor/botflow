package com.robotikflow.core.interfaces;

import java.util.List;
import java.util.Map;

import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;
import com.robotikflow.core.services.formula.eval.EvalContext;

public interface IGenericApiMethodProviderService
	extends IProviderService
{
    List<Map<String, Object>> call(
        final String baseUrl,
        final Map<String, ScriptOrFunctionOrValue> params, 
        final Map<String, Object> state,
        final EvalContext scriptContext,
        final Workspace workspace) 
		throws Exception;

    Credential getCredential();

    Provider getApiProvider();
}
