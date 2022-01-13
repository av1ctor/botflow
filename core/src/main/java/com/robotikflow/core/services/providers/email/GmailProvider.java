package com.robotikflow.core.services.providers.email;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.Message;
import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.interfaces.props.FileProps;
import com.robotikflow.core.models.entities.Provider;

public class GmailProvider 
    extends EmailBaseProvider
{
    public static final String name = "gmailProvider";

    private ICredentialService credentialService;
    private Gmail _client;
    
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
        super.initialize(provider);
		
		credentialService = credentialServiceFactory.buildByPubId(
            provider.getFields().getString("credential"), 
            provider.getWorkspace());

        var googleCredential = (GoogleCredential)credentialService.getClient();
       
        _client = new Gmail.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), 
            JacksonFactory.getDefaultInstance(), 
            googleCredential)
			.setApplicationName("RobotikFlow")
            .build();		
	}

	private Gmail getClient() 
		throws Exception
	{
		credentialService.authenticate();
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
		getClient()
			.users().messages()
			.send(
				"me", 
				createMessage(
					getClient()
						.users().getProfile("me").getUserId(), 
                    to, 
					subject, 
					body))
				.execute();
		
        return null;
	}

	private static Message createMessageWithEmail(
        final MimeMessage emailContent)
        throws MessagingException, IOException 
    {
        var buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        var bytes = buffer.toByteArray();
        var encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        var message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }	
    
    @Override
	public Message createMessage(
        final String from, 
        final String to, 
        final String subject, 
        final String bodyText)
        throws MessagingException, IOException 
    {
        var props = new Properties();
        var session = Session.getDefaultInstance(props, null);
 
        var email = new MimeMessage(session);
 
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        
        email.setText(bodyText);
        
        return createMessageWithEmail(email);
    }	

    @Override
	public List<Map<String, Object>> sync(
		final Map<String, Object> params,
		final Map<String, Object> state) 
		throws Exception 
	{
		var client = getClient();
        
        if(state.get("lastId") == null)
		{
			var res = client
				.users()
					.messages()
						.list("me")
				.setMaxResults(1L)
				.execute();
			if(res.getMessages() != null && res.getMessages().size() > 0)
			{
				var ultimaMsgResponse = res.getMessages().get(0);
				var ultimaMsg = client
					.users()
						.messages()
							.get("me", ultimaMsgResponse.getId())
					.execute();
				
                state.put("lastId", ultimaMsg.getHistoryId().toString());
			}

			return null;
		}

		var items = new ArrayList<Map<String, Object>>();
		String pageToken = null; 

		do
		{
			ListHistoryResponse res = null;
			try
			{
				res = client
					.users()
						.history()
							.list("me")
					.setHistoryTypes(Arrays.asList("messageAdded"))
					.setStartHistoryId(new BigInteger((String)state.get("lastId")))
					.setPageToken(pageToken)
					.execute();
			}
			catch(GoogleJsonResponseException e)
			{
				if(e.getStatusCode() == 404)
				{
					var profile = client
						.users()
							.getProfile("me")
						.execute();

					state.put("lastId", profile.getHistoryId().toString());
					break;
				}
				else
				{
					throw e;
				}
			}
	
			if(res.getHistory() == null)
			{
				break;
			}

			for(var history: res.getHistory())
			{
				for(var msg: history.getMessagesAdded())
				{
					Message rfcMsg = null;
					try
					{
						rfcMsg = client
							.users()
								.messages()
									.get("me", msg.getMessage().getId())
							.setFormat("raw")
							.execute();
					}
					catch(GoogleJsonResponseException e)
					{
						if(e.getStatusCode() != 404)
						{
							throw new Exception(String.format("E-mail sync failed for %s", msg.getMessage().getId()), e);
						}
					}
					catch (Exception e) 
					{
						throw new Exception(String.format("E-mail sync failed for %s", msg.getMessage().getId()), e);
					}
			
					if(rfcMsg != null)
					{
						var emailProps = rfcToEmailProps(rfcMsg.decodeRaw());
						
						var props = new HashMap<String, Object>() {{
							put("sender", emailProps.sender.toString());
							put("subject", emailProps.subject.toString());
							put("body", emailProps.body.toString());
						}};

						var makeCopy = params.containsKey("makeCopy") && 
							(boolean)params.get("makeCopy") &&
							emailProps.rawBody != null &&
							emailProps.rawBody.length > 0;
						
						if(makeCopy)
						{
							var fileProps = new FileProps(
								null,
								"email", 
								null, 
								null, 
								"eml", 
								null, 
								(long)emailProps.rawBody.length, 
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
			}
						
			state.put("lastId", res.getHistoryId().toString());
			
			pageToken = res.getNextPageToken();
		} while(pageToken != null);
		
		return items;
	}
}
