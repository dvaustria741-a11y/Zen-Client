package com.zenkai.zenclient.module.modules.misc;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import org.lwjgl.input.Keyboard;

/**
 * AntiAFK — periodically rotates the player to avoid AFK kicks.
 */
public final class AntiAFK extends Module {

    private final NumberSetting interval = addSetting(
            new NumberSetting("Interval", "Seconds between actions", 30, 5, 120, 5));

    private int tickCounter = 0;
    private int rotDir      = 1;

    public AntiAFK() {
        super("AntiAFK", "Prevents AFK kick.", Category.MISC, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre() || mc.thePlayer == null) return;

        tickCounter++;
        int triggerTick = (int) (interval.getValue() * 20); // ticks per second = 20

        if (tickCounter >= triggerTick) {
            tickCounter = 0;
            mc.thePlayer.rotationYaw += 45 * rotDir;
            rotDir = -rotDir;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        tickCounter = 0;
    }
}
