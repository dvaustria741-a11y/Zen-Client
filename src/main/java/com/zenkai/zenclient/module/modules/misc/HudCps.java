package com.zenkai.zenclient.module.modules.misc;

import com.zenkai.zenclient.ZenClient;
import com.zenkai.zenclient.hud.HudElement;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import org.lwjgl.input.Keyboard;

/** Misc module that shows/hides the CPS HUD element. */
public final class HudCps extends Module {

    public HudCps() {
        super("HUD CPS", "Toggle the CPS counter HUD.", Category.MISC, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        HudElement el = ZenClient.getInstance().getHudManager().getElement("CPS");
        if (el != null) el.setVisible(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        HudElement el = ZenClient.getInstance().getHudManager().getElement("CPS");
        if (el != null) el.setVisible(false);
    }
}
