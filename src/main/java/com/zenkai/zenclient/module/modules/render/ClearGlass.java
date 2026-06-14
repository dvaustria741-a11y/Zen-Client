package com.zenkai.zenclient.module.modules.render;

import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * Clear Glass — makes stained glass panes render without the default tint overlay.
 * Full implementation requires a texture resource-pack swap at runtime;
 * this module stub is the hook point for that integration.
 */
public final class ClearGlass extends Module {
    public ClearGlass() {
        super("Clear Glass", "Removes the tint from stained glass.", Category.RENDER, Keyboard.KEY_NONE);
    }
}
