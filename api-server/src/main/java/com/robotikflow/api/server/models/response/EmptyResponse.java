package com.robotikflow.api.server.models.response;

public class EmptyResponse 
{
    private final String nop = "NOP";

    public EmptyResponse() 
	{
    }

    public String getNop() 
	{
        return this.nop;
    }
}

