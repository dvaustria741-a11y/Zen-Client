package com.zenkai.zenclient.module;

import com.zenkai.zenclient.module.modules.combat.ComboCounter;
import com.zenkai.zenclient.module.modules.combat.KillAura;
import com.zenkai.zenclient.module.modules.misc.AntiAFK;
import com.zenkai.zenclient.module.modules.movement.Speed;
import com.zenkai.zenclient.module.modules.movement.Sprint;
import com.zenkai.zenclient.module.modules.movement.ToggleSneak;
import com.zenkai.zenclient.module.modules.movement.ToggleSprint;
import com.zenkai.zenclient.module.modules.performance.FpsBooster;
import com.zenkai.zenclient.module.modules.pvp.Animations;
import com.zenkai.zenclient.module.modules.render.BetterParticles;
import com.zenkai.zenclient.module.modules.render.ClearGlass;
import com.zenkai.zenclient.module.modules.render.ESP;
import com.zenkai.zenclient.module.modules.render.FOVChanger;
import com.zenkai.zenclient.module.modules.render.Freelook;
import com.zenkai.zenclient.module.modules.render.FullBright;
import com.zenkai.zenclient.module.modules.render.HitColor;
import com.zenkai.zenclient.module.modules.render.ItemPhysics;
import com.zenkai.zenclient.module.modules.render.MotionBlur;
import com.zenkai.zenclient.module.modules.render.Zoom;
import com.zenkai.zenclient.module.modules.utility.AutoGG;
import com.zenkai.zenclient.module.modules.utility.BetterChat;
import com.zenkai.zenclient.module.modules.utility.NameProtect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds and manages every registered {@link Module}.
 */
public final class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        registerAll();
    }

    private void registerAll() {
        // ── Combat ──────────────────────────────────────────────────────────
        register(new KillAura());
        register(new ComboCounter());

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
        register(new MotionBlur());
        register(new FOVChanger());
        register(new HitColor());
        register(new ItemPhysics());
        register(new BetterParticles());
        register(new ClearGlass());

        // ── PvP ─────────────────────────────────────────────────────────────
        register(new Animations());

        // ── Performance ─────────────────────────────────────────────────────
        register(new FpsBooster());

        // ── Utility ─────────────────────────────────────────────────────────
        register(new AutoGG());
        register(new BetterChat());
        register(new NameProtect());

        // ── Misc ────────────────────────────────────────────────────────────
        register(new AntiAFK());
    }

    private void register(Module module) { modules.add(module); }

    public List<Module> getModules()                          { return modules; }

    public List<Module> getByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) modules.stream()
                .filter(m -> m.getClass() == clazz)
                .findFirst().orElse(null);
    }

    public Module getByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public void onKeyPress(int keyCode) {
        if (keyCode <= 0) return;
        modules.stream()
                .filter(m -> m.getKeyBind() == keyCode)
                .forEach(Module::toggle);
    }

    public List<Module> getEnabledSorted() {
        return modules.stream()
                .filter(Module::isEnabled)
                .filter(Module::isVisible)
                .sorted(Comparator.comparingInt(m -> -m.getName().length()))
                .collect(Collectors.toList());
    }
}
