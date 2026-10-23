package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.guide.GuidePrimer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class ScrollWidgetRenderer {

    public static final int CHART_W = 128;
    public static final int CHART_H = 72;
    public static final int ROLL_H = 7;
    public static final int ROLL_CAP = 6;
    public static final int ROLL_MID = 8;
    public static final int ROLL_END = 4;
    public static final int SHEET_W = 38;
    public static final int SHEET_H = 12;
    public static final int PULL_U = ROLL_CAP * 2 + ROLL_MID;
    public static final int PULL_W = 9;
    public static final int PULL_H = 12;
    public static final int PULL_TUCK = 3;
    public static final int PAD_W = 5;
    public static final int PAD_H = 4;
    public static final int EDGE = 0xFFA8926A;
    public static final int SHADE = 0x40381E0E;

    private ScrollWidgetRenderer() {
    }

    public static void chartPaper(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        chartPaper(graphics, GuidePrimer.chartPaper(), x, y, w, h);
    }

    private static void chartPaper(GuiGraphicsExtractor graphics, Identifier paper, int x, int y, int w, int h) {
        int scale = GuidePrimer.PHOTO_SCALE;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(1.0F / scale, 1.0F / scale);
        for (int down = 0; down < h; down += CHART_H) {
            for (int across = 0; across < w; across += CHART_W) {
                int pw = Math.min(CHART_W, w - across);
                int ph = Math.min(CHART_H, h - down);
                graphics.blit(RenderPipelines.GUI_TEXTURED, paper, across * scale, down * scale, 0, 0,
                        pw * scale, ph * scale, CHART_W * scale, CHART_H * scale);
            }
        }
        graphics.pose().popMatrix();
    }

    public static void roll(GuiGraphicsExtractor graphics, int x, int y, int w) {
        Identifier sheet = GuidePrimer.pathScroll();
        graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x, y, 0, 0, ROLL_CAP, ROLL_H + 1, SHEET_W, SHEET_H);
        for (int at = ROLL_CAP; at < w - ROLL_CAP; at += ROLL_MID) {
            int piece = Math.min(ROLL_MID, w - ROLL_CAP - at);
            graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x + at, y, ROLL_CAP, 0, piece, ROLL_H + 1, SHEET_W, SHEET_H);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x + w - ROLL_CAP, y, ROLL_CAP + ROLL_MID, 0, ROLL_CAP, ROLL_H + 1, SHEET_W, SHEET_H);
    }

    public static void paper(GuiGraphicsExtractor graphics, int sheetX, int sheetTop, int w, int sheetBottom, Runnable content) {
        paper(graphics, GuidePrimer.chartPaper(), sheetX, sheetTop, w, sheetBottom, content);
    }

    private static void paper(GuiGraphicsExtractor graphics, Identifier paper, int sheetX, int sheetTop, int w, int sheetBottom, Runnable content) {
        if (sheetBottom <= sheetTop) return;
        graphics.fill(sheetX + 1, sheetTop + 1, sheetX + w + 1, sheetBottom + 1, SHADE);
        chartPaper(graphics, paper, sheetX, sheetTop, w, sheetBottom - sheetTop);
        graphics.enableScissor(sheetX, sheetTop, sheetX + w, sheetBottom);
        content.run();
        graphics.disableScissor();
        graphics.fill(sheetX, sheetTop, sheetX + 1, sheetBottom, EDGE);
        graphics.fill(sheetX + w - 1, sheetTop, sheetX + w, sheetBottom, EDGE);
        graphics.fill(sheetX + 1, sheetTop + 1, sheetX + w - 1, sheetTop + 2, SHADE);
    }

    public static void pull(GuiGraphicsExtractor graphics, int x, int y, boolean over) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GuidePrimer.pathScroll(), x, y, PULL_U + (over ? PULL_W : 0), 0, PULL_W, PULL_H, SHEET_W, SHEET_H);
    }

    public static void paperPlain(GuiGraphicsExtractor graphics, int sheetX, int sheetTop, int w, int sheetBottom, Runnable content) {
        paper(graphics, GuidePrimer.chartPlain(), sheetX, sheetTop, w, sheetBottom, content);
    }

    public static int outerWidth(int sheetW) {
        return sheetW + ROLL_END * 2;
    }

    public static int outerHeight(int sheetH) {
        return ROLL_H - 1 + sheetH + ROLL_H;
    }

    public static int open(GuiGraphicsExtractor graphics, int left, int y, int sheetW, int sheetH, Runnable content) {
        return open(graphics, GuidePrimer.chartPaper(), left, y, sheetW, sheetH, content);
    }

    public static int openPlain(GuiGraphicsExtractor graphics, int left, int y, int sheetW, int sheetH, Runnable content) {
        return open(graphics, GuidePrimer.chartPlain(), left, y, sheetW, sheetH, content);
    }

    private static int open(GuiGraphicsExtractor graphics, Identifier paper, int left, int y, int sheetW, int sheetH, Runnable content) {
        int rollW = outerWidth(sheetW);
        int sheetTop = y + ROLL_H - 1;
        int bottomY = sheetTop + sheetH;
        paper(graphics, paper, left + ROLL_END, sheetTop, sheetW, bottomY + 1, content);
        roll(graphics, left, y, rollW);
        roll(graphics, left, bottomY, rollW);
        return bottomY + ROLL_H;
    }
}
