package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.function.BiConsumer;

public final class Submit {

    private Submit() {
    }

    public static void geometry(MultiBufferSource buffers, PoseStack poseStack, RenderType type, BiConsumer<PoseStack.Pose, VertexConsumer> body) {
        body.accept(poseStack.last(), buffers.getBuffer(type));
    }
}
