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
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.Random;

/**
 * AutoScaffold — places blocks under the player's feet automatically.
 *
 * Placement strategy:
 *  1. Target = placePos = feetPos.down() (block player needs to stand on).
 *  2. If target is already solid, nothing to do.
 *  3. Check directly below (supportPos = placePos.down()):
 *       solid → right-click UP face of supportPos.
 *  4. Edge case — supportPos is also air (walking off ledge):
 *       Check the 4 horizontal neighbors of placePos.
 *       First solid neighbor found → right-click its facing-toward-target face.
 *  This handles edge walking and diagonal ledges.
 *
 * Anti-detection:
 *  - Random ±25% delay jitter per placement.
 *  - Yaw rotated toward support block; pitch randomized 80-90°.
 */
public final class AutoScaffold extends Module {

    private final NumberSetting  delay    = addSetting(new NumberSetting ("Delay",     "Ms between placements",      80,   0, 500, 5));
    private final BooleanSetting tower    = addSetting(new BooleanSetting("Tower",     "Place while holding Jump",   false));
    private final BooleanSetting sneak    = addSetting(new BooleanSetting("Sneak",     "Force-sneak while placing",  true));
    private final BooleanSetting safeOnly = addSetting(new BooleanSetting("Safe Only", "Only place when falling",    false));

    private final Random rng = new Random();

    private long   lastPlace = 0L;
    private long   nextDelay = 80L;
    private int    savedSlot = -1;
    private double prevY     = 0.0;

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

        BlockPos feetPos  = new BlockPos(p);
        BlockPos placePos = feetPos.down();

        // Already solid?
        if (isSolid(placePos)) return;

        // Resolve a support block and the face to click
        PlaceTarget pt = resolveSupport(placePos);
        if (pt == null) return;

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

        // Aim toward the support block
        double cx = pt.support.getX() + 0.5 - p.posX;
        double cz = pt.support.getZ() + 0.5 - p.posZ;
        p.rotationYaw   = (float) Math.toDegrees(Math.atan2(cz, cx)) - 90f;
        p.rotationPitch = 75f + rng.nextFloat() * 15f;

        ItemStack stack  = p.inventory.getStackInSlot(blockSlot);
        Vec3      hitVec = new Vec3(
                placePos.getX() + 0.5,
                placePos.getY() + (pt.face == EnumFacing.UP ? 0.0 : 0.5),
                placePos.getZ() + 0.5);
        mc.playerController.onPlayerRightClick(p, mc.theWorld, stack, pt.support, pt.face, hitVec);
        p.swingItem();

        p.rotationYaw   = origYaw;
        p.rotationPitch = origPitch;

        restoreSlot();
        if (sneak.isEnabled()) p.setSneaking(false);

        lastPlace = now;
        nextDelay = baseDelay();
    }

    // ── Support resolution ────────────────────────────────────────────────────

    private static final EnumFacing[] HORIZONTALS = {
        EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST
    };

    /**
     * Finds a solid block to right-click in order to place a block at placePos.
     * Priority: block directly below > any solid horizontal neighbor.
     */
    private PlaceTarget resolveSupport(BlockPos placePos) {
        // 1. Block directly below (normal scaffold)
        BlockPos below = placePos.down();
        if (isSolid(below)) {
            return new PlaceTarget(below, EnumFacing.UP);
        }
        // 2. Horizontal neighbors (edge scaffold)
        for (EnumFacing face : HORIZONTALS) {
            BlockPos neighbor = placePos.offset(face);
            if (isSolid(neighbor)) {
                // Click the face of the neighbor that points toward placePos
                return new PlaceTarget(neighbor, face.getOpposite());
            }
        }
        return null;
    }

    private boolean isSolid(BlockPos pos) {
        Material mat = mc.theWorld.getBlockState(pos).getBlock().getMaterial();
        return mat != Material.air && mat != Material.water && mat != Material.lava;
    }

    private static class PlaceTarget {
        final BlockPos   support;
        final EnumFacing face;
        PlaceTarget(BlockPos support, EnumFacing face) {
            this.support = support;
            this.face    = face;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long baseDelay() {
        long d = (long)(double) delay.getValue();
        if (d <= 0) return 0;
        long jitter = Math.max(1L, d / 4);
        return d + (long)((rng.nextDouble() * 2 - 1) * jitter);
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
