package com.zenkai.zenclient.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Zen Client Event Bus.
 *
 * Subscribers register themselves via {@link #subscribe(Object)}.
 * Any method annotated with {@link EventTarget} whose single parameter
 * is a subclass of {@link Event} will be invoked when that event is posted.
 *
 * Thread-safe via CopyOnWriteArrayList per event type.
 */
public final class EventBus {

    /** Wrapper pairing a subscriber instance with one of its listener methods. */
    private static final class ListenerEntry {
        final Object   instance;
        final Method   method;
        final byte     priority;

        ListenerEntry(Object instance, Method method, byte priority) {
            this.instance = instance;
            this.method   = method;
            this.priority = priority;
            method.setAccessible(true);
        }
    }

    private final Map<Class<? extends Event>, CopyOnWriteArrayList<ListenerEntry>> registry
            = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------
    // Registration
    // -----------------------------------------------------------------------

    /** Register all {@link EventTarget}-annotated methods on {@code subscriber}. */
    public void subscribe(Object subscriber) {
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventTarget.class)) continue;
            if (method.getParameterCount() != 1)               continue;

            Class<?> paramType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(paramType))      continue;

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) paramType;

            byte priority = method.getAnnotation(EventTarget.class).priority();
            ListenerEntry entry = new ListenerEntry(subscriber, method, priority);

            registry.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                    .add(entry);

            // Keep list sorted by priority descending
            registry.get(eventClass).sort(Comparator.comparingInt((ListenerEntry e) -> e.priority).reversed());
        }
    }

    /** Remove all listeners belonging to {@code subscriber}. */
    public void unsubscribe(Object subscriber) {
        for (CopyOnWriteArrayList<ListenerEntry> listeners : registry.values()) {
            listeners.removeIf(e -> e.instance == subscriber);
        }
    }

    // -----------------------------------------------------------------------
    // Dispatch
    // -----------------------------------------------------------------------

    /**
     * Post {@code event} to all registered listeners.
     * @return the event after all listeners have processed it
     */
    public <T extends Event> T post(T event) {
        CopyOnWriteArrayList<ListenerEntry> listeners = registry.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) return event;

        for (ListenerEntry entry : listeners) {
            try {
                entry.method.invoke(entry.instance, event);
            } catch (Exception ex) {
                System.err.println("[ZenClient/EventBus] Exception in listener "
                        + entry.instance.getClass().getSimpleName()
                        + "#" + entry.method.getName() + ": " + ex.getMessage());
                ex.printStackTrace();
            }

            // Stop dispatch if cancelled
            if (event.isCancellable() && event.isCancelled()) break;
        }

        return event;
    }
}
