package com.zenkai.zenclient.module.modules.movement;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * ToggleSprint — forces the player to sprint continuously while enabled,
 * regardless of movement input.
 */
public final class ToggleSprint extends Module {

    public ToggleSprint() {
        super("ToggleSprint", "Sprints continuously while enabled.", Category.MOVEMENT, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;
        if (mc.thePlayer == null) return;

        mc.thePlayer.setSprinting(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.setSprinting(false);
        }
    }
}
