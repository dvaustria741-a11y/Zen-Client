package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;

/**
 * HUD widget — current frames per second.
 *
 * {@code Minecraft.debugFPS} is private in the MCP mappings used for this
 * project, so the FPS is instead tracked by counting render frames over a
 * rolling 1-second window.
 */
public final class FpsHud extends HudElement {

    private static final float PAD_X = 4f;
    private static final float PAD_Y = 3f;

    private int  frameCount = 0;
    private int  fps        = 0;
    private long windowStart = System.currentTimeMillis();

    public FpsHud() {
        super("FPS", 2, 2);
    }

    @Override
    public void render(float partialTicks) {
        frameCount++;

        long now = System.currentTimeMillis();
        if (now - windowStart >= 1000L) {
            fps         = frameCount;
            frameCount  = 0;
            windowStart = now;
        }

        int    color = fps >= 60 ? 0xFF55FF55      // green  — smooth
                     : fps >= 30 ? 0xFFFFFF55      // yellow — ok
                     :             0xFFFF5555;     // red    — poor

        String label  = "FPS";
        String value  = String.valueOf(fps);
        float  lw     = RenderUtil.stringWidth(label);
        float  vw     = RenderUtil.stringWidth(value);
        float  fh     = RenderUtil.fontHeight();
        float  w      = PAD_X + lw + 4 + vw + PAD_X;
        float  h      = PAD_Y + fh + PAD_Y;
        float  x      = getX(), y = getY();

        RenderUtil.drawRoundedRect(x, y, w, h, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, Theme.BORDER_DIM);

        RenderUtil.drawString(label, x + PAD_X,          y + PAD_Y, Theme.TEXT_DIM, false);
        RenderUtil.drawString(value, x + PAD_X + lw + 4, y + PAD_Y, color,          false);
    }

    @Override public float getWidth()  { return PAD_X + RenderUtil.stringWidth("FPS") + 4 + RenderUtil.stringWidth("000") + PAD_X; }
    @Override public float getHeight() { return PAD_Y + RenderUtil.fontHeight() + PAD_Y; }
}
