package com.zenkai.zenclient.module.modules.movement;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

/**
 * AutoScaffold — places blocks under the player's feet automatically.
 *
 * How it works:
 *  Each PRE-update tick the module checks whether the block directly below the
 *  player is solid.  If it is not (air, water, lava), it:
 *    1. Searches the hotbar (slots 0-8) for the first valid ItemBlock stack.
 *    2. Temporarily switches to that slot (restoring the original slot afterwards).
 *    3. Forces the player to sneak so the look-down pitch produces a valid
 *       block-placement ray (optional — enabled by the Sneak setting).
 *    4. Calls playerController.onPlayerRightClick to place the block on the
 *       UP face of the block one below the feet position.
 *
 * Settings:
 *  Delay     — minimum ms between placements (avoids bypasses flagging).
 *  Tower     — also place while holding Jump (build a tower upward).
 *  Sneak     — force-sneak while placing so the player doesn't walk off.
 *  Safe Only — only place when the player is actually falling (posY decreasing).
 */
public final class AutoScaffold extends Module {

    private final NumberSetting  delay    = addSetting(new NumberSetting ("Delay",     "Ms between placements",       50,   0, 500, 5));
    private final BooleanSetting tower    = addSetting(new BooleanSetting("Tower",     "Place while holding Jump",    false));
    private final BooleanSetting sneak    = addSetting(new BooleanSetting("Sneak",     "Force-sneak while placing",   true));
    private final BooleanSetting safeOnly = addSetting(new BooleanSetting("Safe Only", "Only place when falling",     false));

    private long lastPlace      = 0L;
    private int  savedSlot      = -1;
    /** Last Y position — used for falling detection. */
    private double prevY        = 0.0;

    public AutoScaffold() {
        super("AutoScaffold", "Automatically places blocks under your feet.", Category.MOVEMENT, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        savedSlot = -1;
        prevY     = mc.thePlayer != null ? mc.thePlayer.posY : 0.0;
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

        // Tower mode gate — skip unless jumping is held
        boolean jumping = mc.gameSettings.keyBindJump.isKeyDown();
        if (tower.isEnabled() && jumping) {
            // In tower mode we want to place every tick, handled below
        } else if (tower.isEnabled()) {
            // Tower enabled but not jumping — act like normal scaffold
        }

        // Falling check
        boolean isFalling = p.posY < prevY && !p.onGround;
        prevY = p.posY;
        if (safeOnly.isEnabled() && !isFalling) return;

        // Delay gate
        long now = System.currentTimeMillis();
        if (now - lastPlace < (long) delay.getValue()) return;

        // Check block under feet — place at feet-1 (block the player stands on)
        BlockPos feetPos  = new BlockPos(p);
        BlockPos placePos = feetPos.down();

        // If there's already a solid block there, nothing to do
        net.minecraft.block.state.IBlockState state = mc.theWorld.getBlockState(placePos);
        Material mat = state.getBlock().getMaterial();
        if (mat != Material.air && mat != Material.water && mat != Material.lava) return;

        // Find a usable block in the hotbar
        int blockSlot = findBlockInHotbar();
        if (blockSlot < 0) return;

        // Switch slot if needed
        int originalSlot = p.inventory.currentItem;
        if (originalSlot != blockSlot) {
            savedSlot = originalSlot;
            p.inventory.currentItem = blockSlot;
        }

        // Optional sneak so player doesn't slip off the edge
        if (sneak.isEnabled()) p.setSneaking(true);

        // Save & override look direction — pitch straight down for placement
        float origYaw   = p.rotationYaw;
        float origPitch = p.rotationPitch;
        p.rotationPitch = 90f;   // look straight down

        // Place on the UP face of placePos
        ItemStack stack  = p.inventory.getStackInSlot(blockSlot);
        Vec3 hitVec      = new Vec3(placePos.getX() + 0.5,
                                    placePos.getY() + 1.0,
                                    placePos.getZ() + 0.5);
        mc.playerController.onPlayerRightClick(p, mc.theWorld, stack, placePos, EnumFacing.UP, hitVec);
        p.swingItem();

        // Restore look direction
        p.rotationYaw   = origYaw;
        p.rotationPitch = origPitch;

        // Restore slot after placement
        restoreSlot();
        if (sneak.isEnabled()) p.setSneaking(false);

        lastPlace = now;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns the hotbar slot (0-8) of the first stack that is an ItemBlock
     * with at least 1 item, or -1 if none found.
     * Prefers the current slot to minimise unnecessary switching.
     */
    private int findBlockInHotbar() {
        EntityPlayerSP p = mc.thePlayer;
        // Check current slot first (avoid unnecessary switches)
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

    // ── Public accessor for HUD ───────────────────────────────────────────────

    /** Returns total blocks available across the hotbar, or 0 if none. */
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
