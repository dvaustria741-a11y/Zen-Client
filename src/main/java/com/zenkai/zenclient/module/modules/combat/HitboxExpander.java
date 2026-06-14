package com.zenkai.zenclient.module.modules.combat;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

/**
 * Hitbox Expander — inflates the client-side bounding box of nearby entities
 * so they are easier to click on / hit manually.
 *
 * Runs on EventUpdate POST (after vanilla entity updates reset BBs to their
 * normal size). The expanded BB persists until the entity's next tick update,
 * which means it is active for the entire render frame where your crosshair
 * raycasts and where KillAura does distance checks.
 *
 * Server-side hitboxes are unaffected — this is purely visual/client targeting.
 */
public final class HitboxExpander extends Module {

    private final NumberSetting  expand  = addSetting(new NumberSetting ("Expand", "Box expansion in blocks", 0.3, 0.05, 2.0, 0.05));
    private final BooleanSetting players = addSetting(new BooleanSetting("Players", "Expand player hitboxes",  true));
    private final BooleanSetting mobs    = addSetting(new BooleanSetting("Mobs",    "Expand mob hitboxes",     false));

    public HitboxExpander() {
        super("Hitbox Expander", "Enlarges entity bounding boxes client-side.", Category.COMBAT, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        // POST — entity onUpdate() has already run and reset BBs to normal size.
        // We re-expand here so the inflated box is in place for the next frame.
        if (!event.isPost() || mc.theWorld == null || mc.thePlayer == null) return;

        double e = expand.getValue();

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof EntityLivingBase)) continue;
            EntityLivingBase entity = (EntityLivingBase) obj;
            if (entity == mc.thePlayer)             continue;
            if (entity.getHealth() <= 0)            continue;

            boolean isPlayer = entity instanceof EntityPlayer;
            if (isPlayer  && !players.isEnabled())  continue;
            if (!isPlayer && !mobs.isEnabled())      continue;

            AxisAlignedBB bb = entity.getEntityBoundingBox();
            entity.setEntityBoundingBox(bb.expand(e, e, e));
        }
    }
}
