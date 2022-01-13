package com.robotikflow.core.services.providers.email;

import java.io.ByteArrayInputStream;

import com.robotikflow.core.factories.CredentialServiceFactory;
import com.robotikflow.core.interfaces.IEmailProviderService;
import com.robotikflow.core.interfaces.props.EmailProps;
import com.robotikflow.core.models.entities.Provider;

import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EmailBaseProvider 
	implements IEmailProviderService
{
    @Autowired
    protected CredentialServiceFactory credentialServiceFactory;

	protected Provider provider;

    @Override
    public void initialize(
        final Provider provider) 
		throws Exception 
    {
		this.provider = provider;
	}
    
	protected EmailProps rfcToEmailProps(
		final byte[] raw) 
		throws Exception 
	{
		var props = new EmailProps(raw);

		if(raw != null && raw.length > 0)
		{
			var builder = new DefaultMessageBuilder();
			builder.setMimeEntityConfig(
				MimeConfig.custom()
				.setMaxLineLen(-1)
				.setMaxHeaderCount(-1)
				.setMaxHeaderLen(-1)
				.build());
			var mimeMsg = builder.parseMessage(new ByteArrayInputStream(raw));

			if(mimeMsg.getFrom() != null)
			{
				for(var sender: mimeMsg.getFrom())
				{
					if(props.sender.length() > 0)
					{
						props.sender.append(',');
					}
					props.sender.append(sender.getAddress());
				}
			}
			
			props.subject.append(mimeMsg.getSubject());
			
			if(mimeMsg.getBody() != null)
			{
				if(mimeMsg.getBody() instanceof SingleBody)
				{
					var body = (SingleBody)mimeMsg.getBody();
					var part = body.getInputStream();
					props.body.append(new String(part.readAllBytes()));
				}
				else if(mimeMsg.getBody() instanceof Multipart)
				{
					var body = (Multipart)mimeMsg.getBody();
					var entities = body.getBodyParts();
					for(var entity : entities)
					{
						if(entity.getMimeType().equals("text/plain") || entity.getMimeType().equals("text/html"))
						{
							var part = ((SingleBody)entity.getBody()).getInputStream();
							props.body.append(new String(part.readAllBytes()));
						}
					}
				}
			}
			
		}

		return props;
	}
}
