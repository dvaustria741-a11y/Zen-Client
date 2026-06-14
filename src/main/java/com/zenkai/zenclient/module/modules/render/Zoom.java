package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import org.lwjgl.input.Keyboard;

/**
 * Zoom — reduces the field of view while enabled to give a scoped-in view.
 * The original FOV is restored when the module is disabled.
 */
public final class Zoom extends Module {

    private final NumberSetting fov = addSetting(
            new NumberSetting("FOV", "Field of view while zoomed", 20, 1, 70, 1));

    private float savedFov;

    public Zoom() {
        super("Zoom", "Zooms the camera in.", Category.RENDER, Keyboard.KEY_C);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        savedFov = mc.gameSettings.fovSetting;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        mc.gameSettings.fovSetting = fov.getFloat();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.gameSettings.fovSetting = savedFov;
    }
}
