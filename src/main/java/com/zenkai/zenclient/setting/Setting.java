package com.zenkai.zenclient.setting;

/**
 * Base class for all module settings.
 *
 * @param <T> the value type (Boolean, Double, String, etc.)
 */
public abstract class Setting<T> {

    private final String name;
    private final String description;
    private       T      value;

    protected Setting(String name, String description, T defaultValue) {
        this.name        = name;
        this.description = description;
        this.value       = defaultValue;
    }

    // -----------------------------------------------------------------------

    public String getName()        { return name; }
    public String getDescription() { return description; }

    public T getValue()            { return value; }

    public void setValue(T value) {
        this.value = validate(value);
    }

    /**
     * Optionally validate / clamp the incoming value.
     * Default implementation returns it unchanged.
     */
    protected T validate(T value) { return value; }

    // -----------------------------------------------------------------------
    // Serialisation helpers (override in subclasses for JSON persistence)
    // -----------------------------------------------------------------------

    public abstract String serialise();
    public abstract void   deserialise(String raw);
}
