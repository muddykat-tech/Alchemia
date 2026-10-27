package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class SceneCapture {

    public static final int SLOTS = 2;

    private static final int DEMAND_FRAMES = 3;
    private static final int FRESH_FRAMES = 2;
    private static final float EDGE_MARGIN = 0.002F;

    public record View(Matrix4f projection, Matrix4f modelView, Vec3 camera) {

        public float[] project(double x, double y, double z) {
            Vector4f clip = new Vector4f((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z), 1.0F);
            modelView.transform(clip);
            projection.transform(clip);
            if (clip.w <= 1.0E-4F) return null;
            float u = clip.x / clip.w * 0.5F + 0.5F;
            float v = clip.y / clip.w * 0.5F + 0.5F;
            if (u < EDGE_MARGIN || u > 1.0F - EDGE_MARGIN || v < EDGE_MARGIN || v > 1.0F - EDGE_MARGIN) return null;
            return new float[]{u, v};
        }

        public float[] projectClamped(double x, double y, double z) {
            Vector4f clip = new Vector4f((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z), 1.0F);
            modelView.transform(clip);
            projection.transform(clip);
            if (clip.w <= 1.0E-4F) return null;
            return new float[]{Mth.clamp(clip.x / clip.w * 0.5F + 0.5F, 0.0F, 1.0F), Mth.clamp(clip.y / clip.w * 0.5F + 0.5F, 0.0F, 1.0F)};
        }
    }

    public static final ResourceLocation BEHIND_ID = new ResourceLocation(Alchemia.MODID, "dynamic/reality_behind");

    private static final Object[] OWNERS = new Object[SLOTS];
    private static final long[] USED = new long[SLOTS];
    private static final View[] VIEWS = new View[SLOTS];
    private static final Slot LIVE = new Slot();
    private static final Slot BEHIND = new Slot();
    private static final Slot[] FROZEN = new Slot[SLOTS];
    private static @Nullable View liveView;
    private static @Nullable View camera;
    private static boolean behindRegistered;
    private static int demand;
    private static long frame;
    private static long liveFrame = Long.MIN_VALUE / 2;
    private static long clock;

    static {
        for (int slot = 0; slot < SLOTS; slot++) FROZEN[slot] = new Slot();
    }

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

    public static int textureId(int slot) {
        return slot < 0 || slot >= SLOTS ? -1 : FROZEN[slot].getId();
    }

    public static boolean captureBehind(RenderTarget main) {
        BEHIND.ensure(main.width, main.height);
        if (!behindRegistered) {
            Minecraft.getInstance().getTextureManager().register(BEHIND_ID, BEHIND);
            behindRegistered = true;
        }
        copy(main, BEHIND.target);
        return true;
    }

    private static void copy(RenderTarget from, RenderTarget to) {
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, from.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, to.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, from.width, from.height, 0, 0, to.width, to.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAfterLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        frame++;
        camera = new View(new Matrix4f(event.getProjectionMatrix()), cameraView(event.getCamera()), event.getCamera().getPosition());
        if (demand <= 0) return;
        demand--;
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        LIVE.ensure(main.width, main.height);
        copy(main, LIVE.target);
        liveFrame = frame;
        liveView = camera;
    }

    public static Matrix4f cameraView(Camera camera) {
        return new Matrix4f().rotateX(camera.getXRot() * Mth.DEG_TO_RAD).rotateY((camera.getYRot() + 180.0F) * Mth.DEG_TO_RAD);
    }

    private static final class Slot extends AbstractTexture {

        private @Nullable TextureTarget target;

        private void ensure(int width, int height) {
            if (target != null && target.width == width && target.height == height) return;
            if (target == null) {
                target = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            } else {
                target.resize(width, height, Minecraft.ON_OSX);
            }
            target.setFilterMode(GL11.GL_LINEAR);
        }

        @Override
        public int getId() {
            return target == null ? -1 : target.getColorTextureId();
        }

        @Override
        public void load(ResourceManager manager) {
        }

        @Override
        public void releaseId() {
        }

        @Override
        public void close() {
            if (target != null) target.destroyBuffers();
            target = null;
        }
    }
}
