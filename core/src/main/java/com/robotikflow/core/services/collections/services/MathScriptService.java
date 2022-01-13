package com.robotikflow.core.services.collections.services;

public class MathScriptService
{
	public Integer max(Integer a, Integer b)
	{
		return Math.max(a, b);
	}
	
	public Long max(Long a, Long b)
	{
		return Math.max(a, b);
	}
	
	public Double max(Double a, Double b)
	{
		return Math.max(a, b);
	}

	public Double max(Integer a, Double b)
	{
		return Math.max((double)a, b);
	}

	public Double max(Double a, Integer b)
	{
		return Math.max(a, (double)b);
	}

	public Double max(Long a, Double b)
	{
		return Math.max((double)a, b);
	}

	public Double max(Double a, Long b)
	{
		return Math.max(a, (double)b);
	}

	public Integer min(Integer a, Integer b)
	{
		return Math.min(a, b);
	}
	
	public Long min(Long a, Long b)
	{
		return Math.min(a, b);
	}
	
	public Double min(Double a, Double b)
	{
		return Math.min(a, b);
	}

	public Double min(Double a, Long b)
	{
		return Math.min(a, (double)b);
	}

	public Double min(Long a, Double b)
	{
		return Math.min((double)a, b);
	}

	public Double min(Double a, Integer b)
	{
		return Math.min(a, (double)b);
	}

	public Double min(Integer a, Double b)
	{
		return Math.min((double)a, b);
	}
}

