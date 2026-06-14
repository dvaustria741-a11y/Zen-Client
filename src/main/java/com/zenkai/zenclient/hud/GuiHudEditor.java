package com.zenkai.zenclient.hud;

import com.zenkai.zenclient.ZenClient;
import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * Full-screen drag editor for HUD elements.
 *
 * Open with the HOME key during normal play.
 * Drag any element to reposition it.
 * ESC saves positions and returns to game.
 */
public final class GuiHudEditor extends GuiScreen {

    private HudElement dragging;
    private float      dragOffX, dragOffY;

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        // Semi-transparent full-screen dim
        RenderUtil.drawRect(0, 0, width, height, Theme.BG_OVERLAY);

        // Centre hint bar
        String hint = "HUD Editor  \u00b7  Drag to reposition  \u00b7  ESC to save & close";
        float hintX = (width  - RenderUtil.stringWidth(hint)) / 2f;
        RenderUtil.drawRect(0, 0, width, RenderUtil.fontHeight() + 8, 0xCC060012);
        RenderUtil.drawString(hint, hintX, 4, Theme.TEXT_DIM, true);

        HudManager hm = ZenClient.getInstance().getHudManager();

        for (HudElement el : hm.getElements()) {

            // Render the element itself
            el.render(partialTicks);

            float ex = el.getX(), ey = el.getY();
            float ew = el.getWidth(), eh = el.getHeight();

            boolean hovered = mouseX >= ex && mouseX <= ex + ew
                           && mouseY >= ey && mouseY <= ey + eh;

            // Outline colour depends on drag/hover state
            int outline = (el == dragging) ? Theme.ACCENT
                        : hovered          ? Theme.ACCENT_LT
                        :                    Theme.BORDER_DIM;

            RenderUtil.drawRoundedRectOutline(ex, ey, ew, eh, 3, 1f, outline);

            // Name label centred above the element
            String label = el.getName();
            RenderUtil.drawString(label,
                    ex + (ew - RenderUtil.stringWidth(label)) / 2f,
                    ey - RenderUtil.fontHeight() - 2,
                    Theme.TEXT_ACCENT, true);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ── Dragging ──────────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        if (button != 0) return;
        HudManager hm = ZenClient.getInstance().getHudManager();
        for (HudElement el : hm.getElements()) {
            if (el.contains(x, y)) {
                dragging = el;
                dragOffX = x - el.getX();
                dragOffY = y - el.getY();
                return;
            }
        }
    }

    @Override
    protected void mouseReleased(int x, int y, int button) {
        if (button == 0) dragging = null;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (dragging == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        // Convert raw display coordinates to scaled GUI coordinates
        float mx = Mouse.getX() * sr.getScaledWidth()  / (float) mc.displayWidth;
        float my = (mc.displayHeight - Mouse.getY() - 1)
                 * sr.getScaledHeight() / (float) mc.displayHeight;

        float nx = mx - dragOffX;
        float ny = my - dragOffY;

        // Clamp to screen bounds
        nx = Math.max(0, Math.min(sr.getScaledWidth()  - dragging.getWidth(),  nx));
        ny = Math.max(0, Math.min(sr.getScaledHeight() - dragging.getHeight(), ny));

        dragging.setX(nx);
        dragging.setY(ny);
    }

    // ── Close ─────────────────────────────────────────────────────────────────

    @Override
    public void onGuiClosed() {
        ZenClient.getInstance().saveHudPositions();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
