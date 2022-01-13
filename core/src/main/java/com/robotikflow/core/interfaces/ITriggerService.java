package com.robotikflow.core.interfaces;

import java.util.List;
import java.util.Map;

import com.robotikflow.core.models.entities.Trigger;

public interface ITriggerService 
    extends IObjService<Trigger>
{
    List<Map<String, Object>> sync(
		final Map<String, Object> state) 
		throws Exception;
}
