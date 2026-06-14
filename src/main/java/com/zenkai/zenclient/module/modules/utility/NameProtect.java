package com.zenkai.zenclient.module.modules.utility;

import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * Name Protect — replaces the local player's name in chat with "Player".
 * Prevents accidental name reveals in screenshots of chat.
 */
public final class NameProtect extends Module {

    private final BooleanSetting inChat = addSetting(
            new BooleanSetting("In Chat", "Hide name in received chat messages", true));

    public NameProtect() {
        super("Name Protect", "Hides your username from chat.", Category.UTILITY, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!inChat.isEnabled() || mc.thePlayer == null) return;
        String name = mc.thePlayer.getName();
        String raw  = event.message.getFormattedText();
        if (raw.contains(name)) {
            event.message = new ChatComponentText(
                    raw.replace(name, EnumChatFormatting.YELLOW + "Player" + EnumChatFormatting.RESET));
        }
    }
}
