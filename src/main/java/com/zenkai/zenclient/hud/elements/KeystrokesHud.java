package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * HUD widget — live WASD / Space / LMB / RMB keystroke display.
 *
 * Layout (K = key cell side, G = gap):
 * <pre>
 *        [W]
 *      [A][S][D]
 *       [SPACE]
 *     [LMB][RMB]
 * </pre>
 *
 * Key state is polled directly from LWJGL each render frame.
 * Mouse buttons are also polled so no event bus subscription is needed.
 */
public final class KeystrokesHud extends HudElement {

    // Layout constants (GUI pixels)
    private static final float K   = 15f;   // key cell side
    private static final float G   =  2f;   // gap between cells
    private static final float PAD =  3f;   // outer padding

    // Total size (3 columns × K + 2 × G + 2 × PAD)
    private static final float W = PAD + K * 3 + G * 2 + PAD;
    private static final float H = PAD + K * 4 + G * 3 + PAD;

    public KeystrokesHud() {
        super("Keystrokes", 2, 80);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(float partialTicks) {

        // --- Poll key states ---
        int wKey  = mc.gameSettings.keyBindForward.getKeyCode();
        int aKey  = mc.gameSettings.keyBindLeft.getKeyCode();
        int sKey  = mc.gameSettings.keyBindBack.getKeyCode();
        int dKey  = mc.gameSettings.keyBindRight.getKeyCode();
        int spcKey = mc.gameSettings.keyBindJump.getKeyCode();

        boolean wDown   = Keyboard.isKeyDown(wKey);
        boolean aDown   = Keyboard.isKeyDown(aKey);
        boolean sDown   = Keyboard.isKeyDown(sKey);
        boolean dDown   = Keyboard.isKeyDown(dKey);
        boolean spcDown = Keyboard.isKeyDown(spcKey);
        boolean lDown   = Mouse.isButtonDown(0);
        boolean rDown   = Mouse.isButtonDown(1);

        float ox = getX(), oy = getY();

        // Background
        RenderUtil.drawRoundedRect(ox, oy, W, H, 4, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(ox, oy, W, H, 4, 0.5f, Theme.BORDER_DIM);

        // Row 0 — W (centred over ASD)
        float r0y = oy + PAD;
        float wX  = ox + PAD + K + G;
        drawKey(wX, r0y, K, K, "W", wDown);

        // Row 1 — A S D
        float r1y = oy + PAD + K + G;
        drawKey(ox + PAD,         r1y, K, K, "A", aDown);
        drawKey(ox + PAD + K + G, r1y, K, K, "S", sDown);
        drawKey(ox + PAD + K*2 + G*2, r1y, K, K, "D", dDown);

        // Row 2 — SPACE (full width)
        float r2y  = oy + PAD + K*2 + G*2;
        float spcW = K * 3 + G * 2;
        drawKey(ox + PAD, r2y, spcW, K, "SPC", spcDown);

        // Row 3 — LMB / RMB (split)
        float r3y  = oy + PAD + K*3 + G*3;
        float mbW  = (spcW - G) / 2f;
        drawKey(ox + PAD,          r3y, mbW, K, "LMB", lDown);
        drawKey(ox + PAD + mbW + G, r3y, mbW, K, "RMB", rDown);
    }

    // ── Key cell ──────────────────────────────────────────────────────────────

    private void drawKey(float x, float y, float w, float h,
                         String label, boolean pressed) {

        int bg      = pressed ? Theme.ACCENT         : Theme.BG_SETTING;
        int outline = pressed ? Theme.ACCENT_LT      : Theme.BORDER_DIM;
        int text    = pressed ? Theme.TEXT            : Theme.TEXT_DIM;

        RenderUtil.drawRoundedRect(x, y, w, h, 3, bg);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, outline);

        float fh = RenderUtil.fontHeight();
        float tx = x + (w - RenderUtil.stringWidth(label)) / 2f;
        float ty = y + (h - fh) / 2f;

        RenderUtil.drawString(label, tx, ty, text, false);
    }

    @Override public float getWidth()  { return W; }
    @Override public float getHeight() { return H; }
}
