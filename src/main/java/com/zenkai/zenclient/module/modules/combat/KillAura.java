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
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

/**
 * KillAura — automatically attacks nearby entities.
 *
 * Settings:
 *  Range, CPS (1-100), Target mode, Rotations, Smooth Aim + Speed,
 *  Through Walls, Team Check (BedWars scoreboard / name-color fallback).
 */
public final class KillAura extends Module {

    private final NumberSetting  range       = addSetting(new NumberSetting ("Range",       "Attack range in blocks",              3.5, 1.0, 6.0, 0.1));
    private final NumberSetting  cps         = addSetting(new NumberSetting ("CPS",         "Clicks per second",                    12,  1,   100, 1));
    private final ModeSetting    targetMode  = addSetting(new ModeSetting   ("Target",      "What to target",           "Players", "Players", "Mobs", "All"));
    private final BooleanSetting rotations   = addSetting(new BooleanSetting("Rotations",   "Rotate towards target",               true));
    private final BooleanSetting smoothAim   = addSetting(new BooleanSetting("Smooth Aim",  "Smoothly interpolate aim rotation",   false));
    private final NumberSetting  smoothSpeed = addSetting(new NumberSetting ("Smooth Speed","Aim speed (higher = faster lock-on)", 0.15, 0.01, 1.0, 0.01));
    private final BooleanSetting teamCheck   = addSetting(new BooleanSetting("Team Check",  "Skip teammates (BedWars / teams)",    false));

    private EntityLivingBase target;
    private long             lastAttack     = 0L;
    private float            currentYaw     = 0f;
    private float            currentPitch   = 0f;
    private boolean          aimInitialised = false;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities.", Category.COMBAT, Keyboard.KEY_R);
    }

    @Override public void onEnable() { super.onEnable(); aimInitialised = false; }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;

        target = findTarget();
        if (target == null) { aimInitialised = false; return; }

        long now   = System.currentTimeMillis();
        long delay = 1000L / (long) cps.getInt();
        if (now - lastAttack < delay) return;
        lastAttack = now;

        if (rotations.isEnabled()) rotateTowards(target);

        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, target);
        if (mc.thePlayer.isSprinting()) mc.thePlayer.setSprinting(true);
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

    private boolean isValidTarget(Object obj) {
        if (!(obj instanceof EntityLivingBase)) return false;
        if (obj == mc.thePlayer)                return false;
        EntityLivingBase living = (EntityLivingBase) obj;
        if (living.getHealth() <= 0)            return false;

        switch (targetMode.getValue()) {
            case "Players": {
                if (!(obj instanceof EntityPlayer)) return false;
                if (teamCheck.isEnabled() && isSameTeam((EntityPlayer) obj)) return false;
                return true;
            }
            case "Mobs": return !(obj instanceof EntityPlayer);
            default: {
                if (obj instanceof EntityPlayer && teamCheck.isEnabled() && isSameTeam((EntityPlayer) obj)) return false;
                return true;
            }
        }
    }

    private boolean isSameTeam(EntityPlayer other) {
        if (mc.thePlayer == null || mc.theWorld == null) return false;
        Scoreboard board = mc.theWorld.getScoreboard();
        ScorePlayerTeam myTeam    = board.getPlayersTeam(mc.thePlayer.getName());
        ScorePlayerTeam theirTeam = board.getPlayersTeam(other.getName());
        if (myTeam != null && theirTeam != null) return myTeam == theirTeam;
        EnumChatFormatting myColor    = extractTeamColor(mc.thePlayer.getDisplayName());
        EnumChatFormatting theirColor = extractTeamColor(other.getDisplayName());
        if (myColor != null && theirColor != null) return myColor == theirColor;
        return false;
    }

    private EnumChatFormatting extractTeamColor(net.minecraft.util.IChatComponent nameComponent) {
        String formatted = nameComponent.getFormattedText();
        for (int i = 0; i < formatted.length() - 1; i++) {
            if (formatted.charAt(i) == '\u00a7') {
                char code = formatted.charAt(i + 1);
                for (EnumChatFormatting fmt : EnumChatFormatting.values()) {
                    if (fmt.toString().length() == 2 && fmt.toString().charAt(1) == code) {
                        if (fmt.isColor()) return fmt;
                        break;
                    }
                }
            }
        }
        return null;
    }

    private void rotateTowards(EntityLivingBase entity) {
        double dX = entity.posX - mc.thePlayer.posX;
        double dY = (entity.posY + entity.getEyeHeight()) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dZ = entity.posZ - mc.thePlayer.posZ;
        double dist      = MathHelper.sqrt_double(dX * dX + dZ * dZ);
        float targetYaw  = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90F;
        float targetPitch= (float) -Math.toDegrees(Math.atan2(dY, dist));

        if (!smoothAim.isEnabled()) {
            mc.thePlayer.rotationYaw   = targetYaw;
            mc.thePlayer.rotationPitch = targetPitch;
            return;
        }
        if (!aimInitialised) { currentYaw = mc.thePlayer.rotationYaw; currentPitch = mc.thePlayer.rotationPitch; aimInitialised = true; }
        float speed = smoothSpeed.getFloat();
        currentYaw   += wrapDegrees(targetYaw - currentYaw) * speed;
        currentPitch += (targetPitch - currentPitch) * speed;
        currentPitch  = Math.max(-90f, Math.min(90f, currentPitch));
        mc.thePlayer.rotationYaw   = currentYaw;
        mc.thePlayer.rotationPitch = currentPitch;
    }

    private static float wrapDegrees(float d) {
        d %= 360f;
        if (d >= 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return d;
    }

    @Override public void onDisable() { super.onDisable(); target = null; aimInitialised = false; }
}
