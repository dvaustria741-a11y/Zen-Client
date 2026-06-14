package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * FullBright — maximises gamma so dark areas are fully lit.
 */
public final class FullBright extends Module {

    private float savedGamma = 0F;

    public FullBright() {
        super("FullBright", "Makes everything bright.", Category.RENDER, Keyboard.KEY_G);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        savedGamma = mc.gameSettings.gammaSetting;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.gameSettings.gammaSetting = savedGamma;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        mc.gameSettings.gammaSetting = 1000F;
    }
}
