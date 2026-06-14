package com.zenkai.zenclient.gui;

import com.zenkai.zenclient.ZenClient;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.module.Category;
import com.zenkai.zenclient.module.Module;
import com.zenkai.zenclient.setting.Setting;
import com.zenkai.zenclient.setting.settings.BooleanSetting;
import com.zenkai.zenclient.setting.settings.ModeSetting;
import com.zenkai.zenclient.setting.settings.NumberSetting;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

/**
 * ZenClient ClickGUI — Lunar-style module browser.
 *
 * Layout:
 *  ┌─── title bar (30px) ──────────────────────────────────┐
 *  │ sidebar (90px) │ divider │ module list (scrollable)   │
 *  └───────────────────────────────────────────────────────┘
 *
 * Open: RIGHT SHIFT   Close: ESC or RIGHT SHIFT again
 */
public final class ClickGUI extends GuiScreen {

    // ── Panel layout (GUI-scaled pixels) ──────────────────────────────────
    private static final int PANEL_W  = 420;
    private static final int PANEL_H  = 270;
    private static final int SIDE_W   = 92;
    private static final int TITLE_H  = 30;
    private static final int MOD_H    = 38;   // module row height
    private static final int MOD_GAP  = 2;
    private static final int SET_H    = 22;   // setting row height
    private static final int SET_GAP  = 1;
    private static final int SLIDER_W = 70;
    private static final int PAD      = 5;

    // ── Colours ────────────────────────────────────────────────────────────
    // Dark purple-black palette inspired by Lunar Client's cleanliness.
    private static final int C_PANEL_BG    = 0xF20A0716;
    private static final int C_TITLE_BG    = 0xFF110D20;
    private static final int C_TITLE_BG2   = 0xFF0C091A;
    private static final int C_SIDEBAR_BG  = 0xCC0D0A1C;
    private static final int C_MODULE_BG   = 0xCC0E0B1C;
    private static final int C_MODULE_HOV  = 0xCC170F28;
    private static final int C_MODULE_ON   = 0xCC160C26;
    private static final int C_SET_BG      = 0xCC0B0818;
    private static final int C_SET_HOV     = 0xCC120A22;
    private static final int C_DIVIDER     = 0xFF1C1030;
    private static final int C_BORDER      = 0xFF241540;
    private static final int C_TOGGLE_OFF  = 0xFF2B2040;
    private static final int C_ACCENT      = 0xFF8B2FC9;
    private static final int C_ACCENT_LT   = 0xFFBB66EE;
    private static final int C_ACCENT_DK   = 0xFF5B1090;
    private static final int C_TEXT        = 0xFFE8E8F0;
    private static final int C_TEXT_DIM    = 0xFF8A88A0;
    private static final int C_TEXT_HINT   = 0xFF4A4860;
    private static final int C_TEXT_ACC    = 0xFFCC88FF;
    private static final int C_WHITE       = 0xFFFFFFFF;

    // ── State ──────────────────────────────────────────────────────────────
    private Category       selCategory   = Category.COMBAT;
    private Module         expandedMod   = null;
    private float          scroll        = 0f;

    // Slider drag
    private NumberSetting  dragSlider    = null;
    private int            sliderTrackX  = 0;

    // Panel top-left (recomputed each frame)
    private int px, py;

    // ── Render ─────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        px = (width  - PANEL_W) / 2;
        py = (height - PANEL_H) / 2;

        // ── Full-screen dim ────────────────────────────────────────────────
        RenderUtil.drawRect(0, 0, width, height, 0x99000000);

        // ── Drop shadow ────────────────────────────────────────────────────
        RenderUtil.drawRect(px - 4, py - 4, PANEL_W + 8, PANEL_H + 8, 0x33000000);
        RenderUtil.drawRect(px - 2, py - 2, PANEL_W + 4, PANEL_H + 4, 0x22000000);

        // ── Panel ──────────────────────────────────────────────────────────
        RenderUtil.drawRect(px, py, PANEL_W, PANEL_H, C_PANEL_BG);
        RenderUtil.drawRoundedRectOutline(px, py, PANEL_W, PANEL_H, 5, 1f, C_BORDER);

        // ── Title bar ──────────────────────────────────────────────────────
        RenderUtil.drawGradientRect(px, py, PANEL_W, TITLE_H, C_TITLE_BG, C_TITLE_BG2);
        // Bottom accent line
        RenderUtil.drawRect(px, py + TITLE_H - 1, PANEL_W, 1, C_ACCENT_DK);
        // Logo ◆
        RenderUtil.drawString("\u25C6", px + 10, py + (TITLE_H - 8) / 2f, C_ACCENT, true);
        RenderUtil.drawString("ZenClient", px + 22, py + (TITLE_H - 8) / 2f, C_ACCENT_LT, true);
        // Right hint
        String hint = "v" + ZenClient.VERSION + "  \u00b7  RSHIFT";
        RenderUtil.drawString(hint,
                px + PANEL_W - RenderUtil.stringWidth(hint) - 8,
                py + (TITLE_H - 8) / 2f,
                C_TEXT_HINT, false);

        // ── Sidebar background ─────────────────────────────────────────────
        RenderUtil.drawRect(px, py + TITLE_H, SIDE_W, PANEL_H - TITLE_H, C_SIDEBAR_BG);

        // ── Sidebar divider ────────────────────────────────────────────────
        RenderUtil.drawRect(px + SIDE_W, py + TITLE_H, 1, PANEL_H - TITLE_H, C_DIVIDER);

        // ── Draw sidebar ───────────────────────────────────────────────────
        renderSidebar(mouseX, mouseY);

        // ── Draw modules ───────────────────────────────────────────────────
        renderModules(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ── Sidebar ────────────────────────────────────────────────────────────

    private void renderSidebar(int mouseX, int mouseY) {
        int sx = px + 4;
        int sy = py + TITLE_H + 10;
        int sw = SIDE_W - 8;

        for (Category cat : Category.values()) {
            boolean sel = cat == selCategory;
            boolean hov = mouseX >= sx && mouseX <= sx + sw
                       && mouseY >= sy && mouseY <= sy + 24;

            if (sel) {
                // Subtle highlight background
                RenderUtil.drawRect(sx, sy, sw, 24, 0xFF140A24);
                // Left accent stripe
                RenderUtil.drawRect(sx, sy + 3, 2, 18, C_ACCENT);
                // Right fade for selected
                RenderUtil.drawGradientRectH(sx + 2, sy, sw - 2, 24, 0x20882ACC, 0x00000000);
            } else if (hov) {
                RenderUtil.drawRect(sx, sy, sw, 24, 0xFF0F0820);
            }

            int col = sel ? C_ACCENT_LT : hov ? C_TEXT : C_TEXT_DIM;
            String label = cat.getIcon() + " " + cat.getDisplayName();
            RenderUtil.drawString(label, sx + 8, sy + 8, col, sel);

            sy += 28;
        }

        // Bottom version watermark
        String by = "by Zenkai";
        RenderUtil.drawString(by,
                px + (SIDE_W - RenderUtil.stringWidth(by)) / 2f,
                py + PANEL_H - 12,
                C_TEXT_HINT, false);
    }

    // ── Module list ────────────────────────────────────────────────────────

    private void renderModules(int mouseX, int mouseY) {
        List<Module> modules = ZenClient.getInstance()
                                        .getModuleManager()
                                        .getByCategory(selCategory);

        int cx = px + SIDE_W + 1 + PAD;
        int cw = PANEL_W - SIDE_W - 1 - PAD * 2;
        int cy = py + TITLE_H + 4;
        int ch = PANEL_H - TITLE_H - 8;

        // Clamp scroll
        int totalH = computeContentHeight(modules);
        if (scroll < 0) scroll = 0;
        if (scroll > Math.max(0, totalH - ch)) scroll = Math.max(0, totalH - ch);

        RenderUtil.startScissor(cx, cy, cw, ch);

        int my = cy - (int) scroll;

        for (Module mod : modules) {
            if (my + MOD_H > cy && my < cy + ch) {
                renderModuleRow(mod, cx, my, cw, mouseX, mouseY);
            }
            my += MOD_H + MOD_GAP;

            if (mod == expandedMod) {
                for (Setting<?> s : mod.getSettings()) {
                    if (my + SET_H > cy && my < cy + ch) {
                        renderSettingRow(s, cx + 8, my, cw - 16, mouseX, mouseY);
                    }
                    my += SET_H + SET_GAP;
                }
                my += 3;
            }
        }

        // Scroll indicator (if needed)
        if (totalH > ch) {
            float thumbH  = Math.max(20f, ch * ch / (float) totalH);
            float thumbY  = cy + (scroll / (float)(totalH - ch)) * (ch - thumbH);
            RenderUtil.drawRect(cx + cw - 3, cy, 2, ch, 0xFF1A1030);
            RenderUtil.drawRect(cx + cw - 3, (int) thumbY, 2, (int) thumbH, C_ACCENT_DK);
        }

        RenderUtil.stopScissor();
    }

    private void renderModuleRow(Module mod, int x, int y, int w, int mx, int my) {
        boolean on  = mod.isEnabled();
        boolean exp = mod == expandedMod;
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + MOD_H;

        // Row background
        int bg = on ? (hov ? C_MODULE_HOV : C_MODULE_ON) : (hov ? C_MODULE_HOV : C_MODULE_BG);
        RenderUtil.drawRect(x, y, w, MOD_H, bg);

        // Top/bottom hairlines
        RenderUtil.drawRect(x, y, w, 1, 0xFF16102A);
        RenderUtil.drawRect(x, y + MOD_H - 1, w, 1, 0xFF16102A);

        // Left enabled stripe
        if (on) {
            RenderUtil.drawRect(x, y, 2, MOD_H, C_ACCENT);
            RenderUtil.drawGradientRectH(x + 2, y, 16, MOD_H, 0x30882ACC, 0x00000000);
        }

        // Module name
        int nameCol = on ? C_ACCENT_LT : C_TEXT;
        RenderUtil.drawString(mod.getName(), x + 10, y + 7, nameCol, on);

        // Description (truncated)
        String desc = mod.getDescription();
        int maxDescW = w - 60;
        if (RenderUtil.stringWidth(desc) > maxDescW) {
            desc = mc.fontRendererObj.trimStringToWidth(desc, maxDescW) + "..";
        }
        RenderUtil.drawString(desc, x + 10, y + 21, C_TEXT_HINT, false);

        // ── Toggle button ──────────────────────────────────────────────────
        drawToggle(x + w - 32, y + (MOD_H - 12) / 2, 24, 12, on);

        // ── Expand chevron (if has settings) ──────────────────────────────
        if (!mod.getSettings().isEmpty()) {
            String chev = exp ? "\u25B2" : "\u25BC";
            boolean chevHov = mx >= x + w - 52 && mx < x + w - 34 && my >= y && my <= y + MOD_H;
            int chevCol = chevHov ? C_TEXT_DIM : C_TEXT_HINT;
            RenderUtil.drawString(chev, x + w - 48, y + (MOD_H - 8) / 2f, chevCol, false);
        }
    }

    private void renderSettingRow(Setting<?> setting, int x, int y, int w, int mx, int my) {
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + SET_H;

        RenderUtil.drawRect(x, y, w, SET_H, hov ? C_SET_HOV : C_SET_BG);

        // Indent line
        RenderUtil.drawRect(x, y, 1, SET_H, C_ACCENT_DK);

        // Setting name
        RenderUtil.drawString(setting.getName(), x + 8, y + (SET_H - 8) / 2f, C_TEXT_DIM, false);

        // ── Control ────────────────────────────────────────────────────────
        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            drawToggle(x + w - 30, y + (SET_H - 12) / 2, 24, 12, bs.isEnabled());

        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            String val = "\u25C4 " + ms.getValue() + " \u25BA";
            RenderUtil.drawString(val, x + w - RenderUtil.stringWidth(val) - 4,
                    y + (SET_H - 8) / 2f, C_TEXT_ACC, false);

        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            double pct = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());

            // Value text
            String val = formatNum(ns);
            int valW = RenderUtil.stringWidth(val);
            RenderUtil.drawString(val,
                    x + w - SLIDER_W - valW - 10,
                    y + (SET_H - 8) / 2f,
                    C_TEXT_ACC, false);

            // Slider track
            int slX = x + w - SLIDER_W - 4;
            int slY = y + (SET_H - 4) / 2;
            RenderUtil.drawRect(slX, slY, SLIDER_W, 4, 0xFF1E1A30);
            RenderUtil.drawRect(slX, slY, (int)(SLIDER_W * pct), 4, C_ACCENT);
            // Knob
            RenderUtil.drawCircle(slX + (float)(SLIDER_W * pct), slY + 2f, 4.5f, C_WHITE);
            RenderUtil.drawCircle(slX + (float)(SLIDER_W * pct), slY + 2f, 3f, C_ACCENT_LT);
        }
    }

    // ── Toggle widget ──────────────────────────────────────────────────────

    private void drawToggle(int x, int y, int w, int h, boolean on) {
        // Track
        int track = on ? C_ACCENT : C_TOGGLE_OFF;
        RenderUtil.drawRoundedRect(x, y, w, h, h / 2f, track);
        // Border
        RenderUtil.drawRoundedRectOutline(x, y, w, h, h / 2f, 0.5f, on ? C_ACCENT_LT : 0xFF3A3058);
        // Knob — slides right when on, left when off
        float kCx = on ? x + w - h / 2f : x + h / 2f;
        RenderUtil.drawCircle(kCx, y + h / 2f, h / 2f - 1.5f, C_WHITE);
    }

    // ── Mouse input ────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        px = (width  - PANEL_W) / 2;
        py = (height - PANEL_H) / 2;

        // ── Sidebar ──────────────────────────────────────────────────────
        int sx = px + 4, sy = py + TITLE_H + 10, sw = SIDE_W - 8;
        for (Category cat : Category.values()) {
            if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + 24) {
                if (selCategory != cat) {
                    selCategory  = cat;
                    expandedMod  = null;
                    scroll       = 0;
                }
                return;
            }
            sy += 28;
        }

        // ── Module list ───────────────────────────────────────────────────
        List<Module> modules = ZenClient.getInstance()
                                        .getModuleManager()
                                        .getByCategory(selCategory);

        int cx = px + SIDE_W + 1 + PAD;
        int cw = PANEL_W - SIDE_W - 1 - PAD * 2;
        int cy = px + TITLE_H + 4; // intentional: will be overridden below
        cy = py + TITLE_H + 4;

        int my = cy - (int) scroll;

        for (Module mod : modules) {
            if (mouseX >= cx && mouseX <= cx + cw && mouseY >= my && mouseY <= my + MOD_H) {
                boolean hasSettings = !mod.getSettings().isEmpty();

                // Expand chevron zone
                if (hasSettings && mouseX >= cx + cw - 52 && mouseX < cx + cw - 34) {
                    expandedMod = (expandedMod == mod) ? null : mod;
                    return;
                }
                // Toggle zone
                mod.toggle();
                return;
            }
            my += MOD_H + MOD_GAP;

            if (mod == expandedMod) {
                for (Setting<?> s : mod.getSettings()) {
                    if (mouseX >= cx + 8 && mouseX <= cx + cw - 8
                     && mouseY >= my && mouseY <= my + SET_H) {
                        handleSettingClick(s, mouseX, cx + 8, cw - 16, button);
                        return;
                    }
                    my += SET_H + SET_GAP;
                }
                my += 3;
            }
        }
    }

    private void handleSettingClick(Setting<?> s, int mouseX, int sx, int sw, int button) {
        if (s instanceof BooleanSetting) {
            ((BooleanSetting) s).toggle();

        } else if (s instanceof ModeSetting) {
            ((ModeSetting) s).cycle();

        } else if (s instanceof NumberSetting) {
            // Begin slider drag — record track origin
            sliderTrackX  = sx + sw - SLIDER_W - 4;
            dragSlider    = (NumberSetting) s;
            updateSlider(mouseX);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLast) {
        if (dragSlider != null && button == 0) {
            updateSlider(mouseX);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragSlider = null;
    }

    private void updateSlider(int mouseX) {
        if (dragSlider == null) return;
        float t = (float)(mouseX - sliderTrackX) / SLIDER_W;
        t = Math.max(0f, Math.min(1f, t));
        double raw = dragSlider.getMin() + t * (dragSlider.getMax() - dragSlider.getMin());
        // Snap to increment
        double inc = dragSlider.getIncrement();
        raw = Math.round(raw / inc) * inc;
        dragSlider.setValue(raw);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scroll -= wheel / 4f;
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Total pixel height of the module list for the current category. */
    private int computeContentHeight(List<Module> modules) {
        int h = 0;
        for (Module m : modules) {
            h += MOD_H + MOD_GAP;
            if (m == expandedMod) {
                h += m.getSettings().size() * (SET_H + SET_GAP) + 3;
            }
        }
        return h;
    }

    private static String formatNum(NumberSetting ns) {
        double inc = ns.getIncrement();
        if (inc == (int) inc && inc >= 1.0) {
            return String.valueOf(ns.getInt());
        }
        return String.format("%.2f", ns.getValue());
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
