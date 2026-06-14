package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.ZenClient;
import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;
import com.zenkai.zenclient.module.modules.movement.AutoScaffold;

/**
 * HUD widget — shows AutoScaffold status and remaining block count.
 *
 * Layout:
 *  ┌──────────────────────┐
 *  │  AutoScaffold   ON   │   ← purple accent when active, grey when off
 *  │  Blocks:        64   │   ← total placeable blocks in hotbar
 *  └──────────────────────┘
 *
 * Toggle visibility from the HUD Editor (ESC → drag/hide).
 */
public final class AutoScaffoldHud extends HudElement {

    private static final float PAD_X = 4f;
    private static final float PAD_Y = 3f;

    public AutoScaffoldHud() {
        super("AutoScaffold", 2, 200);
    }

    @Override
    public void render(float partialTicks) {
        AutoScaffold mod = ZenClient.getInstance()
                .getModuleManager().getModule(AutoScaffold.class);

        boolean active = mod != null && mod.isEnabled();
        int     blocks = (mod != null) ? mod.countHotbarBlocks() : 0;

        float fh  = RenderUtil.fontHeight();
        float row = fh + 2f;
        float w   = getWidth();
        float h   = getHeight();
        float x   = getX(), y = getY();

        RenderUtil.drawRoundedRect(x, y, w, h, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, Theme.BORDER_DIM);

        // Row 1: label + ON/OFF
        String statusLabel = active ? "ON"  : "OFF";
        int    statusColor = active ? Theme.ACCENT_LT : Theme.TEXT_HINT;
        RenderUtil.drawString("AutoScaffold", x + PAD_X, y + PAD_Y, Theme.TEXT_DIM, false);
        RenderUtil.drawString(statusLabel,
                x + w - PAD_X - RenderUtil.stringWidth(statusLabel),
                y + PAD_Y, statusColor, false);

        // Row 2: block count
        String blockStr = String.valueOf(blocks);
        int    blockCol = blocks > 0 ? Theme.TEXT : Theme.TEXT_HINT;
        RenderUtil.drawString("Blocks:", x + PAD_X, y + PAD_Y + row, Theme.TEXT_DIM, false);
        RenderUtil.drawString(blockStr,
                x + w - PAD_X - RenderUtil.stringWidth(blockStr),
                y + PAD_Y + row, blockCol, false);
    }

    @Override public float getWidth()  {
        return PAD_X + RenderUtil.stringWidth("AutoScaffold") + 8 + RenderUtil.stringWidth("OFF") + PAD_X;
    }
    @Override public float getHeight() {
        return PAD_Y + (RenderUtil.fontHeight() + 2) * 2 - 2 + PAD_Y;
    }
}
