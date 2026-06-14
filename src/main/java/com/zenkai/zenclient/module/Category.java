package com.zenkai.zenclient.module;

/** Top-level categories used to group modules in the ClickGUI. */
public enum Category {
    COMBAT     ("Combat",      '\u2694'),
    MOVEMENT   ("Movement",    '\u26A1'),
    RENDER     ("Render",      '\u25A0'),
    PVP        ("PvP",         '\u2694'),
    PERFORMANCE("Performance", '\u26A1'),
    UTILITY    ("Utility",     '\u2699'),
    MISC       ("Misc",        '\u2022');

    private final String displayName;
    private final char   icon;

    Category(String displayName, char icon) {
        this.displayName = displayName;
        this.icon        = icon;
    }

    public String getDisplayName() { return displayName; }
    public char   getIcon()        { return icon; }
}
