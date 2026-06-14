package com.zenkai.zenclient.event.events;

import com.zenkai.zenclient.event.Event;

/** Fired whenever a mouse button is pressed or released. */
public class EventMouse extends Event {

    private final int     button;   // 0 = left, 1 = right, 2 = middle
    private final boolean pressed;

    public EventMouse(int button, boolean pressed) {
        this.button  = button;
        this.pressed = pressed;
    }

    public int     getButton()  { return button;  }
    public boolean isPressed()  { return pressed; }
}
