package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

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

    private static final Identifier FOLIO = texture("folio");
    private static final Identifier MAP_PAPER = texture("map_paper");
    private static final Identifier DASH_H = texture("dash_h");
    private static final Identifier DASH_V = texture("dash_v");
    private static final Identifier TRAY = texture("tray");
    private static final Identifier VIAL = texture("vial");
    private static final Identifier VIAL_GLASS = texture("vial_glass");
    private static final Identifier SKULLS = texture("skulls");
    private static final Identifier NOTCH = texture("notch");
    private static final Identifier WIDGETS = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/widgets.png");
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

    public static final Identifier BUTTON = sprite("button");
    public static final Identifier BUTTON_HIGHLIGHTED = sprite("button_highlighted");
    public static final Identifier BUTTON_PRESSED = sprite("button_pressed");
    public static final Identifier BUTTON_DISABLED = sprite("button_disabled");
    public static final Identifier BUTTON_READY = sprite("button_ready");
    public static final Identifier BUTTON_READY_HIGHLIGHTED = sprite("button_ready_highlighted");
    public static final Identifier BUTTON_READY_PRESSED = sprite("button_ready_pressed");
    public static final Identifier WORK_FILL = sprite("work_fill");
    public static final Identifier LEG_PROGRESS = sprite("leg_progress");

    public enum Icon {
        INGREDIENTS(3), COPY(5), UNKNOWN(8), EYE(10), STIR(11), BREW(12), REDSTONE(13), SPLASH(14), TOOL(15), EYE_CLOSED(16);

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

    public static void folio(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, FOLIO, x, y, 0, 0, FOLIO_W, FOLIO_H, FOLIO_SHEET_W, FOLIO_SHEET_H);
    }

    public static void mapPaper(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, MAP_PAPER, x, y, 0, 0, MAP_TILE, MAP_TILE, MAP_TILE, MAP_TILE);
    }

    public static void mapPaperPiece(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int colour) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, MAP_PAPER, x, y, Math.floorMod(x, MAP_TILE), Math.floorMod(y, MAP_TILE),
                w, h, MAP_TILE, MAP_TILE, colour);
    }

    public static void dashAcross(GuiGraphicsExtractor graphics, int x0, int x1, int y, int phase, int colour) {
        for (int x = x0; x < x1; x += DASH - 2) {
            int length = Math.min(DASH - 2, x1 - x);
            graphics.blit(RenderPipelines.GUI_TEXTURED, DASH_H, x, y, phase & 1, 0, length, 1, DASH, 1, colour);
        }
    }

    public static void dashDown(GuiGraphicsExtractor graphics, int x, int y0, int y1, int phase, int colour) {
        for (int y = y0; y < y1; y += DASH - 2) {
            int length = Math.min(DASH - 2, y1 - y);
            graphics.blit(RenderPipelines.GUI_TEXTURED, DASH_V, x, y, 0, phase & 1, 1, length, 1, DASH, colour);
        }
    }

    public static void tray(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TRAY, x, y, 0, 0, TRAY_W, TRAY_H, TRAY_W, TRAY_H);
    }

    public static void vial(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, VIAL, x, y, 0, 0, VIAL_W, VIAL_H, VIAL_W, VIAL_H);
    }

    public static void vialGlass(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, VIAL_GLASS, x, y, 0, 0, VIAL_W, VIAL_H, VIAL_W, VIAL_H);
    }

    public static void notch(GuiGraphicsExtractor graphics, int centreX, int y, boolean boiling) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, NOTCH, centreX - NOTCH_W / 2, y, boiling ? NOTCH_W + 1 : 0, 0,
                NOTCH_W, NOTCH_H, NOTCH_W * 2 + 1, NOTCH_H);
    }

    public static void icon(GuiGraphicsExtractor graphics, Icon icon, int x, int y, boolean faded) {
        widget(graphics, x, y, icon.column * ICON_STEP, faded ? ICON_STEP : 0, ICON, ICON, -1);
    }

    public static void icon(GuiGraphicsExtractor graphics, Icon icon, int x, int y, boolean faded, int alpha) {
        widget(graphics, x, y, icon.column * ICON_STEP, faded ? ICON_STEP : 0, ICON, ICON, ARGB.color(alpha, 0xFFFFFF));
    }

    public static void cell(GuiGraphicsExtractor graphics, int x, int y, Cell state) {
        widget(graphics, x, y, CELL_U + state.ordinal() * (CELL + 1), CELL_V, CELL, CELL, -1);
    }

    public static void divider(GuiGraphicsExtractor graphics, int centreX, int y, int width) {
        int w = Math.min(width, DIVIDER_W);
        widget(graphics, centreX - w / 2, y, DIVIDER_U + (DIVIDER_W - w) / 2, DIVIDER_V, w, DIVIDER_H, -1);
    }

    public static void diamond(GuiGraphicsExtractor graphics, int x, int y) {
        widget(graphics, x, y, DIAMOND_U, DIAMOND_V, DIAMOND, DIAMOND, -1);
    }

    public static void lettered(GuiGraphicsExtractor graphics, Font font, String text, float x, float capTop, float scale, int colour) {
        if (text.isEmpty()) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, capTop);
        graphics.pose().scale(scale, scale);
        graphics.text(font, Lettering.of(text), 0, Lettering.RISE, colour, false);
        graphics.pose().popMatrix();
    }

    public static int letteredWidth(Font font, String text) {
        return text.isEmpty() ? 0 : font.width(Lettering.of(text));
    }

    public static void initialled(GuiGraphicsExtractor graphics, Font font, String text, float centreX, float y, float scale) {
        float x = centreX - letteredWidth(font, text) * scale / 2.0F;
        String initial = text.isEmpty() ? "" : text.substring(0, text.offsetByCodePoints(0, 1));
        String rest = text.substring(initial.length());
        lettered(graphics, font, text, x + scale, y + scale, scale, INK_SHADOW);
        lettered(graphics, font, initial, x, y, scale, INK_RED);
        lettered(graphics, font, rest, x + letteredWidth(font, initial) * scale, y, scale, INK);
    }

    private static void widget(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h, int colour) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, x, y, u, v, w, h, WIDGET_SHEET, WIDGET_SHEET, colour);
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/cauldron/" + name + ".png");
    }

    private static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "cauldron/" + name);
    }
}
