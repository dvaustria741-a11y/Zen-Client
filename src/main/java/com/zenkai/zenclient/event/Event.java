package com.zenkai.zenclient.event;

/**
 * Base class for all Zen Client events.
 * Extend this to create new event types.
 */
public abstract class Event {

    private boolean cancelled = false;

    /** Whether this event can be cancelled by a listener. */
    public boolean isCancellable() {
        return false;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (!isCancellable()) {
            throw new UnsupportedOperationException("Event " + getClass().getSimpleName() + " is not cancellable.");
        }
        this.cancelled = cancelled;
    }

    /** Convenience cancel method — only works on cancellable events. */
    public void cancel() {
        setCancelled(true);
    }
}
