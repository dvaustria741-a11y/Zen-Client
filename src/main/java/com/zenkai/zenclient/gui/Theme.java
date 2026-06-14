package com.zenkai.zenclient.gui;

/**
 * Zen Client visual theme — purple × black palette.
 *
 * All constants are packed ARGB integers (0xAARRGGBB).
 */
public final class Theme {

    // ── Backgrounds ──────────────────────────────────────────────────────────
    public static final int BG_OVERLAY   = 0xC0050010;  // full-screen dim
    public static final int BG_MAIN      = 0xF20A0018;  // main panel
    public static final int BG_SIDEBAR   = 0xF2100022;  // category sidebar
    public static final int BG_MODULE    = 0xE0140025;  // module row
    public static final int BG_MODULE_H  = 0xE0280045;  // module hover
    public static final int BG_SETTING   = 0xE00C001A;  // setting row
    public static final int BG_SEARCH    = 0xF0180030;  // search bar

    // ── Accent / brand ───────────────────────────────────────────────────────
    public static final int ACCENT       = 0xFF8B2FC9;  // primary purple
    public static final int ACCENT_LT    = 0xFFBB55EE;  // highlight purple
    public static final int ACCENT_DK    = 0xFF5B0F99;  // dark purple
    public static final int ACCENT_GLOW  = 0x408B2FC9;  // translucent purple glow
    public static final int BORDER       = 0xFF6A0EA0;  // panel border
    public static final int BORDER_DIM   = 0xFF380060;  // subtle border

    // ── Text ─────────────────────────────────────────────────────────────────
    public static final int TEXT         = 0xFFFFFFFF;
    public static final int TEXT_DIM     = 0xFFBBBBBB;
    public static final int TEXT_HINT    = 0xFF666677;
    public static final int TEXT_ACCENT  = 0xFFCC88FF;

    // ── Toggles ──────────────────────────────────────────────────────────────
    public static final int TOGGLE_ON    = 0xFF9B4DCA;
    public static final int TOGGLE_TRACK = 0xFF1E1E3A;
    public static final int TOGGLE_OFF   = 0xFF2A2A2A;
    public static final int TOGGLE_KNOB  = 0xFFFFFFFF;

    // ── Category sidebar ─────────────────────────────────────────────────────
    public static final int CAT_ACTIVE   = 0xFF6B1FA0;
    public static final int CAT_HOVER    = 0xFF250040;

    // ── Slider ───────────────────────────────────────────────────────────────
    public static final int SLIDER_TRACK = 0xFF1E1E3A;
    public static final int SLIDER_FILL  = 0xFF8B2FC9;
    public static final int SLIDER_KNOB  = 0xFFFFFFFF;

    // ── Scrollbar ────────────────────────────────────────────────────────────
    public static final int SCROLL_BG    = 0xFF100020;
    public static final int SCROLL_THUMB = 0xFF6B1FA0;

    private Theme() {}

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Return {@code color} with a new alpha value (0-255). */
    public static int alpha(int color, int a) {
        return (color & 0x00FFFFFF) | (Math.max(0, Math.min(255, a)) << 24);
    }

    /** Scale the alpha of {@code color} by {@code factor} (0-1). */
    public static int scaleAlpha(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        return (color & 0x00FFFFFF) | ((int)(a * factor) << 24);
    }

    /** Linearly interpolate between two ARGB colors. */
    public static int lerpColor(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int af = (from>>24)&0xFF, rf = (from>>16)&0xFF, gf = (from>>8)&0xFF, bf = from&0xFF;
        int at = (to  >>24)&0xFF, rt = (to  >>16)&0xFF, gt = (to  >>8)&0xFF, bt = to  &0xFF;
        return ((int)(af+(at-af)*t)<<24)|((int)(rf+(rt-rf)*t)<<16)
              |((int)(gf+(gt-gf)*t)<< 8)| (int)(bf+(bt-bf)*t);
    }
}
