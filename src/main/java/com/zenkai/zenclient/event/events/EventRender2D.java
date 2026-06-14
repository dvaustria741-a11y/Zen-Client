package com.zenkai.zenclient.event.events;

import com.zenkai.zenclient.event.Event;
import net.minecraft.client.gui.ScaledResolution;

/** Fired when the 2-D HUD is being rendered. */
public class EventRender2D extends Event {

    private final ScaledResolution scaledResolution;
    private final float            partialTicks;

    public EventRender2D(ScaledResolution scaledResolution, float partialTicks) {
        this.scaledResolution = scaledResolution;
        this.partialTicks     = partialTicks;
    }

    public ScaledResolution getScaledResolution() { return scaledResolution; }
    public float            getPartialTicks()     { return partialTicks; }

    public int getScreenWidth()  { return scaledResolution.getScaledWidth(); }
    public int getScreenHeight() { return scaledResolution.getScaledHeight(); }
}
