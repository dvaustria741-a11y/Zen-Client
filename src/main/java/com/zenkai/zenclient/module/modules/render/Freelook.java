package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * Freelook — locks the player's movement direction to the facing angle at
 * the moment of activation, while the camera continues to follow mouse
 * input. Implemented by swapping rotationYaw/Pitch between the locked
 * "body" angle (used during the movement portion of the tick) and the
 * live "camera" angle (used for rendering) each client tick.
 */
public final class Freelook extends Module {

    private float bodyYaw;
    private float cameraYaw;
    private float cameraPitch;

    public Freelook() {
        super("Freelook", "Look around without changing movement direction.", Category.RENDER, Keyboard.KEY_LMENU);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.thePlayer == null) return;

        bodyYaw     = mc.thePlayer.rotationYaw;
        cameraYaw   = mc.thePlayer.rotationYaw;
        cameraPitch = mc.thePlayer.rotationPitch;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.thePlayer == null) return;

        if (event.isPre()) {
            cameraYaw   = mc.thePlayer.rotationYaw;
            cameraPitch = mc.thePlayer.rotationPitch;

            mc.thePlayer.rotationYaw   = bodyYaw;
            mc.thePlayer.prevRotationYaw = bodyYaw;
        } else {
            mc.thePlayer.rotationYaw   = cameraYaw;
            mc.thePlayer.rotationPitch = cameraPitch;
            mc.thePlayer.prevRotationYaw   = cameraYaw;
            mc.thePlayer.prevRotationPitch = cameraPitch;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer == null) return;

        mc.thePlayer.rotationYaw       = cameraYaw;
        mc.thePlayer.rotationPitch     = cameraPitch;
        mc.thePlayer.prevRotationYaw   = cameraYaw;
        mc.thePlayer.prevRotationPitch = cameraPitch;
    }
}
