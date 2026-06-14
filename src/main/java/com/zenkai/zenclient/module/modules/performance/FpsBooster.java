package com.zenkai.zenclient.module.modules.performance;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.ModeSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

/**
 * FPS Booster — applies aggressive rendering optimisations to improve frame rate.
 *
 * Profile "Performance": minimum quality, maximum speed.
 * Profile "Balanced":    moderate quality reduction.
 * Profile "Quality":     stock or near-stock settings (safe baseline).
 */
public final class FpsBooster extends Module {

    private final ModeSetting    profile    = addSetting(new ModeSetting   ("Profile",       "Optimisation preset",                      "Balanced", "Performance", "Balanced", "Quality"));
    private final BooleanSetting particles  = addSetting(new BooleanSetting("Limit Particles","Reduce particle count",                    true));
    private final BooleanSetting noFancy    = addSetting(new BooleanSetting("Fast Graphics",  "Force Fast graphics mode",                 true));
    private final BooleanSetting noAO       = addSetting(new BooleanSetting("No Ambient Occ", "Disable ambient occlusion",                false));
    private final BooleanSetting dynamicFps = addSetting(new BooleanSetting("Dynamic FPS",    "Limit FPS when game is not in focus",      true));
    private final NumberSetting  bgFps      = addSetting(new NumberSetting ("BG FPS Limit",   "Max FPS when tabbed out",                  15, 1, 60, 1));

    // Saved originals
    private int     savedParticles;
    private boolean savedFancy;
    private int     savedAO;
    private int     savedMipmap;
    private int     savedFpsLimit;

    public FpsBooster() {
        super("FPS Booster", "Optimises game rendering for better performance.", Category.PERFORMANCE, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        savedParticles = mc.gameSettings.particleSetting;
        savedFancy     = mc.gameSettings.fancyGraphics;
        savedAO        = mc.gameSettings.ambientOcclusion;
        savedMipmap    = mc.gameSettings.mipmapLevels;
        savedFpsLimit  = mc.gameSettings.limitFramerate;
        applyProfile();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.gameSettings.particleSetting  = savedParticles;
        mc.gameSettings.fancyGraphics    = savedFancy;
        mc.gameSettings.ambientOcclusion = savedAO;
        mc.gameSettings.mipmapLevels     = savedMipmap;
        mc.gameSettings.limitFramerate   = savedFpsLimit;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;
        if (dynamicFps.isEnabled() && !Display.isActive()) {
            mc.gameSettings.limitFramerate = bgFps.getInt();
        } else if (dynamicFps.isEnabled()) {
            mc.gameSettings.limitFramerate = savedFpsLimit;
        }
        if (particles.isEnabled()) {
            mc.gameSettings.particleSetting = profile.is("Performance") ? 2
                                            : profile.is("Balanced")    ? 1
                                            : 0;
        }
        if (noFancy.isEnabled()) {
            mc.gameSettings.fancyGraphics = false;
        }
        if (noAO.isEnabled()) {
            mc.gameSettings.ambientOcclusion = 0;
        }
    }

    private void applyProfile() {
        switch (profile.getValue()) {
            case "Performance":
                mc.gameSettings.fancyGraphics    = false;
                mc.gameSettings.ambientOcclusion = 0;
                mc.gameSettings.particleSetting  = 2;
                mc.gameSettings.mipmapLevels     = 0;
                break;
            case "Balanced":
                mc.gameSettings.fancyGraphics    = false;
                mc.gameSettings.ambientOcclusion = 1;
                mc.gameSettings.particleSetting  = 1;
                mc.gameSettings.mipmapLevels     = 2;
                break;
            case "Quality":
                mc.gameSettings.fancyGraphics    = true;
                mc.gameSettings.ambientOcclusion = 2;
                mc.gameSettings.particleSetting  = 0;
                mc.gameSettings.mipmapLevels     = 4;
                break;
        }
    }
}
