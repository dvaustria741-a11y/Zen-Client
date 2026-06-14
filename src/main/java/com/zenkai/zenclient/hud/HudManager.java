package com.zenkai.zenclient.hud;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventRender2D;
import com.zenkai.zenclient.hud.elements.CoordsHud;
import com.zenkai.zenclient.hud.elements.CpsHud;
import com.zenkai.zenclient.hud.elements.FpsHud;
import com.zenkai.zenclient.hud.elements.KeystrokesHud;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns every {@link HudElement}, dispatches render calls, and
 * handles position persistence to disk.
 *
 * Subscribed to the Zen EventBus by ZenClient during init.
 */
public final class HudManager {

    private final List<HudElement> elements = new ArrayList<>();

    public HudManager() {
        // Default positions: stacked vertically on the left at x=2
        register(new FpsHud());           // y=2
        register(new CpsHud());           // y=20
        register(new CoordsHud());        // y=40
        register(new KeystrokesHud());    // y=80
    }

    private void register(HudElement e) { elements.add(e); }

    // ── Event listener ────────────────────────────────────────────────────────

    @EventTarget
    public void onRender2D(EventRender2D event) {
        for (HudElement el : elements) {
            if (el.isVisible()) el.render(event.getPartialTicks());
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public List<HudElement> getElements() { return elements; }

    // ── Persistence ───────────────────────────────────────────────────────────

    /**
     * Write all element positions and visibility flags to {@code file}.
     * Format (one line per element): {@code name=x,y,visible}
     */
    public void savePositions(File file) {
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (HudElement el : elements) {
                pw.println(el.getName() + "="
                         + el.getX() + ","
                         + el.getY() + ","
                         + el.isVisible());
            }
        } catch (IOException ex) {
            System.err.println("[ZenClient/HudManager] Save failed: " + ex.getMessage());
        }
    }

    /** Restore positions from the file written by {@link #savePositions}. */
    public void loadPositions(File file) {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String   name = line.substring(0, eq).trim();
                String[] vals = line.substring(eq + 1).split(",");
                if (vals.length < 3) continue;
                for (HudElement el : elements) {
                    if (!el.getName().equals(name)) continue;
                    el.setX(Float.parseFloat(vals[0]));
                    el.setY(Float.parseFloat(vals[1]));
                    el.setVisible(Boolean.parseBoolean(vals[2]));
                }
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("[ZenClient/HudManager] Load failed: " + ex.getMessage());
        }
    }
}
