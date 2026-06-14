package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;
import net.minecraft.client.Minecraft;

/**
 * HUD widget — current frames per second.
 *
 * Reads {@code Minecraft.debugFPS} (public static int, updated by the engine
 * each second) so the value matches what the F3 debug screen shows.
 */
public final class FpsHud extends HudElement {

    private static final float PAD_X = 4f;
    private static final float PAD_Y = 3f;

    public FpsHud() {
        super("FPS", 2, 2);
    }

    @Override
    public void render(float partialTicks) {
        int    fps  = Minecraft.debugFPS;
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
