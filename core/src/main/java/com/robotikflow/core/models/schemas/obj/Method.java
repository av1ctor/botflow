package com.robotikflow.core.models.schemas.obj;

import javax.validation.constraints.NotBlank;

public class Method 
{
    @NotBlank
    private String script;

	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
}
