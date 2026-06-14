package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import org.lwjgl.input.Keyboard;

/**
 * FOV Changer — sets a custom field-of-view value while enabled.
 * Original FOV is restored on disable.
 */
public final class FOVChanger extends Module {

    private final NumberSetting fov = addSetting(
            new NumberSetting("FOV", "Custom field of view", 90, 30, 150, 1));

    private float savedFov;

    public FOVChanger() {
        super("FOV Changer", "Customise your field of view.", Category.RENDER, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        savedFov = mc.gameSettings.fovSetting;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.gameSettings.fovSetting = savedFov;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        mc.gameSettings.fovSetting = fov.getFloat();
    }
}
