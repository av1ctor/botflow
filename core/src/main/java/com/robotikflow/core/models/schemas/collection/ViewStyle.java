package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

public class ViewStyle 
{
	@NotNull
	private Short height;
    private String color;
    private Boolean striped;
	private ViewBorder border;

    public Short getHeight() {
        return height;
    }

    public void setHeight(Short height) {
        this.height = height;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getStriped() {
        return striped;
    }

    public void setStriped(Boolean striped) {
        this.striped = striped;
    }

    public ViewBorder getBorder() {
        return border;
    }

    public void setBorder(ViewBorder border) {
        this.border = border;
    }
}
