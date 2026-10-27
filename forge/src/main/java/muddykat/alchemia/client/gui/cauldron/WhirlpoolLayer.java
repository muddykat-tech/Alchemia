package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.PathRenderer;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.Whirlpool;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import muddykat.alchemia.client.effect.ARGB;
import net.minecraft.util.Mth;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class WhirlpoolLayer {

    private static final int INK = 0x17130F;
    private static final int FAINT = 0xFF6F6352;
    private static final int SHINE = 0xFFE4D6B3;
    private static final int LINK_ALPHA = 170;
    private static final double BASE_TURNS = 2.0;
    private static final int PAPER = 0xFFE4D6B3;
    private static final int FADE_CHUNKS = 12;
    private static final double FADE_START = 0.55;
    private static final double FADE_CORE = 0.15;
    private static final double FADE_MAX = 0.85;
    private static final double EXIT_TURNS = 1.75;

    public record Hover(Component name, Component detail) {
    }

    private WhirlpoolLayer() {
    }

    public static @Nullable Hover draw(GuiGraphics graphics, MapView view, PotionMap map, BrewBase base,
                                       PathPoint brewAt, Set<String> exits, double turn, double pathScale,
                                       Predicate<PathPoint> hidden, int mouseX, int mouseY) {
        Whirlpool held = map.whirlpoolAt(brewAt);
        PathRenderer.Clip clip = view.clip();
        double nearest = Double.MAX_VALUE;
        Hover hover = null;

        for (Whirlpool pool : map.whirlpools()) {
            if (!exits.contains(pool.mark(base))) continue;

            PathRenderer.dashes(graphics, view.toScreen(pool.link()), 1, ARGB.color(LINK_ALPHA, INK), true, 0.0, clip, pathScale);
            PathPoint exit = view.toScreen(pool.exit());
            double mark = Math.max(2.0, PotionMap.CELL * 0.3 * view.zoom());
            PathRenderer.polyline(graphics, spiral(view, pool.exit(), PotionMap.CELL * 0.45, -pool.spin(), 0.0, EXIT_TURNS),
                    line(view, false), FAINT);
            PathRenderer.disc(graphics, exit.x(), exit.y(), Math.max(1.0, mark * 0.3), ARGB.opaque(INK));

            double distance = Math.hypot(mouseX - exit.x(), mouseY - exit.y());
            if (!hidden.test(pool.exit()) && distance <= mark + 3 && distance < nearest) {
                nearest = distance;
                hover = new Hover(Component.translatable("alchemia.gui.whirl.exit"), Component.translatable("alchemia.gui.whirl.exit.hint"));
            }
        }

        for (Whirlpool pool : map.whirlpools()) {
            PathPoint centre = view.toScreen(pool.centre());
            double reach = pool.radius() * view.zoom();
            if (!view.overlaps(centre, reach)) continue;

            boolean caught = pool == held;
            double turns = BASE_TURNS + pool.radius() / PotionMap.CELL;
            List<PathPoint> groove = spiral(view, pool.centre(), pool.radius(), pool.spin(), pool.spin() * turn, turns);
            drawFadingSpiral(graphics, groove, line(view, caught), caught ? ARGB.opaque(INK) : FAINT);

            double distance = Math.hypot(mouseX - centre.x(), mouseY - centre.y());
            if (!hidden.test(pool.centre()) && distance <= reach && distance < nearest) {
                nearest = distance;
                hover = new Hover(Component.translatable("alchemia.gui.whirl"), Component.translatable(exits.contains(pool.mark(base))
                        ? "alchemia.gui.whirl.hint.known" : "alchemia.gui.whirl.hint"));
            }
        }
        return hover;
    }

    public static List<PathPoint> flight(PotionMap map, PathPoint from, PathPoint to) {
        for (Whirlpool pool : map.whirlpools()) {
            if (pool.exit().distanceTo(to) < 1.0 && pool.centre().distanceTo(from) <= pool.radius()) return pool.link();
        }
        return List.of();
    }

    private static void drawFadingSpiral(GuiGraphics graphics, List<PathPoint> groove, double weight, int ink) {
        int count = groove.size();
        for (int chunk = 0; chunk < FADE_CHUNKS; chunk++) {
            int from = chunk * (count - 1) / FADE_CHUNKS;
            int to = (chunk + 1) * (count - 1) / FADE_CHUNKS;
            if (to <= from) continue;

            double t = (chunk + 0.5) / FADE_CHUNKS;
            double outer = Mth.clamp((t - FADE_START) / (1.0 - FADE_START), 0.0, 1.0);
            double inner = Mth.clamp(1.0 - t / FADE_CORE, 0.0, 1.0);
            float fade = (float) Math.min(FADE_MAX, outer * outer * (3.0 - 2.0 * outer) + inner * 0.35);

            List<PathPoint> piece = groove.subList(from, to + 1);
            List<PathPoint> shine = new ArrayList<>(piece.size());
            for (PathPoint point : piece) shine.add(new PathPoint(point.x() + 1.0, point.y() + 1.0));
            PathRenderer.polyline(graphics, shine, weight, ARGB.opaque(ARGB.srgbLerp(fade, SHINE, PAPER)));
            PathRenderer.polyline(graphics, piece, weight, ARGB.opaque(ARGB.srgbLerp(fade, ink, PAPER)));
        }
    }

    private static double line(MapView view, boolean caught) {
        double base = Mth.clamp(view.zoom(), 0.6, 2.0);
        return caught ? base * 1.6 : base;
    }

    private static List<PathPoint> spiral(MapView view, PathPoint centre, double radius, int spin, double turn, double turns) {
        int samples = Mth.clamp((int) Math.round(radius * view.zoom() * turns * 0.9), 24, 220);
        double start = radius * 0.08;
        List<PathPoint> points = new ArrayList<>(samples + 1);
        for (int step = 0; step <= samples; step++) {
            double t = (double) step / samples;
            double r = start + (radius - start) * t;
            double angle = turn - spin * t * turns * 2.0 * Math.PI;
            points.add(view.toScreen(new PathPoint(centre.x() + Math.cos(angle) * r, centre.y() + Math.sin(angle) * r)));
        }
        return points;
    }
}
