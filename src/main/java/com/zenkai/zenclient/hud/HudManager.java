package com.zenkai.zenclient.hud;

import com.zenkai.zenclient.event.EventTarget;
import com.zenkai.zenclient.event.events.EventRender2D;
import com.zenkai.zenclient.hud.elements.ArmorHud;
import com.zenkai.zenclient.hud.elements.ClockHud;
import com.zenkai.zenclient.hud.elements.ComboHud;
import com.zenkai.zenclient.hud.elements.CoordsHud;
import com.zenkai.zenclient.hud.elements.CpsHud;
import com.zenkai.zenclient.hud.elements.DirectionHud;
import com.zenkai.zenclient.hud.elements.FpsHud;
import com.zenkai.zenclient.hud.elements.KeystrokesHud;
import com.zenkai.zenclient.hud.elements.PotionHud;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns every {@link HudElement}, dispatches render calls, and
 * handles position persistence to disk.
 */
public final class HudManager {

    private final List<HudElement> elements = new ArrayList<>();

    public HudManager() {
        register(new FpsHud());
        register(new CpsHud());
        register(new CoordsHud());
        register(new KeystrokesHud());
        register(new ComboHud());
        register(new ArmorHud());
        register(new DirectionHud());
        register(new ClockHud());
        register(new PotionHud());
    }

    private void register(HudElement e) { elements.add(e); }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        for (HudElement el : elements) {
            if (el.isVisible()) el.render(event.getPartialTicks());
        }
    }

    public List<HudElement> getElements() { return elements; }

    public void savePositions(File file) {
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (HudElement el : elements) {
                pw.println(el.getName() + "=" + el.getX() + "," + el.getY() + "," + el.isVisible());
            }
        } catch (IOException ex) {
            System.err.println("[ZenClient/HudManager] Save failed: " + ex.getMessage());
        }
    }

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
