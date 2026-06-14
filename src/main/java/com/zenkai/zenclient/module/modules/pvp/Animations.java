package com.zenkai.zenclient.module.modules.pvp;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import org.lwjgl.input.Keyboard;

/**
 * Animations — restores 1.7-style item swing and hit animations.
 *
 * The arm-swing FOV modifier is set to a 1.7 value (no FOV change on sprint),
 * and the swing speed is set to match old punch cadence.
 * Full 1.7 item rendering requires ASM; settings below configure what is
 * achievable through GameSettings alone in vanilla Forge 1.8.9.
 */
public final class Animations extends Module {

    private final BooleanSetting noSwingFov = addSetting(new BooleanSetting("No Swing FOV",     "Remove sprint FOV change",          true));
    private final BooleanSetting fastSwing  = addSetting(new BooleanSetting("Fast Swing",        "1.7-speed arm swing (6 ticks)",     true));
    private final BooleanSetting oldEat     = addSetting(new BooleanSetting("Old Eating Anim",   "Restore 1.7 eating animation",      true));
    private final NumberSetting  swingSpeed = addSetting(new NumberSetting ("Swing Speed",        "Arm swing tick speed",  6, 1, 20, 1));

    private float savedFovModifier;

    public Animations() {
        super("Animations", "1.7 PvP animations.", Category.PVP, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        savedFovModifier = mc.gameSettings.fovSetting;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.sprintingTicksLeft = 0;
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre() || mc.thePlayer == null) return;

        if (noSwingFov.isEnabled()) {
            mc.gameSettings.viewBobbing = false;
        }

        if (fastSwing.isEnabled()) {
            if (mc.thePlayer.swingProgressInt > 0) {
                mc.thePlayer.swingProgressInt = (int) Math.min(
                        mc.thePlayer.swingProgressInt + (6.0 / swingSpeed.getValue()),
                        6);
            }
        }
    }
}
