package com.robotikflow.api.server.models.response;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.schemas.collection.Form;
import com.robotikflow.core.models.schemas.collection.FormElement;
import com.robotikflow.core.models.schemas.collection.FormElementType;
import com.robotikflow.core.models.schemas.collection.FormFieldElement;

public class CollectionFormResponse 
{
    private final CollectionSchemaFilteredResponse schema;
    
    public CollectionFormResponse(
        final CollectionWithSchema collection,
        final Form form
    )
    {
        var formFields = collectFields(form);
        var schema = collection.getSchemaObj();

        var fields = schema.getColumns().entrySet().stream()
            .filter(e -> formFields.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var classes = schema.getClasses() != null?
            schema.getClasses().entrySet().stream()
                .filter(e -> fields.values().stream()
                    .anyMatch(f -> f.getClass_().equals(e.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)):
            null;

        var refs = schema.getRefs() != null?
            schema.getRefs().entrySet().stream()
                .filter(e -> fields.values().stream()
                    .anyMatch(f -> f.getRef().getName().equals(e.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)):
            null;
                    
        this.schema = new CollectionSchemaFilteredResponse(
            schema, fields, refs, classes, List.of(form));
    }

    public CollectionSchemaFilteredResponse getSchema() {
        return schema;
    }

    private Set<String> collectFields(
        final List<FormElement> elements)
    {
        var res = new HashSet<String>();
        
        for(var elm : elements)
        {
            if(elm.getType() == FormElementType.field)   
            {
                res.add(((FormFieldElement)elm).getName());
            }

            if(elm.getElements() != null)
            {
                res.addAll(collectFields(elm.getElements()));
            }
        }

        return res;
    }

    private Set<String> collectFields(
        final Form form)
    {
        var res = new HashSet<String>();

        for(var step : form.getSteps())
        {
            if(step.getElements() != null)
            {
                res.addAll(collectFields(step.getElements()));
            }
        }

        return res;
    }
}
