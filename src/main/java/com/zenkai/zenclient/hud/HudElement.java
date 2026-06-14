package com.zenkai.zenclient.hud;

import net.minecraft.client.Minecraft;

/**
 * Base class for every draggable HUD widget.
 *
 * Subclasses implement {@link #render(float)}, {@link #getWidth()}, and
 * {@link #getHeight()}.  Position is managed by {@link HudManager} and
 * persisted to disk between sessions.
 */
public abstract class HudElement {

    /** Minecraft shortcut available to every subclass. */
    protected static final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private float   x, y;
    private boolean visible = true;

    protected HudElement(String name, float defaultX, float defaultY) {
        this.name = name;
        this.x    = defaultX;
        this.y    = defaultY;
    }

    // ── Abstract interface ────────────────────────────────────────────────────

    /** Draw the widget at its current {@link #getX()}/{@link #getY()} position. */
    public abstract void render(float partialTicks);

    /** Current pixel width (used for drag hit-testing and editor outline). */
    public abstract float getWidth();

    /** Current pixel height (used for drag hit-testing and editor outline). */
    public abstract float getHeight();

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String  getName()    { return name;    }
    public float   getX()       { return x;       }
    public float   getY()       { return y;       }
    public boolean isVisible()  { return visible; }

    public void setX(float x)         { this.x       = x; }
    public void setY(float y)         { this.y       = y; }
    public void setVisible(boolean v) { this.visible = v; }

    /** AABB point-in-rect test in scaled GUI pixels. */
    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + getWidth()
            && my >= y && my <= y + getHeight();
    }
}
