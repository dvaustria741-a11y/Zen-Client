package com.zenkai.zenclient.hud.elements;

import com.zenkai.zenclient.gui.Theme;
import com.zenkai.zenclient.gui.util.RenderUtil;
import com.zenkai.zenclient.hud.HudElement;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * HUD widget — lists active potion effects with remaining duration.
 */
public final class PotionHud extends HudElement {

    private static final float PAD_X = 4f;
    private static final float PAD_Y = 3f;
    private static final float ROW   = 11f;
    private static final float GAP   = 1f;

    public PotionHud() {
        super("Potions", 2, 210);
    }

    @Override
    public void render(float partialTicks) {
        if (mc.thePlayer == null) return;

        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        if (effects.isEmpty()) return;

        List<PotionEffect> sorted = new ArrayList<>(effects);
        sorted.sort(Comparator.comparingInt(PotionEffect::getDuration).reversed());

        float x = getX(), y = getY();
        float w = getWidth();
        float h = PAD_Y + sorted.size() * (ROW + GAP) - GAP + PAD_Y;

        RenderUtil.drawRoundedRect(x, y, w, h, 3, Theme.BG_MODULE);
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 3, 0.5f, Theme.BORDER_DIM);

        float ry = y + PAD_Y;
        for (PotionEffect eff : sorted) {
            Potion  p   = Potion.potionTypes[eff.getPotionID()];
            String  name = p.getName().replace("potion.effect.", "");
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

            int lvl = eff.getAmplifier() + 1;
            if (lvl > 1) name += " " + toRoman(lvl);

            String dur = formatDuration(eff.getDuration());
            int    col = p.isBadEffect() ? 0xFFFF6666 : Theme.ACCENT_LT;

            RenderUtil.drawString(name, x + PAD_X, ry, col, false);
            float dw = RenderUtil.stringWidth(dur);
            RenderUtil.drawString(dur, x + w - PAD_X - dw, ry, Theme.TEXT_DIM, false);

            ry += ROW + GAP;
        }
    }

    private static String formatDuration(int ticks) {
        int secs = ticks / 20;
        return String.format("%d:%02d", secs / 60, secs % 60);
    }

    private static String toRoman(int n) {
        String[] r = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };
        return n < r.length ? r[n] : String.valueOf(n);
    }

    @Override public float getWidth()  { return PAD_X + RenderUtil.stringWidth("Resistance II") + 10 + RenderUtil.stringWidth("0:00") + PAD_X; }
    @Override public float getHeight() {
        if (mc.thePlayer == null) return PAD_Y * 2 + ROW;
        int cnt = Math.max(1, mc.thePlayer.getActivePotionEffects().size());
        return PAD_Y + cnt * (ROW + GAP) - GAP + PAD_Y;
    }
}
