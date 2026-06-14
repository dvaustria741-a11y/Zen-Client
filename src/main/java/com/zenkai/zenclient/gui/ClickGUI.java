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
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ZenClient ClickGUI — Lunar-style module browser with search, smooth open animation.
 *
 * Open:  RIGHT SHIFT
 * Close: ESC or RIGHT SHIFT
 */
public final class ClickGUI extends GuiScreen {

    // ── Panel layout ──────────────────────────────────────────────────────
    private static final int PANEL_W  = 390;
    private static final int PANEL_H  = 250;
    private static final int SIDE_W   = 88;
    private static final int TITLE_H  = 28;
    private static final int SEARCH_H = 18;
    private static final int MOD_H    = 36;
    private static final int MOD_GAP  = 2;
    private static final int SET_H    = 20;
    private static final int SET_GAP  = 1;
    private static final int SLIDER_W = 68;
    private static final int PAD      = 5;

    // ── Colours ───────────────────────────────────────────────────────────
    private static final int C_PANEL_BG   = 0xF20A0716;
    private static final int C_TITLE_BG   = 0xFF110D20;
    private static final int C_TITLE_BG2  = 0xFF0C091A;
    private static final int C_SIDEBAR_BG = 0xCC0D0A1C;
    private static final int C_MODULE_BG  = 0xCC0E0B1C;
    private static final int C_MODULE_HOV = 0xCC170F28;
    private static final int C_MODULE_ON  = 0xCC160C26;
    private static final int C_SET_BG     = 0xCC0B0818;
    private static final int C_SET_HOV    = 0xCC120A22;
    private static final int C_DIVIDER    = 0xFF1C1030;
    private static final int C_BORDER     = 0xFF241540;
    private static final int C_TOGGLE_OFF = 0xFF2B2040;
    private static final int C_ACCENT     = 0xFF8B2FC9;
    private static final int C_ACCENT_LT  = 0xFFBB66EE;
    private static final int C_ACCENT_DK  = 0xFF5B1090;
    private static final int C_TEXT       = 0xFFE8E8F0;
    private static final int C_TEXT_DIM   = 0xFF8A88A0;
    private static final int C_TEXT_HINT  = 0xFF4A4860;
    private static final int C_TEXT_ACC   = 0xFFCC88FF;
    private static final int C_WHITE      = 0xFFFFFFFF;
    private static final int C_SEARCH_BG  = 0xFF0A081A;

    // ── State ─────────────────────────────────────────────────────────────
    private Category      selCategory = Category.COMBAT;
    private Module        expandedMod = null;
    private float         scroll      = 0f;
    private GuiTextField  searchField;
    private NumberSetting dragSlider  = null;
    private int           sliderTrackX;

    // Open animation (scale from 0.85 to 1.0 over ~180ms)
    private final long openTime = System.currentTimeMillis();

    private int px, py;

    // ── Init ──────────────────────────────────────────────────────────────

    @Override
    public void initGui() {
        px = (width  - PANEL_W) / 2;
        py = (height - PANEL_H) / 2;
        int sfX = px + SIDE_W + 1 + PAD;
        int sfW = PANEL_W - SIDE_W - 1 - PAD * 2;
        searchField = new GuiTextField(0, mc.fontRendererObj,
                sfX + 4, py + TITLE_H + 4, sfW - 8, SEARCH_H - 6);
        searchField.setMaxStringLength(32);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setTextColor(0xFFBBBBCC);
    }

    // ── Render ────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        px = (width  - PANEL_W) / 2;
        py = (height - PANEL_H) / 2;

        // Open animation
        float animT = Math.min(1f, (System.currentTimeMillis() - openTime) / 180f);
        float scale = 0.85f + 0.15f * easeOut(animT);
        float alpha = animT;

        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(px + PANEL_W / 2f, py + PANEL_H / 2f, 0);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1f);
        net.minecraft.client.renderer.GlStateManager.translate(-(px + PANEL_W / 2f), -(py + PANEL_H / 2f), 0);

        // Full-screen dim (fades in with animation)
        RenderUtil.drawRect(0, 0, width, height, alphaOf(0x99000000, alpha));

        // Drop shadow
        RenderUtil.drawRect(px - 4, py - 4, PANEL_W + 8, PANEL_H + 8, alphaOf(0x33000000, alpha));

        // Panel
        RenderUtil.drawRect(px, py, PANEL_W, PANEL_H, C_PANEL_BG);
        RenderUtil.drawRoundedRectOutline(px, py, PANEL_W, PANEL_H, 5, 1f, C_BORDER);

        // Title bar
        RenderUtil.drawGradientRect(px, py, PANEL_W, TITLE_H, C_TITLE_BG, C_TITLE_BG2);
        RenderUtil.drawRect(px, py + TITLE_H - 1, PANEL_W, 1, C_ACCENT_DK);
        RenderUtil.drawString("\u25C6", px + 10, py + (TITLE_H - 8) / 2f, C_ACCENT, true);
        RenderUtil.drawString("ZenClient", px + 22, py + (TITLE_H - 8) / 2f, C_ACCENT_LT, true);
        String hint = "v" + ZenClient.VERSION + "  \u00b7  RSHIFT";
        RenderUtil.drawString(hint,
                px + PANEL_W - RenderUtil.stringWidth(hint) - 8,
                py + (TITLE_H - 8) / 2f, C_TEXT_HINT, false);

        // Sidebar
        RenderUtil.drawRect(px, py + TITLE_H, SIDE_W, PANEL_H - TITLE_H, C_SIDEBAR_BG);
        RenderUtil.drawRect(px + SIDE_W, py + TITLE_H, 1, PANEL_H - TITLE_H, C_DIVIDER);

        renderSidebar(mouseX, mouseY);

        // Search bar area background
        int sfX = px + SIDE_W + 1 + PAD;
        int sfW = PANEL_W - SIDE_W - 1 - PAD * 2;
        RenderUtil.drawRect(sfX, py + TITLE_H, sfW, SEARCH_H + 2, C_SEARCH_BG);
        RenderUtil.drawRect(sfX, py + TITLE_H + SEARCH_H + 1, sfW, 1, C_DIVIDER);

        // Search icon + field
        RenderUtil.drawString("\u2315", sfX + 3, py + TITLE_H + 5f, C_TEXT_HINT, false);
        if (searchField.getText().isEmpty()) {
            RenderUtil.drawString("Search modules...",
                    sfX + 14, py + TITLE_H + 5f, C_TEXT_HINT, false);
        }
        searchField.xPosition = sfX + 14;
        searchField.yPosition = py + TITLE_H + 4;
        searchField.width     = sfW - 18;
        searchField.drawTextBox();

        renderModules(mouseX, mouseY);

        net.minecraft.client.renderer.GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────

    private void renderSidebar(int mouseX, int mouseY) {
        int sx = px + 4, sy = py + TITLE_H + 8, sw = SIDE_W - 8;
        for (Category cat : Category.values()) {
            boolean sel = cat == selCategory;
            boolean hov = mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + 22;

            if (sel) {
                RenderUtil.drawRect(sx, sy, sw, 22, 0xFF140A24);
                RenderUtil.drawRect(sx, sy + 2, 2, 18, C_ACCENT);
                RenderUtil.drawGradientRectH(sx + 2, sy, sw - 2, 22, 0x20882ACC, 0x00000000);
            } else if (hov) {
                RenderUtil.drawRect(sx, sy, sw, 22, 0xFF0F0820);
            }

            int col = sel ? C_ACCENT_LT : hov ? C_TEXT : C_TEXT_DIM;
            String label = cat.getIcon() + " " + cat.getDisplayName();
            RenderUtil.drawString(label, sx + 8, sy + 7, col, sel);
            sy += 26;
        }

        String by = "by Zenkai";
        RenderUtil.drawString(by,
                px + (SIDE_W - RenderUtil.stringWidth(by)) / 2f,
                py + PANEL_H - 10, C_TEXT_HINT, false);
    }

    // ── Module list ───────────────────────────────────────────────────────

    private void renderModules(int mouseX, int mouseY) {
        List<Module> modules = filteredModules();

        int cx = px + SIDE_W + 1 + PAD;
        int cw = PANEL_W - SIDE_W - 1 - PAD * 2;
        int cy = py + TITLE_H + SEARCH_H + 4;
        int ch = PANEL_H - TITLE_H - SEARCH_H - 8;

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

        if (totalH > ch) {
            float thumbH = Math.max(18f, ch * ch / (float) totalH);
            float thumbY = cy + (scroll / (float)(totalH - ch)) * (ch - thumbH);
            RenderUtil.drawRect(cx + cw - 3, cy, 2, ch, 0xFF1A1030);
            RenderUtil.drawRect(cx + cw - 3, (int) thumbY, 2, (int) thumbH, C_ACCENT_DK);
        }

        RenderUtil.stopScissor();
    }

    private List<Module> filteredModules() {
        List<Module> base = ZenClient.getInstance().getModuleManager().getByCategory(selCategory);
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) return base;
        return base.stream()
                .filter(m -> m.getName().toLowerCase().contains(q)
                          || m.getDescription().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    private void renderModuleRow(Module mod, int x, int y, int w, int mx, int my) {
        boolean on  = mod.isEnabled();
        boolean exp = mod == expandedMod;
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + MOD_H;

        int bg = on ? (hov ? C_MODULE_HOV : C_MODULE_ON) : (hov ? C_MODULE_HOV : C_MODULE_BG);
        RenderUtil.drawRect(x, y, w, MOD_H, bg);
        RenderUtil.drawRect(x, y, w, 1, 0xFF16102A);
        RenderUtil.drawRect(x, y + MOD_H - 1, w, 1, 0xFF16102A);

        if (on) {
            RenderUtil.drawRect(x, y, 2, MOD_H, C_ACCENT);
            RenderUtil.drawGradientRectH(x + 2, y, 16, MOD_H, 0x30882ACC, 0x00000000);
        }

        int nameCol = on ? C_ACCENT_LT : C_TEXT;
        RenderUtil.drawString(mod.getName(), x + 10, y + 6, nameCol, on);

        String desc = mod.getDescription();
        int maxDescW = w - 58;
        if (RenderUtil.stringWidth(desc) > maxDescW)
            desc = mc.fontRendererObj.trimStringToWidth(desc, maxDescW) + "..";
        RenderUtil.drawString(desc, x + 10, y + 19, C_TEXT_HINT, false);

        drawToggle(x + w - 30, y + (MOD_H - 12) / 2, 24, 12, on);

        if (!mod.getSettings().isEmpty()) {
            String chev = exp ? "\u25B2" : "\u25BC";
            boolean chevHov = mx >= x + w - 50 && mx < x + w - 32 && my >= y && my <= y + MOD_H;
            RenderUtil.drawString(chev, x + w - 46, y + (MOD_H - 8) / 2f,
                    chevHov ? C_TEXT_DIM : C_TEXT_HINT, false);
        }
    }

    private void renderSettingRow(Setting<?> setting, int x, int y, int w, int mx, int my) {
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + SET_H;
        RenderUtil.drawRect(x, y, w, SET_H, hov ? C_SET_HOV : C_SET_BG);
        RenderUtil.drawRect(x, y, 1, SET_H, C_ACCENT_DK);
        RenderUtil.drawString(setting.getName(), x + 8, y + (SET_H - 8) / 2f, C_TEXT_DIM, false);

        if (setting instanceof BooleanSetting) {
            drawToggle(x + w - 28, y + (SET_H - 12) / 2, 24, 12, ((BooleanSetting) setting).isEnabled());
        } else if (setting instanceof ModeSetting) {
            String val = "\u25C4 " + ((ModeSetting) setting).getValue() + " \u25BA";
            RenderUtil.drawString(val, x + w - RenderUtil.stringWidth(val) - 4,
                    y + (SET_H - 8) / 2f, C_TEXT_ACC, false);
        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            double pct = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            String val = formatNum(ns);
            int valW = RenderUtil.stringWidth(val);
            RenderUtil.drawString(val, x + w - SLIDER_W - valW - 10,
                    y + (SET_H - 8) / 2f, C_TEXT_ACC, false);
            int slX = x + w - SLIDER_W - 4;
            int slY = y + (SET_H - 4) / 2;
            RenderUtil.drawRect(slX, slY, SLIDER_W, 4, 0xFF1E1A30);
            RenderUtil.drawRect(slX, slY, (int)(SLIDER_W * pct), 4, C_ACCENT);
            RenderUtil.drawCircle(slX + (float)(SLIDER_W * pct), slY + 2f, 4f, C_WHITE);
            RenderUtil.drawCircle(slX + (float)(SLIDER_W * pct), slY + 2f, 2.5f, C_ACCENT_LT);
        }
    }

    private void drawToggle(int x, int y, int w, int h, boolean on) {
        RenderUtil.drawRoundedRect(x, y, w, h, h / 2f, on ? C_ACCENT : C_TOGGLE_OFF);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, h / 2f, 0.5f, on ? C_ACCENT_LT : 0xFF3A3058);
        float kCx = on ? x + w - h / 2f : x + h / 2f;
        RenderUtil.drawCircle(kCx, y + h / 2f, h / 2f - 1.5f, C_WHITE);
    }

    // ── Mouse / keyboard input ────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (searchField != null) {
            searchField.mouseClicked(mouseX, mouseY, button);
        }
        px = (width  - PANEL_W) / 2;
        py = (height - PANEL_H) / 2;

        int sx = px + 4, sy = py + TITLE_H + 8, sw = SIDE_W - 8;
        for (Category cat : Category.values()) {
            if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + 22) {
                if (selCategory != cat) { selCategory = cat; expandedMod = null; scroll = 0; }
                return;
            }
            sy += 26;
        }

        List<Module> modules = filteredModules();
        int cx = px + SIDE_W + 1 + PAD;
        int cw = PANEL_W - SIDE_W - 1 - PAD * 2;
        int cy = py + TITLE_H + SEARCH_H + 4;
        int my = cy - (int) scroll;

        for (Module mod : modules) {
            if (mouseX >= cx && mouseX <= cx + cw && mouseY >= my && mouseY <= my + MOD_H) {
                if (!mod.getSettings().isEmpty() && mouseX >= cx + cw - 50 && mouseX < cx + cw - 32) {
                    expandedMod = (expandedMod == mod) ? null : mod;
                    return;
                }
                mod.toggle();
                return;
            }
            my += MOD_H + MOD_GAP;
            if (mod == expandedMod) {
                for (Setting<?> s : mod.getSettings()) {
                    if (mouseX >= cx + 8 && mouseX <= cx + cw - 8 && mouseY >= my && mouseY <= my + SET_H) {
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
            sliderTrackX = sx + sw - SLIDER_W - 4;
            dragSlider   = (NumberSetting) s;
            updateSlider(mouseX);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLast) {
        if (dragSlider != null && button == 0) updateSlider(mouseX);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) { dragSlider = null; }

    private void updateSlider(int mouseX) {
        if (dragSlider == null) return;
        float t   = (float)(mouseX - sliderTrackX) / SLIDER_W;
        t = Math.max(0f, Math.min(1f, t));
        double raw = dragSlider.getMin() + t * (dragSlider.getMax() - dragSlider.getMin());
        double inc = dragSlider.getIncrement();
        dragSlider.setValue(Math.round(raw / inc) * inc);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) scroll -= wheel / 4f;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField != null && searchField.isFocused()) {
            searchField.textboxKeyTyped(typedChar, keyCode);
            expandedMod = null;
            scroll      = 0;
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private int computeContentHeight(List<Module> modules) {
        int h = 0;
        for (Module m : modules) {
            h += MOD_H + MOD_GAP;
            if (m == expandedMod) h += m.getSettings().size() * (SET_H + SET_GAP) + 3;
        }
        return h;
    }

    private static String formatNum(NumberSetting ns) {
        double inc = ns.getIncrement();
        return (inc == (int) inc && inc >= 1.0) ? String.valueOf(ns.getInt())
                                                 : String.format("%.2f", ns.getValue());
    }

    private static float easeOut(float t) {
        float inv = 1f - t; return 1f - inv * inv * inv;
    }

    private static int alphaOf(int color, float alpha) {
        int a = (int)(((color >> 24) & 0xFF) * alpha);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
