package com.robotikflow.core.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.robotikflow.core.interfaces.IObjService;

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ObjBaseServiceFactory
{
	private static Logger logger = LoggerFactory.getLogger(ObjBaseServiceFactory.class);
	
	protected static Map<String,  Class<?>> scanComponents(
		final String packageName)
	{
		var res = new HashMap<String, Class<?>>();

		try
		{
			var scanner = new ClassPathScanningCandidateComponentProvider(false);
			scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
			var classes = scanner.findCandidateComponents(packageName);
			for (var bean: classes) 
			{
					var name = bean.getBeanClassName();
					Class<?> klass = Class.forName(name);
					if(IObjService.class.isAssignableFrom(klass))
					{
						try
						{
							var field = (String)klass.getField("name").get(null);
							res.put(field, klass);
						}
						catch(Exception ex)
						{
							logger.error(String.format("Class scanning failed for %s", name), ex);
						}
					}
				}
			}
		catch(Exception ex)
		{
			logger.error(String.format("Class scanning failed on package %s", packageName), ex);
			return null;
		}

		return res;
	}


}