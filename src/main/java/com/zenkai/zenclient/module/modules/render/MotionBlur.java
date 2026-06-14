package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventRender2D;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * Motion Blur — lightweight screen-space blur using the GL accumulation buffer.
 * Requires an accumulation buffer allocated on display creation; if the buffer
 * is unavailable the effect is silently skipped.
 */
public final class MotionBlur extends Module {

    private final NumberSetting strength = addSetting(
            new NumberSetting("Strength", "Blur strength (0=off, 1=max)", 0.5, 0.1, 0.9, 0.05));

    public MotionBlur() {
        super("Motion Blur", "Smooth motion blur effect.", Category.RENDER, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        try {
            float s = strength.getFloat();
            GlStateManager.pushMatrix();
            GL11.glAccum(GL11.GL_MULT,   1f - s);
            GL11.glAccum(GL11.GL_ACCUM,  s);
            GL11.glAccum(GL11.GL_RETURN, 1f);
            GlStateManager.popMatrix();
        } catch (Exception ignored) {}
    }
}
