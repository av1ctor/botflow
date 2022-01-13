package com.robotikflow.api.server.models.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class OffsetLimitRequest 
    extends PageRequest
{
    private static final long serialVersionUID = 1L;
    private int offset;

    public OffsetLimitRequest(
        int offset, 
        int limit, 
        Sort sort)
    {
        super(offset, limit, sort);
        this.offset = offset;
    }

    public OffsetLimitRequest(
        int offset, 
        int limit)
    {
        this(offset, limit, Sort.unsorted());
    }

    public static OffsetLimitRequest of(
        int offset, 
        int limit, 
        Sort sort) 
    {
		return new OffsetLimitRequest(offset, limit, sort);
	}

    @Override
    public long getOffset() 
    {
        return this.offset;
    }    
}