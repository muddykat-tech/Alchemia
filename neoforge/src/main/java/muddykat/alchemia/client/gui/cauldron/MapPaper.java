package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

public final class MapPaper {

    private static final double GRID_BASE = PotionMap.CELL / 2.0;
    private static final double GRID_MIN_PIXELS = 8.0;
    private static final int GRID_RGB = 0x967E5C;
    private static final int GRID_ALPHA = 40;
    private static final double GRID_FADE_FROM = 0.115;
    private static final double GRID_FADE_TO = 0.12;

    private MapPaper() {
    }

    public static void paper(GuiGraphicsExtractor graphics, MapView view) {
        int tile = CauldronArt.MAP_TILE;
        long firstX = (long) Math.floor(view.mapAtX(view.left()) / tile);
        long lastX = (long) Math.floor(view.mapAtX(view.right()) / tile);
        long firstY = (long) Math.floor(view.mapAtY(view.top()) / tile);
        long lastY = (long) Math.floor(view.mapAtY(view.bottom()) / tile);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) view.screenX(0.0), (float) view.screenY(0.0));
        pose.scale((float) view.zoom(), (float) view.zoom());
        for (long row = firstY; row <= lastY; row++) {
            for (long column = firstX; column <= lastX; column++) {
                CauldronArt.mapPaper(graphics, (int) (column * tile), (int) (row * tile));
            }
        }
        pose.popMatrix();
    }

    public static void grid(GuiGraphicsExtractor graphics, MapView view) {
        double zoom = view.zoom();
        double fade = Mth.clamp((zoom - GRID_FADE_FROM) / (GRID_FADE_TO - GRID_FADE_FROM), 0.0, 1.0);
        if (fade <= 0.0) return;
        double spacing = GRID_BASE;
        while (spacing * zoom < GRID_MIN_PIXELS) spacing *= 2.0;
        double minor = Mth.clamp((spacing * zoom - GRID_MIN_PIXELS) / GRID_MIN_PIXELS, 0.0, 1.0);
        int left = view.left();
        int top = view.top();
        int right = view.right();
        int bottom = view.bottom();
        int phaseX = (int) Math.round(view.screenX(0.0));
        int phaseY = (int) Math.round(view.screenY(0.0));

        for (long line = (long) Math.floor(view.mapAtX(left) / spacing); line <= (long) Math.ceil(view.mapAtX(right) / spacing); line++) {
            int x = (int) Math.round(view.screenX(line * spacing));
            int alpha = alpha(line, minor, fade);
            if (x < left || x >= right || alpha <= 0) continue;
            CauldronArt.dashDown(graphics, x, top, bottom, top - phaseY, ARGB.color(alpha, GRID_RGB));
        }
        for (long line = (long) Math.floor(view.mapAtY(top) / spacing); line <= (long) Math.ceil(view.mapAtY(bottom) / spacing); line++) {
            int y = (int) Math.round(view.screenY(line * spacing));
            int alpha = alpha(line, minor, fade);
            if (y < top || y >= bottom || alpha <= 0) continue;
            CauldronArt.dashAcross(graphics, left, right, y, left - phaseX, ARGB.color(alpha, GRID_RGB));
        }
    }

    private static int alpha(long line, double minor, double fade) {
        return (int) Math.round(GRID_ALPHA * fade * (Math.floorMod(line, 2L) == 0 ? 1.0 : minor));
    }
}
