package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;

/**
 * HUD widget — player coordinates (X / Y / Z) and facing direction.
 *
 * Values are taken from {@code mc.thePlayer} each frame, so they update
 * smoothly with partial-tick interpolation.
 */
public final class CoordsHud extends HudElement {

    private static final float PAD_X = 4f;
    private static final float PAD_Y = 3f;
    private static final float ROW_GAP = 2f;

    public CoordsHud() {
        super("Coords", 2, 40);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(float partialTicks) {
        if (mc.thePlayer == null) return;

        // Interpolate position for smooth sub-tick movement
        double px = mc.thePlayer.prevPosX
                  + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * partialTicks;
        double py = mc.thePlayer.prevPosY
                  + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * partialTicks;
        double pz = mc.thePlayer.prevPosZ
                  + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * partialTicks;

        String xStr = String.format("X: %.1f", px);
        String yStr = String.format("Y: %.1f", py);
        String zStr = String.format("Z: %.1f", pz);
        String fStr = "F: " + facing(mc.thePlayer.rotationYaw);

        float fh  = RenderUtil.fontHeight();
        float row = fh + ROW_GAP;
        float w   = getWidth();
        float h   = getHeight();
        float x   = getX(), y = getY();

        RenderUtil.drawRoundedRect(x, y, w, h, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, Theme.BORDER_DIM);

        // Axis labels in dim colour, values right-aligned
        drawRow(xStr, 0xFF5599FF, x, y + PAD_Y,            w);
        drawRow(yStr, 0xFF55FF99, x, y + PAD_Y + row,      w);
        drawRow(zStr, 0xFFFF5555, x, y + PAD_Y + row * 2,  w);
        drawRow(fStr, Theme.TEXT_ACCENT, x, y + PAD_Y + row * 3, w);
    }

    private void drawRow(String text, int color, float x, float y, float w) {
        // Label (first 2 chars "X:") dimmed, rest in color
        String[] parts = text.split(" ", 2);
        RenderUtil.drawString(parts[0] + " ", x + PAD_X, y, Theme.TEXT_DIM, false);
        if (parts.length > 1) {
            float lx = x + PAD_X + RenderUtil.stringWidth(parts[0] + " ");
            RenderUtil.drawString(parts[1], lx, y, color, false);
        }
    }

    /** Convert yaw to cardinal direction string. */
    private static String facing(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw <  22.5f || yaw >= 337.5f) return "S";
        if (yaw <  67.5f) return "SW";
        if (yaw < 112.5f) return "W";
        if (yaw < 157.5f) return "NW";
        if (yaw < 202.5f) return "N";
        if (yaw < 247.5f) return "NE";
        if (yaw < 292.5f) return "E";
        return "SE";
    }

    @Override public float getWidth() {
        return PAD_X + RenderUtil.stringWidth("X: ")
             + RenderUtil.stringWidth("-00000.0")
             + PAD_X;
    }
    @Override public float getHeight() {
        return PAD_Y + (RenderUtil.fontHeight() + ROW_GAP) * 4 - ROW_GAP + PAD_Y;
    }
}
