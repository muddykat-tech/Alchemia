package muddykat.alchemia.client.effect;

#if MC_VER >= 26300
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;

import java.util.Optional;
#endif
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;

public final class SenseShader {

#if MC_VER >= 26300
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/sense_see_through"))
            .withDepthStencilState(Optional.empty())
            .build();

    private static final RenderType SEE_THROUGH = RenderType.create("alchemia_sense_see_through", RenderSetup.builder(PIPELINE).createRenderSetup());
#else
    private static final int TEXT_CUTOFF = 27;
#endif

    private SenseShader() {
    }

    public static RenderType seeThrough() {
#if MC_VER >= 26300
        return SEE_THROUGH;
#else
        return RenderTypes.textSeeThrough(SenseGeometry.WHITE);
#endif
    }

    public static void vertex(VertexConsumer vertex, int colour, float u, float v) {
#if MC_VER >= 26300
        vertex.setColor(colour);
#else
        int alpha = ARGB.alpha(colour);
        vertex.setColor(alpha == 0 ? colour : ARGB.color(Math.max(TEXT_CUTOFF, alpha), colour)).setUv(u, v).setLight(LightCoordsUtil.FULL_BRIGHT);
#endif
    }
}
