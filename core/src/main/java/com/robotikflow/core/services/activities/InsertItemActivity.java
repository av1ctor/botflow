package com.robotikflow.core.services.activities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robotikflow.core.interfaces.IActivityService;
import com.robotikflow.core.interfaces.IObjService;
import com.robotikflow.core.interfaces.props.FileProps;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.repositories.UserRepository;
import com.robotikflow.core.services.DocumentService;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.core.services.factories.DocumentServiceFactory;
import com.robotikflow.core.services.formula.eval.EvalContext;

import org.springframework.beans.factory.annotation.Autowired;

public class InsertItemActivity 
    implements IActivityService
{
    public static final String name = "insertItemActivity";

	@Autowired
	private CollectionService collectionService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private DocumentServiceFactory documentServiceFactory;
	@Autowired
	private UserRepository userRepo;

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
        final Map<String, Object> values,
        final EvalContext scriptContext) 
        throws Exception 
    {
        var workspace = collection.getWorkspace();
    
        var vars = new HashMap<String, Object>();
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
                    .evalValueOrScriptOrFieldOrFunction(
                        IObjService.mapToScriptOrFunctionOrFieldOrValue(value), 
                        values, 
                        scriptContext);
                vars.put(entry.getKey(), evaluated);
            }
        }

        var user = values.containsKey("creator")?
            userRepo
                .findByEmailAndIdWorkspace(
                    (String)values.get("creator"), 
                    workspace.getId()):
            collection.getCreatedBy();
        if(user == null)
        {
            user = collection.getCreatedBy();
        }
        
        var ctx = collectionService.reconfigScriptContext(
            scriptContext, collection, user);
        
        var item = collectionService
            .inserirItem(collection, ctx, vars, false, false, user, true, null, true);

        if(values.containsKey("files"))
        {
            var itemId = (String)item.get(collectionService.getIdName());
                        
            if(values.get("files") instanceof List<?>)
            {
                var dir = createDir(collection);

                @SuppressWarnings("unchecked")
                var files = (List<FileProps>)values.get("files");
                if(files != null)
                {
                    var index = 1;
                    for(var file : files)
                    {
                        Document document = null;
                        
                        if(file.contents == null)
                        {
                            document = documentService
                                .criarExterno(
                                    file.provider,
                                    file.id,
                                    file.path,
                                    file.name, 
                                    file.extension,
                                    file.mimeType,
                                    file.size,
                                    null,
                                    user, 
                                    file.createdAt,
                                    file.modifiedAt,
                                    file.url,
                                    workspace);
                        }
                        else
                        {
                            var fileName = String.format(
                                (file.name == null? "file-": file.name) + "%s-%s.%s", 
                                file.id != null? file.id: itemId,
                                index, 
                                file.extension);
                            
                            document = documentServiceFactory.build(collection.getProvider())
                                .criar(
                                    collection.getProvider(),
                                    fileName, 
                                    file.contents.length, 
                                    file.contents, 
                                    dir, 
                                    user, 
                                    collection.getWorkspace());
                        }
                    
                        collectionService
                            .adicionarDocumentAoItem(
                                document, 
                                collection, 
                                itemId, 
                                user,
                                collection.getWorkspace());
                    
                        ++index;
                    }   
                }
            }
        }

        return values;
    }

	private Document createDir(
		final CollectionWithSchema collection)
		throws Exception 
	{
		return documentServiceFactory.build(collection.getProvider())
			.criarDiretorio(
				collection.getProvider(),
				String.format("collections/%s/files", collection.getPubId()), 
				false,
				collection.getCreatedBy(), 
				collection.getWorkspace());
	}
}
