package com.robotikflow.core.interfaces;

import java.util.Map;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.Obj;
import com.robotikflow.core.models.schemas.collection.valuetype.FunctionNames;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrFieldOrValue;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;

public interface IObjService<T extends Obj<?>>
{
     void initialize(
        final T obj)
        throws Exception;
    
    default Object execute(
        final String operation, 
        final Map<String, Object> params)
        throws Exception
    {
        throw new ObjException("Unknown operation");
    }

    static ScriptOrFunctionOrValue mapToScriptOrFunctionOrValue(
        final Map<String, Object> map)
    {
        var value = new ScriptOrFunctionOrValue();
        
        if(map.containsKey("function"))
        {
            value.setFunction(FunctionNames.valueOf((String)map.get("function")));
        }
        else if(map.containsKey("script"))
        {
            value.setScript((String)map.get("script"));
        }
        else if(map.containsKey("text"))
        {
            value.setValue(map.get("text"));
        }
        else
        {
            value.setValue(map.get("value"));
        }

        return value;
    }

    static ScriptOrFunctionOrFieldOrValue mapToScriptOrFunctionOrFieldOrValue(
        final Map<String, Object> map)
    {
        if(!map.containsKey("field"))
        {
            return new ScriptOrFunctionOrFieldOrValue(
                mapToScriptOrFunctionOrValue(map));
        }

        var value = new ScriptOrFunctionOrFieldOrValue();
        value.setField((String)map.get("field"));
        return value;
    }

}
