package com.robotikflow.core.services.providers.email;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.microsoft.graph.models.extensions.EmailAddress;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.ItemBody;
import com.microsoft.graph.models.extensions.Message;
import com.microsoft.graph.models.extensions.Recipient;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.IMessageDeltaCollectionPage;
import com.robotikflow.core.interfaces.props.FileProps;
import com.robotikflow.core.models.entities.Provider;

public class OutlookProvider 
    extends EmailBaseProvider
{
    public static final String name = "outlookProvider";

    private IGraphServiceClient _client;
    
	private static String OUTLOOK_INBOX = "inbox";

    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
        super.initialize(provider);
		
        var credentialService = credentialServiceFactory.buildByPubId(
            provider.getFields().getString("credential"), 
            provider.getWorkspace());

        _client = (IGraphServiceClient)credentialService.getClient();
    }

	private IGraphServiceClient getClient()
	{
		return _client;
	}
   
    @Override
    public Object sendMessage(
        final String from,
		final String to,
		final String subject,
		final String body) 
        throws Exception
    {
		
		var client = getClient();

		client.me()
			.sendMail(
				createMessage(
                    from, 
                    to, 
					subject, 
					body
                ),
				false)
			.buildRequest()
			.post();
        
        return null;
	}

    @Override
    public Message createMessage(
	    final String from,	
        final String to, 
		final String subject, 
		final String body) 
	{
		var message = new Message();

		message.subject = subject;

		var itemBody = new ItemBody();
		itemBody.contentType = BodyType.TEXT;
		itemBody.content = body;
		message.body = itemBody;
		
		var toRecipientsList = new LinkedList<Recipient>();
		var toRecipient = new Recipient();
		var emailAddress = new EmailAddress();
		emailAddress.address = to;
		toRecipient.emailAddress = emailAddress;
		toRecipientsList.add(toRecipient);
		message.toRecipients = toRecipientsList;

		return message;
	}

    @Override
	public List<Map<String, Object>> sync(
		final Map<String, Object> params,
		final Map<String, Object> state) 
		throws Exception 
    {
        IMessageDeltaCollectionPage page;

        var client = getClient();
        
        if(state.get("cursor") == null)
        {
            page = client.me()
                .mailFolders(OUTLOOK_INBOX)
                .messages()
                .delta()
                .buildRequest(Arrays.asList(
                    new QueryOption(
                        "$filter", 
                        String.format(
                            "receivedDateTime+ge+%s", 
                            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT))
                    )))
                .select("sender,subject")
                .get();
        }
        else
        {
            page = client.me()
                .mailFolders(OUTLOOK_INBOX)
                .messages()
                .delta((String)state.get("cursor"))
                .buildRequest()
                .select("sender,subject")
                .get();
        }

		var items = new ArrayList<Map<String, Object>>();
        
        while(page != null)
        {
            var messages = page.getCurrentPage();
            if(messages != null)
            {
                for(var message : messages)
                {
                    var body = client
                        .customRequest(String.format(
                            "/me/messages/%s/$value", message.id), 
                            String.class)
                        .buildRequest()
                        .get();
                    /*var res = service.me()
                        .messages(message.id)
                        .buildRequest()
                        .get();*/
                    
                    var props = new HashMap<String, Object>() {{
                        put("sender", message.sender.emailAddress.address);
                        put("subject", message.subject);
                        put("body", body);
                    }};

                    var rawBody = body.getBytes();

                    var makeCopy = params.containsKey("makeCopy") && 
                        (boolean)params.get("makeCopy") &&
                        rawBody != null &&
                        rawBody.length > 0;
                    
                    if(makeCopy)
                    {
                        var fileProps = new FileProps(
                            null,
                            "email", 
                            null, 
                            null, 
                            "eml", 
                            null, 
                            (long)rawBody.length, 
                            ZonedDateTime.now(), 
                            null, 
                            null, 
                            null);

                        var files = new ArrayList<FileProps>() {{
                            add(fileProps);
                        }};

                        props.put("files", files);
                    }
                
                    items.add(props);
                }
            }

            var delta = page.deltaLink();
            if(delta != null)
            {
                state.put("cursor", delta);
                break;
            }
                
            page = page
                .getNextPage()
                .buildRequest()
                .get();
        }

		return items;
	}
}
