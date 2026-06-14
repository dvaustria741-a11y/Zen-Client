package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;

/**
 * HUD widget — compass strip showing cardinal directions.
 * The current heading is centred with neighbouring directions fading out.
 */
public final class DirectionHud extends HudElement {

    private static final float W   = 120f;
    private static final float H   = 20f;
    private static final float PAD = 4f;

    private static final String[] DIRS  = { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };
    private static final float    SPAN  = 360f / DIRS.length;

    public DirectionHud() {
        super("Direction", 200, 2);
    }

    @Override
    public void render(float partialTicks) {
        if (mc.thePlayer == null) return;

        float yaw = mc.thePlayer.rotationYaw % 360f;
        if (yaw < 0) yaw += 360f;

        float x = getX(), y = getY();
        RenderUtil.drawRoundedRect(x, y, W, H, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, W, H, 3, 0.5f, Theme.BORDER_DIM);

        float cx = x + W / 2f;
        float ty = y + (H - RenderUtil.fontHeight()) / 2f;

        // Draw a window of ±90° around the current yaw
        for (int i = 0; i < DIRS.length; i++) {
            float dirYaw = i * SPAN;
            float delta  = dirYaw - yaw;
            // Wrap to -180..180
            while (delta >  180) delta -= 360;
            while (delta < -180) delta += 360;

            if (Math.abs(delta) > 90) continue;

            float px     = cx + delta * (W / 2f) / 90f;
            String label = DIRS[i];
            float  lw    = RenderUtil.stringWidth(label);

            float dist  = Math.abs(delta) / 90f;
            boolean main = label.length() == 1;
            int col = main
                    ? (dist < 0.15f ? Theme.ACCENT_LT : blendColor(Theme.TEXT, Theme.TEXT_HINT, dist))
                    : blendColor(Theme.TEXT_DIM, 0x004A4860, dist);

            RenderUtil.drawString(label, px - lw / 2f, ty, col, dist < 0.15f && main);
        }

        // Centre tick
        RenderUtil.drawRect(cx - 0.5f, y + H - 3f, 1f, 3f, Theme.ACCENT);
    }

    private static int blendColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar=(a>>16)&0xFF, ag=(a>>8)&0xFF, ab=a&0xFF, aa=(a>>24)&0xFF;
        int br=(b>>16)&0xFF, bg=(b>>8)&0xFF, bb=b&0xFF, ba=(b>>24)&0xFF;
        return ((int)(aa+(ba-aa)*t)<<24)|((int)(ar+(br-ar)*t)<<16)|((int)(ag+(bg-ag)*t)<<8)|(int)(ab+(bb-ab)*t);
    }

    @Override public float getWidth()  { return W; }
    @Override public float getHeight() { return H; }
}
