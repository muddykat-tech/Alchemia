package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
#if MC_VER >= 26300
import com.mojang.renderpearl.api.commands.RenderPass;
#endif
#if MC_VER >= 26200
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
#else
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
#endif
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
#if MC_VER >= 26300
import java.util.Optional;
import java.util.OptionalDouble;
#endif

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class RealityPass {

    public record Request(FracturePattern pattern, RealityBreak.Plane plane, RealityBreak.Choreography choreography, int seed, Vec3 origin) {
    }

    private record Built(RealityBreak.Frame frame, PoseStack.Pose pose) {
    }

    private static final List<Request> QUEUE = new ArrayList<>();
    private static final int LAYERS = 4;

#if MC_VER >= 26200
    private static @Nullable StagedVertexBuffer staging;
#else
    private static @Nullable ByteBufferBuilder bytes;
#endif
    private static @Nullable Method bobHurt;
    private static @Nullable Method bobView;
    private static boolean reflected;

    private RealityPass() {
    }

    public static boolean supported() {
        return true;
    }

    public static void queue(Request request) {
        QUEUE.add(request);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        if (QUEUE.isEmpty()) return;
        List<Request> requests = List.copyOf(QUEUE);
        QUEUE.clear();

        Minecraft minecraft = Minecraft.getInstance();
#if MC_VER >= 26200
        RenderTarget main = minecraft.gameRenderer.mainRenderTarget();
#else
        RenderTarget main = minecraft.getMainRenderTarget();
#endif
        if (main.getDepthTextureView() == null || !SceneCapture.captureBehind(main)) return;

        CameraRenderState camera = event.getLevelRenderState().cameraRenderState;
        Matrix4f view = new Matrix4f(event.getModelViewMatrix());
        SceneCapture.View live = new SceneCapture.View(projection(minecraft, camera), view, camera.pos);

        List<Built> built = new ArrayList<>();
        for (Request request : requests) {
            Vec3 origin = request.origin();
            PoseStack poseStack = new PoseStack();
            poseStack.translate(origin.x - camera.pos.x, origin.y - camera.pos.y, origin.z - camera.pos.z);
            RealityBreak.Mapping mapping = rest -> live.projectClamped(origin.x + rest.x, origin.y + rest.y, origin.z + rest.z);
            built.add(new Built(RealityBreak.frame(request.pattern(), request.plane(), request.choreography(), request.seed(), mapping),
                    poseStack.last()));
        }

        RenderType[] types = {PortalShader.interior(), RenderTypes.debugQuads(), RenderTypes.text(SceneCapture.BEHIND_ID), RenderTypes.debugQuads()};
        Matrix4fStack modelView = RenderSystem.getModelViewStack();

#if MC_VER >= 26200
        modelView.pushMatrix().mul(view);
        PreparedRenderType[] prepared = new PreparedRenderType[LAYERS];
        for (int layer = 0; layer < LAYERS; layer++) prepared[layer] = types[layer].prepare();
        modelView.popMatrix();

        if (staging == null) staging = new StagedVertexBuffer(() -> "Alchemia reality", 65536);
        StagedVertexBuffer buffer = staging;
        StagedVertexBuffer.Draw[] draws = new StagedVertexBuffer.Draw[LAYERS];
        for (int layer = 0; layer < LAYERS; layer++) draws[layer] = buffer.appendDraw(types[layer].format(), types[layer].primitiveTopology());
        for (int layer = 0; layer < LAYERS; layer++) emit(layer, built, buffer.getVertexBuilder(draws[layer]));
        buffer.upload();
#if MC_VER >= 26300
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Alchemia reality",
                main.getColorTextureView(), Optional.empty(), main.getDepthTextureView(), OptionalDouble.empty())) {
            for (int layer = 0; layer < LAYERS; layer++) {
                StagedVertexBuffer.ExecuteInfo info = buffer.getExecuteInfo(draws[layer]);
                if (info != null) prepared[layer].drawFromBuffer(info, renderPass);
            }
        }
#else
        for (int layer = 0; layer < LAYERS; layer++) {
            StagedVertexBuffer.ExecuteInfo info = buffer.getExecuteInfo(draws[layer]);
            if (info != null) prepared[layer].drawFromBuffer(info);
        }
#endif
        buffer.endFrame();
#else
        if (bytes == null) bytes = new ByteBufferBuilder(65536);
        modelView.pushMatrix().mul(view);
        try {
            for (int layer = 0; layer < LAYERS; layer++) {
                BufferBuilder builder = new BufferBuilder(bytes, types[layer].mode(), types[layer].format());
                emit(layer, built, builder);
                MeshData mesh = builder.build();
                if (mesh != null) types[layer].draw(mesh);
            }
        } finally {
            modelView.popMatrix();
        }
#endif
    }

    private static void emit(int layer, List<Built> built, VertexConsumer buffer) {
        for (Built entry : built) {
            RealityBreak.Frame frame = entry.frame();
            PoseStack.Pose pose = entry.pose();
            switch (layer) {
                case 0 -> {
                    if (frame.holes()) frame.voids(pose, buffer, true);
                }
                case 1 -> {
                    if (frame.flights()) frame.walls(pose, buffer);
                }
                case 2 -> {
                    if (frame.flights()) frame.shards(pose, buffer, true);
                }
                default -> {
                    if (frame.flights()) frame.shards(pose, buffer, false);
                    frame.lines(pose, buffer);
                }
            }
        }
    }

    private static Matrix4f projection(Minecraft minecraft, CameraRenderState camera) {
        Matrix4f projection = new Matrix4f(camera.projectionMatrix);
        if (!reflected) {
            reflected = true;
            try {
                bobHurt = GameRenderer.class.getDeclaredMethod("bobHurt", CameraRenderState.class, PoseStack.class);
                bobView = GameRenderer.class.getDeclaredMethod("bobView", CameraRenderState.class, PoseStack.class);
                bobHurt.setAccessible(true);
                bobView.setAccessible(true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                bobHurt = null;
                bobView = null;
            }
        }
        if (bobHurt == null || bobView == null) return projection;
        PoseStack bob = new PoseStack();
        try {
            bobHurt.invoke(minecraft.gameRenderer, camera, bob);
            if (minecraft.options.bobView().get()) bobView.invoke(minecraft.gameRenderer, camera, bob);
        } catch (ReflectiveOperationException exception) {
            return projection;
        }
        return projection.mul(bob.last().pose());
    }
}
