package com.robotikflow.core.models.schemas.app.screens.elements;

import javax.validation.constraints.NotBlank;

public class TextScreenElement 
    extends ScreenElement
{
    @NotBlank
    private String text;
    private int size = 12;
    private String color;
    private String backgroundColor;
    private String justify;
    private String align;
    private boolean bold = false;

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public String getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public String getJustify() {
        return justify;
    }
    public void setJustify(String justify) {
        this.justify = justify;
    }
    public String getAlign() {
        return align;
    }
    public void setAlign(String align) {
        this.align = align;
    }
    public boolean isBold() {
        return bold;
    }
    public void setBold(boolean bold) {
        this.bold = bold;
    }
}
