package com.zenkai.zenclient.module;

import com.zenkai.zenclient.ZenClient;
import com.zenkai.zenclient.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every Zen Client module (hack/cheat/feature).
 *
 * Subclasses add settings in their constructor via {@link #addSetting(Setting)},
 * and override {@link #onEnable()}, {@link #onDisable()}, and subscribe to
 * events through the global event bus.
 */
public abstract class Module {

    // Minecraft shortcut available to all subclasses
    protected static final Minecraft mc = Minecraft.getMinecraft();

    private final String   name;
    private final String   description;
    private final Category category;
    private       int      keyBind;
    private       boolean  enabled = false;
    private       boolean  visible = true;   // shown in ClickGUI / array list

    private final List<Setting<?>> settings = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    protected Module(String name, String description, Category category, int keyBind) {
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.keyBind     = keyBind;
    }

    protected Module(String name, String description, Category category) {
        this(name, description, category, 0);
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    /** Called once when the module is toggled ON. Subscribes to event bus. */
    public void onEnable() {
        ZenClient.getInstance().getEventBus().subscribe(this);
    }

    /** Called once when the module is toggled OFF. Unsubscribes from event bus. */
    public void onDisable() {
        ZenClient.getInstance().getEventBus().unsubscribe(this);
    }

    // -----------------------------------------------------------------------
    // Toggle
    // -----------------------------------------------------------------------

    public final boolean isEnabled() { return enabled; }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public final void toggle() { setEnabled(!enabled); }

    // -----------------------------------------------------------------------
    // Settings
    // -----------------------------------------------------------------------

    protected final <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> getSettings() { return settings; }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public String   getName()        { return name; }
    public String   getDescription() { return description; }
    public Category getCategory()    { return category; }
    public int      getKeyBind()     { return keyBind; }
    public boolean  isVisible()      { return visible; }

    public void setKeyBind(int key)    { this.keyBind = key; }
    public void setVisible(boolean v)  { this.visible = v; }

    @Override
    public String toString() { return name; }
}
