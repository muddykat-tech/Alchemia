package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import muddykat.alchemia.common.effect.TeleportFieldEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class TeleportFieldRenderer extends EntityRenderer<TeleportFieldEntity, TeleportFieldRenderer.State> {

    public static final float LIFT = 0.02F;
    public static final int SCAR_SPOKES = 14;
    public static final int SCAR_RINGS = 9;
    public static final float SCAR_EXTENT = 1.3F;
    public static final float SCAR_SIZE = TeleportFieldEntity.SPLASH_REACH / SCAR_EXTENT;
    public static final float SCAR_LOOSE = 0.45F;
    public static final float SCAR_CRACK_TICKS = 5.0F;
    public static final float HOVER_CHANCE = 0.55F;
    public static final float HOVER_CORE = 0.08F;
    public static final float HOVER_LOW = 0.35F;
    public static final float HOVER_HIGH = 1.1F;
    public static final float HOVER_BOB = 0.06F;

    public static class State extends EntityRenderState {
        float age;
        float collapse;
        int mode;
        int seed;
    }

    public TeleportFieldRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected boolean affectedByCulling(TeleportFieldEntity entity) {
        return false;
    }

    @Override
    public void extractRenderState(TeleportFieldEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.age(partialTicks);
        state.collapse = entity.collapse(state.age);
        state.mode = entity.mode();
        state.seed = entity.getId();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (camera.pos.y < state.y + LIFT) return;

        boolean scar = state.mode == TeleportFieldEntity.SCAR;
        FracturePattern pattern = scar
                ? new FracturePattern(state.seed * 13L + 3L, SCAR_SPOKES, SCAR_RINGS, SCAR_EXTENT, SCAR_LOOSE)
                : ReturnPortalRenderer.pattern(state.seed);
        float size = scar ? SCAR_SIZE : TeleportFieldEntity.FIELD_RADIUS / ReturnPortalRenderer.HOLE;
        RealityBreak.Plane plane = ground(size, size / ReturnPortalRenderer.PLANE_WIDTH * 0.6F);
        RealityBreak.Choreography choreography = scar
                ? new Scar(state.age)
                : new Field(state.age, state.collapse);
        if (RealityPass.supported()) {
            RealityPass.queue(new RealityPass.Request(pattern, plane, choreography, state.seed, new Vec3(state.x, state.y, state.z)));
        } else {
            RealityBreak.render(collector, poseStack, RealityBreak.frame(pattern, plane, choreography, state.seed, null), null);
        }
        super.submit(state, poseStack, collector, camera);
    }

    public static RealityBreak.Plane ground(float size, float scale) {
        return new RealityBreak.Plane(new Vector3f(0.0F, LIFT, 0.0F), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 1.0F),
                new Vector3f(0.0F, 1.0F, 0.0F), size, size, scale);
    }

    public record Field(float age, float collapse) implements RealityBreak.Choreography {

        private ReturnPortalRenderer.Choreography base() {
            return new ReturnPortalRenderer.Choreography(age, collapse);
        }

        private static float roll(FracturePattern.Cell cell, float salt) {
            float noise = (float) Math.sin(cell.cx() * 91.7F + cell.cy() * 237.3F + salt) * 43758.547F;
            return noise - (float) Math.floor(noise);
        }

        @Override
        public RealityBreak.CellState cell(FracturePattern.Cell cell, int index) {
            RealityBreak.CellState state = base().cell(cell, index);
            if (!state.hole() || state.shift() > 0.0F || state.flight() <= 0.0F) return state;
            if (cell.reach() < HOVER_CORE || roll(cell, 0.0F) > HOVER_CHANCE) return state;

            float rise = Mth.clamp(state.flight(), 0.0F, 1.0F);
            rise = rise * rise * rise * (rise * (rise * 6.0F - 15.0F) + 10.0F);
            float height = HOVER_LOW + (HOVER_HIGH - HOVER_LOW) * roll(cell, 3.1F);
            float bob = HOVER_BOB * (float) Math.sin(age * 0.032F + roll(cell, 7.7F) * Mth.TWO_PI);
            return RealityBreak.CellState.hovering(Math.max(0.001F, rise * (height + bob)), age + index * 13.0F);
        }

        @Override
        public float crackFront() {
            return base().crackFront();
        }

        @Override
        public float crackGlow() {
            return base().crackGlow();
        }

        @Override
        public float rimGlow() {
            return base().rimGlow();
        }

        @Override
        public boolean rimLines() {
            return base().rimLines();
        }
    }

    public record Scar(float age) implements RealityBreak.Choreography {

        private static float smooth(float value) {
            float t = Mth.clamp(value, 0.0F, 1.0F);
            return t * t * (3.0F - 2.0F * t);
        }

        @Override
        public RealityBreak.CellState cell(FracturePattern.Cell cell, int index) {
            float reach = cell.reach() + cell.wobble() * 0.1F;
            if (reach >= SCAR_LOOSE || cell.wobble() < -0.35F) return RealityBreak.CellState.REST;
            float since = age - (1.0F + reach / SCAR_LOOSE * 4.0F + cell.wobble() * 1.5F);
            if (since <= 0.0F) return RealityBreak.CellState.REST;

            float settle = Mth.clamp(since / 4.0F, 0.0F, 1.0F);
            float jolt = smooth(settle) * (1.0F + 0.35F * (float) Math.sin(settle * Math.PI));
            float drift = 1.0F + 0.15F * (float) Math.sin(age * 0.25F + index * 1.7F);
            float shift = jolt * drift * (0.7F + 0.5F * (1.0F - reach / SCAR_LOOSE));
            float snap = (age - TeleportFieldEntity.SCAR_SNAP - (1.0F - reach / SCAR_LOOSE) * 3.0F) / 3.0F;
            if (snap <= 0.0F) return new RealityBreak.CellState(0.0F, true, 0.0F, shift);
            if (snap < 1.0F) return new RealityBreak.CellState(0.0F, true, 0.0F, shift * (1.0F - snap * snap));
            float flash = 1.0F - (snap - 1.0F) * 3.0F / 5.0F;
            return new RealityBreak.CellState(0.0F, false, Mth.clamp(flash, 0.0F, 1.0F));
        }

        @Override
        public float crackFront() {
            return SCAR_EXTENT * 1.25F * smooth(age / SCAR_CRACK_TICKS);
        }

        @Override
        public float crackGlow() {
            if (age < SCAR_CRACK_TICKS) return 0.95F;
            float settle = Mth.lerp(smooth((age - SCAR_CRACK_TICKS) / 12.0F), 0.95F, 0.55F) + 0.06F * (float) Math.sin(age * 0.3F);
            float heal = TeleportFieldEntity.SCAR_SNAP + 4.0F;
            return settle * (1.0F - smooth((age - heal) / (TeleportFieldEntity.SCAR_TICKS - heal)));
        }

        @Override
        public float rimGlow() {
            return 0.8F;
        }
    }
}
