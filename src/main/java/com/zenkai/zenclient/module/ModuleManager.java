package com.zenkai.zenclient.module;

import com.zenkai.zenclient.module.modules.combat.KillAura;
import com.zenkai.zenclient.module.modules.misc.AntiAFK;
import com.zenkai.zenclient.module.modules.movement.Speed;
import com.zenkai.zenclient.module.modules.movement.Sprint;
import com.zenkai.zenclient.module.modules.movement.ToggleSneak;
import com.zenkai.zenclient.module.modules.movement.ToggleSprint;
import com.zenkai.zenclient.module.modules.render.ESP;
import com.zenkai.zenclient.module.modules.render.Freelook;
import com.zenkai.zenclient.module.modules.render.FullBright;
import com.zenkai.zenclient.module.modules.render.Zoom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds and manages every registered {@link Module}.
 *
 * Register new modules in {@link #registerAll()}.
 */
public final class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        registerAll();
    }

    // -----------------------------------------------------------------------
    // Registration
    // -----------------------------------------------------------------------

    private void registerAll() {
        // ── Combat ──────────────────────────────────────────────────────────
        register(new KillAura());

        // ── Movement ────────────────────────────────────────────────────────
        register(new Sprint());
        register(new Speed());
        register(new ToggleSprint());
        register(new ToggleSneak());

        // ── Render ──────────────────────────────────────────────────────────
        register(new ESP());
        register(new FullBright());
        register(new Zoom());
        register(new Freelook());

        // ── Misc ────────────────────────────────────────────────────────────
        register(new AntiAFK());
    }

    private void register(Module module) {
        modules.add(module);
    }

    // -----------------------------------------------------------------------
    // Lookup
    // -----------------------------------------------------------------------

    public List<Module> getModules() { return modules; }

    public List<Module> getByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) modules.stream()
                .filter(m -> m.getClass() == clazz)
                .findFirst()
                .orElse(null);
    }

    public Module getByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    // -----------------------------------------------------------------------
    // Key bind handling (call from event listener)
    // -----------------------------------------------------------------------

    public void onKeyPress(int keyCode) {
        if (keyCode <= 0) return;
        modules.stream()
                .filter(m -> m.getKeyBind() == keyCode)
                .forEach(Module::toggle);
    }

    // -----------------------------------------------------------------------
    // Array-list order (sorted by name length for HUD)
    // -----------------------------------------------------------------------

    public List<Module> getEnabledSorted() {
        return modules.stream()
                .filter(Module::isEnabled)
                .filter(Module::isVisible)
                .sorted(Comparator.comparingInt(m -> -m.getName().length()))
                .collect(Collectors.toList());
    }
}
