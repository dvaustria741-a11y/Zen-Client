package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;
import com.zenkai.zenclient.module.modules.combat.ComboCounter;

/**
 * HUD widget — displays the current PvP combo count.
 * Fades in/out based on recent activity (2-second idle = hide).
 */
public final class ComboHud extends HudElement {

    private static final float PAD_X = 5f;
    private static final float PAD_Y = 3f;
    private static final long  IDLE_MS = 3000L;

    public ComboHud() {
        super("Combo", 100, 100);
    }

    @Override
    public void render(float partialTicks) {
        int  combo  = ComboCounter.getCombo();
        long lastHit = ComboCounter.getLastHit();

        if (combo == 0 && System.currentTimeMillis() - lastHit > IDLE_MS) return;

        float fh = RenderUtil.fontHeight();
        float w  = getWidth();
        float h  = getHeight();
        float x  = getX(), y = getY();

        RenderUtil.drawRoundedRect(x, y, w, h, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, Theme.BORDER_DIM);

        // "COMBO" label
        String label = "COMBO";
        RenderUtil.drawString(label, x + PAD_X, y + PAD_Y, Theme.TEXT_DIM, false);

        // Big combo number — colour scales: white → yellow → orange → purple
        int   col   = combo >= 10 ? Theme.ACCENT_LT
                    : combo >=  5 ? 0xFFFFAA00
                    :               Theme.TEXT;
        String num  = String.valueOf(combo);
        float  numX = x + w - PAD_X - RenderUtil.stringWidth(num);
        RenderUtil.drawString(num, numX, y + PAD_Y, col, combo >= 5);
    }

    @Override public float getWidth()  { return PAD_X + RenderUtil.stringWidth("COMBO") + 12 + RenderUtil.stringWidth("000") + PAD_X; }
    @Override public float getHeight() { return PAD_Y + RenderUtil.fontHeight() + PAD_Y; }
}
