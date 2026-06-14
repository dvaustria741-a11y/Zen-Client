package com.zenkai.zenclient.module.modules.utility;

import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Better Chat — adds timestamps and extends chat history length.
 */
public final class BetterChat extends Module {

    private final BooleanSetting timestamps  = addSetting(new BooleanSetting("Timestamps",   "Show time prefix on messages",    true));
    private final BooleanSetting antiSpam    = addSetting(new BooleanSetting("Anti Spam",    "Filter repeated messages",        false));
    private final NumberSetting  historySize = addSetting(new NumberSetting ("History",      "Chat history line count",         1000, 100, 3000, 100));

    private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
    private String lastMessage = "";

    public BetterChat() {
        super("Better Chat", "Chat enhancements: timestamps, history.", Category.UTILITY, Keyboard.KEY_NONE);
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
    public void onChatReceived(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();

        if (antiSpam.isEnabled() && raw.equals(lastMessage)) {
            event.setCanceled(true);
            return;
        }
        lastMessage = raw;

        if (timestamps.isEnabled()) {
            String time = EnumChatFormatting.DARK_GRAY + "[" + fmt.format(new Date()) + "] "
                        + EnumChatFormatting.RESET;
            event.message = new ChatComponentText(time).appendSibling(event.message);
        }
    }
}
