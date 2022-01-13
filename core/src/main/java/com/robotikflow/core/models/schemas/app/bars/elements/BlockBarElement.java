package com.robotikflow.core.models.schemas.app.bars.elements;

import javax.validation.Valid;

public class BlockBarElement 
    extends BarElement
{
    @Valid
    private BarElementWidths widths = new BarElementWidths();
    private String align;

    public BarElementWidths getWidths() {
        return widths;
    }
    public void setWidths(BarElementWidths widths) {
        this.widths = widths;
    }
    public String getAlign() {
        return align;
    }
    public void setAlign(String align) {
        this.align = align;
    }
}
