package com.zenkai.zenclient.gui.util;

/**
 * Animation helpers used throughout the ClickGUI.
 */
public final class AnimationUtil {

    private AnimationUtil() {}

    // ── Interpolation ────────────────────────────────────────────────────────

    /** Linear interpolation — move {@code current} toward {@code target} by {@code speed} (0-1). */
    public static float lerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    /** Snap to target if within {@code threshold} to avoid infinite micro-steps. */
    public static float lerpSnap(float current, float target, float speed, float threshold) {
        float next = lerp(current, target, speed);
        return Math.abs(next - target) < threshold ? target : next;
    }

    // ── Easing ───────────────────────────────────────────────────────────────

    /** Ease-out cubic: fast start, slow end. */
    public static float easeOut(float t) {
        t = Math.max(0f, Math.min(1f, t));
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }

    /** Ease-in cubic: slow start, fast end. */
    public static float easeIn(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * t;
    }

    /** Ease-in-out quad. */
    public static float easeInOut(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
    }

    /** Overshoot spring: goes slightly past 1.0 then settles. */
    public static float spring(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return (float)(Math.sin(t * Math.PI * (0.2f + 2.5f * t * t * t))
                       * Math.pow(1f - t, 2.2f) + t)
               * (1f + 1.2f * (1f - t));
    }

    // ── Time helpers ─────────────────────────────────────────────────────────

    /** Normalise {@code elapsed} milliseconds against {@code durationMs} → 0..1. */
    public static float progress(long startMs, long durationMs) {
        float t = (System.currentTimeMillis() - startMs) / (float) durationMs;
        return Math.max(0f, Math.min(1f, t));
    }
}
