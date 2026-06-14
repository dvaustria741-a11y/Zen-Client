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
 * ESP — draws bounding boxes around entities so they are visible through walls.
 *
 * Fix: EventRender3D is now actually posted by ZenClient#onRenderWorld
 * (RenderWorldLastEvent bridge that was previously missing). GL state is
 * fully saved/restored so other render passes are not affected.
 */
public final class ESP extends Module {

    private final ModeSetting    mode      = addSetting(new ModeSetting   ("Mode",    "Render mode",      "Box",   "Box", "Glow"));
    private final BooleanSetting players   = addSetting(new BooleanSetting("Players", "Highlight players",         true));
    private final BooleanSetting mobs      = addSetting(new BooleanSetting("Mobs",    "Highlight mobs",            false));
    private final ColorSetting   boxColor  = addSetting(new ColorSetting  ("Color",   "Box colour",      new Color(255, 0, 80, 200)));

    public ESP() {
        super("ESP", "Highlights entities through walls.", Category.RENDER, Keyboard.KEY_Z);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        // --- Save GL state ---
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.pushMatrix();

        // Disable depth so boxes render through terrain/blocks
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(1.5f);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof Entity)) continue;
            Entity entity = (Entity) obj;
            if (entity == mc.thePlayer)                                    continue;
            if (entity instanceof EntityPlayer  && !players.isEnabled())   continue;
            if (!(entity instanceof EntityPlayer) && !mobs.isEnabled())    continue;
            if (!(entity instanceof EntityLivingBase))                     continue;
            if (((EntityLivingBase) entity).getHealth() <= 0)             continue;

            drawBox(entity, boxColor.getColor(), event.getPartialTicks());
        }

        // --- Restore GL state ---
        GlStateManager.popMatrix();
        GL11.glPopAttrib();
    }

    private void drawBox(Entity entity, Color color, float pt) {
        // Interpolated render position relative to the camera
        double rx = mc.getRenderManager().viewerPosX;
        double ry = mc.getRenderManager().viewerPosY;
        double rz = mc.getRenderManager().viewerPosZ;

        double x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * pt) - rx;
        double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * pt) - ry;
        double z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * pt) - rz;

        float hw = entity.width  / 2F;   // half-width
        float h  = entity.height;

        float r = color.getRed()   / 255F;
        float g = color.getGreen() / 255F;
        float b = color.getBlue()  / 255F;
        float a = color.getAlpha() / 255F;

        GL11.glColor4f(r, g, b, a);

        // Bottom face
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(x - hw, y,     z - hw);
        GL11.glVertex3d(x + hw, y,     z - hw);
        GL11.glVertex3d(x + hw, y,     z + hw);
        GL11.glVertex3d(x - hw, y,     z + hw);
        GL11.glEnd();

        // Top face
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(x - hw, y + h, z - hw);
        GL11.glVertex3d(x + hw, y + h, z - hw);
        GL11.glVertex3d(x + hw, y + h, z + hw);
        GL11.glVertex3d(x - hw, y + h, z + hw);
        GL11.glEnd();

        // Vertical pillars connecting top and bottom
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x - hw, y,     z - hw); GL11.glVertex3d(x - hw, y + h, z - hw);
        GL11.glVertex3d(x + hw, y,     z - hw); GL11.glVertex3d(x + hw, y + h, z - hw);
        GL11.glVertex3d(x + hw, y,     z + hw); GL11.glVertex3d(x + hw, y + h, z + hw);
        GL11.glVertex3d(x - hw, y,     z + hw); GL11.glVertex3d(x - hw, y + h, z + hw);
        GL11.glEnd();
    }
}
