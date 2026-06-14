package com.zenkai.zenclient.setting.settings;

import com.zenkai.zenclient.setting.Setting;

/** A simple on/off toggle setting. */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public boolean isEnabled() { return getValue(); }

    public void toggle() { setValue(!getValue()); }

    @Override
    public String serialise() { return Boolean.toString(getValue()); }

    @Override
    public void deserialise(String raw) { setValue(Boolean.parseBoolean(raw)); }
}
