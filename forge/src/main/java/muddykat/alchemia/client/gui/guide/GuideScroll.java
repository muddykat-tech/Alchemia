package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.client.gui.GuiDraw;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class GuideScroll {

    private static final int TRACK_U = 146;
    private static final int TRACK_V = 28;
    private static final int TRACK_H = 24;
    private static final int THUMB_U = 155;
    private static final int THUMB_H = 15;
    private static final int SCROLL_CAP = 3;
    private static final int THUMB_CAP = 4;
    private static final int THUMB_FILL_V = 4;
    private static final int GRIP_V = 6;
    private static final int GRIP_H = 5;

    private double scroll;
    private int contentHeight;
    private int viewTop;
    private int viewBottom;
    private boolean dragging;
    private double dragOffset;

    public double offset() {
        return scroll;
    }

    public void set(double offset) {
        scroll = offset;
    }

    public void reset() {
        scroll = 0;
        dragging = false;
    }

    public int viewTop() {
        return viewTop;
    }

    public int viewBottom() {
        return viewBottom;
    }

    public boolean overflows() {
        return contentHeight > viewBottom - viewTop;
    }

    public void wheel(double scrollY) {
        scroll = Mth.clamp(scroll - scrollY * LINE * 2, 0, Math.max(0, contentHeight - (viewBottom - viewTop)));
    }

    public void release() {
        dragging = false;
    }

    public boolean dragging() {
        return dragging;
    }

    void panel(GuidePage page, GuiGraphics graphics, int from, int to, Supplier<Integer> body) {
        viewTop = from;
        viewBottom = to;
        int maxScroll = Math.max(0, contentHeight - (to - from));
        scroll = Mth.clamp(scroll, 0, maxScroll);

        GuiDraw.scissor(graphics, RIGHT_X - 2, from, RIGHT_X + PAGE_W + 2, to);
        int end = body.get();
        graphics.disableScissor();
        contentHeight = end + (int) scroll - from;

        maxScroll = Math.max(0, contentHeight - (to - from));
        if (maxScroll > 0) scrollbar(page, graphics, from, to, maxScroll);
    }

    public boolean startDrag(double bookX, double bookY) {
        int height = viewBottom - viewTop;
        int maxScroll = Math.max(1, contentHeight - height);
        int thumbH = thumbHeight(height);
        int thumbY = viewTop + (int) Math.round((height - thumbH) * scroll / maxScroll);
        int x = scrollX();
        if (bookX < x || bookX >= x + TRACK_W || bookY < viewTop || bookY >= viewBottom) return false;

        if (bookY < thumbY || bookY >= thumbY + thumbH) {
            scroll = Mth.clamp((bookY - viewTop - thumbH / 2.0) / (height - thumbH) * maxScroll, 0, maxScroll);
            thumbY = viewTop + (int) Math.round((height - thumbH) * scroll / maxScroll);
        }
        dragging = true;
        dragOffset = bookY - thumbY;
        return true;
    }

    public void drag(double bookY) {
        int height = viewBottom - viewTop;
        int maxScroll = Math.max(1, contentHeight - height);
        int thumbH = thumbHeight(height);
        scroll = Mth.clamp((bookY - dragOffset - viewTop) / Math.max(1, height - thumbH) * maxScroll, 0, maxScroll);
    }

    private static int scrollX() {
        return RIGHT_X + PAGE_W - TRACK_W + 4;
    }

    private void scrollbar(GuidePage page, GuiGraphics graphics, int from, int to, int maxScroll) {
        int x = scrollX();
        int height = to - from;
        page.widget(graphics, x, from, TRACK_U, TRACK_V, TRACK_W, SCROLL_CAP);
        for (int y = from + SCROLL_CAP; y < to - SCROLL_CAP; y += TRACK_H - SCROLL_CAP * 2) {
            page.widget(graphics, x, y, TRACK_U, TRACK_V + SCROLL_CAP, TRACK_W, Math.min(TRACK_H - SCROLL_CAP * 2, to - SCROLL_CAP - y));
        }
        page.widget(graphics, x, to - SCROLL_CAP, TRACK_U, TRACK_V + TRACK_H - SCROLL_CAP, TRACK_W, SCROLL_CAP);

        int thumbH = thumbHeight(height);
        int thumbY = from + (int) Math.round((height - thumbH) * scroll / maxScroll);
        boolean over = dragging || page.hovered(x, thumbY, TRACK_W, thumbH);
        int u = THUMB_U + (over ? 9 : 0);
        page.widget(graphics, x, thumbY, u, TRACK_V, TRACK_W, THUMB_CAP);
        for (int y = thumbY + THUMB_CAP; y < thumbY + thumbH - THUMB_CAP; y += 2) {
            page.widget(graphics, x, y, u, TRACK_V + THUMB_FILL_V, TRACK_W, Math.min(2, thumbY + thumbH - THUMB_CAP - y));
        }
        page.widget(graphics, x, thumbY + thumbH - THUMB_CAP, u, TRACK_V + THUMB_H - THUMB_CAP, TRACK_W, THUMB_CAP);
        page.widget(graphics, x, thumbY + (thumbH - GRIP_H) / 2, u, TRACK_V + GRIP_V, TRACK_W, GRIP_H);
    }

    private int thumbHeight(int height) {
        if (contentHeight <= 0) return height;
        return Mth.clamp(height * height / contentHeight, THUMB_H, height);
    }
}
