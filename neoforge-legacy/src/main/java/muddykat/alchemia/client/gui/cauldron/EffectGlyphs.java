package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.Alchemia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import muddykat.alchemia.client.effect.ARGB;

public final class EffectGlyphs {

    public static final int UNKNOWN_TINT = 0xFF7A6A52;

    private static final ResourceLocation GUIDE_WIDGETS = ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final int GUIDE_SHEET = 256;
    private static final int CLOSED_EYE_U = 208;
    private static final int CLOSED_EYE_V = 3;
    private static final int CLOSED_EYE_W = 12;
    private static final int CLOSED_EYE_H = 5;
    private static final int OPEN_EYE_U = 130;
    private static final int OPEN_EYE_V = 2;
    private static final int OPEN_EYE_W = 12;
    private static final int OPEN_EYE_H = 8;
    private static final ResourceLocation SYMBOLS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "textures/book/images/symbols.png");
    private static final int SYMBOLS_W = 21;
    private static final int SYMBOLS_H = 45;
    private static final int UNKNOWN_U = 14;
    private static final int UNKNOWN_V = 16;
    private static final int UNKNOWN_W = 7;
    private static final int UNKNOWN_H = 11;
    private static final int UNKNOWN_CELL = 16;

    private EffectGlyphs() {
    }

    public static int fadedUnknown(int alpha) {
        return ARGB.color(alpha * (UNKNOWN_TINT >>> 24) / 255, UNKNOWN_TINT & 0xFFFFFF);
    }

    public static void unknown(GuiGraphics graphics, int cx, int cy, int node) {
        symbol(graphics, cx, cy, node, UNKNOWN_TINT);
    }

    public static void closedEye(GuiGraphics graphics, int cx, int cy, int node) {
        int w = Math.max(1, Math.round(node * (float) CLOSED_EYE_W / UNKNOWN_CELL));
        int h = Math.max(1, Math.round(w * (float) CLOSED_EYE_H / CLOSED_EYE_W));
        GuiDraw.blit(graphics, GUIDE_WIDGETS, cx - w / 2, cy - h / 2, CLOSED_EYE_U, CLOSED_EYE_V, w, h, CLOSED_EYE_W, CLOSED_EYE_H, GUIDE_SHEET, GUIDE_SHEET, -1);
    }

    public static void openEye(GuiGraphics graphics, int cx, int cy, int node) {
        int w = Math.max(1, Math.round(node * (float) OPEN_EYE_W / UNKNOWN_CELL));
        int h = Math.max(1, Math.round(w * (float) OPEN_EYE_H / OPEN_EYE_W));
        GuiDraw.blit(graphics, GUIDE_WIDGETS, cx - w / 2, cy - h / 2, OPEN_EYE_U, OPEN_EYE_V, w, h, OPEN_EYE_W, OPEN_EYE_H, GUIDE_SHEET, GUIDE_SHEET, -1);
    }

    public static void symbol(GuiGraphics graphics, int cx, int cy, int node, int tint) {
        int w = Math.max(1, Math.round(node * (float) UNKNOWN_W / UNKNOWN_CELL));
        int h = Math.max(1, Math.round(node * (float) UNKNOWN_H / UNKNOWN_CELL));
        GuiDraw.blit(graphics, SYMBOLS_TEXTURE, cx - w / 2, cy - h / 2, UNKNOWN_U, UNKNOWN_V, w, h, UNKNOWN_W, UNKNOWN_H, SYMBOLS_W, SYMBOLS_H, tint);
    }
}
