package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.guide.GuidePrimer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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

    public static void chartPaper(GuiGraphics graphics, int x, int y, int w, int h) {
        chartPaper(graphics, GuidePrimer.chartPaper(), x, y, w, h);
    }

    private static void chartPaper(GuiGraphics graphics, ResourceLocation paper, int x, int y, int w, int h) {
        int scale = GuidePrimer.PHOTO_SCALE;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(1.0F / scale, 1.0F / scale, 1.0F);
        for (int down = 0; down < h; down += CHART_H) {
            for (int across = 0; across < w; across += CHART_W) {
                int pw = Math.min(CHART_W, w - across);
                int ph = Math.min(CHART_H, h - down);
                GuiDraw.blit(graphics, paper, across * scale, down * scale, 0, 0, pw * scale, ph * scale, CHART_W * scale, CHART_H * scale);
            }
        }
        graphics.pose().popPose();
    }

    public static void roll(GuiGraphics graphics, int x, int y, int w) {
        ResourceLocation sheet = GuidePrimer.pathScroll();
        GuiDraw.blit(graphics, sheet, x, y, 0, 0, ROLL_CAP, ROLL_H + 1, SHEET_W, SHEET_H);
        for (int at = ROLL_CAP; at < w - ROLL_CAP; at += ROLL_MID) {
            int piece = Math.min(ROLL_MID, w - ROLL_CAP - at);
            GuiDraw.blit(graphics, sheet, x + at, y, ROLL_CAP, 0, piece, ROLL_H + 1, SHEET_W, SHEET_H);
        }
        GuiDraw.blit(graphics, sheet, x + w - ROLL_CAP, y, ROLL_CAP + ROLL_MID, 0, ROLL_CAP, ROLL_H + 1, SHEET_W, SHEET_H);
    }

    public static void paper(GuiGraphics graphics, int sheetX, int sheetTop, int w, int sheetBottom, Runnable content) {
        paper(graphics, GuidePrimer.chartPaper(), sheetX, sheetTop, w, sheetBottom, content);
    }

    private static void paper(GuiGraphics graphics, ResourceLocation paper, int sheetX, int sheetTop, int w, int sheetBottom, Runnable content) {
        if (sheetBottom <= sheetTop) return;
        graphics.fill(sheetX + 1, sheetTop + 1, sheetX + w + 1, sheetBottom + 1, SHADE);
        chartPaper(graphics, paper, sheetX, sheetTop, w, sheetBottom - sheetTop);
        GuiDraw.scissor(graphics, sheetX, sheetTop, sheetX + w, sheetBottom);
        content.run();
        graphics.disableScissor();
        graphics.fill(sheetX, sheetTop, sheetX + 1, sheetBottom, EDGE);
        graphics.fill(sheetX + w - 1, sheetTop, sheetX + w, sheetBottom, EDGE);
        graphics.fill(sheetX + 1, sheetTop + 1, sheetX + w - 1, sheetTop + 2, SHADE);
    }

    public static void pull(GuiGraphics graphics, int x, int y, boolean over) {
        GuiDraw.blit(graphics, GuidePrimer.pathScroll(), x, y, PULL_U + (over ? PULL_W : 0), 0, PULL_W, PULL_H, SHEET_W, SHEET_H);
    }

    public static void paperPlain(GuiGraphics graphics, int sheetX, int sheetTop, int w, int sheetBottom, Runnable content) {
        paper(graphics, GuidePrimer.chartPlain(), sheetX, sheetTop, w, sheetBottom, content);
    }

    public static int outerWidth(int sheetW) {
        return sheetW + ROLL_END * 2;
    }

    public static int outerHeight(int sheetH) {
        return ROLL_H - 1 + sheetH + ROLL_H;
    }

    public static int open(GuiGraphics graphics, int left, int y, int sheetW, int sheetH, Runnable content) {
        return open(graphics, GuidePrimer.chartPaper(), left, y, sheetW, sheetH, content);
    }

    public static int openPlain(GuiGraphics graphics, int left, int y, int sheetW, int sheetH, Runnable content) {
        return open(graphics, GuidePrimer.chartPlain(), left, y, sheetW, sheetH, content);
    }

    private static int open(GuiGraphics graphics, ResourceLocation paper, int left, int y, int sheetW, int sheetH, Runnable content) {
        int rollW = outerWidth(sheetW);
        int sheetTop = y + ROLL_H - 1;
        int bottomY = sheetTop + sheetH;
        paper(graphics, paper, left + ROLL_END, sheetTop, sheetW, bottomY + 1, content);
        roll(graphics, left, y, rollW);
        roll(graphics, left, bottomY, rollW);
        return bottomY + ROLL_H;
    }
}
