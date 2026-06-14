package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;
import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * HUD widget — clicks per second for left and right mouse buttons.
 *
 * Clicks are sampled via LWJGL {@code Mouse.isButtonDown()} on every
 * render frame (rising-edge detection) and counted over a rolling 1-second
 * window so the display reacts instantly.
 */
public final class CpsHud extends HudElement {

    private static final float PAD_X     = 4f;
    private static final float PAD_Y     = 3f;
    private static final long  WINDOW_MS = 1000L;

    // Timestamps of recent clicks, kept in insertion order
    private final Deque<Long> leftClicks  = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();

    private boolean prevLeft  = false;
    private boolean prevRight = false;

    public CpsHud() {
        super("CPS", 2, 20);
    }

    // ── Sample + count ────────────────────────────────────────────────────────

    private int sample(Deque<Long> clicks, boolean down, boolean prev) {
        long now = System.currentTimeMillis();

        // Rising-edge → new click
        if (down && !prev) clicks.addLast(now);

        // Evict timestamps outside the 1-second window
        Iterator<Long> it = clicks.iterator();
        while (it.hasNext() && now - it.next() > WINDOW_MS) it.remove();

        return clicks.size();
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(float partialTicks) {
        boolean leftDown  = Mouse.isButtonDown(0);
        boolean rightDown = Mouse.isButtonDown(1);

        int lCps = sample(leftClicks,  leftDown,  prevLeft);
        int rCps = sample(rightClicks, rightDown, prevRight);

        prevLeft  = leftDown;
        prevRight = rightDown;

        float fh  = RenderUtil.fontHeight();
        float row = fh + 2f;              // row height including gap
        float w   = getWidth();
        float h   = getHeight();
        float x   = getX(), y = getY();

        RenderUtil.drawRoundedRect(x, y, w, h, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, Theme.BORDER_DIM);

        // Left CPS row
        String lLabel = "L-CPS";
        String lVal   = String.valueOf(lCps);
        int    lColor = leftDown ? Theme.ACCENT_LT : Theme.TEXT;
        RenderUtil.drawString(lLabel, x + PAD_X,                                y + PAD_Y,       Theme.TEXT_DIM, false);
        RenderUtil.drawString(lVal,   x + w - PAD_X - RenderUtil.stringWidth(lVal), y + PAD_Y,  lColor,         false);

        // Right CPS row
        String rLabel = "R-CPS";
        String rVal   = String.valueOf(rCps);
        int    rColor = rightDown ? Theme.ACCENT_LT : Theme.TEXT;
        RenderUtil.drawString(rLabel, x + PAD_X,                                y + PAD_Y + row, Theme.TEXT_DIM, false);
        RenderUtil.drawString(rVal,   x + w - PAD_X - RenderUtil.stringWidth(rVal), y + PAD_Y + row, rColor,    false);
    }

    @Override public float getWidth()  { return PAD_X + RenderUtil.stringWidth("L-CPS") + 12 + RenderUtil.stringWidth("00") + PAD_X; }
    @Override public float getHeight() { return PAD_Y + (RenderUtil.fontHeight() + 2) * 2 - 2 + PAD_Y; }
}
