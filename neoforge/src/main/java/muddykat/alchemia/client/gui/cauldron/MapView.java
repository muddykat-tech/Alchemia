package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.PathRenderer;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public record MapView(int left, int top, int width, int height, double centreX, double centreY,
                      double camX, double camY, double zoom) {

    public static final int NODE_BASE = 18;

    private static final double PATH_BASE_SCALE = 2.0;
    private static final double MIN_PATH_SCALE = 0.5;

    public int right() {
        return left + width;
    }

    public int bottom() {
        return top + height;
    }

    public double screenX(double mapX) {
        return centreX + (mapX - camX) * zoom;
    }

    public double screenY(double mapY) {
        return centreY + (mapY - camY) * zoom;
    }

    public double mapAtX(double screen) {
        return camX + (screen - centreX) / zoom;
    }

    public double mapAtY(double screen) {
        return camY + (screen - centreY) / zoom;
    }

    public PathPoint toScreen(PathPoint point) {
        return new PathPoint(screenX(point.x()), screenY(point.y()));
    }

    public List<PathPoint> toScreen(List<PathPoint> points) {
        List<PathPoint> screen = new ArrayList<>(points.size());
        for (PathPoint point : points) screen.add(toScreen(point));
        return screen;
    }

    public boolean overlaps(PathPoint centre, double reach) {
        return centre.x() + reach >= left && centre.x() - reach <= left + width
                && centre.y() + reach >= top && centre.y() - reach <= top + height;
    }

    public PathRenderer.Clip clip() {
        return new PathRenderer.Clip(left, top, left + width, top + height);
    }

    public double pathScale() {
        return PATH_BASE_SCALE * Mth.clamp(zoom, MIN_PATH_SCALE, MapCamera.MAX_ZOOM);
    }

    public int effectNode() {
        return Math.max(6, (int) Math.round(NODE_BASE * Mth.clamp(zoom, 0.55, 2.0)));
    }

    public double markerZoom() {
        return effectNode() / 16.0;
    }

    double mapX(int column) {
        return camX + (left + column + 0.5 - centreX) / zoom;
    }

    double mapY(int row) {
        return camY + (top + row + 0.5 - centreY) / zoom;
    }
}
