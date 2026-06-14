package com.zenkai.zenclient.module.modules.movement;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.ModeSetting;
import org.lwjgl.input.Keyboard;

/**
 * Sprint — keeps the player sprinting automatically.
 */
public final class Sprint extends Module {

    private final ModeSetting mode = addSetting(
            new ModeSetting("Mode", "Sprinting mode", "Legit", "Legit", "OmniSprint"));

    public Sprint() {
        super("Sprint", "Automatically sprints.", Category.MOVEMENT, Keyboard.KEY_V);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;
        if (mc.thePlayer == null) return;

        boolean moving = mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0;

        if (mode.is("Legit")) {
            // Only sprint when moving forward, mimicking vanilla behaviour
            if (mc.thePlayer.moveForward > 0) {
                mc.thePlayer.setSprinting(true);
            }
        } else {
            // OmniSprint: sprint in any movement direction
            if (moving) {
                mc.thePlayer.setSprinting(true);
            }
        }
    }
}
