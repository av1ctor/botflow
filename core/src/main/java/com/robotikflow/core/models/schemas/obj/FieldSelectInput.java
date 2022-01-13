package com.robotikflow.core.models.schemas.obj;

import java.util.List;

import javax.validation.Valid;

public class FieldSelectInput
	extends FieldInput
{
    @Valid
    private List<FieldSelectInputOption> options;
	@Valid
	private FieldSelectInputSource source;

    public FieldSelectInput()
    {
        super(FieldInputType.select);
    }

	public void setOptions(List<FieldSelectInputOption> options) {
		this.options = options;
	}
	public List<FieldSelectInputOption> getOptions() {
		return options;
	}
	public FieldSelectInputSource getSource() {
		return source;
	}
	public void setSource(FieldSelectInputSource source) {
		this.source = source;
	}
}
