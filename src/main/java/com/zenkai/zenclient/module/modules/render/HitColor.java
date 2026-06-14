package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.ColorSetting;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

/**
 * Hit Color — customises the red damage-tint flash colour when entities are hurt.
 * Actual override of the tint requires a mixin/ASM hook into EntityRenderer;
 * this module exposes the setting for future integration.
 */
public final class HitColor extends Module {

    public final ColorSetting hitColor = addSetting(
            new ColorSetting("Hit Color", "Entity hurt-tint colour", new Color(255, 0, 0, 100)));
    public final ColorSetting selfColor = addSetting(
            new ColorSetting("Self Color", "Own damage-tint colour",  new Color(255, 0, 0, 100)));

    public HitColor() {
        super("Hit Color", "Customise entity hit flash colour.", Category.RENDER, Keyboard.KEY_NONE);
    }
}
