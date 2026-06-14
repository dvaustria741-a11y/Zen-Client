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
 * Attack-without-looking behaviour:
 *   When BOTH {@code Rotations} AND {@code Smooth Aim} are OFF, the aura
 *   attacks the nearest valid entity regardless of where the crosshair is
 *   pointing.  {@code mc.playerController.attackEntity()} sends a
 *   C02PacketUseEntity directly — the 1.8.9 server validates distance only,
 *   not look-angle, so the hit registers correctly.
 *
 *   When {@code Rotations} is ON the player's yaw/pitch are overridden before
 *   the attack packet.  {@code Smooth Aim} is a sub-option of Rotations and
 *   has no effect while Rotations is disabled.
 *
 * Summary:
 *   Rotations OFF + Smooth Aim OFF  →  attack silently, no head-turn
 *   Rotations ON  + Smooth Aim OFF  →  instant snap-aim, then attack
 *   Rotations ON  + Smooth Aim ON   →  smooth interpolated aim, then attack
 */
public final class KillAura extends Module {

    private final NumberSetting  range       = addSetting(new NumberSetting ("Range",       "Attack range in blocks",              3.5, 1.0, 6.0, 0.1));
    private final NumberSetting  cps         = addSetting(new NumberSetting ("CPS",         "Clicks per second",                    12,  1,   100, 1));
    private final ModeSetting    targetMode  = addSetting(new ModeSetting   ("Target",      "What to target",           "Players", "Players", "Mobs", "All"));
    private final BooleanSetting rotations   = addSetting(new BooleanSetting("Rotations",   "Rotate head toward target before hit", true));
    private final BooleanSetting smoothAim   = addSetting(new BooleanSetting("Smooth Aim",  "Smooth rotation (ignored if Rotations OFF)", false));
    private final NumberSetting  smoothSpeed = addSetting(new NumberSetting ("Smooth Speed","Aim interpolation speed",  0.15, 0.01, 1.0, 0.01));
    private final BooleanSetting teamCheck   = addSetting(new BooleanSetting("Team Check",  "Skip teammates (BedWars / teams)",    false));

    private EntityLivingBase target;
    private long             lastAttack     = 0L;
    private float            currentYaw     = 0f;
    private float            currentPitch   = 0f;
    private boolean          aimInitialised = false;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities.", Category.COMBAT, Keyboard.KEY_R);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        aimInitialised = false;
    }

    // ── Main tick ─────────────────────────────────────────────────────────────

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;

        target = findTarget();
        if (target == null) {
            aimInitialised = false;
            return;
        }

        // CPS gate
        long now   = System.currentTimeMillis();
        long delay = 1000L / (long) cps.getInt();
        if (now - lastAttack < delay) return;
        lastAttack = now;

        // ── Rotation ──────────────────────────────────────────────────────────
        // Only modify the player's look direction when Rotations is ON.
        // With Rotations OFF the attack fires silently — cursor position is
        // irrelevant because attackEntity() does not check look-angle server-side.
        if (rotations.isEnabled()) {
            rotateTowards(target);
        }

        // ── Attack ────────────────────────────────────────────────────────────
        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, target);

        // Preserve sprint so the post-hit speed penalty is avoided
        if (mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }
    }

    // ── Target selection ──────────────────────────────────────────────────────

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
                if (obj instanceof EntityPlayer
                        && teamCheck.isEnabled()
                        && isSameTeam((EntityPlayer) obj)) return false;
                return true;
            }
        }
    }

    // ── Team check ────────────────────────────────────────────────────────────

    private boolean isSameTeam(EntityPlayer other) {
        if (mc.thePlayer == null || mc.theWorld == null) return false;

        Scoreboard       board     = mc.theWorld.getScoreboard();
        ScorePlayerTeam  myTeam   = board.getPlayersTeam(mc.thePlayer.getName());
        ScorePlayerTeam  theirTeam = board.getPlayersTeam(other.getName());

        if (myTeam != null && theirTeam != null) return myTeam == theirTeam;

        // Fallback: compare name-tag colour (BedWars teams)
        EnumChatFormatting myCol   = extractTeamColor(mc.thePlayer.getDisplayName());
        EnumChatFormatting theirCol = extractTeamColor(other.getDisplayName());
        return myCol != null && myCol == theirCol;
    }

    private static EnumChatFormatting extractTeamColor(net.minecraft.util.IChatComponent comp) {
        String fmt = comp.getFormattedText();
        for (int i = 0; i < fmt.length() - 1; i++) {
            if (fmt.charAt(i) == '\u00a7') {
                char code = fmt.charAt(i + 1);
                for (EnumChatFormatting f : EnumChatFormatting.values()) {
                    if (f.isColor()
                            && f.toString().length() == 2
                            && f.toString().charAt(1) == code) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    // ── Rotation ──────────────────────────────────────────────────────────────

    /**
     * Rotates the player toward {@code entity}.
     * Only called when {@code Rotations} is ON.
     * {@code Smooth Aim} selects between instant snap and interpolated approach.
     */
    private void rotateTowards(EntityLivingBase entity) {
        double dX = entity.posX - mc.thePlayer.posX;
        double dY = (entity.posY + entity.getEyeHeight())
                  - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dZ     = entity.posZ - mc.thePlayer.posZ;
        double dist   = MathHelper.sqrt_double(dX * dX + dZ * dZ);
        float  tYaw   = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90F;
        float  tPitch = (float) -Math.toDegrees(Math.atan2(dY, dist));

        // ── Instant snap (Smooth Aim OFF) ─────────────────────────────────────
        if (!smoothAim.isEnabled()) {
            mc.thePlayer.rotationYaw   = tYaw;
            mc.thePlayer.rotationPitch = tPitch;
            return;
        }

        // ── Smooth interpolation (Smooth Aim ON) ──────────────────────────────
        if (!aimInitialised) {
            currentYaw     = mc.thePlayer.rotationYaw;
            currentPitch   = mc.thePlayer.rotationPitch;
            aimInitialised = true;
        }

        float speed = smoothSpeed.getFloat();
        currentYaw   += wrapDegrees(tYaw   - currentYaw)   * speed;
        currentPitch += (tPitch - currentPitch)             * speed;
        currentPitch  = Math.max(-90f, Math.min(90f, currentPitch));

        mc.thePlayer.rotationYaw   = currentYaw;
        mc.thePlayer.rotationPitch = currentPitch;
    }

    private static float wrapDegrees(float d) {
        d %= 360f;
        if (d >=  180f) d -= 360f;
        if (d < -180f)  d += 360f;
        return d;
    }

    // ── Disable ───────────────────────────────────────────────────────────────

    @Override
    public void onDisable() {
        super.onDisable();
        target         = null;
        aimInitialised = false;
    }
}
