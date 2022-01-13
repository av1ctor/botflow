package com.robotikflow.api.server.models.response;

import java.util.List;
import java.util.Map;

import com.robotikflow.core.models.schemas.collection.CollectionSchema;
import com.robotikflow.core.models.schemas.collection.Field;
import com.robotikflow.core.models.schemas.collection.Form;
import com.robotikflow.core.models.schemas.collection.Klass;
import com.robotikflow.core.models.schemas.collection.Ref;

public class CollectionSchemaFilteredResponse 
{
	private final float version;
    private final Map<String, Field> columns;
    private final Map<String, Ref> refs;
    private final Map<String, Klass> classes;
    private final List<Form> forms;

    public CollectionSchemaFilteredResponse(
        final CollectionSchema schema,
        final Map<String, Field> fields, 
        final Map<String, Ref> refs, 
        final Map<String, Klass> classes,
        final List<Form> forms) 
    {
        this.version = schema.getVersion();
        this.columns = fields;
        this.refs = refs;
        this.classes = classes;
        this.forms = forms;
    }
    
    public float getVersion() {
        return version;
    }
    public Map<String, Field> getColumns() {
        return columns;
    }
    public Map<String, Ref> getRefs() {
        return refs;
    }
    public Map<String, Klass> getClasses() {
        return classes;
    }
    public List<Form> getForms() {
        return forms;
    }
}