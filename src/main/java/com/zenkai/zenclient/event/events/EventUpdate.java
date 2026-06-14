package com.zenkai.zenclient.event.events;

import com.zenkai.zenclient.event.Event;

/** Fired on the client update tick (pre/post). */
public class EventUpdate extends Event {

    public enum Stage { PRE, POST }

    private final Stage stage;

    public EventUpdate(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() { return stage; }

    public boolean isPre()  { return stage == Stage.PRE; }
    public boolean isPost() { return stage == Stage.POST; }
}
