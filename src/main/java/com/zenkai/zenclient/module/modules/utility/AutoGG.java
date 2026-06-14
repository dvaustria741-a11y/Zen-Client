package com.zenkai.zenclient.module.modules.utility;

import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.ModeSetting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * Auto GG — automatically sends "gg" or a custom phrase when a game ends.
 */
public final class AutoGG extends Module {

    private final ModeSetting phrase = addSetting(new ModeSetting(
            "Message", "Message to send", "gg", "gg", "GG", "Good game", "gg ez"));
    private final BooleanSetting delay = addSetting(new BooleanSetting("Delay", "Small delay before sending", true));

    private static final String[] TRIGGERS = {
        "game over", "winner", "you won", "you lost", "top kills",
        "round over", "match over", "final kill"
    };

    public AutoGG() {
        super("Auto GG", "Sends 'gg' when a game ends.", Category.UTILITY, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MinecraftForge.EVENT_BUS.unsubscribe(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (mc.thePlayer == null) return;
        String msg = event.message.getUnformattedText().toLowerCase();
        for (String trigger : TRIGGERS) {
            if (msg.contains(trigger)) {
                final String toSend = phrase.getValue();
                if (delay.isEnabled()) {
                    new Thread(() -> {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                        mc.thePlayer.sendChatMessage(toSend);
                    }).start();
                } else {
                    mc.thePlayer.sendChatMessage(toSend);
                }
                break;
            }
        }
    }
}
