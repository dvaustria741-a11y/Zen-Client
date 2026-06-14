package com.zenkai.zenclient.module.modules.utility;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

/**
 * AutoSoup — automatically switches to and consumes mushroom stew from the
 * hotbar when the player's health drops below the configured threshold.
 *
 * Standard UHC/SkyWars style: hotbar is searched left-to-right for the first
 * soup, the slot is selected, the item is used, and the previous slot is
 * restored the following tick.
 */
public final class AutoSoup extends Module {

    private final NumberSetting hpThreshold = addSetting(
            new NumberSetting("HP Threshold", "Eat soup when HP falls below this", 15, 1, 19, 1));

    private int prevSlot = -1;

    public AutoSoup() {
        super("Auto Soup", "Eats mushroom stew when low on health.", Category.UTILITY, Keyboard.KEY_NONE);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        prevSlot = -1;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre() || mc.thePlayer == null || mc.theWorld == null) return;

        // Restore previous slot after eating (one tick after the eat)
        if (prevSlot != -1) {
            mc.thePlayer.inventory.currentItem = prevSlot;
            prevSlot = -1;
            return;
        }

        float hp = mc.thePlayer.getHealth();
        if (hp > hpThreshold.getValue()) return;

        // Scan hotbar (slots 0-8) for the first available soup
        int soupSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemSoup) {
                soupSlot = i;
                break;
            }
        }

        if (soupSlot == -1) return;

        // Save current slot, switch to soup, use it
        prevSlot = mc.thePlayer.inventory.currentItem;
        mc.thePlayer.inventory.currentItem = soupSlot;
        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld,
                mc.thePlayer.inventory.getStackInSlot(soupSlot));
    }
}
