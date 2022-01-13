package com.robotikflow.core.interfaces;

import java.util.List;
import java.util.Map;

public interface IEmailProviderService
	extends IProviderService
{
	Object createMessage(
        final String from, 
        final String to, 
        final String subject, 
        final String bodyText)
		throws Exception;

	Object sendMessage(
		final String from,
		final String to,
		final String subject,
		final String body)
		throws Exception;
	
	List<Map<String, Object>> sync(
        final Map<String, Object> params,
        final Map<String, Object> state) 
		throws Exception;
	
}
