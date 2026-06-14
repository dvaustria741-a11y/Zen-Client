package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;
import net.minecraft.item.ItemStack;

/**
 * HUD widget — displays armour slots with durability bars.
 * Slots rendered top-to-bottom: helmet, chestplate, leggings, boots.
 */
public final class ArmorHud extends HudElement {

    private static final float PAD_X  = 4f;
    private static final float PAD_Y  = 3f;
    private static final float ROW    = 12f;
    private static final float GAP    = 2f;

    private static final String[] LABELS = { "H", "C", "L", "B" };

    public ArmorHud() {
        super("Armor", 2, 130);
    }

    @Override
    public void render(float partialTicks) {
        if (mc.thePlayer == null) return;

        ItemStack[] armor = mc.thePlayer.inventory.armorInventory;
        float x = getX(), y = getY();
        float w = getWidth(), h = getHeight();

        RenderUtil.drawRoundedRect(x, y, w, h, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, Theme.BORDER_DIM);

        // Slots 3=helmet, 2=chest, 1=legs, 0=boots
        for (int i = 0; i < 4; i++) {
            ItemStack slot = armor[3 - i];
            float ry = y + PAD_Y + i * (ROW + GAP);

            // Slot label
            RenderUtil.drawString(LABELS[i], x + PAD_X, ry + 2f, Theme.TEXT_DIM, false);

            if (slot == null) {
                RenderUtil.drawString("--", x + PAD_X + 14f, ry + 2f, Theme.TEXT_HINT, false);
                continue;
            }

            // Item name (trimmed)
            String name = slot.getDisplayName();
            if (RenderUtil.stringWidth(name) > 55) {
                name = mc.fontRendererObj.trimStringToWidth(name, 55) + "..";
            }
            RenderUtil.drawString(name, x + PAD_X + 14f, ry + 2f, Theme.TEXT, false);

            // Durability bar
            if (slot.isItemStackDamageable()) {
                int maxDur = slot.getMaxDamage();
                int curDur = maxDur - slot.getItemDamage();
                float pct  = (float) curDur / maxDur;

                float barX = x + w - PAD_X - 30f;
                float barY = ry + ROW - 3f;
                RenderUtil.drawRect(barX, barY, 30f, 2f, 0xFF1E1A30);

                int col = pct > 0.5f ? 0xFF55FF55
                        : pct > 0.25f ? 0xFFFFFF55
                        :               0xFFFF5555;
                RenderUtil.drawRect(barX, barY, 30f * pct, 2f, col);
            }
        }
    }

    @Override public float getWidth()  { return PAD_X + 14f + 55f + 32f + PAD_X; }
    @Override public float getHeight() { return PAD_Y + (ROW + GAP) * 4 - GAP + PAD_Y; }
}
