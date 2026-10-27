package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import muddykat.alchemia.common.effect.PortalTimeline;
import muddykat.alchemia.common.effect.ReturnPortalEntity;
import muddykat.alchemia.common.effect.TeleportFieldEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ReturnPortalRenderer extends EntityRenderer<ReturnPortalEntity> {

    public static boolean RIM_LINES = false;
    public static final int SPOKES = 13;
    public static final int RINGS = 9;
    public static final float EXTENT = 1.45F;
    public static final float PLANE_WIDTH = 1.2F;
    public static final float PLANE_HEIGHT = 1.55F;
    public static final float HOLE = 0.62F;
    public static final float BREAK_SPREAD = 9.0F;
    public static final float FALL_TICKS = 16.0F;
    public static final float LANDED = 1.8F;
    public static final float FLARE = 1.8F;
    public static final float FADE_TICKS = 6.0F;
    public static final float JOSTLE_BAND = 0.2F;
    public static final float JOSTLE_TICKS = 4.0F;
    public static final float CREEP = 1.2F;
    public static final float STRAY_BAND = 0.5F;
    public static final float STRAY_CHANCE = 0.4F;
    public static final float STRAY_SHIFT = 0.75F;
    public static final float CREEP_TICKS = 40.0F;
    public static final float GLOW = 0.7F;
    public static final float LINE_BASE = 0.3F;
    public static final float LINE_PER_HIT = 0.17F;
    public static final Impacts IMPACTS = new Impacts(new float[]{0.5F, 0.95F, EXTENT * 1.25F}, new float[]{1.5F, 2.0F, 2.5F},
            new float[]{0.55F, 0.75F, 1.0F}, new float[]{0.55F, 0.85F, 0.85F}, new float[]{0.3F, 0.55F, 0.55F},
            new float[]{0.3F, 0.55F, 0.55F}, 1, 0.5F, 0.3F, 12.0F, 4.0F, 3.0F, true);
    private static final float GROUND_SHATTER_SIZE = TeleportFieldRenderer.SCAR_SIZE;
    private static final float GROUND_SHATTER_HOLD = TeleportFieldEntity.SCAR_SNAP - 10.0F;

    public static class State {
        double x;
        double y;
        double z;
        int lightCoords;
        float age;
        float collapse;
        float yaw;
        int seed;
    }

    public ReturnPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public State createRenderState() {
        return new State();
    }

    @Override
    public boolean shouldRender(ReturnPortalEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    private void extractRenderState(ReturnPortalEntity entity, State state, float partialTicks) {
        state.x = Mth.lerp(partialTicks, entity.xo, entity.getX());
        state.y = Mth.lerp(partialTicks, entity.yo, entity.getY());
        state.z = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        state.age = entity.tickCount + partialTicks;
        state.collapse = entity.collapsing() ? entity.collapse() + partialTicks : 0.0F;
        state.yaw = entity.getYRot();
        state.seed = entity.getId();
    }

    @Override
    public void render(ReturnPortalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        State state = createRenderState();
        extractRenderState(entity, state, partialTicks);
        state.lightCoords = packedLight;
        submit(state, poseStack, buffers, entityRenderDispatcher.camera);
    }

    @Override
    public ResourceLocation getTextureLocation(ReturnPortalEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private void submit(State state, PoseStack poseStack, MultiBufferSource collector, Camera camera) {
        boolean portalFacing = front(state, camera);
        boolean groundVisible = camera.getPosition().y >= state.y + TeleportFieldRenderer.LIFT;
        if (!portalFacing && !groundVisible) return;

        Vec3 origin = new Vec3(state.x, state.y, state.z);
        if (portalFacing) {
            RealityPass.queue(new RealityPass.Request(pattern(state.seed), plane(state.yaw),
                    new Choreography(state.age, state.collapse, state.seed), state.seed, origin));
        }
        if (groundVisible) {
            RealityPass.queue(new RealityPass.Request(groundPattern(state.seed), groundPlane(), groundChoreography(state),
                    state.seed, origin));
        }

    }

    private static FracturePattern groundPattern(int seed) {
        return new FracturePattern(seed * 13L + 3L, TeleportFieldRenderer.SCAR_SPOKES, TeleportFieldRenderer.SCAR_RINGS,
                TeleportFieldRenderer.SCAR_EXTENT, TeleportFieldRenderer.SCAR_LOOSE);
    }

    private static RealityBreak.Plane groundPlane() {
        return TeleportFieldRenderer.ground(GROUND_SHATTER_SIZE, GROUND_SHATTER_SIZE / PLANE_WIDTH * 0.6F);
    }

    private static TeleportFieldRenderer.Scar groundChoreography(State state) {
        float age = Math.min(state.age, GROUND_SHATTER_HOLD) + state.collapse;
        return new TeleportFieldRenderer.Scar(age);
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

    private static boolean front(State state, Camera camera) {
        double radians = Math.toRadians(state.yaw);
        return (camera.getPosition().x - state.x) * -Math.sin(radians) + (camera.getPosition().z - state.z) * Math.cos(radians) < 0.0;
    }

    public record Choreography(float age, float collapse, int seed) implements RealityBreak.Choreography, Impacts.Clock {

        private static boolean breaks(FracturePattern.Cell cell) {
            return cell.reach() + cell.wobble() * 0.1F < HOLE;
        }

        private static float breakAt(FracturePattern.Cell cell) {
            return PortalTimeline.TEAR_START + 1.5F + Mth.clamp(cell.reach() / HOLE, 0.0F, 1.0F) * BREAK_SPREAD + cell.wobble() * 2.0F;
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
            return roll(cell, 0.0F);
        }

        private static float roll(FracturePattern.Cell cell, float salt) {
            float noise = (float) Math.sin(cell.cx() * 127.1F + cell.cy() * 311.7F + salt) * 43758.547F;
            return noise - (float) Math.floor(noise);
        }

        private static float landing(float since) {
            return since < LANDED ? 0.0F : (float) Math.exp(-(since - LANDED) / 3.0F);
        }

        private static float jump(float since) {
            return since < 0.0F ? 0.0F : 0.7F * (float) Math.exp(-since / 2.5F);
        }

        private static float jostleAt(FracturePattern.Cell cell) {
            return PortalTimeline.TEAR_START + BREAK_SPREAD + (cell.reach() - HOLE) / JOSTLE_BAND * 3.0F + cell.wobble() * 2.0F;
        }

        private float opened() {
            return age - collapse;
        }

        @Override
        public float age(int level) {
            return age - PortalTimeline.HIT_TICKS[level];
        }

        @Override
        public boolean reached(int level) {
            return opened() >= PortalTimeline.HIT_TICKS[level];
        }

        @Override
        public float undone(int level) {
            if (collapse <= 0.0F || !reached(level)) return Impacts.NEVER;
            return collapse - PortalTimeline.unhitTick(level);
        }

        private RealityBreak.CellState jostle(FracturePattern.Cell cell, int index, float flash) {
            if (opened() < jostleAt(cell)) return new RealityBreak.CellState(0.0F, false, flash);
            float grow = (age - jostleAt(cell)) / JOSTLE_TICKS;
            float settle = Mth.clamp(grow, 0.0F, 1.0F);
            float jolt = settle * settle * (3.0F - 2.0F * settle) * (1.0F + 0.35F * (float) Math.sin(settle * Math.PI));
            float creep = (age - jostleAt(cell)) / CREEP_TICKS;
            float drift = 1.0F + CREEP * creep / (1.0F + creep) + 0.22F * (float) Math.sin(age * 0.09F + index * 1.7F);
            float shift = jolt * drift * (stray(cell) ? STRAY_SHIFT : 1.0F);
            if (collapse <= 0.0F) return new RealityBreak.CellState(0.0F, true, flash, shift);

            float since = collapse - (PortalTimeline.FRAME_FROM + roll(cell, 17.0F) * PortalTimeline.FRAME_TICKS);
            if (since < 0.0F) {
                float tremble = since > -2.0F ? 0.12F * (1.0F + since / 2.0F) * (float) Math.sin(since * 9.0F) : 0.0F;
                return new RealityBreak.CellState(0.0F, true, flash, shift * (1.0F + tremble));
            }
            float left = Impacts.stepped(since);
            if (left > 0.0F) return new RealityBreak.CellState(0.0F, true, Math.max(flash, jump(since)), shift * left);
            return new RealityBreak.CellState(0.0F, false, Math.max(flash, landing(since)));
        }

        private static float returnAt(FracturePattern.Cell cell) {
            return PortalTimeline.HOLE_FROM + Mth.clamp(1.0F - cell.reach() / HOLE, 0.0F, 1.0F) * PortalTimeline.HOLE_TICKS
                    + (roll(cell, 5.0F) - 0.5F) * 1.5F;
        }

        private static int region(float reach) {
            for (int level = 0; level < PortalTimeline.SHATTER; level++) {
                if (reach < IMPACTS.front()[level]) return level;
            }
            return PortalTimeline.SHATTER;
        }

        @Override
        public RealityBreak.CellState cell(FracturePattern.Cell cell, int index) {
            float flash = IMPACTS.flash(this, cell);
            if (jostles(cell)) return jostle(cell, index, flash);
            if (!breaks(cell) || opened() < breakAt(cell)) return IMPACTS.cracked(this, cell, seed, index, flash);
            boolean chip = IMPACTS.chip(cell, seed, index) && reached(IMPACTS.chipLevel());
            if (collapse <= 0.0F) {
                if (chip) return IMPACTS.cracked(this, cell, seed, index, flash);
                return new RealityBreak.CellState(Math.min(1.0F, (age - breakAt(cell)) / FALL_TICKS), true, 0.0F);
            }
            if (chip) {
                if (undone(IMPACTS.chipLevel()) < 0.0F) return new RealityBreak.CellState(1.0F, true, 0.0F);
                return IMPACTS.cracked(this, cell, seed, index, flash);
            }

            float since = collapse - returnAt(cell);
            if (since < 0.0F) return new RealityBreak.CellState(1.0F, true, 0.0F);
            float left = Impacts.stepped(since);
            if (left > 0.0F) return new RealityBreak.CellState(Math.min(1.0F, left * 1.4F), true, 0.0F);
            return IMPACTS.cracked(this, cell, seed, index, Math.max(flash, landing(since)));
        }

        @Override
        public float crackFront() {
            return collapse > 0.0F ? IMPACTS.front()[PortalTimeline.SHATTER] : IMPACTS.crackFront(this);
        }

        @Override
        public float crackGlow() {
            if (collapse > 0.0F) return (0.5F + 0.08F * (float) Math.sin(age * 0.35F)) * GLOW;
            return IMPACTS.crackGlow(this, age) * GLOW;
        }

        @Override
        public float crackLight(float reach) {
            float undone = undone(region(reach));
            if (undone < 0.0F) return 1.0F;
            float flare = 1.0F + FLARE * (float) Math.exp(-undone / 1.5F) * Math.min(1.0F, undone / 0.4F);
            float fade = Mth.clamp((undone - 1.0F) / FADE_TICKS, 0.0F, 1.0F);
            float flicker = (float) Math.sin(undone * 7.3F + reach * 41.0F) > 0.0F ? 1.0F : 0.7F;
            return flare * (1.0F - fade * fade * (3.0F - 2.0F * fade)) * (fade > 0.0F ? flicker : 1.0F);
        }

        @Override
        public boolean rimLines() {
            return RIM_LINES;
        }

        @Override
        public float lineWidth() {
            return LINE_BASE + LINE_PER_HIT * (collapse > 0.0F ? PortalTimeline.HIT_TICKS.length : IMPACTS.lit(this));
        }

        @Override
        public float rimGlow() {
            return 0.82F + 0.18F * (float) Math.sin(age * 0.3F);
        }
    }
}
