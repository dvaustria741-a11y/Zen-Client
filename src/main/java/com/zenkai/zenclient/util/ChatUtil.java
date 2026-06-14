package com.zenkai.zenclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/** Utility methods for sending chat messages to the player. */
public final class ChatUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final String PREFIX =
            EnumChatFormatting.DARK_GRAY + "[" +
            EnumChatFormatting.LIGHT_PURPLE + "Zen" +
            EnumChatFormatting.WHITE + "Client" +
            EnumChatFormatting.DARK_GRAY + "] " +
            EnumChatFormatting.GRAY;

    private ChatUtil() {}

    /** Sends a prefixed message to the local chat HUD (not to the server). */
    public static void sendMessage(String message) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.addChatMessage(new ChatComponentText(PREFIX + message));
    }

    /** Sends a plain message without prefix. */
    public static void sendRaw(String message) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }

    /** Formats a module enable/disable notification. */
    public static void moduleToggle(String moduleName, boolean enabled) {
        String state = enabled
                ? EnumChatFormatting.GREEN + "enabled"
                : EnumChatFormatting.RED   + "disabled";
        sendMessage(EnumChatFormatting.WHITE + moduleName + EnumChatFormatting.GRAY + " " + state + EnumChatFormatting.GRAY + ".");
    }
}
