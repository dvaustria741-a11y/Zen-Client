package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.ModeSetting;
import org.lwjgl.input.Keyboard;

/** Better Particles — controls particle rendering density. */
public final class BetterParticles extends Module {

    private final ModeSetting    density = addSetting(new ModeSetting   ("Density", "Overall particle count", "Normal", "Minimal", "Normal", "All"));
    private final BooleanSetting water   = addSetting(new BooleanSetting("Water",   "Show water particles",  true));
    private final BooleanSetting crit    = addSetting(new BooleanSetting("Crits",   "Show crit particles",   true));

    public BetterParticles() {
        super("Better Particles", "Fine-grained particle control.", Category.RENDER, Keyboard.KEY_NONE);
    }

    @Override public void onEnable() { super.onEnable(); applyMode(); }
    @Override public void onDisable() { super.onDisable(); mc.gameSettings.particleSetting = 0; }

    @EventTarget
    public void onUpdate(EventUpdate event) { if (event.isPre()) applyMode(); }

    private void applyMode() {
        switch (density.getValue()) {
            case "Minimal": mc.gameSettings.particleSetting = 2; break;
            case "Normal":  mc.gameSettings.particleSetting = 1; break;
            case "All":     mc.gameSettings.particleSetting = 0; break;
        }
    }
}
