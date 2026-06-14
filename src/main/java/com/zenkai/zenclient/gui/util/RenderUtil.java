package com.zenkai.zenclient.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/**
 * Low-level GL rendering utilities.
 *
 * All coordinates are in scaled GUI pixels.
 * Colors are packed ARGB integers (0xAARRGGBB).
 */
public final class RenderUtil {

    private RenderUtil() {}

    // ── GL state management ──────────────────────────────────────────────────

    public static void beginShape() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
    }

    public static void endShape() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private static void color(int argb) {
        GL11.glColor4f(
                ((argb >> 16) & 0xFF) / 255f,
                ((argb >> 8)  & 0xFF) / 255f,
                ( argb        & 0xFF) / 255f,
                ((argb >> 24) & 0xFF) / 255f
        );
    }

    // ── Rectangles ───────────────────────────────────────────────────────────

    public static void drawRect(float x, float y, float w, float h, int c) {
        beginShape();
        color(c);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x,     y    );
        GL11.glVertex2f(x,     y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y    );
        GL11.glEnd();
        endShape();
    }

    public static void drawGradientRect(float x, float y, float w, float h,
                                        int topColor, int bottomColor) {
        beginShape();
        GL11.glBegin(GL11.GL_QUADS);
        color(topColor);
        GL11.glVertex2f(x,     y    );
        GL11.glVertex2f(x + w, y    );
        color(bottomColor);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x,     y + h);
        GL11.glEnd();
        endShape();
    }

    public static void drawGradientRectH(float x, float y, float w, float h,
                                         int leftColor, int rightColor) {
        beginShape();
        GL11.glBegin(GL11.GL_QUADS);
        color(leftColor);
        GL11.glVertex2f(x,     y    );
        GL11.glVertex2f(x,     y + h);
        color(rightColor);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y    );
        GL11.glEnd();
        endShape();
    }

    // ── Rounded rectangles ───────────────────────────────────────────────────

    /** Filled rounded rectangle with equal corner radii. */
    public static void drawRoundedRect(float x, float y, float w, float h, float r, int color) {
        r = Math.min(r, Math.min(w, h) / 2f);
        beginShape();
        color(color);

        // Center + 4 axis-aligned strips
        fillQuad(x + r, y,     w - 2*r, h      );   // horizontal span
        fillQuad(x,     y + r, r,       h - 2*r);   // left strip
        fillQuad(x+w-r, y + r, r,       h - 2*r);   // right strip

        // Corners
        fillArc(x + r,     y + r,     r, 180, 270);
        fillArc(x + w - r, y + r,     r, 270, 360);
        fillArc(x + w - r, y + h - r, r,   0,  90);
        fillArc(x + r,     y + h - r, r,  90, 180);

        endShape();
    }

    /** Rounded rectangle outline (no fill). */
    public static void drawRoundedRectOutline(float x, float y, float w, float h,
                                              float r, float lineW, int color) {
        r = Math.min(r, Math.min(w, h) / 2f);
        // Implemented as 4 filled edge strips + 4 arc rings
        // Top / bottom
        drawRect(x + r,     y,         w - 2*r, lineW, color);
        drawRect(x + r,     y + h - lineW, w - 2*r, lineW, color);
        // Left / right
        drawRect(x,         y + r,     lineW, h - 2*r, color);
        drawRect(x + w - lineW, y + r, lineW, h - 2*r, color);

        beginShape();
        color(color);
        strokeArc(x + r,     y + r,     r, 180, 270, lineW);
        strokeArc(x + w - r, y + r,     r, 270, 360, lineW);
        strokeArc(x + w - r, y + h - r, r,   0,  90, lineW);
        strokeArc(x + r,     y + h - r, r,  90, 180, lineW);
        endShape();
    }

    /**
     * Rounded rectangle where only left corners are rounded.
     * Used for the sidebar (right edge is flush with panel edge).
     */
    public static void drawRoundedRectLeft(float x, float y, float w, float h, float r, int color) {
        r = Math.min(r, Math.min(w, h) / 2f);
        beginShape();
        color(color);
        fillQuad(x + r, y,     w - r, h      );
        fillQuad(x,     y + r, r,     h - 2*r);
        fillArc(x + r, y + r,     r, 180, 270);
        fillArc(x + r, y + h - r, r,  90, 180);
        endShape();
    }

    // ── Circles ──────────────────────────────────────────────────────────────

    public static void drawCircle(float cx, float cy, float radius, int color) {
        beginShape();
        color(color);
        fillArc(cx, cy, radius, 0, 360);
        endShape();
    }

    public static void drawCircleOutline(float cx, float cy, float radius, float lineW, int color) {
        beginShape();
        color(color);
        strokeArc(cx, cy, radius, 0, 360, lineW);
        endShape();
    }

    // ── Low-level GL helpers (called between beginShape/endShape) ────────────

    private static void fillQuad(float x, float y, float w, float h) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x,     y    );
        GL11.glVertex2f(x,     y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y    );
        GL11.glEnd();
    }

    /** Filled arc (triangle-fan, angles in degrees). */
    private static void fillArc(float cx, float cy, float r, float startDeg, float endDeg) {
        int segs = 10;
        float s = (float) Math.toRadians(startDeg);
        float e = (float) Math.toRadians(endDeg);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= segs; i++) {
            float a = s + (e - s) * i / segs;
            GL11.glVertex2f(cx + (float) Math.cos(a) * r,
                            cy + (float) Math.sin(a) * r);
        }
        GL11.glEnd();
    }

    /** Stroked arc (quad-strip ring). */
    private static void strokeArc(float cx, float cy, float r, float startDeg, float endDeg, float lw) {
        int segs = 10;
        float s = (float) Math.toRadians(startDeg);
        float e = (float) Math.toRadians(endDeg);
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= segs; i++) {
            float a = s + (e - s) * i / segs;
            float cos = (float) Math.cos(a), sin = (float) Math.sin(a);
            GL11.glVertex2f(cx + cos * (r - lw), cy + sin * (r - lw));
            GL11.glVertex2f(cx + cos * r,         cy + sin * r        );
        }
        GL11.glEnd();
    }

    // ── Text ─────────────────────────────────────────────────────────────────

    public static void drawString(String text, float x, float y, int color, boolean shadow) {
        GlStateManager.enableTexture2D();
        if (shadow) {
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, x, y, color);
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString(text, (int) x, (int) y, color);
        }
    }

    public static int stringWidth(String text) {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
    }

    public static int fontHeight() {
        return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
    }

    // ── Scissor / clipping ───────────────────────────────────────────────────

    /**
     * Enable GL scissor clipping in GUI (scaled) coordinates.
     * Remember to call {@link #stopScissor()} when done.
     */
    public static void startScissor(int x, int y, int w, int h) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int f  = sr.getScaleFactor();
        int sh = Minecraft.getMinecraft().displayHeight;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * f, sh - (y + h) * f, w * f, h * f);
    }

    public static void stopScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
