package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import muddykat.alchemia.common.effect.RiftEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class RiftRenderer extends EntityRenderer<RiftEntity> {

    public static final int SPOKES = 12;
    public static final int RINGS = 8;
    public static final float EXTENT = 1.3F;
    public static final float WIDTH = 0.95F;
    public static final float HEIGHT = 1.35F;
    public static final float CENTRE_Y = 0.95F;
    public static final float STAND_OFF = 0.45F;
    public static final float ARRIVE_WIDTH = 1.5F;
    public static final float ARRIVE_HEIGHT = 1.6F;
    public static final float ARRIVE_CENTRE_Y = 1.05F;
    public static final float ARRIVE_STAND_OFF = 0.6F;
    public static final float HOLE = 0.62F;
    public static final float ARRIVE_HOLE = 0.8F;
    public static final float CRACK_TICKS = 24.0F;
    public static final float BREAK_SPREAD = 7.0F;
    public static final float FALL_TICKS = 10.0F;
    public static final float TEAR_TICKS = 4.0F;
    public static final float SEAL_SPREAD = 8.0F;
    public static final float RETURN_TICKS = 5.0F;
    public static final float FLASH_TICKS = 5.0F;
    public static final float HEAL_FROM = 10.0F;

    public static class State {
        double x;
        double y;
        double z;
        int lightCoords;
        float age;
        float closing;
        float opened;
        int mode;
        int seed;
        boolean own;
    }

    public RiftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public State createRenderState() {
        return new State();
    }

    @Override
    public boolean shouldRender(RiftEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    private void extractRenderState(RiftEntity entity, State state, float partialTicks) {
        state.x = Mth.lerp(partialTicks, entity.xo, entity.getX());
        state.y = Mth.lerp(partialTicks, entity.yo, entity.getY());
        state.z = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        state.age = entity.tickCount + partialTicks;
        state.closing = entity.isClosing() ? entity.closing() + partialTicks : 0.0F;
        state.opened = entity.opened(partialTicks);
        state.mode = entity.mode();
        state.seed = entity.getId();
        Minecraft minecraft = Minecraft.getInstance();
        state.own = minecraft.player != null && entity.owner() == minecraft.player.getId();
    }

    @Override
    public void render(RiftEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        State state = createRenderState();
        extractRenderState(entity, state, partialTicks);
        state.lightCoords = packedLight;
        submit(state, poseStack, buffers, entityRenderDispatcher.camera);
    }

    @Override
    public ResourceLocation getTextureLocation(RiftEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private void submit(State state, PoseStack poseStack, MultiBufferSource collector, Camera camera) {
        if (state.own) return;
        boolean arriving = state.mode == RiftEntity.ARRIVE;
        Choreography choreography = arriving ? new Arrival(state.age, state.closing) : new Departure(state.age, state.opened, state.closing);
        RealityBreak.Plane plane = plane(state, camera, arriving);
        RealityPass.queue(new RealityPass.Request(pattern(state.seed), plane, choreography, state.seed, new Vec3(state.x, state.y, state.z)));

    }

    public static FracturePattern pattern(int seed) {
        return new FracturePattern(seed * 47L + 11L, SPOKES, RINGS, EXTENT, HOLE);
    }

    public static RealityBreak.Plane plane(State state, Camera camera, boolean arriving) {
        double dx = camera.getPosition().x - state.x;
        double dz = camera.getPosition().z - state.z;
        double length = Math.sqrt(dx * dx + dz * dz);
        Vector3f toViewer = length < 1.0E-4 ? new Vector3f(0.0F, 0.0F, 1.0F) : new Vector3f((float) (dx / length), 0.0F, (float) (dz / length));
        Vector3f right = new Vector3f(toViewer.z, 0.0F, -toViewer.x);
        Vector3f centre = new Vector3f(toViewer).mul(arriving ? -ARRIVE_STAND_OFF : STAND_OFF).add(0.0F, arriving ? ARRIVE_CENTRE_Y : CENTRE_Y, 0.0F);
        return new RealityBreak.Plane(centre, right, new Vector3f(0.0F, 1.0F, 0.0F), toViewer, arriving ? ARRIVE_WIDTH : WIDTH,
                arriving ? ARRIVE_HEIGHT : HEIGHT);
    }

    private interface Choreography extends RealityBreak.Choreography {

        static boolean breaks(FracturePattern.Cell cell, float hole) {
            return cell.reach() + cell.wobble() * 0.1F < hole;
        }

        static float inner(FracturePattern.Cell cell, float hole) {
            return Mth.clamp(cell.reach() / hole, 0.0F, 1.0F);
        }

        static float smooth(float value) {
            float t = Mth.clamp(value, 0.0F, 1.0F);
            return t * t * (3.0F - 2.0F * t);
        }

        static float heal(float closing, float glow) {
            float heal = Mth.clamp((closing - HEAL_FROM) / (RiftEntity.CLOSE_TICKS - HEAL_FROM), 0.0F, 1.0F);
            return glow * (1.0F - heal) * (1.0F - heal);
        }
    }

    public record Departure(float age, float opened, float closing) implements Choreography {

        private static float breakAt(FracturePattern.Cell cell) {
            return Choreography.inner(cell, HOLE) * BREAK_SPREAD + cell.wobble() * 1.5F + 0.5F;
        }

        private static float sealAt(FracturePattern.Cell cell) {
            return (1.0F - Choreography.inner(cell, HOLE)) * SEAL_SPREAD + 1.0F + cell.wobble();
        }

        @Override
        public RealityBreak.CellState cell(FracturePattern.Cell cell, int index) {
            if (opened < 0.0F || !Choreography.breaks(cell, HOLE)) return RealityBreak.CellState.REST;
            float since = opened - breakAt(cell);
            if (since < 0.0F) return RealityBreak.CellState.REST;
            float fall = Math.min(1.0F, since / FALL_TICKS);
            float back = closing > 0.0F ? (closing - sealAt(cell)) / RETURN_TICKS : 0.0F;
            if (back <= 0.0F) return new RealityBreak.CellState(fall, true, 0.0F);
            if (back < 1.0F) return new RealityBreak.CellState(fall * (1.0F - back * back), true, 0.0F);
            float flash = 1.0F - (closing - sealAt(cell) - RETURN_TICKS) / FLASH_TICKS;
            return new RealityBreak.CellState(0.0F, false, Mth.clamp(flash, 0.0F, 1.0F));
        }

        @Override
        public float crackFront() {
            return EXTENT * (opened >= 0.0F ? 1.25F : 1.2F * Choreography.smooth(age / CRACK_TICKS));
        }

        @Override
        public float crackGlow() {
            if (closing > 0.0F) return Choreography.heal(closing, 0.6F);
            float pulse = 0.12F * (float) Math.sin(age * 0.35F);
            return opened >= 0.0F ? 0.8F : 0.55F + pulse;
        }

        @Override
        public float rimGlow() {
            return 0.85F + 0.15F * (float) Math.sin(age * 0.3F);
        }
    }

    public record Arrival(float age, float closing) implements Choreography {

        private static float returnAt(FracturePattern.Cell cell) {
            return (1.0F - Choreography.inner(cell, ARRIVE_HOLE)) * SEAL_SPREAD + 1.0F + cell.wobble();
        }

        @Override
        public RealityBreak.CellState cell(FracturePattern.Cell cell, int index) {
            if (!Choreography.breaks(cell, ARRIVE_HOLE)) return RealityBreak.CellState.REST;
            if (Choreography.inner(cell, ARRIVE_HOLE) > age / TEAR_TICKS) return RealityBreak.CellState.REST;
            if (closing <= 0.0F) return new RealityBreak.CellState(1.0F, true, 0.0F);
            float back = (closing - returnAt(cell)) / RETURN_TICKS;
            if (back <= 0.0F) return new RealityBreak.CellState(1.0F, true, 0.0F);
            if (back < 1.0F) return new RealityBreak.CellState(1.0F - back * back, true, 0.0F);
            float flash = 1.0F - (closing - returnAt(cell) - RETURN_TICKS) / FLASH_TICKS;
            return new RealityBreak.CellState(0.0F, false, Mth.clamp(flash, 0.0F, 1.0F));
        }

        @Override
        public float crackFront() {
            return EXTENT * 1.25F * Choreography.smooth(age / TEAR_TICKS);
        }

        @Override
        public float crackGlow() {
            return closing > 0.0F ? Choreography.heal(closing, 0.6F) : 0.8F;
        }

        @Override
        public float rimGlow() {
            return 0.85F + 0.15F * (float) Math.sin(age * 0.3F);
        }
    }
}
