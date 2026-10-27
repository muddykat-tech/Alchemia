package muddykat.alchemia.client.effect;

#if MC_VER >= 26300
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
#else
import com.mojang.blaze3d.pipeline.RenderPipeline;
#endif
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public final class SenseShader {

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/sense_see_through"))
            .withDepthStencilState(Optional.empty())
            .build();

    private static final RenderType SEE_THROUGH = RenderType.create("alchemia_sense_see_through", RenderSetup.builder(PIPELINE).createRenderSetup());

    private SenseShader() {
    }

    public static RenderType seeThrough() {
        return SEE_THROUGH;
    }

    public static void vertex(VertexConsumer vertex, int colour, float u, float v) {
        vertex.setColor(colour);
    }
}
