package com.zenkai.zenclient.module.modules.movement;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.Random;

/**
 * AutoScaffold — places blocks under the player's feet automatically.
 *
 * Placement fix:
 *   feetPos   = BlockPos(player)          — block AT player feet (air)
 *   placePos  = feetPos.down()            — block player stands ON (needs fill)
 *   supportPos = placePos.down()          — block BELOW the target slot
 *   We right-click the UP face of supportPos → block appears at placePos. ✓
 *
 *   Old code used placePos as the right-click target with UP face,
 *   which placed the block at feetPos (inside the player) — server rejected it.
 *
 * Anti-detection:
 *   - Random delay jitter (±25 % of Delay setting) avoids fixed-interval flags.
 *   - Player yaw is rotated toward the support block before the packet,
 *     matching what a human would do when looking at their feet.
 *   - Pitch is set to 80-90° (slightly varied) rather than exactly 90°.
 */
public final class AutoScaffold extends Module {

    private final NumberSetting  delay    = addSetting(new NumberSetting ("Delay",     "Ms between placements",       80,   0, 500, 5));
    private final BooleanSetting tower    = addSetting(new BooleanSetting("Tower",     "Place while holding Jump",    false));
    private final BooleanSetting sneak    = addSetting(new BooleanSetting("Sneak",     "Force-sneak while placing",   true));
    private final BooleanSetting safeOnly = addSetting(new BooleanSetting("Safe Only", "Only place when falling",     false));

    private final Random rng = new Random();

    private long   lastPlace  = 0L;
    private long   nextDelay  = 80L;
    private int    savedSlot  = -1;
    private double prevY      = 0.0;

    public AutoScaffold() {
        super("AutoScaffold", "Automatically places blocks under your feet.", Category.MOVEMENT, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        savedSlot = -1;
        prevY     = mc.thePlayer != null ? mc.thePlayer.posY : 0.0;
        nextDelay = baseDelay();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        restoreSlot();
        if (mc.thePlayer != null) mc.thePlayer.setSneaking(false);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPre()) return;
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null) return;

        boolean isFalling = p.posY < prevY && !p.onGround;
        prevY = p.posY;
        if (safeOnly.isEnabled() && !isFalling) return;

        long now = System.currentTimeMillis();
        if (now - lastPlace < nextDelay) return;

        BlockPos feetPos    = new BlockPos(p);
        BlockPos placePos   = feetPos.down();
        BlockPos supportPos = placePos.down();

        net.minecraft.block.state.IBlockState state = mc.theWorld.getBlockState(placePos);
        Material mat = state.getBlock().getMaterial();
        if (mat != Material.air && mat != Material.water && mat != Material.lava) return;

        net.minecraft.block.state.IBlockState supState = mc.theWorld.getBlockState(supportPos);
        Material supMat = supState.getBlock().getMaterial();
        if (supMat == Material.air || supMat == Material.water || supMat == Material.lava) return;

        int blockSlot = findBlockInHotbar();
        if (blockSlot < 0) return;

        int originalSlot = p.inventory.currentItem;
        if (originalSlot != blockSlot) {
            savedSlot = originalSlot;
            p.inventory.currentItem = blockSlot;
        }

        if (sneak.isEnabled()) p.setSneaking(true);

        float origYaw   = p.rotationYaw;
        float origPitch = p.rotationPitch;

        double cx = supportPos.getX() + 0.5 - p.posX;
        double cz = supportPos.getZ() + 0.5 - p.posZ;
        float  targetYaw = (float) Math.toDegrees(Math.atan2(cz, cx)) - 90f;
        float  targetPitch = 80f + rng.nextFloat() * 10f;

        p.rotationYaw   = targetYaw;
        p.rotationPitch = targetPitch;

        ItemStack stack = p.inventory.getStackInSlot(blockSlot);
        Vec3 hitVec = new Vec3(
                placePos.getX() + 0.5,
                placePos.getY(),
                placePos.getZ() + 0.5);
        mc.playerController.onPlayerRightClick(p, mc.theWorld, stack, supportPos, EnumFacing.UP, hitVec);
        p.swingItem();

        p.rotationYaw   = origYaw;
        p.rotationPitch = origPitch;

        restoreSlot();
        if (sneak.isEnabled()) p.setSneaking(false);

        lastPlace = now;
        nextDelay = baseDelay();
    }

    private long baseDelay() {
        long d = (long)(double) delay.getValue();
        if (d <= 0) return 0;
        long jitter = (long)(d * 0.25);
        return d - jitter + (jitter > 0 ? (rng.nextLong() % (jitter * 2 + 1) + (jitter * 2 + 1)) % (jitter * 2 + 1) : 0);
    }

    private int findBlockInHotbar() {
        EntityPlayerSP p = mc.thePlayer;
        ItemStack cur = p.inventory.getStackInSlot(p.inventory.currentItem);
        if (isUsableBlock(cur)) return p.inventory.currentItem;
        for (int i = 0; i < 9; i++) {
            if (isUsableBlock(p.inventory.getStackInSlot(i))) return i;
        }
        return -1;
    }

    private static boolean isUsableBlock(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemBlock && stack.stackSize > 0;
    }

    private void restoreSlot() {
        if (savedSlot >= 0 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = savedSlot;
            savedSlot = -1;
        }
    }

    public int countHotbarBlocks() {
        if (mc.thePlayer == null) return 0;
        int total = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (isUsableBlock(s)) total += s.stackSize;
        }
        return total;
    }
}
