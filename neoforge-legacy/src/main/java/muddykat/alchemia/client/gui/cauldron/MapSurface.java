package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.gui.GuiGraphics;
import muddykat.alchemia.client.effect.ARGB;

public final class MapSurface {

    public static final int DEAD_RGB = 0x6B2418;

    private static final int DEAD_FILL = ARGB.color(70, DEAD_RGB);
    private static final int DEAD_RIM = over(ARGB.color(150, DEAD_RGB), DEAD_FILL);
    private static final double DEAD_MIN_PIXELS = 3.0;
    private static final double DEAD_RIM_ZOOM = 0.45;

    private static final int FOG_FILL = ARGB.opaque(0xCBB88F);
    private static final int FOG_SHADE = ARGB.opaque(0xBBA57C);
    private static final int FOG_EDGE = 0xC03B2A1E;
    private static final int FOG_RIM = over(FOG_EDGE, FOG_SHADE);
    private static final double FOG_MIN_PIXELS = 3.0;

    private static final int EDGE_FILL = 0x662A4A8E;
    private static final int EDGE_BORDER = 0xCC1C315F;
    private static final int EDGE_BORDER_WIDTH = 2;

    private static final byte CLEAR = 0;
    private static final byte BAND = 1;
    private static final byte DEEP = 2;

    private final MapRaster dead = new MapRaster("dead");
    private final MapRaster fogged = new MapRaster("fog");
    private final MapRaster edge = new MapRaster("edge");

    private Ground ground;
    private Fog fogDrawn;
    private boolean[] inside = new boolean[0];
    private boolean[] deadAt = new boolean[0];
    private byte[] fogAt = new byte[0];

    private record Ground(MapView view, PotionMap map) {
    }

    private record Fog(Ground ground, boolean on, int version) {
    }

    public void update(MapView view, PotionMap map, MapFog fog, long now) {
        int width = view.width();
        int height = view.height();
        if (width <= 0 || height <= 0) return;
        if (inside.length != width * height) {
            inside = new boolean[width * height];
            deadAt = new boolean[width * height];
            fogAt = new byte[width * height];
        }

        Ground nextGround = new Ground(view, map);
        if (!nextGround.equals(ground)) {
            ground = nextGround;
            classifyGround(view, map);
            paintDead(width, height, view.zoom() >= DEAD_RIM_ZOOM);
            paintEdge(width, height);
        }

        double push = fog.push(now);
        Fog nextFog = new Fog(nextGround, !fog.isEmpty(), fog.version());
        if (!nextFog.equals(fogDrawn) || push < 1.0) {
            fogDrawn = nextFog;
            if (nextFog.on()) classifyFog(view, fog, push);
            paintFog(width, height, nextFog.on());
        }
    }

    public void drawDeadSpace(GuiGraphics graphics) {
        if (ground != null) dead.draw(graphics, ground.view().left(), ground.view().top());
    }

    public void drawFog(GuiGraphics graphics) {
        if (ground != null) fogged.draw(graphics, ground.view().left(), ground.view().top());
    }

    public void drawEdge(GuiGraphics graphics) {
        if (ground != null) edge.draw(graphics, ground.view().left(), ground.view().top());
    }

    public void close() {
        dead.close();
        fogged.close();
        edge.close();
        ground = null;
        fogDrawn = null;
    }

    private void classifyGround(MapView view, PotionMap map) {
        int width = view.width();
        int height = view.height();
        int level = 0;
        while (level + 1 < map.lodLevels() && PotionMap.tileSize(level) * view.zoom() < DEAD_MIN_PIXELS) level++;

        double step = 1.0 / view.zoom();
        for (int row = 0; row < height; row++) {
            double mapY = view.mapY(row);
            double mapX = view.mapX(0);
            for (int column = 0; column < width; column++, mapX += step) {
                int index = row * width + column;
                boolean in = map.inBounds(mapX, mapY);
                inside[index] = in;
                deadAt[index] = in && map.sample(level, mapX, mapY) > 0.0;
            }
        }
    }

    private void classifyFog(MapView view, MapFog fog, double push) {
        int width = view.width();
        int height = view.height();
        int level = fog.level(view.zoom(), FOG_MIN_PIXELS);

        double step = 1.0 / view.zoom();
        for (int row = 0; row < height; row++) {
            double mapY = view.mapY(row);
            double mapX = view.mapX(0);
            for (int column = 0; column < width; column++, mapX += step) {
                double clear = fog.clear(level, mapX, mapY, push);
                fogAt[row * width + column] = clear < MapFog.LINE - MapFog.SHADE ? DEEP : clear < MapFog.LINE ? BAND : CLEAR;
            }
        }
    }

    private void paintDead(int width, int height, boolean rimmed) {
        int[] pixels = dead.begin(width, height);
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                int index = row * width + column;
                if (!deadAt[index]) continue;
                boolean rim = rimmed && (open(deadAt, width, height, column - 1, row) || open(deadAt, width, height, column + 1, row)
                        || open(deadAt, width, height, column, row - 1) || open(deadAt, width, height, column, row + 1));
                pixels[index] = rim ? DEAD_RIM : DEAD_FILL;
            }
        }
        dead.finish();
    }

    private void paintFog(int width, int height, boolean on) {
        int[] pixels = fogged.begin(width, height);
        if (on) {
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    int index = row * width + column;
                    byte shade = fogAt[index];
                    if (shade == CLEAR) continue;
                    boolean rim = clearAt(width, height, column - 1, row) || clearAt(width, height, column + 1, row)
                            || clearAt(width, height, column, row - 1) || clearAt(width, height, column, row + 1);
                    pixels[index] = rim ? FOG_RIM : shade == DEEP ? FOG_FILL : FOG_SHADE;
                }
            }
        }
        fogged.finish();
    }

    private void paintEdge(int width, int height) {
        int[] pixels = edge.begin(width, height);
        boolean[] near = new boolean[width * height];
        int[] count = new int[Math.max(width, height) + 1];

        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                count[column + 1] = count[column] + (inside[row * width + column] ? 1 : 0);
            }
            for (int column = 0; column < width; column++) {
                int from = Math.max(0, column - EDGE_BORDER_WIDTH);
                int to = Math.min(width, column + EDGE_BORDER_WIDTH + 1);
                near[row * width + column] = count[to] - count[from] > 0;
            }
        }

        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) count[row + 1] = count[row] + (near[row * width + column] ? 1 : 0);
            for (int row = 0; row < height; row++) {
                int index = row * width + column;
                if (inside[index]) continue;
                int from = Math.max(0, row - EDGE_BORDER_WIDTH);
                int to = Math.min(height, row + EDGE_BORDER_WIDTH + 1);
                pixels[index] = count[to] - count[from] > 0 ? EDGE_BORDER : EDGE_FILL;
            }
        }
        edge.finish();
    }

    private static boolean open(boolean[] field, int width, int height, int column, int row) {
        if (column < 0 || row < 0 || column >= width || row >= height) return false;
        return !field[row * width + column];
    }

    private boolean clearAt(int width, int height, int column, int row) {
        if (column < 0 || row < 0 || column >= width || row >= height) return false;
        return fogAt[row * width + column] == CLEAR;
    }

    private static int over(int top, int bottom) {
        float topAlpha = ARGB.alpha(top) / 255.0F;
        float bottomAlpha = ARGB.alpha(bottom) / 255.0F * (1.0F - topAlpha);
        float alpha = topAlpha + bottomAlpha;
        if (alpha <= 0.0F) return 0;
        int red = Math.round((ARGB.red(top) * topAlpha + ARGB.red(bottom) * bottomAlpha) / alpha);
        int green = Math.round((ARGB.green(top) * topAlpha + ARGB.green(bottom) * bottomAlpha) / alpha);
        int blue = Math.round((ARGB.blue(top) * topAlpha + ARGB.blue(bottom) * bottomAlpha) / alpha);
        return ARGB.color(Math.round(alpha * 255.0F), red, green, blue);
    }
}
