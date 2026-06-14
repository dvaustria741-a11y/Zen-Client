package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * HUD widget — real-world clock (HH:mm:ss).
 */
public final class ClockHud extends HudElement {

    private static final float PAD_X = 4f;
    private static final float PAD_Y = 3f;

    private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");

    public ClockHud() {
        super("Clock", 2, 180);
    }

    @Override
    public void render(float partialTicks) {
        String time = fmt.format(new Date());
        float  x    = getX(), y = getY();
        float  w    = getWidth(), h = getHeight();

        RenderUtil.drawRoundedRect(x, y, w, h, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, Theme.BORDER_DIM);

        RenderUtil.drawString("Time", x + PAD_X, y + PAD_Y, Theme.TEXT_DIM, false);
        float tw = RenderUtil.stringWidth(time);
        RenderUtil.drawString(time, x + w - PAD_X - tw, y + PAD_Y, Theme.TEXT_ACCENT, false);
    }

    @Override public float getWidth()  { return PAD_X + RenderUtil.stringWidth("Time") + 10 + RenderUtil.stringWidth("00:00:00") + PAD_X; }
    @Override public float getHeight() { return PAD_Y + RenderUtil.fontHeight() + PAD_Y; }
}
