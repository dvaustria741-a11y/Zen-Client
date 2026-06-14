package com.zenkai.zenclient.event.events;

import com.zenkai.zenclient.event.Event;

/** Fired when any keyboard key is pressed. */
public class EventKey extends Event {

    private final int keyCode;

    public EventKey(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyCode() { return keyCode; }
}
