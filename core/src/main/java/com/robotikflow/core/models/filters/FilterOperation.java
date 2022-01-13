package com.robotikflow.core.models.filters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FilterOperation 
{
	public FilterOperationType value() default FilterOperationType.EQ;
	
	public boolean isCaseInsensitive() default false;
}
