package com.zenkai.zenclient.module.modules.combat;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.ModeSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

/**
 * KillAura — automatically attacks nearby entities.
 *
 * Fixes applied:
 * 1. Aim raised to full eye level instead of half-eye (aim higher).
 * 2. Sprint is re-applied after attacking to prevent the vanilla 1.8.9
 *    sprint-cancel on hit which caused the camera FOV to dip ("zoom").
 */
public final class KillAura extends Module {

    private final NumberSetting  range        = addSetting(new NumberSetting ("Range",        "Attack range in blocks",           3.5, 1.0, 6.0, 0.1));
    private final NumberSetting  cps          = addSetting(new NumberSetting ("CPS",          "Clicks per second",                 12, 1,   20,  1));
    private final ModeSetting    targetMode   = addSetting(new ModeSetting   ("Target",       "What to target",         "Players", "Players", "Mobs", "All"));
    private final BooleanSetting rotations    = addSetting(new BooleanSetting("Rotations",    "Rotate towards target",             true));
    private final BooleanSetting throughWalls = addSetting(new BooleanSetting("ThroughWalls", "Attack through walls",              false));

    private EntityLivingBase target;
    private long             lastAttack = 0L;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities.", Category.COMBAT, Keyboard.KEY_R);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;

        target = findTarget();
        if (target == null) return;

        long now   = System.currentTimeMillis();
        long delay = 1000L / (long) cps.getInt();
        if (now - lastAttack < delay) return;
        lastAttack = now;

        if (rotations.isEnabled()) {
            rotateTowards(target);
        }

        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, target);

        // Re-apply sprint so the vanilla sprint-cancel-on-hit FOV dip doesn't fire.
        // The dip happens because Minecraft sets sprinting to false on attack;
        // restoring it here (same tick, before the packet is sent) prevents the FOV pop.
        if (mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }
    }

    private EntityLivingBase findTarget() {
        double maxRange = range.getValue();

        return mc.theWorld.loadedEntityList.stream()
                .filter(this::isValidTarget)
                .map(e -> (EntityLivingBase) e)
                .filter(e -> mc.thePlayer.getDistanceToEntity(e) <= maxRange)
                .min((a, b) -> Double.compare(
                        mc.thePlayer.getDistanceToEntity(a),
                        mc.thePlayer.getDistanceToEntity(b)))
                .orElse(null);
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return false;
        if (entity == mc.thePlayer)                return false;
        EntityLivingBase living = (EntityLivingBase) entity;
        if (living.getHealth() <= 0)               return false;

        switch (targetMode.getValue()) {
            case "Players": return entity instanceof EntityPlayer;
            case "Mobs":    return !(entity instanceof EntityPlayer);
            default:        return true;
        }
    }

    /**
     * Rotate the player's camera toward the target's eyes.
     *
     * Previously aimed at {@code entity.getEyeHeight() / 2} (mid-body),
     * now aims at full {@code entity.getEyeHeight()} so attacks land higher
     * and are less likely to miss crouching or short-hitbox targets.
     */
    private void rotateTowards(EntityLivingBase entity) {
        double dX = entity.posX - mc.thePlayer.posX;
        double dY = (entity.posY + entity.getEyeHeight())
                  - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dZ = entity.posZ - mc.thePlayer.posZ;

        double dist  = MathHelper.sqrt_double(dX * dX + dZ * dZ);
        float  yaw   = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90F;
        float  pitch = (float) -Math.toDegrees(Math.atan2(dY, dist));

        mc.thePlayer.rotationYaw   = yaw;
        mc.thePlayer.rotationPitch = pitch;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
    }
}
