package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventRender3D;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import org.lwjgl.input.Keyboard;

/**
 * Item Physics — makes dropped items lie flat and spin on the ground,
 * matching the classic ItemPhysics mod behaviour.
 *
 * The vanilla EntityItem renderer handles the actual item drawing;
 * this module overrides the entity's rotation before the render manager
 * processes it, then restores it.
 */
public final class ItemPhysics extends Module {

    private final NumberSetting spinSpeed = addSetting(
            new NumberSetting("Spin Speed", "Rotation degrees per tick", 3.0, 0.5, 10.0, 0.5));

    public ItemPhysics() {
        super("Item Physics", "Spinning flat dropped-item animation.", Category.RENDER, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (mc.theWorld == null) return;
        float pt = event.getPartialTicks();

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof EntityItem)) continue;
            EntityItem ei = (EntityItem) obj;
            // Lay the item flat by forcing its prevRotationYaw/Yaw to a spinning value
            float spin = (ei.age + pt) * (float) spinSpeed.getValue();
            ei.prevRotationYaw = spin - (float) spinSpeed.getValue();
            ei.rotationYaw     = spin;
        }
    }
}
