package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Method 
{
	@NotNull
	@Size(min=1, max=1024)
	private String script;

	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
}
