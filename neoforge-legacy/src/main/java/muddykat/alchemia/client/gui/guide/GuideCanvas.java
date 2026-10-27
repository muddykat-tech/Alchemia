package muddykat.alchemia.client.gui.guide;

import net.minecraft.network.chat.Component;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class GuideCanvas {

    private record Hotspot(int x, int y, int w, int h, @Nullable Runnable action, Supplier<List<Component>> tooltip) {
        boolean covers(double bx, double by) {
            return bx >= x && bx < x + w && by >= y && by < y + h;
        }
    }

    private final GuideScroll scroll;
    private final List<Hotspot> hotspots = new ArrayList<>();
    private final List<Hotspot> rightHotspots = new ArrayList<>();
    private @Nullable Object marqueeKey;
    private long marqueeStart;
    private boolean hoveredMarquee;
    private int left;
    private int top;
    private float scale = 1.0F;
    private double bookMouseX;
    private double bookMouseY;

    public GuideCanvas(GuideScroll scroll) {
        this.scroll = scroll;
    }

    public void place(int left, int top, float scale) {
        this.left = left;
        this.top = top;
        this.scale = scale;
    }

    public int left() {
        return left;
    }

    public int top() {
        return top;
    }

    public float scale() {
        return scale;
    }

    public void toBook(double mouseX, double mouseY) {
        bookMouseX = (mouseX - left) / scale;
        bookMouseY = (mouseY - top) / scale;
    }

    public double bookX() {
        return bookMouseX;
    }

    public double bookY() {
        return bookMouseY;
    }

    public void begin() {
        hotspots.clear();
        rightHotspots.clear();
        hoveredMarquee = false;
    }

    public void end() {
        if (!hoveredMarquee) marqueeKey = null;
    }

    public boolean hovered(int x, int y, int w, int h) {
        return bookMouseX >= x && bookMouseX < x + w && bookMouseY >= y && bookMouseY < y + h;
    }

    public boolean inView() {
        return bookMouseY >= scroll.viewTop() && bookMouseY < scroll.viewBottom();
    }

    public void hotspot(int x, int y, int w, int h, @Nullable Runnable action, Supplier<List<Component>> tooltip) {
        hotspots.add(new Hotspot(x, y, w, h, action, tooltip));
    }

    public void clippedHotspot(int x, int y, int w, int h, @Nullable Runnable action, Supplier<List<Component>> tooltip) {
        int y0 = Math.max(y, scroll.viewTop());
        int y1 = Math.min(y + h, scroll.viewBottom());
        if (y1 > y0) hotspot(x, y0, w, y1 - y0, action, tooltip);
    }

    public void clippedRightClick(int x, int y, int w, int h, Runnable action) {
        int y0 = Math.max(y, scroll.viewTop());
        int y1 = Math.min(y + h, scroll.viewBottom());
        if (y1 > y0) rightHotspots.add(new Hotspot(x, y0, w, y1 - y0, action, List::of));
    }

    public List<Component> tooltip() {
        for (int index = hotspots.size() - 1; index >= 0; index--) {
            Hotspot spot = hotspots.get(index);
            if (spot.covers(bookMouseX, bookMouseY)) return spot.tooltip().get();
        }
        return List.of();
    }

    public boolean click() {
        for (int index = hotspots.size() - 1; index >= 0; index--) {
            Hotspot spot = hotspots.get(index);
            if (!spot.covers(bookMouseX, bookMouseY)) continue;
            if (spot.action() == null) return false;
            spot.action().run();
            return true;
        }
        return false;
    }

    public boolean rightClick() {
        for (int index = rightHotspots.size() - 1; index >= 0; index--) {
            Hotspot spot = rightHotspots.get(index);
            if (spot.covers(bookMouseX, bookMouseY) && spot.action() != null) {
                spot.action().run();
                return true;
            }
        }
        return false;
    }

    long marquee(Object hover, long now) {
        hoveredMarquee = true;
        if (!hover.equals(marqueeKey)) {
            marqueeKey = hover;
            marqueeStart = now;
        }
        return marqueeStart;
    }
}
