package com.zenkai.zenclient.module.modules.combat;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Hold Attack — while LMB is held down this module repeatedly attacks
 * whatever entity the crosshair is aimed at, at the configured CPS.
 *
 * This is separate from KillAura: Hold Attack requires you to manually
 * aim (no auto-rotation), it just removes the need to click individually.
 * Works well combined with KillAura's Smooth Aim — KillAura handles
 * targeting/rotation, Hold Attack fires when you squeeze LMB.
 */
public final class HoldAttack extends Module {

    private final NumberSetting cps = addSetting(
            new NumberSetting("CPS", "Clicks per second while holding LMB", 12, 1, 20, 1));

    private long lastAttack = 0L;

    public HoldAttack() {
        super("Hold Attack", "Auto-attacks crosshair target while LMB is held.", Category.COMBAT, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Only fire while left mouse button is physically held
        if (!Mouse.isButtonDown(0)) return;

        long now   = System.currentTimeMillis();
        long delay = 1000L / Math.max(1L, (long) cps.getInt());
        if (now - lastAttack < delay) return;

        // Attack whatever entity the crosshair is over
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) return;
        if (!(mop.entityHit instanceof EntityLivingBase)) return;

        EntityLivingBase target = (EntityLivingBase) mop.entityHit;
        if (target.getHealth() <= 0) return;

        lastAttack = now;
        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, target);

        // Prevent the vanilla sprint-cancel FOV dip on hit
        if (mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }
    }
}
