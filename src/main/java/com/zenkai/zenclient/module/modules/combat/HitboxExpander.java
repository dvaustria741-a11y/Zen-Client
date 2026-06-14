package com.zenkai.zenclient.module.modules.combat;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

/**
 * Hitbox Expander — inflates the client-side bounding box of nearby entities.
 *
 * Performance notes (fixes lag):
 *  • Only processes entities within (expand + 10) blocks — distant entities
 *    are skipped entirely, avoiding most of the iteration cost on busy servers.
 *  • Uses getDistanceSqToEntity (no sqrt) for the culling check.
 *  • Skips the setEntityBoundingBox call when the BB is already the correct
 *    expanded size, eliminating redundant object allocations.
 *  • Runs on EventUpdate POST so the expanded BB is active for raycasts
 *    during the following render frame (vanilla resets BBs each entity tick).
 */
public final class HitboxExpander extends Module {

    private final NumberSetting  expand  = addSetting(new NumberSetting ("Expand",  "Box expansion in blocks", 0.3, 0.05, 2.0, 0.05));
    private final BooleanSetting players = addSetting(new BooleanSetting("Players", "Expand player hitboxes",  true));
    private final BooleanSetting mobs    = addSetting(new BooleanSetting("Mobs",    "Expand mob hitboxes",     false));

    public HitboxExpander() {
        super("Hitbox Expander", "Enlarges entity bounding boxes client-side.", Category.COMBAT, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!event.isPost() || mc.theWorld == null || mc.thePlayer == null) return;

        double e       = expand.getValue();
        // Only iterate entities within this radius (saves iterating 200+ entities on busy servers)
        double maxDistSq = (e + 10.0) * (e + 10.0);

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof EntityLivingBase)) continue;
            EntityLivingBase entity = (EntityLivingBase) obj;
            if (entity == mc.thePlayer)  continue;
            if (entity.getHealth() <= 0) continue;

            // Distance cull — avoids the expand() call for far-away entities
            if (mc.thePlayer.getDistanceSqToEntity(entity) > maxDistSq) continue;

            boolean isPlayer = entity instanceof EntityPlayer;
            if ( isPlayer && !players.isEnabled()) continue;
            if (!isPlayer && !mobs.isEnabled())    continue;

            AxisAlignedBB current = entity.getEntityBoundingBox();

            // Guard: if the BB already has the correct expanded size, skip the
            // setEntityBoundingBox call entirely (no new object, no state change).
            double baseW = entity.width  + 0.1;   // vanilla adds ~0.1 on set
            double baseH = entity.height + 0.1;
            double expectedW = baseW + e * 2;
            double expectedH = baseH + e * 2;
            double actualW   = current.maxX - current.minX;
            double actualH   = current.maxY - current.minY;
            if (Math.abs(actualW - expectedW) < 0.001 && Math.abs(actualH - expectedH) < 0.001) {
                continue; // already expanded — nothing to do
            }

            entity.setEntityBoundingBox(current.expand(e, e, e));
        }
    }
}
