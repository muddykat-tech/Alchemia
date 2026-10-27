package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import muddykat.alchemia.client.effect.ARGB;

public final class CauldronArt {

    public static final int FOLIO_W = 372;
    public static final int FOLIO_H = 250;
    private static final int FOLIO_SHEET_W = 512;
    private static final int FOLIO_SHEET_H = 256;

    public static final int MAP_TILE = 256;
    private static final int DASH = 256;

    public static final int TRAY_W = 178;
    public static final int TRAY_H = 99;
    public static final int TRAY_TUCK = 4;
    public static final int TRAY_SLOT_X = 8;
    public static final int TRAY_SLOT_Y = TRAY_TUCK + 8;
    public static final int TRAY_HOTBAR_GAP = 4;


    public static final int VIAL_W = 100;
    public static final int VIAL_H = 10;
    public static final int SKULL = 9;
    public static final int NOTCH_W = 7;
    public static final int NOTCH_H = 5;

    public static final int CELL = 18;
    public static final int ICON = 12;
    public static final int DIAMOND = 5;

    public static final int INK = 0xFF3B2A1E;
    public static final int INK_HEAD = 0xFF2A1D13;
    public static final int INK_SOFT = 0xFF604A34;
    public static final int INK_FAINT = 0xFF967E5C;
    public static final int INK_RED = 0xFF8E2A22;
    public static final int INK_SHADOW = 0xFFB7A27B;
    public static final int PAGE_SHINE = 0xFFF6EED6;
    public static final int RULE = 0xFFB8A27A;

    private static final ResourceLocation FOLIO = texture("folio");
    private static final ResourceLocation MAP_PAPER = texture("map_paper");
    private static final ResourceLocation DASH_H = texture("dash_h");
    private static final ResourceLocation DASH_V = texture("dash_v");
    private static final ResourceLocation TRAY = texture("tray");
    private static final ResourceLocation VIAL = texture("vial");
    private static final ResourceLocation VIAL_GLASS = texture("vial_glass");
    private static final ResourceLocation SKULLS = texture("skulls");
    private static final ResourceLocation NOTCH = texture("notch");
    private static final ResourceLocation WIDGETS = new ResourceLocation(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final int WIDGET_SHEET = 256;
    private static final int ICON_STEP = 13;
    private static final int CELL_U = 176;
    private static final int CELL_V = 28;
    private static final int DIVIDER_U = 90;
    private static final int DIVIDER_V = 80;
    private static final int DIVIDER_W = 150;
    public static final int DIVIDER_H = 7;
    private static final int DIAMOND_U = 86;
    private static final int DIAMOND_V = 140;

    public static final ResourceLocation BUTTON = sprite("button");
    public static final ResourceLocation BUTTON_HIGHLIGHTED = sprite("button_highlighted");
    public static final ResourceLocation BUTTON_PRESSED = sprite("button_pressed");
    public static final ResourceLocation BUTTON_DISABLED = sprite("button_disabled");
    public static final ResourceLocation BUTTON_READY = sprite("button_ready");
    public static final ResourceLocation BUTTON_READY_HIGHLIGHTED = sprite("button_ready_highlighted");
    public static final ResourceLocation BUTTON_READY_PRESSED = sprite("button_ready_pressed");
    public static final ResourceLocation WORK_FILL = sprite("work_fill");
    public static final ResourceLocation LEG_PROGRESS = sprite("leg_progress");

    public enum Icon {
        COPY(5), UNKNOWN(8), EYE(10), STIR(11), BREW(12), REDSTONE(13), SPLASH(14), EYE_CLOSED(16);

        private final int column;

        Icon(int column) {
            this.column = column;
        }
    }

    public enum Cell {
        PLAIN, SELECTED, GILT, SHADED
    }

    private CauldronArt() {
    }

    public static void folio(GuiGraphics graphics, int x, int y) {
        GuiDraw.blit(graphics, FOLIO, x, y, 0, 0, FOLIO_W, FOLIO_H, FOLIO_SHEET_W, FOLIO_SHEET_H);
    }

    public static void mapPaper(GuiGraphics graphics, int x, int y) {
        GuiDraw.blit(graphics, MAP_PAPER, x, y, 0, 0, MAP_TILE, MAP_TILE, MAP_TILE, MAP_TILE);
    }

    public static void mapPaperPiece(GuiGraphics graphics, int x, int y, int w, int h, int colour) {
        GuiDraw.blit(graphics, MAP_PAPER, x, y, Math.floorMod(x, MAP_TILE), Math.floorMod(y, MAP_TILE), w, h, MAP_TILE, MAP_TILE, colour);
    }

    public static void dashAcross(GuiGraphics graphics, int x0, int x1, int y, int phase, int colour) {
        for (int x = x0; x < x1; x += DASH - 2) {
            int length = Math.min(DASH - 2, x1 - x);
            GuiDraw.blit(graphics, DASH_H, x, y, phase & 1, 0, length, 1, DASH, 1, colour);
        }
    }

    public static void dashDown(GuiGraphics graphics, int x, int y0, int y1, int phase, int colour) {
        for (int y = y0; y < y1; y += DASH - 2) {
            int length = Math.min(DASH - 2, y1 - y);
            GuiDraw.blit(graphics, DASH_V, x, y, 0, phase & 1, 1, length, 1, DASH, colour);
        }
    }

    public static void tray(GuiGraphics graphics, int x, int y) {
        GuiDraw.blit(graphics, TRAY, x, y, 0, 0, TRAY_W, TRAY_H, TRAY_W, TRAY_H);
    }

    public static void vial(GuiGraphics graphics, int x, int y) {
        GuiDraw.blit(graphics, VIAL, x, y, 0, 0, VIAL_W, VIAL_H, VIAL_W, VIAL_H);
    }

    public static void vialGlass(GuiGraphics graphics, int x, int y) {
        GuiDraw.blit(graphics, VIAL_GLASS, x, y, 0, 0, VIAL_W, VIAL_H, VIAL_W, VIAL_H);
    }

    public static void notch(GuiGraphics graphics, int centreX, int y, boolean boiling) {
        GuiDraw.blit(graphics, NOTCH, centreX - NOTCH_W / 2, y, boiling ? NOTCH_W + 1 : 0, 0, NOTCH_W, NOTCH_H, NOTCH_W * 2 + 1, NOTCH_H);
    }

    public static void icon(GuiGraphics graphics, Icon icon, int x, int y, boolean faded) {
        widget(graphics, x, y, icon.column * ICON_STEP, faded ? ICON_STEP : 0, ICON, ICON, -1);
    }

    public static void icon(GuiGraphics graphics, Icon icon, int x, int y, boolean faded, int alpha) {
        widget(graphics, x, y, icon.column * ICON_STEP, faded ? ICON_STEP : 0, ICON, ICON, ARGB.color(alpha, 0xFFFFFF));
    }

    public static void cell(GuiGraphics graphics, int x, int y, Cell state) {
        widget(graphics, x, y, CELL_U + state.ordinal() * (CELL + 1), CELL_V, CELL, CELL, -1);
    }

    public static void divider(GuiGraphics graphics, int centreX, int y, int width) {
        int w = Math.min(width, DIVIDER_W);
        widget(graphics, centreX - w / 2, y, DIVIDER_U + (DIVIDER_W - w) / 2, DIVIDER_V, w, DIVIDER_H, -1);
    }

    public static void diamond(GuiGraphics graphics, int x, int y) {
        widget(graphics, x, y, DIAMOND_U, DIAMOND_V, DIAMOND, DIAMOND, -1);
    }

    public static void lettered(GuiGraphics graphics, Font font, String text, float x, float capTop, float scale, int colour) {
        if (text.isEmpty()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(x, capTop, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, Lettering.of(text), 0, Lettering.RISE, colour, false);
        graphics.pose().popPose();
    }

    public static int letteredWidth(Font font, String text) {
        return text.isEmpty() ? 0 : font.width(Lettering.of(text));
    }

    public static void initialled(GuiGraphics graphics, Font font, String text, float centreX, float y, float scale) {
        float x = centreX - letteredWidth(font, text) * scale / 2.0F;
        String initial = text.isEmpty() ? "" : text.substring(0, text.offsetByCodePoints(0, 1));
        String rest = text.substring(initial.length());
        lettered(graphics, font, text, x + scale, y + scale, scale, INK_SHADOW);
        lettered(graphics, font, initial, x, y, scale, INK_RED);
        lettered(graphics, font, rest, x + letteredWidth(font, initial) * scale, y, scale, INK);
    }

    public static String clip(Font font, Component text, int width) {
        String plain = text.getString();
        if (font.width(plain) <= width) return plain;
        return font.plainSubstrByWidth(plain, width - font.width("…")) + "…";
    }

    private static void widget(GuiGraphics graphics, int x, int y, int u, int v, int w, int h, int colour) {
        GuiDraw.blit(graphics, WIDGETS, x, y, u, v, w, h, WIDGET_SHEET, WIDGET_SHEET, colour);
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(Alchemia.MODID, "textures/gui/cauldron/" + name + ".png");
    }

    private static ResourceLocation sprite(String name) {
        return new ResourceLocation(Alchemia.MODID, "cauldron/" + name);
    }
}
