package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class RealityPass {

    public record Request(FracturePattern pattern, RealityBreak.Plane plane, RealityBreak.Choreography choreography, int seed, Vec3 origin) {
    }

    private record Built(RealityBreak.Frame frame, PoseStack.Pose pose) {
    }

    private static final List<Request> QUEUE = new ArrayList<>();
    private static final int LAYERS = 4;

    private static @Nullable ByteBufferBuilder bytes;

    private RealityPass() {
    }

    public static boolean supported() {
        return true;
    }

    public static void queue(Request request) {
        QUEUE.add(request);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAfterLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || QUEUE.isEmpty()) return;
        List<Request> requests = List.copyOf(QUEUE);
        QUEUE.clear();

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.getMainRenderTarget();
        RenderType portal = PortalShader.interior();
        if (portal == null || !SceneCapture.captureBehind(main)) return;

        Vec3 camera = event.getCamera().getPosition();
        Matrix4f view = new Matrix4f(event.getModelViewMatrix());
        SceneCapture.View live = new SceneCapture.View(new Matrix4f(event.getProjectionMatrix()), view, camera);

        List<Built> built = new ArrayList<>();
        for (Request request : requests) {
            Vec3 origin = request.origin();
            PoseStack poseStack = new PoseStack();
            poseStack.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);
            RealityBreak.Mapping mapping = rest -> live.projectClamped(origin.x + rest.x, origin.y + rest.y, origin.z + rest.z);
            built.add(new Built(RealityBreak.frame(request.pattern(), request.plane(), request.choreography(), request.seed(), mapping),
                    poseStack.last()));
        }

        RenderType[] types = {portal, RenderType.debugQuads(), RenderType.text(SceneCapture.BEHIND_ID), RenderType.debugQuads()};
        if (bytes == null) bytes = new ByteBufferBuilder(65536);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.mul(view);
        RenderSystem.applyModelViewMatrix();
        try {
            for (int layer = 0; layer < LAYERS; layer++) {
                BufferBuilder builder = new BufferBuilder(bytes, types[layer].mode(), types[layer].format());
                emit(layer, built, builder);
                MeshData mesh = builder.build();
                if (mesh != null) types[layer].draw(mesh);
            }
        } finally {
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
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
}
