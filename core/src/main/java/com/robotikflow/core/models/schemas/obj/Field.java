package com.robotikflow.core.models.schemas.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Field 
{
    private Integer index;
    @NotBlank
    private String title;
    @NotNull
    private FieldType type;
	private Boolean multiple;
    private String desc;
	private Integer width;
    private Boolean required;
    private Boolean disabled;
    private Boolean hidden;
    private Object default_;
    private String placeholder;
    @Valid
    private Map<@NotBlank String, @Valid Field> fields;
    @Valid
    private List<@NotNull FieldInput> inputs;

	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public FieldType getType() {
		return type;
	}
	public void setType(FieldType type) {
		this.type = type;
	}
	public boolean isMultiple() {
		return multiple != null && multiple.booleanValue()?
			true:
			false;
	}
	public Boolean getMultiple() {
		return multiple;
	}
	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public boolean isRequired() {
		return required != null && required.booleanValue()?
			true:
			false;
	}
	public Boolean getRequired() {
		return required;
	}
	public void setRequired(Boolean required) {
		this.required = required;
	}
	public boolean isDisabled() {
		return disabled != null && disabled.booleanValue()?
			true:
			false;
	}
	public Boolean getDisabled() {
		return disabled;
	}
	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}
	public boolean isHidden() {
		return hidden != null && hidden.booleanValue()?
			true:
			false;
	}
	public Boolean getHidden() {
		return hidden;
	}
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	public Object getDefault() {
		return default_;
	}
	public void setDefault(Object default_) {
		this.default_ = default_;
	}
	public String getPlaceholder() {
		return placeholder;
	}
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}
    public Map<String, Field> getFields() {
        return fields;
    }
    public void setFields(Map<String, Field> fields) {
        this.fields = fields;
    }
	public List<FieldInput> getInputs() {
		return inputs;
	}
	public void setInput(FieldInput input) 
        throws Exception 
    {
		if(this.inputs == null)
        {
            this.inputs = new ArrayList<FieldInput>() {{
				add(input);
			}};
        }
        else
        {
            if(this.inputs.size() != 1)
            {
                throw new Exception("A single input must be defined");
            }
            
            this.inputs.set(0, input);
        }
	}
	public void setInputs(List<FieldInput> inputs) 
        throws Exception
    {
		if(this.inputs != null)
        {
            throw new Exception("A single input was already defined");
        }
        this.inputs = inputs;
	}
	public void setValidate(List<FieldInputValidation> validate) 
        throws Exception 
    {
		if(this.inputs == null)
        {
            this.inputs = new ArrayList<FieldInput>() {{
				add(new FieldTextInput());
			}};
        }
        else
        {
            if(this.inputs.size() != 1)
            {
                throw new Exception("A single input must be defined");
            }
        }
		this.inputs.get(0).setValidate(validate);
	}
}
