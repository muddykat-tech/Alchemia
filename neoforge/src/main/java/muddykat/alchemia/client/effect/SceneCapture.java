package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
#if MC_VER >= 26300
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.textures.GpuSampler;
#else
#if MC_VER >= 26200
import com.mojang.blaze3d.GpuFormat;
#endif
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
#endif
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import muddykat.alchemia.Alchemia;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class SceneCapture {

    public static final int SLOTS = 2;

    private static final int DEMAND_FRAMES = 3;
    private static final int FRESH_FRAMES = 2;
    private static final float EDGE_MARGIN = 0.002F;

    public record View(Matrix4f projection, Matrix4f modelView, Vec3 camera) {

        public float @Nullable [] project(double x, double y, double z) {
            Vector4f clip = new Vector4f((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z), 1.0F);
            modelView.transform(clip);
            projection.transform(clip);
            if (clip.w <= 1.0E-4F) return null;
            float u = clip.x / clip.w * 0.5F + 0.5F;
            float v = clip.y / clip.w * 0.5F + 0.5F;
            if (u < EDGE_MARGIN || u > 1.0F - EDGE_MARGIN || v < EDGE_MARGIN || v > 1.0F - EDGE_MARGIN) return null;
            return new float[]{u, v};
        }

        public float @Nullable [] projectClamped(double x, double y, double z) {
            Vector4f clip = new Vector4f((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z), 1.0F);
            modelView.transform(clip);
            projection.transform(clip);
            if (clip.w <= 1.0E-4F) return null;
            return new float[]{Mth.clamp(clip.x / clip.w * 0.5F + 0.5F, 0.0F, 1.0F), Mth.clamp(clip.y / clip.w * 0.5F + 0.5F, 0.0F, 1.0F)};
        }
    }

    public static final Identifier BEHIND_ID = Identifier.fromNamespaceAndPath(Alchemia.MODID, "dynamic/reality_behind");

    private static final Object[] OWNERS = new Object[SLOTS];
    private static final long[] USED = new long[SLOTS];
    private static final View[] VIEWS = new View[SLOTS];
    private static @Nullable View liveView;
    private static @Nullable View camera;
    private static int demand;
    private static long frame;
    private static long liveFrame = Long.MIN_VALUE / 2;
    private static long clock;

    private SceneCapture() {
    }

    public static boolean supported() {
        return true;
    }

    public static void request() {
        demand = DEMAND_FRAMES;
    }

    public static @Nullable View camera() {
        return camera;
    }

    public static @Nullable View view(int slot) {
        return slot < 0 || slot >= SLOTS ? null : VIEWS[slot];
    }

    public static int slotOf(Object owner) {
        for (int slot = 0; slot < SLOTS; slot++) {
            if (owner.equals(OWNERS[slot]) && VIEWS[slot] != null) {
                USED[slot] = ++clock;
                return slot;
            }
        }
        return -1;
    }

    public static void release(Object owner) {
        for (int slot = 0; slot < SLOTS; slot++) {
            if (owner.equals(OWNERS[slot])) {
                OWNERS[slot] = null;
                VIEWS[slot] = null;
            }
        }
    }

    public static int freeze(Object owner) {
        if (liveView == null || LIVE.target == null || frame - liveFrame > FRESH_FRAMES) return -1;
        int slot = slotOf(owner);
        if (slot < 0) {
            slot = 0;
            for (int candidate = 1; candidate < SLOTS; candidate++) {
                if (OWNERS[candidate] == null || USED[candidate] < USED[slot]) slot = candidate;
                if (OWNERS[candidate] == null) break;
            }
        }
        FROZEN[slot].ensure(LIVE.target.width, LIVE.target.height);
        copy(LIVE.target, FROZEN[slot].target);
        OWNERS[slot] = owner;
        VIEWS[slot] = liveView;
        USED[slot] = ++clock;
        return slot;
    }

    private static void copy(RenderTarget from, TextureTarget to) {
#if MC_VER >= 26300
        to.copyColorFrom(from);
#else
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(from.getColorTexture(), to.getColorTexture(), 0, 0, 0, 0, 0,
                to.width, to.height);
#endif
    }

    public static @Nullable GpuTextureView textureView(int slot) {
        Slot frozen = FROZEN[slot];
        return frozen.target == null ? null : frozen.target.getColorTextureView();
    }

    public static GpuSampler sampler() {
        return RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
    }

    private static final Slot LIVE = new Slot("live");
    private static final Slot BEHIND = new Slot("behind");
    private static boolean behindRegistered;
    private static final Slot[] FROZEN = new Slot[SLOTS];

    static {
        for (int slot = 0; slot < SLOTS; slot++) FROZEN[slot] = new Slot("reality " + slot);
    }

    private static final class Slot extends AbstractTexture {

        private final String label;
        private @Nullable TextureTarget target;

        private Slot(String label) {
            this.label = label;
        }

        private void ensure(int width, int height) {
            if (target != null && target.width == width && target.height == height) return;
            if (target == null) {
#if MC_VER >= 26300
                target = new TextureTarget("Alchemia " + label, width, height, GpuFormat.RGBA8_UNORM, null);
#elif MC_VER >= 26200
                target = new TextureTarget("Alchemia " + label, width, height, false, GpuFormat.RGBA8_UNORM);
#else
                target = new TextureTarget("Alchemia " + label, width, height, false);
#endif
            } else {
                target.resize(width, height);
            }
            texture = target.getColorTexture();
            textureView = target.getColorTextureView();
            sampler = SceneCapture.sampler();
        }

        @Override
        public void close() {
            if (target != null) target.destroyBuffers();
            target = null;
            texture = null;
            textureView = null;
        }
    }

    public static boolean captureBehind(RenderTarget main) {
        if (main.getColorTexture() == null) return false;
        BEHIND.ensure(main.width, main.height);
        if (!behindRegistered) {
            Minecraft.getInstance().getTextureManager().register(BEHIND_ID, BEHIND);
            behindRegistered = true;
        }
        copy(main, BEHIND.target);
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        frame++;
        camera = new View(new Matrix4f(event.getLevelRenderState().cameraRenderState.projectionMatrix),
                new Matrix4f(event.getModelViewMatrix()), event.getLevelRenderState().cameraRenderState.pos);
        if (demand <= 0) return;
        demand--;
        Minecraft minecraft = Minecraft.getInstance();
#if MC_VER >= 26200
        RenderTarget main = minecraft.gameRenderer.mainRenderTarget();
#else
        RenderTarget main = minecraft.getMainRenderTarget();
#endif
        if (main.getColorTexture() == null) return;
        LIVE.ensure(main.width, main.height);
        copy(main, LIVE.target);
        liveFrame = frame;
        liveView = camera;
    }
}
