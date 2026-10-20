package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddykat.alchemia.common.effect.FlameCloudEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class FlameCloudRenderer extends EntityRenderer<FlameCloudEntity, FlameCloudRenderer.State> {

    private static final int SEGMENTS = 32;
    private static final float GLOW_LIFT = 0.03F;
    private static final float SCORCH_LIFT = 0.02F;
    private static final int GLOW_CORE = 0xFFB040;
    private static final int GLOW_EDGE = 0xFF4A10;
    private static final int SCORCH = 0x1A0C06;
    private static final float IGNITE_TICKS = 8.0F;
    private static final float RING_TICKS = 7.0F;
    private static final float TONGUE_ASPECT = 1.4F;

    public static class State extends EntityRenderState {
        float age;
        float radius;
        float full;
        int duration;
        boolean lingering;
        int seed;
    }

    public FlameCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected boolean affectedByCulling(FlameCloudEntity entity) {
        return false;
    }

    @Override
    public void extractRenderState(FlameCloudEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.radius = entity.radius(state.age);
        state.full = entity.fullRadius();
        state.duration = entity.duration();
        state.lingering = entity.lingering();
        state.seed = entity.getId();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.age >= state.duration) return;
        float dx = (float) (camera.pos.x - state.x);
        float dz = (float) (camera.pos.z - state.z);
        float length = Mth.sqrt(dx * dx + dz * dz);
        float rightX = length < 1.0E-4F ? 1.0F : dz / length;
        float rightZ = length < 1.0E-4F ? 0.0F : -dx / length;

        TextureAtlasSprite[] fire = {
                Minecraft.getInstance().getAtlasManager().get(ModelBakery.FIRE_0),
                Minecraft.getInstance().getAtlasManager().get(ModelBakery.FIRE_1)
        };
        if (state.lingering) {
            napalm(state, poseStack, collector);
        } else {
            blast(state, poseStack, collector, fire, rightX, rightZ);
        }
        super.submit(state, poseStack, collector, camera);
    }

    private static void napalm(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        float age = state.age;
        float radius = state.radius;
        float spread = smooth(age / IGNITE_TICKS);
        float reach = Math.min(radius, state.full * spread);
        float fade = 1.0F - smooth((age - (state.duration - 10.0F)) / 10.0F);
        float sincePulse = (age - 1.0F) % FlameCloudEntity.PULSE_TICKS;
        float flare = 1.0F + 0.45F * (float) Math.exp(-sincePulse / 3.0F);
        float flicker = 0.85F + 0.1F * (float) Math.sin(age * 0.9F) + 0.05F * (float) Math.sin(age * 2.3F + 1.3F);

        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(),
                (pose, buffer) -> disc(pose, buffer, reach * 1.08F, SCORCH_LIFT, ARGB.color((int) (130 * fade), SCORCH),
                        ARGB.color((int) (70 * fade), SCORCH), ARGB.color(0, SCORCH)));
        collector.submitCustomGeometry(poseStack, RenderTypes.lightning(),
                (pose, buffer) -> disc(pose, buffer, reach * 1.15F, GLOW_LIFT, ARGB.color((int) (150 * fade * flicker * Math.min(flare, 1.2F)), GLOW_CORE),
                        ARGB.color((int) (90 * fade * flicker), GLOW_EDGE), ARGB.color(0, GLOW_EDGE)));
    }

    private static void blast(State state, PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite[] fire, float rightX,
                              float rightZ) {
        float age = state.age;
        float life = Mth.clamp(age / state.duration, 0.0F, 1.0F);
        float ring = state.full * easeOut(Math.min(1.0F, age / RING_TICKS));
        float fade = 1.0F - smooth((life - 0.45F) / 0.55F);
        float flash = (float) Math.exp(-age / 4.0F);

        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(),
                (pose, buffer) -> disc(pose, buffer, ring * 1.05F, SCORCH_LIFT, ARGB.color((int) (120 * fade), SCORCH),
                        ARGB.color((int) (60 * fade), SCORCH), ARGB.color(0, SCORCH)));
        collector.submitCustomGeometry(poseStack, RenderTypes.lightning(),
                (pose, buffer) -> disc(pose, buffer, ring * 1.2F + 0.4F, GLOW_LIFT, ARGB.color((int) (230 * Math.min(1.0F, flash + 0.25F * fade)), GLOW_CORE),
                        ARGB.color((int) (140 * fade), GLOW_EDGE), ARGB.color(0, GLOW_EDGE)));

        RandomSource random = RandomSource.create(state.seed * 7919L + 17L);
        int count = Math.max(14, (int) (state.full * 11.0F));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
            for (int index = 0; index < count; index++) {
                float angle = (index + random.nextFloat() * 0.6F) * Mth.TWO_PI / count;
                float jitter = 1.0F + (random.nextFloat() - 0.5F) * 0.18F;
                float size = 0.75F + random.nextFloat() * 0.5F;
                float lag = random.nextFloat() * 0.15F;
                float height = size * (1.0F - smooth((life - lag) / (1.0F - lag))) * (0.6F + 0.6F * flash);
                if (height <= 0.02F) continue;
                float x = Mth.cos(angle) * ring * jitter;
                float z = Mth.sin(angle) * ring * jitter;
                tongue(pose, buffer, fire[index & 1], x, 0.0F, z, height, rightX, rightZ, (index & 2) != 0, ARGB.color((int) (240 * fade), 255, 230, 200));
            }
            int core = 7;
            for (int index = 0; index < core; index++) {
                float angle = random.nextFloat() * Mth.TWO_PI;
                float spread = random.nextFloat() * 0.35F * ring;
                float rise = age * (0.05F + random.nextFloat() * 0.04F);
                float size = (1.6F + random.nextFloat() * 0.8F) * (1.0F - smooth(life / 0.7F));
                if (size <= 0.02F) continue;
                tongue(pose, buffer, fire[(index + 1) & 1], Mth.cos(angle) * spread, rise, Mth.sin(angle) * spread, size, rightX, rightZ,
                        (index & 1) == 0, ARGB.color((int) (230 * fade), 255, 255, 255));
            }
        });
    }

    private static void tongue(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite, float x, float y, float z, float height,
                               float rightX, float rightZ, boolean flip, int colour) {
        float half = height / TONGUE_ASPECT * 0.5F;
        float u0 = flip ? sprite.getU1() : sprite.getU0();
        float u1 = flip ? sprite.getU0() : sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float sink = height * 0.06F;
        fireVertex(pose, buffer, x - rightX * half, y - sink, z - rightZ * half, u0, v1, colour);
        fireVertex(pose, buffer, x + rightX * half, y - sink, z + rightZ * half, u1, v1, colour);
        fireVertex(pose, buffer, x + rightX * half, y + height - sink, z + rightZ * half, u1, v0, colour);
        fireVertex(pose, buffer, x - rightX * half, y + height - sink, z - rightZ * half, u0, v0, colour);
    }

    private static void fireVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int colour) {
        buffer.addVertex(pose, x, y, z).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void disc(PoseStack.Pose pose, VertexConsumer buffer, float radius, float lift, int core, int middle, int edge) {
        if (radius <= 0.01F) return;
        float mid = radius * 0.55F;
        for (int segment = 0; segment < SEGMENTS; segment++) {
            float a = segment * Mth.TWO_PI / SEGMENTS;
            float b = (segment + 1) * Mth.TWO_PI / SEGMENTS;
            float ca = Mth.cos(a);
            float sa = Mth.sin(a);
            float cb = Mth.cos(b);
            float sb = Mth.sin(b);
            buffer.addVertex(pose, cb * mid, lift, sb * mid).setColor(middle);
            buffer.addVertex(pose, ca * mid, lift, sa * mid).setColor(middle);
            buffer.addVertex(pose, 0.0F, lift, 0.0F).setColor(core);
            buffer.addVertex(pose, 0.0F, lift, 0.0F).setColor(core);
            buffer.addVertex(pose, cb * radius, lift, sb * radius).setColor(edge);
            buffer.addVertex(pose, ca * radius, lift, sa * radius).setColor(edge);
            buffer.addVertex(pose, ca * mid, lift, sa * mid).setColor(middle);
            buffer.addVertex(pose, cb * mid, lift, sb * mid).setColor(middle);
        }
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float easeOut(float value) {
        float t = 1.0F - Mth.clamp(value, 0.0F, 1.0F);
        return 1.0F - t * t * t;
    }
}
