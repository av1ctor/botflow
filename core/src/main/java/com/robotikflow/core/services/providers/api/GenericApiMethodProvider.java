package com.robotikflow.core.services.providers.api;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.interfaces.IGenericApiMethodProviderService;
import com.robotikflow.core.interfaces.IGenericApiProviderService;
import com.robotikflow.core.interfaces.IObjService;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.misc.ObjFields;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.core.services.formula.eval.EvalContext;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class GenericApiMethodProvider 
    implements IGenericApiMethodProviderService
{
    public static final String name = "genericApiMethodProvider";
    
    @Autowired
    private ProviderServiceFactory providerServiceFactory;
    @Autowired
    private CollectionService collectionService;
    
    private Provider provider;
    private IGenericApiProviderService apiProviderService;
    private static final ObjectMapper objMapper = new ObjectMapper()
        .findAndRegisterModules();
        
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		this.provider = provider;
		
        apiProviderService = (IGenericApiProviderService)providerServiceFactory.buildByPubId(
            provider.getFields().getString("provider"), 
            provider.getWorkspace());
    }

    @Override
    public Credential getCredential() {
        return apiProviderService.getCredential();
    }

    @Override
    public Provider getApiProvider() {
        return apiProviderService.getProvider();
    }

    @Override
    public List<Map<String, Object>> call(
        final String baseUrl,
        final Map<String, ScriptOrFunctionOrValue> params, 
        final Map<String, Object> state,
        final EvalContext scriptContext,
        final Workspace workspace) 
        throws Exception 
    {
        var fields = provider.getFields();

        @SuppressWarnings("unchecked")
        var vars = state.containsKey("vars")? 
            new ObjFields((Map<String, Object>)state.get("vars")): 
            new ObjFields();

        configScripting(
            params, 
            vars, 
            fields.getInt("reruns"),
            scriptContext);

        //
        var pre = evalMap(fields.getMap("preReqVars"), scriptContext);
        vars.putAll(pre);
        
        //
        var bodyFields = fields.getMap("bodyFields");
        var body = bodyFields != null?
            expandMap(evalMap(bodyFields, scriptContext)):
            null;

        // request

        @SuppressWarnings("unchecked")
        var header = (Map<String, Object>)fields.get("headerFields");

        var tree = jsonToTree(apiProviderService.request(
            encodeUrl(resolveUrl(baseUrl, fields.getString("url"), scriptContext)), 
            remapMethod(fields.getString("httpMethod")), 
            remapContentType(fields.getString("reqContentType")), 
            remapContentType(fields.getString("respContentType")),
            remapHeaders(header, scriptContext), 
            body));

        // process response
        var response = new ArrayList<Map<String, Object>>();

        var root = fields.getString("respNodePath") != null?
            walkTree(tree, fields.getString("respNodePath")):
            tree;
    
        if(fields.getBoolean("respIsArray"))
        {
            if(root != null)
            {
                var in = root.elements();
                while(in.hasNext())
                {
                    var elm = in.next();
                    var fieldsExtracted = extractFieldsUsingArray(fields.get("respFields"), elm);
                    response.add(fieldsExtracted);
                    //++cnt;
                }
            }
            
            scriptContext.put("response", response);
        }
        else
        {
            if(root != null)
            {
                var fieldsExtracted = extractFieldsUsingArray(fields.get("respFields"), root);
                response.add(fieldsExtracted);
                scriptContext.put("response", fieldsExtracted);
            }
            else
            {
                scriptContext.put("response", new HashMap<String, Object>());
            }
        }

        //
        var pos = evalMap(fields.getMap("postReqVars"), scriptContext);
        vars.putAll(pos);
   
        //
        state.put("vars", vars);        
        
        return response;
    }

    private HttpMethod remapMethod(
        final String method)
    {
        switch(method)
        {
        case "DELETE":
            return HttpMethod.DELETE;
        case "GET":
            return HttpMethod.GET;
        case "PATCH":
            return HttpMethod.PATCH;
        case "POST":
            return HttpMethod.POST;
        case "PUT":
            return HttpMethod.PUT;
        default:
            return null;
        }
    }

    private MediaType remapContentType(
        final String type)
    {
        switch(type)
        {
        case "FORM_DATA":
            return MediaType.APPLICATION_FORM_URLENCODED;
        case "JSON":
            return MediaType.APPLICATION_JSON;
        case "XML":
            return MediaType.APPLICATION_XML;
        default:
            return null;
        }
    }    

    private URI encodeUrl(
        final String src) 
        throws Exception
    {
        var url = new URL(src);
        var uri =  new URI(
            url.getProtocol(), 
            url.getUserInfo(), 
            url.getHost(), 
            url.getPort(), 
            url.getPath(), 
            url.getQuery(), 
            url.getRef());
        return uri;
	}

	private String templateToString(
        final String template,
        final EvalContext ctx)
    {
        var sub = new StringSubstitutor(new StringLookup(){
            @Override
            public String lookup(String key) {
                var value = collectionService.execScript(key, ctx);
                return value != null? 
                    value.toString():
                    "";
            }
        });

        return template != null? 
            sub.replace(template):
            null;
    }
    
    private String resolveUrl(
        final String baseUrl,
        final String url,
        final EvalContext ctx)
    {
        if(url.indexOf("://") >= 0)
        {
            return templateToString(url, ctx);
        }

        return templateToString(baseUrl + url, ctx);
    }

    private void configScripting(
        final Map<String, ScriptOrFunctionOrValue> params,
        final ObjFields vars,
        final int reruns,
        final EvalContext scriptContext
    )
    {
        //
        var args = new HashMap<String, Object>();
        for(var entry : params.entrySet())
        {
            args.put(
                entry.getKey(), 
                collectionService.evalValueOrScriptOrFunction(
                    entry.getValue(), scriptContext));
        }
        scriptContext.put("params", args);
        
        //
        scriptContext.put("vars", vars);

        //
        var ctx = new HashMap<String, Object>();
        ctx.put("reruns", reruns);
        scriptContext.put("ctx", ctx);
    }

	private Map<String, Object> evalMap(
        final Map<String, Object> map,
        final EvalContext ctx)
    {
        var res = new HashMap<String, Object>();

        if(map != null)
        {
            for(var entry: map.entrySet())
            {
                @SuppressWarnings("unchecked")
                var value = (Map<String, Object>)entry.getValue();
                
                res.put(
                    entry.getKey(), 
                    collectionService.evalValueOrScriptOrFunction(
                        IObjService.mapToScriptOrFunctionOrValue(value), ctx));
            }
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> expandMap(
        final Map<String, Object> map
    )
    {
        var root = new HashMap<String, Object>();
        if(map == null)
        {
            return root;
        }

        for(var entry : map.entrySet())
        {
            var path = entry.getKey();
            var value = entry.getValue();

            var node = root;
            var dot = path.indexOf('.');
            while(dot >= 0)
            {
                var field = path.substring(0, dot);
                var obj = node.get(field);
                if(obj == null)
                {
                    var hm = new HashMap<String, Object>();
                    node.put(field, hm);
                    node = hm;
                }
                else
                {
                    if(!(obj instanceof HashMap<?,?>))
                    {
                        throw new ObjException(
                            String.format("Object not of type Map<?,?> at %s", path));
                    }

                    node = (HashMap<String, Object>)obj;
                }

                path = path.substring(dot+1);
                dot = path.indexOf('.');
            }

            node.put(path, value);
        }

        return root;
    }

    private Map<String, Object> extractFields(
        final Map<String, Object> fields, 
        final JsonNode elm) 
    {
        var res = new HashMap<String, Object>();
        for(var entry : fields.entrySet())
        {
            var path = entry.getKey();
            var value = walkTree(elm, path);
            res.put(path, value.asText());
        }
        return res;
    }

    private Map<String, Object> extractFieldsUsingArray(
        final Object array, 
        final JsonNode elm) 
    {
        var res = new HashMap<String, Object>();

        @SuppressWarnings("unchecked")
        var list = (List<Map<String, Object>>)array;

        for(var item : list)
        {
            var path = (String)item.get("key");
            var value = walkTree(elm, path);
            res.put(path, value.asText());
        }

        return res;
    }

    private JsonNode jsonToTree(
        final String src
    )
    {
        try 
        {
            return src != null?
                objMapper.readTree(src):
                null;
        } 
        catch (IOException e) 
        {
			return null;
		}
    }

    private JsonNode walkTree(
        JsonNode root,
        String path
    )
    {
        if(root == null || path == null)
        {
            return root;
        }

        var dot = path.indexOf('.');
        while(dot >= 0)
        {
            var field = path.substring(0, dot);
            root = root.get(field);
            if(root == null)
            {
                return null;
            }
            path = path.substring(dot+1);
            dot = path.indexOf('.');
        }

        return root.get(path);
    }

    private HttpHeaders remapHeaders(
        final Map<String, Object> headers,
        final EvalContext ctx) 
    {
        var res = new HttpHeaders();
        if(headers != null)
        {
            var map = evalMap(headers, ctx);
            for(var entry : map.entrySet())
            {
                var value = entry.getValue();
                res.add(entry.getKey(), value != null? value.toString(): null);
            }
        }
        return res;
	}    
}