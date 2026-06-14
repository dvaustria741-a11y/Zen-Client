package com.zenkai.zenclient.module;

/** Top-level categories used to group modules in the ClickGUI. */
public enum Category {
    COMBAT  ("Combat",   '\u2694'),   // ⚔
    MOVEMENT("Movement", '\u26A1'),   // ⚡
    RENDER  ("Render",   '\u25A0'),   // ■
    MISC    ("Misc",     '\u2699');   // ⚙

    private final String displayName;
    private final char   icon;

    Category(String displayName, char icon) {
        this.displayName = displayName;
        this.icon        = icon;
    }

    public String getDisplayName() { return displayName; }
    public char   getIcon()        { return icon; }
}
