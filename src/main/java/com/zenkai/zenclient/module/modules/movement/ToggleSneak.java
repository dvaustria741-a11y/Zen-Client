package com.zenkai.zenclient.module.modules.movement;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * ToggleSneak — forces the player to sneak continuously while enabled,
 * so the sneak key does not need to be held down.
 */
public final class ToggleSneak extends Module {

    public ToggleSneak() {
        super("ToggleSneak", "Sneaks continuously while enabled.", Category.MOVEMENT, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;
        if (mc.thePlayer == null) return;

        mc.thePlayer.setSneaking(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.setSneaking(false);
        }
    }
}
