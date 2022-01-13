package com.robotikflow.core.util.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MapToBlobConverter<X, Y>
{
	protected final static Logger logger = 
		LoggerFactory.getLogger(MapToBlobConverter.class);

	public byte[] convertToDatabaseColumn(Map<X, Y> meta) 
	{
		try 
		{
			if(meta == null)
			{
				return null;
			}

			try (
				var bos = new ByteArrayOutputStream(); 
				var oos = new ObjectOutputStream(bos)) 
			{
				oos.writeObject(meta);
				
				return bos.toByteArray();
			}

	    } 
		catch (IOException ex) 
		{
			logger.error("Serialization failed" + ex);
			return null;
	    }
	}
	
	public Map<X, Y> convertToEntityAttribute(byte[] dbData) 
	{
		try 
		{
			if(dbData == null)
			{
				return null;
			}
			
			try (
				var bis = new ByteArrayInputStream(dbData); 
				var ois = new ObjectInputStream(bis)) 
			{
				@SuppressWarnings("unchecked")
				var res = (Map<X, Y>)ois.readObject();
				return res;
			}
	    } 
		catch (IOException|ClassNotFoundException ex) 
		{
			logger.error("Deserialization failed:" + ex);
			return null;
	    }
	}
}