package com.zenkai.zenclient.setting.settings;

import com.zenkai.zenclient.setting.Setting;

import java.util.Arrays;
import java.util.List;

/** A setting that selects one option from a fixed list of mode strings. */
public class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String description, String defaultMode, String... modes) {
        super(name, description, defaultMode);
        this.modes = Arrays.asList(modes);
        if (!this.modes.contains(defaultMode)) {
            throw new IllegalArgumentException("Default mode '" + defaultMode + "' not in modes list.");
        }
    }

    public List<String> getModes() { return modes; }

    /** True if the current value equals {@code mode} (case-insensitive). */
    public boolean is(String mode) { return getValue().equalsIgnoreCase(mode); }

    /** Cycles forward through the mode list. */
    public void cycle() {
        int idx = modes.indexOf(getValue());
        setValue(modes.get((idx + 1) % modes.size()));
    }

    @Override
    protected String validate(String value) {
        for (String m : modes) {
            if (m.equalsIgnoreCase(value)) return m;
        }
        return getValue(); // keep current if invalid
    }

    @Override
    public String serialise() { return getValue(); }

    @Override
    public void deserialise(String raw) { setValue(raw); }
}
