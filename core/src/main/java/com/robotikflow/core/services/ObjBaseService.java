package com.robotikflow.core.services;

import java.util.HashMap;
import java.util.Map;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.Obj;
import com.robotikflow.core.models.repositories.ObjSchemaRepository;
import com.robotikflow.core.models.repositories.ObjStateRepository;
import com.robotikflow.core.models.request.ObjRequest;
import com.robotikflow.core.models.entities.ObjSchema;
import com.robotikflow.core.models.entities.ObjState;
import com.robotikflow.core.models.misc.ObjFields;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class ObjBaseService<T extends ObjSchema>
{
    @Autowired
    private ObjSchemaRepository objSchemaRepo;
    @Autowired
    private ObjStateRepository objStateRepo;

    protected ObjSchema findSchemaByPubId(
        final String pubId
    )
    {
        return objSchemaRepo.findByPubId(pubId);
    }

    public ObjState createState()
    {
        return createState(new HashMap<String, Object>());
    }

    public ObjState createState(
        final Map<String, Object> fields
    )
    {
        var state = new ObjState();
        state.setState(fields);
        
        return objStateRepo.save(state);
    }

    protected void validate(
        final T schema, 
        final Map<String, Object> fields)
    {
        for(var entry : schema.getFields().entrySet())
        {
            var key = entry.getKey();
            var field = entry.getValue();
            Object value = null;
            if(!fields.containsKey(key))
            {
                if(field.isRequired())
                {
                    throw new ObjException(String.format("%s is obligatory", field.getTitle()));
                }
                
                if(field.getDefault() != null)
                {
                    value = field.getDefault();
                }
            }
            else
            {
                if(!field.isDisabled() && !field.isHidden())
                {
                    value = fields.get(key);
                }
            }

            //FIXME: validate value
        }
    }

    private ObjFields mixFields(
        final Map<String, Object> current,
        final Map<String, Object> fields,
        final T schema)
    {
        for(var entry : schema.getFields().entrySet())
        {
            var key = entry.getKey();
            var field = entry.getValue();

            if(field.isDisabled() || field.isHidden())
            {
                fields.put(
                    key, 
                    current != null?
                        current.get(key):
                        field.getDefault());
            }
            else if(!fields.containsKey(key))
            {
                fields.put(key, field.getDefault());
            }
        }

        return new ObjFields(fields);
    }

	protected void setFields(
        final Obj<T> obj,
        final T schema,
        final ObjRequest req) 
    {
        obj.setSchema(schema);
        obj.setFields(
            mixFields(
                obj.getId() != null? 
                    obj.getFields():
                    null, 
                req.getFields(), 
                schema));
    }
}
