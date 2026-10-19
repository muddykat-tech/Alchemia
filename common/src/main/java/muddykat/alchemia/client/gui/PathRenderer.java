package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

import java.util.Arrays;
import java.util.List;

public class PathRenderer {

    public static final double SUPERSAMPLE = 3.0;

    private static final double DASH_ON = 4.0;
    private static final double DASH_OFF = 3.0;
    private static final double DOT_ON = 1.0;
    private static final double DOT_ON_HOLLOW = 2.0;
    private static final double DOT_OFF = 3.0;
    private static final double DOT_CENTRE = DOT_ON_HOLLOW / 2.0;

    private static final int STROKE_TEXELS = 12;
    private static final int DASH_TEXELS = 24;
    private static final int DOT_TEXELS = 12;
    private static final int HOLLOW_DOT_TEXELS = 18;
    private static final double CLIP_MARGIN = 4.0;
    private static final double EPSILON = 1.0E-6;

    private static final double STEP = 1.0;
    private static final int AURA_LAYERS = 3;
    private static final double RING_DASH = 3.0;
    private static final double SPIRAL_TURNS = 1.75;

    public record Clip(double left, double top, double right, double bottom) {
    }

    private static final class Runs {
        private final GuiGraphicsExtractor graphics;

        private int left;
        private int top;
        private int right;
        private int bottom;
        private int colour;
        private boolean pending;

        private Runs(GuiGraphicsExtractor graphics) {
            this.graphics = graphics;
        }

        private void stamp(double x, double y, int thickness, int argb) {
            int size = Math.max(1, thickness);
            int half = size / 2;
            int x0 = (int) Math.round(x) - half;
            int y0 = (int) Math.round(y) - half;

            if (pending && argb == colour) {
                if (y0 == top && y0 + size == bottom && (x0 == right || x0 + size == left)) {
                    left = Math.min(left, x0);
                    right = Math.max(right, x0 + size);
                    return;
                }
                if (x0 == left && x0 + size == right && (y0 == bottom || y0 + size == top)) {
                    top = Math.min(top, y0);
                    bottom = Math.max(bottom, y0 + size);
                    return;
                }
            }

            flush();

            left = x0;
            top = y0;
            right = x0 + size;
            bottom = y0 + size;
            colour = argb;
            pending = true;
        }

        private void flush() {
            if (!pending) return;
            graphics.fill(left, top, right, bottom, colour);
            pending = false;
        }
    }

    private static void pushScaled(GuiGraphicsExtractor graphics) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale((float) (1.0 / SUPERSAMPLE), (float) (1.0 / SUPERSAMPLE));
    }

    private static void popScaled(GuiGraphicsExtractor graphics) {
        graphics.pose().popMatrix();
    }

    public static void dashes(GuiGraphicsExtractor graphics, List<PathPoint> points, int thickness, int argb, boolean teleport) {
        stroke(graphics, points, thickness, argb, teleport, false, 0.0, null, 1.0);
    }

    public static void dashes(GuiGraphicsExtractor graphics, List<PathPoint> points, int thickness, int argb,
                              boolean teleport, Clip clip) {
        stroke(graphics, points, thickness, argb, teleport, false, 0.0, clip, 1.0);
    }

    public static void dashes(GuiGraphicsExtractor graphics, List<PathPoint> points, int thickness, int argb,
                              boolean teleport, double phase, Clip clip) {
        stroke(graphics, points, thickness, argb, teleport, false, phase, clip, 1.0);
    }

    public static void dashes(GuiGraphicsExtractor graphics, List<PathPoint> points, int thickness, int argb,
                              boolean teleport, double phase, Clip clip, double scale) {
        stroke(graphics, points, thickness, argb, teleport, false, phase, clip, scale);
    }

    public static void outline(GuiGraphicsExtractor graphics, List<PathPoint> points, int thickness, int argb,
                               boolean teleport, double phase) {
        stroke(graphics, points, thickness, argb, teleport, true, phase, null, 1.0);
    }

    public static void outline(GuiGraphicsExtractor graphics, List<PathPoint> points, int thickness, int argb,
                               boolean teleport, double phase, Clip clip, double scale) {
        stroke(graphics, points, thickness, argb, teleport, true, phase, clip, scale);
    }

    private static double[] distances(List<PathPoint> points) {
        double[] along = new double[points.size()];
        for (int i = 1; i < points.size(); i++) along[i] = along[i - 1] + points.get(i).distanceTo(points.get(i - 1));
        return along;
    }

    private static PathPoint at(List<PathPoint> points, double[] along, double distance) {
        int last = along.length - 1;
        if (distance <= 0.0) return points.get(0);
        if (distance >= along[last]) return points.get(last);

        int found = Arrays.binarySearch(along, distance);
        if (found >= 0) return points.get(found);

        int after = -found - 1;
        int before = after - 1;
        double span = along[after] - along[before];
        if (span < EPSILON) return points.get(before);
        return points.get(before).lerp(points.get(after), (distance - along[before]) / span);
    }

    private static boolean outside(PathPoint point, Clip clip) {
        return clip != null && (point.x() < clip.left() - CLIP_MARGIN || point.x() > clip.right() + CLIP_MARGIN
                || point.y() < clip.top() - CLIP_MARGIN || point.y() > clip.bottom() + CLIP_MARGIN);
    }

    private static void stroke(GuiGraphicsExtractor graphics, List<PathPoint> points, int thickness, int argb,
                               boolean teleport, boolean hollow, double phase, Clip clip, double scale) {
        if (points.size() < 2 || scale <= 0.0) return;

        double[] along = distances(points);
        double total = along[along.length - 1];
        if (total < EPSILON) return;

        double on = (teleport ? DOT_ON_HOLLOW : DASH_ON) * scale;
        double period = (teleport ? DOT_ON + DOT_OFF : DASH_ON + DASH_OFF) * scale;
        double across = Math.max(1, thickness) * scale / STROKE_TEXELS;

        for (double start = -(Math.max(0.0, phase) % period); start < total; start += period) {
            if (teleport) {
                double centre = start + DOT_CENTRE * scale;
                if (centre < 0.0 || centre > total) continue;

                Identifier sprite = hollow ? AlchemiaSprites.PATH_DOT_HOLLOW : AlchemiaSprites.PATH_DOT;
                int texels = hollow ? HOLLOW_DOT_TEXELS : DOT_TEXELS;
                stamp(graphics, points, along, sprite, texels, centre - texels * across / 2.0, texels * across,
                        0, texels, across, argb, clip);
                continue;
            }

            double from = Math.max(0.0, start);
            double to = Math.min(total, start + on);
            if (to - from < EPSILON) continue;

            int first = (int) Math.round((from - start) / on * DASH_TEXELS);
            int last = (int) Math.round((to - start) / on * DASH_TEXELS);
            if (last <= first) continue;

            stamp(graphics, points, along, hollow ? AlchemiaSprites.PATH_DASH_HOLLOW : AlchemiaSprites.PATH_DASH,
                    DASH_TEXELS, from, to - from, first, last - first, across, argb, clip);
        }
    }

    private static void stamp(GuiGraphicsExtractor graphics, List<PathPoint> points, double[] along, Identifier sprite,
                              int texels, double from, double length, int firstTexel, int texelCount,
                              double across, int argb, Clip clip) {
        PathPoint head = at(points, along, from);
        PathPoint tail = at(points, along, from + length);
        if (outside(head, clip) && outside(tail, clip)) return;

        PathPoint run = tail.minus(head);
        if (run.isZero()) run = at(points, along, from + length + EPSILON * 10).minus(head);
        if (run.isZero()) return;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) head.x(), (float) head.y());
        pose.rotate((float) Math.atan2(run.y(), run.x()));
        pose.scale((float) (length / texelCount), (float) across);
        pose.translate(0.0F, -STROKE_TEXELS / 2.0F);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, texels, STROKE_TEXELS, firstTexel, 0,
                0, 0, texelCount, STROKE_TEXELS, argb);
        pose.popMatrix();
    }

    public static void dot(GuiGraphicsExtractor graphics, double x, double y, int radius, int argb) {
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        graphics.fill(px - radius, py - radius, px + radius, py + radius, argb);
    }

    public static void cross(GuiGraphicsExtractor graphics, double x, double y, double radius, int thickness, int argb) {
        double reach = radius * SUPERSAMPLE;
        int weight = Math.max(1, (int) Math.round(thickness * SUPERSAMPLE));
        int steps = Math.max(2, (int) Math.round(reach));

        Runs runs = new Runs(graphics);
        pushScaled(graphics);

        for (int step = -steps; step <= steps; step++) {
            double along = reach * step / steps;
            runs.stamp(x * SUPERSAMPLE + along, y * SUPERSAMPLE + along, weight, argb);
            runs.stamp(x * SUPERSAMPLE + along, y * SUPERSAMPLE - along, weight, argb);
        }

        runs.flush();
        popScaled(graphics);
    }

    public static void spiral(GuiGraphicsExtractor graphics, double x, double y, double radius, int thickness, int argb) {
        double reach = radius * SUPERSAMPLE;
        int weight = Math.max(1, (int) Math.round(thickness * SUPERSAMPLE));
        double sweep = SPIRAL_TURNS * 2 * Math.PI;
        int steps = Math.max(24, (int) Math.round(reach * 6));

        Runs runs = new Runs(graphics);
        pushScaled(graphics);

        for (int step = 0; step <= steps; step++) {
            double travelled = (double) step / steps;
            double angle = -Math.PI / 2 + sweep * travelled;
            double along = reach * travelled;
            runs.stamp(x * SUPERSAMPLE + Math.cos(angle) * along,
                    y * SUPERSAMPLE + Math.sin(angle) * along, weight, argb);
        }

        runs.flush();
        popScaled(graphics);
    }

    public static void ending(GuiGraphicsExtractor graphics, double x, double y, double radius, int thickness,
                              int argb, boolean teleport) {
        if (teleport) {
            spiral(graphics, x, y, radius, thickness, argb);
        } else {
            cross(graphics, x, y, radius, thickness, argb);
        }
    }

    public static void marker(GuiGraphicsExtractor graphics, double x, double y, int radius, int rgb) {
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        graphics.outline(px - radius, py - radius, radius * 2, radius * 2, ARGB.color(200, rgb));
        graphics.outline(px - radius - 1, py - radius - 1, radius * 2 + 2, radius * 2 + 2, ARGB.color(80, rgb));
    }

    public static void aura(GuiGraphicsExtractor graphics, double cx, double cy, double radius, int rgb, int alpha) {
        for (int layer = AURA_LAYERS; layer >= 1; layer--) {
            disc(graphics, cx, cy, radius * layer / AURA_LAYERS, ARGB.color(Math.max(1, alpha / AURA_LAYERS), rgb));
        }
    }

    public static void disc(GuiGraphicsExtractor graphics, double cx, double cy, double radius, int argb) {
        int reach = (int) Math.round(radius);
        if (reach < 1) return;

        int px = (int) Math.round(cx);
        int py = (int) Math.round(cy);

        for (int dy = -reach; dy <= reach; dy++) {
            int half = (int) Math.round(Math.sqrt((double) reach * reach - (double) dy * dy));
            if (half < 1) continue;
            graphics.fill(px - half, py + dy, px + half, py + dy + 1, argb);
        }
    }

    public static void line(GuiGraphicsExtractor graphics, double x0, double y0, double x1, double y1,
                            double thickness, int argb, boolean dotted) {
        double dx = (x1 - x0) * SUPERSAMPLE;
        double dy = (y1 - y0) * SUPERSAMPLE;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < EPSILON) return;

        int weight = Math.max(1, (int) Math.round(thickness * SUPERSAMPLE));
        int steps = Math.max(1, (int) Math.ceil(length));
        double gap = DOT_OFF * SUPERSAMPLE;

        Runs runs = new Runs(graphics);
        pushScaled(graphics);

        for (int step = 0; step <= steps; step++) {
            double travelled = length * step / steps;
            if (dotted && (travelled % (gap + weight)) >= weight) continue;
            runs.stamp(x0 * SUPERSAMPLE + dx * step / steps, y0 * SUPERSAMPLE + dy * step / steps, weight, argb);
        }

        runs.flush();
        popScaled(graphics);
    }

    public static void chevron(GuiGraphicsExtractor graphics, double x, double y, double angle, double size,
                               double thickness, int argb) {
        double back = angle + Math.PI;
        double spread = Math.PI / 4;
        double tipX = x + Math.cos(angle) * size / 2;
        double tipY = y + Math.sin(angle) * size / 2;

        line(graphics, tipX, tipY, tipX + Math.cos(back - spread) * size, tipY + Math.sin(back - spread) * size, thickness, argb, false);
        line(graphics, tipX, tipY, tipX + Math.cos(back + spread) * size, tipY + Math.sin(back + spread) * size, thickness, argb, false);
    }

    public static void ring(GuiGraphicsExtractor graphics, double cx, double cy, double radius, int argb, boolean dashed) {
        double circumference = 2 * Math.PI * radius;
        int steps = Math.max(8, (int) Math.round(circumference / STEP));

        Runs runs = new Runs(graphics);

        for (int step = 0; step < steps; step++) {
            if (dashed && ((int) (circumference * step / steps / RING_DASH)) % 2 == 1) continue;

            double angle = 2 * Math.PI * step / steps;
            runs.stamp(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, 1, argb);
        }

        runs.flush();
    }
}
