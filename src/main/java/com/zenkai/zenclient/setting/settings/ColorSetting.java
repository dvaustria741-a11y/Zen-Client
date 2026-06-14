package com.zenkai.zenclient.setting.settings;

import com.zenkai.zenclient.setting.Setting;

import java.awt.Color;

/**
 * A setting that stores an ARGB color as a packed integer.
 * Convenience accessors return {@link Color} and individual channels.
 */
public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, String description, Color defaultColor) {
        super(name, description, defaultColor.getRGB());
    }

    public ColorSetting(String name, String description, int argb) {
        super(name, description, argb);
    }

    // -----------------------------------------------------------------------

    public Color  getColor()  { return new Color(getValue(), true); }
    public int    getRed()    { return (getValue() >> 16) & 0xFF; }
    public int    getGreen()  { return (getValue() >> 8)  & 0xFF; }
    public int    getBlue()   { return  getValue()         & 0xFF; }
    public int    getAlpha()  { return (getValue() >> 24) & 0xFF; }

    /** Returns a new color with the same RGB but the supplied alpha (0-255). */
    public Color withAlpha(int alpha) {
        return new Color(getRed(), getGreen(), getBlue(), alpha);
    }

    public void setColor(Color color) { setValue(color.getRGB()); }

    @Override
    public String serialise() { return Integer.toString(getValue()); }

    @Override
    public void deserialise(String raw) {
        try { setValue(Integer.parseInt(raw)); }
        catch (NumberFormatException ignored) { }
    }
}
