package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class RealityBreak {

    public static final int SEAM = 0xF4EEFF;
    public static final int POWDER = 0xA8CFF0;
    public static final int LAVENDER = 0x9A78E0;
    public static final int VOID = 0x0B0618;

    private static final int GLASS_DARK = 0x221638;
    private static final int GLASS_LIGHT = 0x4F3D7A;
    private static final float VOID_OFFSET = 0.0F;
    private static final float SHARD_OFFSET = 0.004F;
    private static final float LINE_OFFSET = 0.008F;
    private static final float CRACK_CORE = 0.011F;
    private static final float CRACK_HALO = 0.05F;
    private static final float RIM_CORE = 0.018F;
    private static final float RIM_HALO = 0.16F;
    private static final float SHARD_EDGE = 0.008F;
    private static final float CRACK_TAIL = 0.16F;
    private static final float CRACK_SHADOW = 0.026F;
    private static final int SHADOW = 0x120A20;
    private static final int SEGMENTS = 3;
    private static final float JAG = 0.06F;
    private static final float DISPLACE = 0.035F;
    private static final float TWIST = 0.1F;
    private static final float LIFT = 0.012F;
    private static final float SLAB = 0.025F;
    private static final float ROCK = 0.11F;
    private static final float HOVER_FULL = 0.3F;
    private static final float HOVER_SPIN = 0.0015F;
    private static final float HOVER_DRIFT = 0.015F;

    public record Plane(Vector3f centre, Vector3f right, Vector3f up, Vector3f toViewer, float width, float height, float scale) {

        public Plane(Vector3f centre, Vector3f right, Vector3f up, Vector3f toViewer, float width, float height) {
            this(centre, right, up, toViewer, width, height, 1.0F);
        }

        public Vector3f at(float x, float y) {
            return new Vector3f(right).mul(x * width).add(new Vector3f(up).mul(y * height)).add(centre);
        }
    }

    public record CellState(float flight, boolean hole, float flash, float shift, float hover, float sway) {

        public static final CellState REST = new CellState(0.0F, false, 0.0F, 0.0F);

        public CellState(float flight, boolean hole, float flash) {
            this(flight, hole, flash, 0.0F);
        }

        public CellState(float flight, boolean hole, float flash, float shift) {
            this(flight, hole, flash, shift, 0.0F, 0.0F);
        }

        public static CellState hovering(float height, float sway) {
            return new CellState(0.0F, true, 0.0F, 0.0F, height, sway);
        }

        public boolean open() {
            return hole && shift <= 0.0F;
        }
    }

    public interface Choreography {

        CellState cell(FracturePattern.Cell cell, int index);

        float crackFront();

        float crackGlow();

        float rimGlow();

        default boolean rimLines() {
            return true;
        }
    }

    public interface Mapping {

        float @Nullable [] uv(Vector3f rest);
    }

    private record Motion(float spin, float tilt, float axis, float kick) {
    }

    public static final class Frame {

        private final FracturePattern pattern;
        private final Plane plane;
        private final Choreography choreography;
        private final CellState[] states;
        private final int seed;
        private final @Nullable Mapping mapping;
        private final boolean holes;
        private final boolean flights;

        private Frame(FracturePattern pattern, Plane plane, Choreography choreography, int seed, @Nullable Mapping mapping) {
            this.pattern = pattern;
            this.plane = plane;
            this.choreography = choreography;
            this.seed = seed;
            this.mapping = mapping;
            List<FracturePattern.Cell> cells = pattern.cells();
            states = new CellState[cells.size()];
            boolean anyHole = false;
            boolean anyFlight = false;
            for (int index = 0; index < cells.size(); index++) {
                CellState state = choreography.cell(cells.get(index), index);
                if (state.shift() > 0.0F && uvs(cells.get(index).vertices()) == null) state = new CellState(0.0F, false, state.flash());
                states[index] = state;
                anyHole |= state.hole();
                anyFlight |= moving(state);
            }
            holes = anyHole;
            flights = anyFlight;
        }

        public boolean holes() {
            return holes;
        }

        public boolean flights() {
            return flights;
        }

        public boolean textured() {
            return mapping != null;
        }

        public void voids(PoseStack.Pose pose, VertexConsumer buffer, boolean shaded) {
            Vector3f offset = new Vector3f(plane.toViewer()).mul(VOID_OFFSET * plane.scale());
            List<FracturePattern.Cell> cells = pattern.cells();
            boolean[] rim = new boolean[pattern.vertexCount()];
            for (int index = 0; index < cells.size(); index++) {
                if (states[index].hole()) continue;
                for (int corner : cells.get(index).vertices()) rim[corner] = true;
            }
            for (int index = 0; index < cells.size(); index++) {
                if (!states[index].hole()) continue;
                int[] corners = cells.get(index).vertices();
                for (int slot = 0; slot < 4; slot++) {
                    int corner = corners[Math.min(slot, corners.length - 1)];
                    Vector3f at = plane.at(pattern.x(corner), pattern.y(corner)).add(offset);
                    if (shaded) {
                        int colour = ARGB.color(255, rim[corner] ? 255 : 0, 255, seed & 0xFF);
                        buffer.addVertex(pose, at.x, at.y, at.z).setUv(pattern.x(corner), pattern.y(corner))
                                .setLight(LightCoordsUtil.FULL_BRIGHT).setColor(colour);
                    } else {
                        buffer.addVertex(pose, at.x, at.y, at.z).setColor(ARGB.opaque(VOID));
                    }
                }
            }
        }

        public void shards(PoseStack.Pose pose, VertexConsumer buffer, boolean textured) {
            Vector3f offset = new Vector3f(plane.toViewer()).mul(SHARD_OFFSET * plane.scale());
            List<FracturePattern.Cell> cells = pattern.cells();
            for (int index = 0; index < cells.size(); index++) {
                if (!moving(states[index])) continue;
                FracturePattern.Cell cell = cells.get(index);
                int[] corners = cell.vertices();
                float[][] uvs = uvs(corners);
                if (textured != (uvs != null)) continue;

                Motion motion = motion(seed, index);
                float flight = states[index].flight();
                float alpha = 1.0F - smoothstep(0.72F, 1.0F, flight);
                Vector3f[] points = place(cell, states[index], motion, offset);
                float depth = flight > 0.0F ? flight * flight * (3.0F - 2.0F * flight) : 0.0F;
                float light = flight > 0.0F ? 1.0F - 0.82F * depth : 0.92F;
                if (textured) {
                    polygon(pose, buffer, points, uvs, ARGB.colorFromFloat(alpha, light, light * 0.97F, light));
                } else {
                    int shade = ARGB.srgbLerp(0.5F + 0.5F * cell.wobble(), ARGB.opaque(GLASS_DARK), ARGB.opaque(GLASS_LIGHT));
                    polygon(pose, buffer, points, null, ARGB.color((int) (alpha * 235.0F), ARGB.scaleRGB(shade, light)));
                    outline(pose, buffer, points, SHARD_EDGE, ARGB.color((int) (alpha * (1.0F - depth) * 220.0F), LAVENDER));
                }
            }
        }

        public void lines(PoseStack.Pose pose, VertexConsumer buffer) {
            if (flights) shardEdges(pose, buffer);
            Vector3f offset = new Vector3f(plane.toViewer()).mul(LINE_OFFSET * plane.scale());
            float front = choreography.crackFront();
            float crackGlow = choreography.crackGlow();
            float rimGlow = choreography.rimGlow();
            boolean rimLines = choreography.rimLines();
            for (FracturePattern.Edge edge : pattern.edges()) {
                if (edge.reach() > front || edge.second() < 0) continue;
                boolean firstHole = states[edge.first()].open();
                boolean secondHole = states[edge.second()].open();
                if (firstHole && secondHole) continue;
                float flash = Math.max(states[edge.first()].flash(), states[edge.second()].flash());
                Vector3f from = plane.at(pattern.x(edge.from()), pattern.y(edge.from())).add(offset);
                Vector3f to = plane.at(pattern.x(edge.to()), pattern.y(edge.to())).add(offset);

                if (firstHole || secondHole) {
                    if (!rimLines) continue;
                    float glow = Math.min(1.0F, rimGlow + flash);
                    line(pose, buffer, from, to, RIM_HALO, ARGB.color((int) (glow * 120.0F), POWDER), ARGB.color(0, POWDER));
                    line(pose, buffer, from, to, RIM_CORE, ARGB.color((int) (glow * 245.0F), SEAM), ARGB.color((int) (glow * 245.0F), SEAM));
                    continue;
                }
                if (states[edge.first()].shift() > 0.0F || states[edge.second()].shift() > 0.0F) continue;
                crack(pose, buffer, edge, offset, front, crackGlow, flash);
            }
        }

        private void crack(PoseStack.Pose pose, VertexConsumer buffer, FracturePattern.Edge edge, Vector3f offset, float front, float crackGlow,
                           float flash) {
            boolean outward = pattern.radius(edge.from()) <= pattern.radius(edge.to());
            int near = outward ? edge.from() : edge.to();
            int far = outward ? edge.to() : edge.from();
            float nearReach = pattern.radius(near);
            float farReach = pattern.radius(far);
            float grown = farReach <= front ? 1.0F : Mth.clamp((front - nearReach) / Math.max(1.0E-4F, farReach - nearReach), 0.0F, 1.0F);
            if (grown <= 0.0F) return;

            float dx = pattern.x(far) - pattern.x(near);
            float dy = pattern.y(far) - pattern.y(near);
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length < 1.0E-5F) return;
            float perpX = -dy / length;
            float perpY = dx / length;
            long hash = edge.from() * 73856093L ^ edge.to() * 19349663L ^ seed * 83492791L;
            float[] xs = new float[SEGMENTS + 1];
            float[] ys = new float[SEGMENTS + 1];
            for (int point = 0; point <= SEGMENTS; point++) {
                float along = grown * point / SEGMENTS;
                boolean pinned = point == 0 || point == SEGMENTS && grown >= 1.0F;
                float jag = pinned ? 0.0F : (hashed(hash, point) - 0.5F) * JAG * length;
                xs[point] = pattern.x(near) + dx * along + perpX * jag;
                ys[point] = pattern.y(near) + dy * along + perpY * jag;
            }

            for (int segment = 0; segment < SEGMENTS; segment++) {
                float startReach = (float) Math.sqrt(xs[segment] * xs[segment] + ys[segment] * ys[segment]);
                float endReach = (float) Math.sqrt(xs[segment + 1] * xs[segment + 1] + ys[segment + 1] * ys[segment + 1]);
                float startGlow = glow(edge.fade(), startReach, crackGlow, flash);
                float endGlow = glow(edge.fade(), endReach, crackGlow, flash);
                if (segment == SEGMENTS - 1 && grown < 1.0F) endGlow *= 0.3F;
                if (startGlow <= 0.01F && endGlow <= 0.01F) continue;
                Vector3f a = plane.at(xs[segment], ys[segment]).add(offset);
                Vector3f b = plane.at(xs[segment + 1], ys[segment + 1]).add(offset);
                float taper = 1.0F - 0.5F * Math.min(1.0F, (startReach + endReach) * 0.5F);
                shade(pose, buffer, a, b, CRACK_SHADOW * taper, ARGB.color((int) (startGlow * 90.0F), SHADOW), ARGB.color((int) (endGlow * 90.0F), SHADOW));
                line(pose, buffer, a, b, CRACK_HALO * taper, ARGB.color((int) (startGlow * 70.0F), POWDER), ARGB.color((int) (endGlow * 70.0F), POWDER),
                        ARGB.color(0, POWDER));
                line(pose, buffer, a, b, CRACK_CORE * taper, ARGB.color((int) (startGlow * 240.0F), SEAM), ARGB.color((int) (endGlow * 240.0F), SEAM),
                        ARGB.color((int) ((startGlow + endGlow) * 50.0F), SEAM));
            }
        }

        private static float glow(float fade, float reach, float crackGlow, float flash) {
            float tail = 1.0F - smoothstep(fade - CRACK_TAIL, fade, reach);
            float distance = 1.0F - 0.35F * Math.min(reach, 1.6F) / 1.6F;
            return Math.min(1.0F, (crackGlow + flash) * tail * distance + flash * 0.3F * tail);
        }

        private void shade(PoseStack.Pose pose, VertexConsumer buffer, Vector3f from, Vector3f to, float width, int start, int end) {
            Vector3f side = new Vector3f(to).sub(from).cross(plane.toViewer());
            if (side.lengthSquared() < 1.0E-10F) return;
            side.normalize().mul(width * plane.scale());
            quad(pose, buffer, from, to, new Vector3f(to).sub(side), new Vector3f(from).sub(side), start, end, ARGB.color(0, end), ARGB.color(0, start));
        }

        private void shardEdges(PoseStack.Pose pose, VertexConsumer buffer) {
            Vector3f offset = new Vector3f(plane.toViewer()).mul((SHARD_OFFSET + 0.001F) * plane.scale());
            List<FracturePattern.Cell> cells = pattern.cells();
            for (int index = 0; index < cells.size(); index++) {
                CellState state = states[index];
                if (!moving(state)) continue;
                FracturePattern.Cell cell = cells.get(index);
                if (uvs(cell.vertices()) == null) continue;
                Vector3f[] points = place(cell, state, motion(seed, index), offset);
                float flight = state.flight();
                float depth = flight * flight * (3.0F - 2.0F * flight);
                float alpha = flight > 0.0F ? (1.0F - smoothstep(0.72F, 1.0F, flight)) * (1.0F - depth)
                        : 0.4F * Math.min(1.0F, state.shift() + state.hover() * 2.0F);
                outline(pose, buffer, points, SHARD_EDGE, ARGB.color((int) (alpha * 200.0F), SEAM));
            }
        }

        private static boolean moving(CellState state) {
            return state.shift() > 0.0F || state.hover() > 0.0F || state.flight() > 0.0F && state.flight() < 1.0F;
        }

        private Vector3f[] place(FracturePattern.Cell cell, CellState state, Motion motion, Vector3f offset) {
            int[] corners = cell.vertices();
            Vector3f[] points = new Vector3f[corners.length];
            Vector3f lift = new Vector3f(plane.toViewer()).mul(LIFT * plane.scale() * motion.tilt() * Math.min(1.0F, state.shift()) + state.hover());
            float[][] moved = new float[corners.length][];
            float lowest = 0.0F;
            for (int corner = 0; corner < corners.length; corner++) {
                if (state.hover() > 0.0F) {
                    moved[corner] = hover(pattern, cell, corners[corner], state, motion);
                } else {
                    moved[corner] = state.flight() > 0.0F
                            ? transit(pattern, cell, corners[corner], state.flight(), motion)
                            : displace(pattern, cell, corners[corner], state.shift(), motion);
                }
                lowest = Math.min(lowest, moved[corner][2]);
            }
            float units = (plane.width() + plane.height()) * 0.5F;
            for (int corner = 0; corner < corners.length; corner++) {
                Vector3f rise = new Vector3f(plane.toViewer()).mul((moved[corner][2] - lowest) * units);
                points[corner] = plane.at(moved[corner][0], moved[corner][1]).add(offset).add(lift).add(rise);
            }
            return points;
        }

        public void walls(PoseStack.Pose pose, VertexConsumer buffer) {
            Vector3f offset = new Vector3f(plane.toViewer()).mul(SHARD_OFFSET * plane.scale());
            Vector3f light = new Vector3f(plane.up()).mul(0.85F).sub(new Vector3f(plane.right()).mul(0.35F)).normalize();
            List<FracturePattern.Cell> cells = pattern.cells();
            for (int index = 0; index < cells.size(); index++) {
                CellState state = states[index];
                if (!moving(state)) continue;
                FracturePattern.Cell cell = cells.get(index);
                Vector3f[] front = place(cell, state, motion(seed, index), offset);
                float flight = state.flight();
                float depth = flight * flight * (3.0F - 2.0F * flight);
                float alpha = flight > 0.0F ? 1.0F - smoothstep(0.72F, 1.0F, flight) : 1.0F;
                float thickness = SLAB * plane.scale() * (flight > 0.0F ? 1.0F - 0.88F * depth : 1.0F);
                Vector3f back = new Vector3f(plane.toViewer()).mul(-thickness);
                Vector3f centre = new Vector3f();
                for (Vector3f point : front) centre.add(point);
                centre.div(front.length);
                int tint = ARGB.srgbLerp(0.5F + 0.5F * cell.wobble(), ARGB.opaque(LAVENDER), ARGB.opaque(POWDER));
                for (int corner = 0; corner < front.length; corner++) {
                    Vector3f a = front[corner];
                    Vector3f b = front[(corner + 1) % front.length];
                    Vector3f out = new Vector3f(a).add(b).mul(0.5F).sub(centre);
                    if (out.lengthSquared() < 1.0E-10F) continue;
                    float shade = 0.45F + 0.55F * Math.max(0.0F, out.normalize().dot(light));
                    int edge = ARGB.color((int) (alpha * 205.0F), ARGB.scaleRGB(ARGB.srgbLerp(0.65F, tint, ARGB.opaque(SEAM)), shade));
                    int deep = ARGB.color((int) (alpha * 170.0F), ARGB.scaleRGB(ARGB.srgbLerp(0.45F, tint, ARGB.opaque(VOID)), shade));
                    quad(pose, buffer, a, b, new Vector3f(b).add(back), new Vector3f(a).add(back), edge, edge, deep, deep);
                }
            }
        }

        private float @Nullable [][] uvs(int[] corners) {
            if (mapping == null) return null;
            float[][] uvs = new float[corners.length][];
            for (int corner = 0; corner < corners.length; corner++) {
                uvs[corner] = mapping.uv(plane.at(pattern.x(corners[corner]), pattern.y(corners[corner])));
                if (uvs[corner] == null) return null;
            }
            return uvs;
        }

        private void outline(PoseStack.Pose pose, VertexConsumer buffer, Vector3f[] points, float width, int colour) {
            for (int corner = 0; corner < points.length; corner++) {
                line(pose, buffer, points[corner], points[(corner + 1) % points.length], width, colour, colour);
            }
        }

        private void line(PoseStack.Pose pose, VertexConsumer buffer, Vector3f from, Vector3f to, float width, int inner, int outer) {
            line(pose, buffer, from, to, width, inner, inner, outer);
        }

        private void line(PoseStack.Pose pose, VertexConsumer buffer, Vector3f from, Vector3f to, float width, int start, int end, int outer) {
            Vector3f side = new Vector3f(to).sub(from).cross(plane.toViewer());
            if (side.lengthSquared() < 1.0E-10F) return;
            side.normalize().mul(width * plane.scale());
            int outerStart = ARGB.color(Math.min(ARGB.alpha(outer), ARGB.alpha(start)), outer);
            int outerEnd = ARGB.color(Math.min(ARGB.alpha(outer), ARGB.alpha(end)), outer);
            quad(pose, buffer, from, to, new Vector3f(to).add(side), new Vector3f(from).add(side), start, end, outerEnd, outerStart);
            quad(pose, buffer, from, to, new Vector3f(to).sub(side), new Vector3f(from).sub(side), start, end, outerEnd, outerStart);
        }
    }

    private RealityBreak() {
    }

    public static Frame frame(FracturePattern pattern, Plane plane, Choreography choreography, int seed, @Nullable Mapping mapping) {
        return new Frame(pattern, plane, choreography, seed, mapping);
    }

    public static void render(SubmitNodeCollector collector, PoseStack poseStack, Frame frame, @Nullable Identifier texture) {
        RenderType portal = PortalShader.interior();
        if (frame.holes() && portal != null) {
            collector.submitCustomGeometry(poseStack, portal, (pose, buffer) -> frame.voids(pose, buffer, true));
        }
        if (frame.flights() && frame.textured() && texture != null) {
            collector.submitCustomGeometry(poseStack, RenderTypes.text(texture), (pose, buffer) -> frame.shards(pose, buffer, true));
        }
        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
            if (frame.holes() && portal == null) frame.voids(pose, buffer, false);
            if (frame.flights()) frame.walls(pose, buffer);
            if (frame.flights()) frame.shards(pose, buffer, false);
            frame.lines(pose, buffer);
        });
    }

    private static float hashed(long hash, int salt) {
        long mixed = hash * 6364136223846793005L + salt * 1442695040888963407L;
        mixed ^= mixed >>> 33;
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= mixed >>> 33;
        return (mixed >>> 40) / (float) (1L << 24);
    }

    private static Motion motion(int seed, int index) {
        RandomSource random = RandomSource.create(seed * 92821L + index * 68917L);
        float spin = (random.nextBoolean() ? 1.0F : -1.0F) * (0.7F + 1.6F * random.nextFloat());
        float tilt = 0.6F + 0.9F * random.nextFloat();
        float axis = random.nextFloat() * Mth.PI;
        float kick = 0.03F + 0.05F * random.nextFloat();
        return new Motion(spin, tilt, axis, kick);
    }

    private static float[] transit(FracturePattern pattern, FracturePattern.Cell cell, int corner, float flight, Motion motion) {
        float depth = flight * flight * (3.0F - 2.0F * flight);
        float scale = 1.0F - 0.88F * depth;
        float pull = 0.82F * depth;
        float reach = (float) Math.sqrt(cell.cx() * cell.cx() + cell.cy() * cell.cy());
        float pop = motion.kick() * (float) Math.sin(Math.min(1.0F, flight * 3.0F) * Math.PI);
        float outX = reach < 1.0E-4F ? 0.0F : cell.cx() / reach;
        float outY = reach < 1.0E-4F ? 0.0F : cell.cy() / reach;
        float centreX = cell.cx() * (1.0F - pull) + outX * pop;
        float centreY = cell.cy() * (1.0F - pull) + outY * pop;

        float dx = pattern.x(corner) - cell.cx();
        float dy = pattern.y(corner) - cell.cy();
        float axisX = (float) Math.cos(motion.axis());
        float axisY = (float) Math.sin(motion.axis());
        float along = dx * axisX + dy * axisY;
        float across = -dx * axisY + dy * axisX;
        float tumble = depth * motion.tilt() * 1.4F;
        float rise = along * (float) Math.sin(tumble) * scale;
        along *= (float) Math.cos(tumble);
        float squashedX = along * axisX - across * axisY;
        float squashedY = along * axisY + across * axisX;
        float angle = depth * motion.spin();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new float[]{centreX + (squashedX * cos - squashedY * sin) * scale, centreY + (squashedX * sin + squashedY * cos) * scale, rise};
    }

    private static float[] hover(FracturePattern pattern, FracturePattern.Cell cell, int corner, CellState state, Motion motion) {
        float time = state.sway();
        float lifted = Math.min(1.0F, state.hover() / HOVER_FULL);
        float angle = lifted * (motion.spin() * HOVER_SPIN * time + 0.12F * (float) Math.sin(time * 0.018F + motion.axis() * 2.0F));
        float tip = lifted * motion.tilt() * 0.14F * (float) Math.sin(time * 0.022F + motion.axis() * 3.0F);
        float centreX = cell.cx() + lifted * HOVER_DRIFT * (float) Math.sin(time * 0.016F + motion.axis());
        float centreY = cell.cy() + lifted * HOVER_DRIFT * (float) Math.cos(time * 0.013F + motion.axis() * 1.7F);
        float scale = 1.0F - 0.1F * lifted;
        float dx = pattern.x(corner) - cell.cx();
        float dy = pattern.y(corner) - cell.cy();
        float rise = (dx * (float) Math.cos(motion.axis()) + dy * (float) Math.sin(motion.axis())) * (float) Math.sin(tip);
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new float[]{centreX + (dx * cos - dy * sin) * scale, centreY + (dx * sin + dy * cos) * scale, rise};
    }

    private static float[] displace(FracturePattern pattern, FracturePattern.Cell cell, int corner, float shift, Motion motion) {
        float reach = (float) Math.sqrt(cell.cx() * cell.cx() + cell.cy() * cell.cy());
        float outX = reach < 1.0E-4F ? 0.0F : cell.cx() / reach;
        float outY = reach < 1.0E-4F ? 0.0F : cell.cy() / reach;
        float swing = (motion.axis() - Mth.HALF_PI) * 0.45F;
        float dirX = outX * (float) Math.cos(swing) - outY * (float) Math.sin(swing);
        float dirY = outX * (float) Math.sin(swing) + outY * (float) Math.cos(swing);
        float push = DISPLACE * shift * (0.6F + motion.kick() * 8.0F);
        float angle = Math.signum(motion.spin()) * TWIST * Math.min(shift, 1.3F) * (0.5F + motion.tilt() / 3.0F);
        float scale = 1.0F - 0.04F * Math.min(shift, 1.3F);
        float dx = pattern.x(corner) - cell.cx();
        float dy = pattern.y(corner) - cell.cy();
        float rock = Math.signum(motion.spin()) * ROCK * Math.min(shift, 1.3F) * motion.tilt() / 1.5F;
        float rise = (dx * (float) Math.cos(motion.axis()) + dy * (float) Math.sin(motion.axis())) * (float) Math.sin(rock);
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new float[]{cell.cx() + dirX * push + (dx * cos - dy * sin) * scale, cell.cy() + dirY * push + (dx * sin + dy * cos) * scale, rise};
    }

    private static void polygon(PoseStack.Pose pose, VertexConsumer buffer, Vector3f[] points, float @Nullable [][] uvs, int colour) {
        int[] order = points.length == 3 ? new int[]{0, 1, 2, 2} : new int[]{0, 1, 2, 3};
        for (int pass = 0; pass < 2; pass++) {
            for (int slot = 0; slot < 4; slot++) {
                int corner = pass == 0 ? order[slot] : order[3 - slot];
                Vector3f at = points[corner];
                VertexConsumer vertex = buffer.addVertex(pose, at.x, at.y, at.z);
                if (uvs != null) {
                    vertex.setUv(uvs[corner][0], uvs[corner][1]).setLight(LightCoordsUtil.FULL_BRIGHT).setColor(colour);
                } else {
                    vertex.setColor(colour);
                }
            }
        }
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, Vector3f a, Vector3f b, Vector3f c, Vector3f d, int ca, int cb, int cc,
                             int cd) {
        buffer.addVertex(pose, a.x, a.y, a.z).setColor(ca);
        buffer.addVertex(pose, b.x, b.y, b.z).setColor(cb);
        buffer.addVertex(pose, c.x, c.y, c.z).setColor(cc);
        buffer.addVertex(pose, d.x, d.y, d.z).setColor(cd);
    }

    public static float smoothstep(float from, float to, float value) {
        float t = Mth.clamp((value - from) / (to - from), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
