package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import muddykat.alchemia.common.effect.PortalTimeline;
import muddykat.alchemia.common.effect.ReturnPortalEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ReturnPortalRenderer extends EntityRenderer<ReturnPortalEntity, ReturnPortalRenderer.State> {

    public static boolean RIM_LINES = false;
    public static final int SPOKES = 13;
    public static final int RINGS = 9;
    public static final float EXTENT = 1.45F;
    public static final float PLANE_WIDTH = 1.2F;
    public static final float PLANE_HEIGHT = 1.55F;
    public static final float HOLE = 0.62F;
    public static final float CRACK_TICKS = 12.0F;
    public static final float BREAK_SPREAD = 9.0F;
    public static final float FALL_TICKS = 16.0F;
    public static final float RETURN_SPREAD = 9.0F;
    public static final float RETURN_TICKS = 5.0F;
    public static final float FLASH_TICKS = 5.0F;
    public static final float HEAL_FROM = 12.0F;
    public static final float JOSTLE_BAND = 0.2F;
    public static final float JOSTLE_TICKS = 4.0F;
    public static final float SNAP_TICKS = 3.0F;
    public static final float CREEP = 1.2F;
    public static final float STRAY_BAND = 0.5F;
    public static final float STRAY_CHANCE = 0.4F;
    public static final float STRAY_SHIFT = 0.75F;
    public static final float CREEP_TICKS = 40.0F;

    public static class State extends EntityRenderState {
        float age;
        float collapse;
        float yaw;
        int seed;
    }

    public ReturnPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected boolean affectedByCulling(ReturnPortalEntity entity) {
        return false;
    }

    @Override
    public void extractRenderState(ReturnPortalEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.collapse = entity.collapsing() ? entity.collapse() + partialTicks : 0.0F;
        state.yaw = entity.getYRot();
        state.seed = entity.getId();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!front(state, camera)) return;

        FracturePattern pattern = pattern(state.seed);
        RealityBreak.Plane plane = plane(state.yaw);
        Choreography choreography = new Choreography(state.age, state.collapse);
        if (RealityPass.supported()) {
            RealityPass.queue(new RealityPass.Request(pattern, plane, choreography, state.seed, new Vec3(state.x, state.y, state.z)));
        } else {
            RealityBreak.render(collector, poseStack, RealityBreak.frame(pattern, plane, choreography, state.seed, null), null);
        }
        super.submit(state, poseStack, collector, camera);
    }

    public static FracturePattern pattern(int seed) {
        return new FracturePattern(seed * 31L + 7L, SPOKES, RINGS, EXTENT, HOLE);
    }

    public static RealityBreak.Plane plane(float yaw) {
        double radians = Math.toRadians(yaw);
        Vector3f right = new Vector3f((float) Math.cos(radians), 0.0F, (float) Math.sin(radians));
        Vector3f toViewer = new Vector3f((float) Math.sin(radians), 0.0F, (float) -Math.cos(radians));
        return new RealityBreak.Plane(new Vector3f(0.0F, (float) PortalTimeline.HALF_HEIGHT, 0.0F), right, new Vector3f(0.0F, 1.0F, 0.0F),
                toViewer, PLANE_WIDTH, PLANE_HEIGHT);
    }

    private static boolean front(State state, CameraRenderState camera) {
        double radians = Math.toRadians(state.yaw);
        return (camera.pos.x - state.x) * -Math.sin(radians) + (camera.pos.z - state.z) * Math.cos(radians) < 0.0;
    }

    public record Choreography(float age, float collapse) implements RealityBreak.Choreography {

        private static boolean breaks(FracturePattern.Cell cell) {
            return cell.reach() + cell.wobble() * 0.1F < HOLE;
        }

        private static float breakAt(FracturePattern.Cell cell) {
            return PortalTimeline.TEAR_START + Mth.clamp(cell.reach() / HOLE, 0.0F, 1.0F) * BREAK_SPREAD + cell.wobble() * 2.0F;
        }

        private static boolean jostles(FracturePattern.Cell cell) {
            float reach = cell.reach() + cell.wobble() * 0.1F;
            if (reach < HOLE) return false;
            if (reach < HOLE + JOSTLE_BAND) return cell.wobble() > -0.3F;
            float far = (reach - HOLE - JOSTLE_BAND) / STRAY_BAND;
            return far < 1.0F && roll(cell) < STRAY_CHANCE * (1.0F - far);
        }

        private static boolean stray(FracturePattern.Cell cell) {
            return cell.reach() + cell.wobble() * 0.1F >= HOLE + JOSTLE_BAND;
        }

        private static float roll(FracturePattern.Cell cell) {
            float noise = (float) Math.sin(cell.cx() * 127.1F + cell.cy() * 311.7F) * 43758.547F;
            return noise - (float) Math.floor(noise);
        }

        private static float jostleAt(FracturePattern.Cell cell) {
            return PortalTimeline.TEAR_START + BREAK_SPREAD + (cell.reach() - HOLE) / JOSTLE_BAND * 3.0F + cell.wobble() * 2.0F;
        }

        private RealityBreak.CellState jostle(FracturePattern.Cell cell, int index) {
            float grow = (age - jostleAt(cell)) / JOSTLE_TICKS;
            if (grow <= 0.0F) return RealityBreak.CellState.REST;
            float settle = Mth.clamp(grow, 0.0F, 1.0F);
            float jolt = settle * settle * (3.0F - 2.0F * settle) * (1.0F + 0.35F * (float) Math.sin(settle * Math.PI));
            float creep = (age - jostleAt(cell)) / CREEP_TICKS;
            float drift = 1.0F + CREEP * creep / (1.0F + creep) + 0.22F * (float) Math.sin(age * 0.09F + index * 1.7F);
            float shift = jolt * drift * (stray(cell) ? STRAY_SHIFT : 1.0F);
            if (collapse <= 0.0F) return new RealityBreak.CellState(0.0F, true, 0.0F, shift);

            float snap = (collapse - 1.0F - (cell.wobble() + 0.5F) * 2.0F) / SNAP_TICKS;
            if (snap <= 0.0F) return new RealityBreak.CellState(0.0F, true, 0.0F, shift);
            if (snap < 1.0F) return new RealityBreak.CellState(0.0F, true, 0.0F, shift * (1.0F - snap * snap));
            float flash = 1.0F - (snap - 1.0F) * SNAP_TICKS / FLASH_TICKS;
            return new RealityBreak.CellState(0.0F, false, Mth.clamp(flash, 0.0F, 1.0F));
        }

        private static float returnAt(FracturePattern.Cell cell) {
            return Mth.clamp(1.0F - cell.reach() / HOLE, 0.0F, 1.0F) * RETURN_SPREAD + 1.0F + cell.wobble();
        }

        @Override
        public RealityBreak.CellState cell(FracturePattern.Cell cell, int index) {
            if (jostles(cell)) return jostle(cell, index);
            if (!breaks(cell)) return RealityBreak.CellState.REST;
            float since = age - breakAt(cell);
            if (since < 0.0F) return RealityBreak.CellState.REST;
            if (collapse <= 0.0F) return new RealityBreak.CellState(Math.min(1.0F, since / FALL_TICKS), true, 0.0F);

            float back = (collapse - returnAt(cell)) / RETURN_TICKS;
            if (back <= 0.0F) return new RealityBreak.CellState(1.0F, true, 0.0F);
            if (back < 1.0F) return new RealityBreak.CellState(1.0F - back * back, true, 0.0F);
            float flash = 1.0F - (collapse - returnAt(cell) - RETURN_TICKS) / FLASH_TICKS;
            return new RealityBreak.CellState(0.0F, false, Mth.clamp(flash, 0.0F, 1.0F));
        }

        @Override
        public float crackFront() {
            float grow = Mth.clamp(age / CRACK_TICKS, 0.0F, 1.0F);
            return EXTENT * 1.25F * grow * grow * (3.0F - 2.0F * grow);
        }

        @Override
        public float crackGlow() {
            if (collapse > 0.0F) {
                float heal = Mth.clamp((collapse - HEAL_FROM) / (PortalTimeline.COLLAPSE_TICKS - HEAL_FROM), 0.0F, 1.0F);
                return 0.6F * (1.0F - heal) * (1.0F - heal);
            }
            float pulse = 0.08F * (float) Math.sin(age * 0.2F);
            if (age < CRACK_TICKS) return 0.9F + 0.1F * (float) Math.sin(age * 2.3F);
            float settle = Mth.clamp((age - CRACK_TICKS) / 12.0F, 0.0F, 1.0F);
            return Mth.lerp(settle, 0.9F, 0.45F) + pulse;
        }

        @Override
        public boolean rimLines() {
            return RIM_LINES;
        }

        @Override
        public float rimGlow() {
            return 0.82F + 0.18F * (float) Math.sin(age * 0.3F);
        }
    }
}
