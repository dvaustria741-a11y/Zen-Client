package com.zenkai.zenclient;

import com.zenkai.zenclient.event.EventBus;
import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventKey;
import com.zenkai.zenclient.event.events.EventRender2D;
import com.zenkai.zenclient.event.events.EventUpdate;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.module.ModuleManager;
import com.zenkai.zenclient.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;
import java.util.List;

/**
 * ZenClient — Forge 1.8.9 utility client.
 *
 * Entry point for the FML mod lifecycle.
 * All subsystems are owned by this singleton.
 */
@Mod(
    modid   = ZenClient.MOD_ID,
    name    = ZenClient.MOD_NAME,
    version = ZenClient.VERSION
)
public final class ZenClient {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    public static final String MOD_ID   = "zenclient";
    public static final String MOD_NAME = "ZenClient";
    public static final String VERSION  = "1.0.0";

    // -----------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------

    @Mod.Instance(MOD_ID)
    private static ZenClient instance;

    public static ZenClient getInstance() { return instance; }

    // -----------------------------------------------------------------------
    // Subsystems
    // -----------------------------------------------------------------------

    private EventBus      eventBus;
    private ModuleManager moduleManager;

    // -----------------------------------------------------------------------
    // FML lifecycle
    // -----------------------------------------------------------------------

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        System.out.println("[ZenClient] Initialising v" + VERSION + " ...");

        eventBus      = new EventBus();
        moduleManager = new ModuleManager();

        // Register self for Forge events (tick, key input)
        MinecraftForge.EVENT_BUS.register(this);

        System.out.println("[ZenClient] Registered " + moduleManager.getModules().size() + " modules.");
    }

    @EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        System.out.println("[ZenClient] Ready.");
    }

    // -----------------------------------------------------------------------
    // Forge event bridges → Zen EventBus
    // -----------------------------------------------------------------------

    /** Bridge: Forge client tick → EventUpdate */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        EventUpdate.Stage stage = event.phase == TickEvent.Phase.START
                ? EventUpdate.Stage.PRE : EventUpdate.Stage.POST;

        eventBus.post(new EventUpdate(stage));
    }

    /** Bridge: Forge render tick → EventRender2D (only on START phase) */
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.currentScreen != null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        eventBus.post(new EventRender2D(sr, event.renderTickTime));
    }

    /** Bridge: Forge key input → EventKey + module key-bind dispatch */
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int key = org.lwjgl.input.Keyboard.getEventKey();
        if (!org.lwjgl.input.Keyboard.getEventKeyState()) return;

        moduleManager.onKeyPress(key);
        eventBus.post(new EventKey(key));
    }

    // -----------------------------------------------------------------------
    // Built-in array-list HUD (drawn via EventRender2D in this class)
    // -----------------------------------------------------------------------

    {
        // Self-subscribe after eventBus is created (done in onInit)
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        List<Module> active = moduleManager.getEnabledSorted();
        if (active.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        int x = event.getScreenWidth() - 2;
        int y = 2;

        for (Module m : active) {
            String label = m.getName();
            int    w     = mc.fontRendererObj.getStringWidth(label);
            mc.fontRendererObj.drawStringWithShadow(label, x - w, y, Color.WHITE.getRGB());
            y += mc.fontRendererObj.FONT_HEIGHT + 1;
        }
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public EventBus      getEventBus()      { return eventBus; }
    public ModuleManager getModuleManager() { return moduleManager; }
}
