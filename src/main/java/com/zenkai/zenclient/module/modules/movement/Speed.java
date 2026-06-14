package com.zenkai.zenclient.module.modules.movement;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventMotion;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.ModeSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import org.lwjgl.input.Keyboard;

/**
 * Speed — increases horizontal player velocity.
 */
public final class Speed extends Module {

    private final ModeSetting   mode  = addSetting(new ModeSetting  ("Mode",  "Speed type", "Strafe", "Strafe", "Vanilla"));
    private final NumberSetting speed = addSetting(new NumberSetting ("Speed", "Speed multiplier", 1.3, 1.0, 5.0, 0.05));

    public Speed() {
        super("Speed", "Move faster.", Category.MOVEMENT, Keyboard.KEY_X);
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (!event.isPre()) return;
        if (mc.thePlayer == null) return;
        if (!mc.thePlayer.onGround) return;

        if (mode.is("Vanilla")) {
            mc.thePlayer.motionX *= speed.getValue();
            mc.thePlayer.motionZ *= speed.getValue();
        } else {
            // Strafe — accelerate in the direction of movement input
            applyStrafe();
        }
    }

    private void applyStrafe() {
        double yawRad  = Math.toRadians(mc.thePlayer.rotationYaw);
        double forward = mc.thePlayer.moveForward;
        double strafe  = mc.thePlayer.moveStrafing;

        if (forward == 0 && strafe == 0) return;

        double angle = Math.atan2(strafe, forward);
        double spd   = speed.getValue() * 0.1;

        mc.thePlayer.motionX -= Math.sin(yawRad + angle) * spd;
        mc.thePlayer.motionZ += Math.cos(yawRad + angle) * spd;
    }
}
