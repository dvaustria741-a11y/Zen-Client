package com.zenkai.zenclient.setting.settings;

import com.zenkai.zenclient.setting.Setting;

/** A numeric (double) setting clamped between [min, max]. */
public class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double increment;

    public NumberSetting(String name, String description,
                         double defaultValue, double min, double max, double increment) {
        super(name, description, defaultValue);
        this.min       = min;
        this.max       = max;
        this.increment = increment;
    }

    public double getMin()       { return min; }
    public double getMax()       { return max; }
    public double getIncrement() { return increment; }

    /** Returns the value as a float for convenience. */
    public float getFloat() { return getValue().floatValue(); }

    /** Returns the value rounded to the nearest int. */
    public int getInt() { return (int) Math.round(getValue()); }

    @Override
    protected Double validate(Double value) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public String serialise() { return Double.toString(getValue()); }

    @Override
    public void deserialise(String raw) {
        try { setValue(Double.parseDouble(raw)); }
        catch (NumberFormatException ignored) { }
    }
}
