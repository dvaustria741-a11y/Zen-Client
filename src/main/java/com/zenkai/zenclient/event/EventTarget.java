package com.zenkai.zenclient.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event listener.
 *
 * Usage:
 *   {@literal @}EventTarget
 *   public void onUpdate(EventUpdate event) { ... }
 *
 * The method must have exactly one parameter that extends {@link Event}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventTarget {
    /** Listener priority — higher runs first. */
    byte priority() default 0;
}
