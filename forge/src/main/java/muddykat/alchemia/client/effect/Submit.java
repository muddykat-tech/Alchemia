package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.function.BiConsumer;

public final class Submit {

    private Submit() {
    }

    public static void geometry(MultiBufferSource buffers, PoseStack poseStack, RenderType type, BiConsumer<PoseStack.Pose, VertexSink> body) {
        VertexSink sink = VertexSink.of(buffers.getBuffer(type), type.format());
        body.accept(poseStack.last(), sink);
        sink.flush();
    }
}
