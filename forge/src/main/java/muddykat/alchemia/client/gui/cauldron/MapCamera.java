package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public final class MapCamera {

    public static final double MAX_ZOOM = 3.0;

    private static final double MIN_ZOOM = 0.1;
    private static final double ZOOM_STEP = 1.18;
    private static final double OVERSCAN = 4 * PotionMap.CELL;
    private static final double FOLLOW_MARGIN = 0.3;

    private final Supplier<PotionMap> map;

    private int left;
    private int top;
    private int width;
    private int height;
    private double x;
    private double y;
    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double anchorWorldX;
    private double anchorWorldY;
    private double anchorScreenX;
    private double anchorScreenY;
    private boolean ready;

    public MapCamera(Supplier<PotionMap> map) {
        this.map = map;
    }

    public void place(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public MapView view() {
        return new MapView(left, top, width, height, centreX(), centreY(), x, y, zoom);
    }

    public boolean ready() {
        return ready;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double zoom() {
        return zoom;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    public void reset(PathPoint at) {
        x = at.x();
        y = at.y();
        zoom = 1.0;
        targetZoom = 1.0;
        ready = true;
    }

    public void scroll(double guiX, double guiY, double scrollY) {
        anchorScreenX = guiX;
        anchorScreenY = guiY;
        anchorWorldX = x + (guiX - centreX()) / zoom;
        anchorWorldY = y + (guiY - centreY()) / zoom;

        double factor = scrollY > 0 ? ZOOM_STEP : 1.0 / ZOOM_STEP;
        targetZoom = Mth.clamp(targetZoom * factor, MIN_ZOOM, MAX_ZOOM);
    }

    public void drag(double dx, double dy) {
        x -= dx / zoom;
        y -= dy / zoom;
        targetZoom = zoom;
        clamp();
    }

    public void tick() {
        if (Math.abs(targetZoom - zoom) < 0.0005) {
            zoom = targetZoom;
            return;
        }
        zoom = Mth.lerp(0.35, zoom, targetZoom);
        x = anchorWorldX - (anchorScreenX - centreX()) / zoom;
        y = anchorWorldY - (anchorScreenY - centreY()) / zoom;
        clamp();
    }

    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
        targetZoom = zoom;
        clamp();
    }

    public void keepNear(PathPoint at) {
        double reachX = width * FOLLOW_MARGIN / zoom;
        double reachY = height * FOLLOW_MARGIN / zoom;
        moveTo(Mth.clamp(x, at.x() - reachX, at.x() + reachX), Mth.clamp(y, at.y() - reachY, at.y() + reachY));
    }

    private void clamp() {
        if (!PotionMap.isReady()) return;
        double limit = map.get().getMaxAlignment() + OVERSCAN;
        x = Mth.clamp(x, -OVERSCAN, limit);
        y = Mth.clamp(y, -OVERSCAN, limit);
    }

    private double centreX() {
        return left + width / 2.0;
    }

    private double centreY() {
        return top + height / 2.0;
    }
}
