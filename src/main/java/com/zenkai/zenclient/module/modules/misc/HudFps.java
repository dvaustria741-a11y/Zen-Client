package com.zenkai.zenclient.module.modules.misc;

import com.zenkai.zenclient.ZenClient;
import com.zenkai.zenclient.hud.HudElement;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import org.lwjgl.input.Keyboard;

/** Misc module that shows/hides the FPS HUD element. */
public final class HudFps extends Module {

    public HudFps() {
        super("HUD FPS", "Toggle the FPS counter HUD.", Category.MISC, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        HudElement el = ZenClient.getInstance().getHudManager().getElement("FPS");
        if (el != null) el.setVisible(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        HudElement el = ZenClient.getInstance().getHudManager().getElement("FPS");
        if (el != null) el.setVisible(false);
    }
}
