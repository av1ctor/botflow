package com.robotikflow.core.util.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ObjectToBlobConverter<T> 
{
	protected final static Logger logger = 
		LoggerFactory.getLogger(ObjectToBlobConverter.class);

	public byte[] convertToDatabaseColumn(T meta) 
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
			logger.error("Serialization failed:" + ex);
			return null;
	    }
	}
	
	public T convertToEntityAttribute(byte[] dbData, Class<T> clazz) 
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
				var res = (T)ois.readObject();
				return res;
			}
	    } 
		catch (IOException|ClassNotFoundException ex) 
		{
			logger.error("Deserialization failed" + ex);
			return null;
	    }
	}
}