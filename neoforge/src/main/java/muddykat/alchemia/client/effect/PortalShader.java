package muddykat.alchemia.client.effect;

#if MC_VER >= 26300
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
#endif
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.Nullable;

public final class PortalShader {

#if MC_VER >= 26300
    public static final Identifier STARS = Identifier.withDefaultNamespace("textures/entity/end_portal/end_portal.png");

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.WORLD_TEXT_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/portal_rift"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Alchemia.MODID, "core/portal_rift"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false))
            .build();

    public static final RenderPipeline GUI_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/portal_rift_gui"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Alchemia.MODID, "core/portal_rift_gui"))
            .build();

    private static final RenderType INTERIOR = RenderType.create("alchemia_portal_rift", RenderSetup.builder(PIPELINE)
            .withTexture("Sampler0", STARS, () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST))
            .useLightmap()
            .createRenderSetup());
#endif

    private PortalShader() {
    }

#if MC_VER >= 26300
    public static TextureSetup guiStars() {
        return TextureSetup.singleTexture(Minecraft.getInstance().getTextureManager().getTexture(STARS).getTextureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
    }
#endif

    public static @Nullable RenderType interior() {
#if MC_VER >= 26300
        return INTERIOR;
#else
        return null;
#endif
    }

}
