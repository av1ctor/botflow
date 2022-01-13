package com.robotikflow.core.models.schemas.app;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.schemas.app.bars.Bar;
import com.robotikflow.core.models.schemas.app.screens.Screen;

public class AppSchema 
{
	@NotNull
	private float version;
    @Valid
    private List<Bar> bars;
    @Valid
    private List<Screen> screens;
    
    public float getVersion() {
        return version;
    }
    public void setVersion(float version) {
        this.version = version;
    }
    public List<Bar> getBars() {
        return bars;
    }
    public void setBars(List<Bar> bars) {
        this.bars = bars;
    }
    public List<Screen> getScreens() {
        return screens;
    }
    public void setScreens(List<Screen> screens) {
        this.screens = screens;
    }
}
