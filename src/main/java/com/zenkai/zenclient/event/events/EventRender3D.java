package com.zenkai.zenclient.event.events;

import com.zenkai.zenclient.event.Event;

/** Fired during the 3-D world render pass. */
public class EventRender3D extends Event {

    private final float partialTicks;

    public EventRender3D(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() { return partialTicks; }
}
