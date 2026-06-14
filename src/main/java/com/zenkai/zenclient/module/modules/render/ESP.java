package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventRender3D;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.ColorSetting;
import com.zenkai.zenclient.setting.settings.ModeSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * ESP — draws boxes around entities so you can see them through walls.
 */
public final class ESP extends Module {

    private final ModeSetting    mode       = addSetting(new ModeSetting   ("Mode",    "Render mode",      "Box",   "Box", "Glow"));
    private final BooleanSetting players    = addSetting(new BooleanSetting("Players", "Highlight players",         true));
    private final BooleanSetting mobs       = addSetting(new BooleanSetting("Mobs",    "Highlight mobs",            false));
    private final ColorSetting   colorAlly  = addSetting(new ColorSetting  ("Color",   "Box colour",      new Color(255, 0, 80, 200)));

    public ESP() {
        super("ESP", "Highlights entities through walls.", Category.RENDER, Keyboard.KEY_Z);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (mc.theWorld == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glLineWidth(1.5f);

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            if (entity instanceof EntityPlayer  && !players.isEnabled()) continue;
            if (!(entity instanceof EntityPlayer) && !mobs.isEnabled())  continue;
            if (!(entity instanceof EntityLivingBase)) continue;

            drawBoundingBox(entity, colorAlly.getColor(), event.getPartialTicks());
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawBoundingBox(Entity entity, Color color, float pt) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * pt
                 - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * pt
                 - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * pt
                 - mc.getRenderManager().viewerPosZ;

        float w = entity.width  / 2F;
        float h = entity.height;

        GL11.glColor4f(color.getRed()/255F, color.getGreen()/255F,
                       color.getBlue()/255F, color.getAlpha()/255F);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        // Bottom face
        GL11.glVertex3d(x - w, y,     z - w);
        GL11.glVertex3d(x + w, y,     z - w);
        GL11.glVertex3d(x + w, y,     z + w);
        GL11.glVertex3d(x - w, y,     z + w);
        GL11.glVertex3d(x - w, y,     z - w);
        // Top face
        GL11.glVertex3d(x - w, y + h, z - w);
        GL11.glVertex3d(x + w, y + h, z - w);
        GL11.glVertex3d(x + w, y + h, z + w);
        GL11.glVertex3d(x - w, y + h, z + w);
        GL11.glVertex3d(x - w, y + h, z - w);
        GL11.glEnd();

        // Vertical pillars
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x + w, y, z - w); GL11.glVertex3d(x + w, y + h, z - w);
        GL11.glVertex3d(x + w, y, z + w); GL11.glVertex3d(x + w, y + h, z + w);
        GL11.glVertex3d(x - w, y, z + w); GL11.glVertex3d(x - w, y + h, z + w);
        GL11.glEnd();
    }
}
