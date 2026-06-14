package com.zenkai.zenclient.module.modules.combat;

import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * Combo Counter — tracks consecutive hits on enemies and resets when the
 * player takes damage or stops attacking within the timeout window.
 *
 * The static {@link #getCombo()} method is read by {@link com.zenkai.zenclient.hud.elements.ComboHud}.
 */
public final class ComboCounter extends Module {

    private final NumberSetting resetTime = addSetting(
            new NumberSetting("Reset Time", "Seconds without hit to reset combo", 2.0, 0.5, 5.0, 0.5));

    private static int  combo     = 0;
    private static long lastHitMs = 0L;

    public ComboCounter() {
        super("Combo Counter", "Counts consecutive hits in PvP.", Category.COMBAT, Keyboard.KEY_NONE);
    }

    public static int  getCombo()   { return combo; }
    public static long getLastHit() { return lastHitMs; }

    @Override
    public void onEnable() {
        super.onEnable();
        combo = 0;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        combo = 0;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (mc.thePlayer == null) return;

        long now = System.currentTimeMillis();

        // If the source is the local player, increment combo
        if (event.source.getEntity() == mc.thePlayer) {
            if (now - lastHitMs > (long)(resetTime.getValue() * 1000)) {
                combo = 0;
            }
            combo++;
            lastHitMs = now;
            return;
        }

        // If the local player takes damage, reset
        if (event.entityLiving == mc.thePlayer) {
            combo = 0;
        }
    }
}
