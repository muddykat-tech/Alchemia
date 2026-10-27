package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.systems.RenderSystem;
#if MC_VER >= 26300
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
#else
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.FilterMode;
#endif
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class PortalShader {

    public static final Identifier STARS = Identifier.withDefaultNamespace("textures/entity/end_portal/end_portal.png");

#if MC_VER >= 26200
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.WORLD_TEXT_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/portal_rift"))
#if MC_VER < 26300
            .withVertexShader("core/text")
#endif
            .withFragmentShader(Identifier.fromNamespaceAndPath(Alchemia.MODID, "core/portal_rift"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false))
            .build();

    public static final RenderPipeline GUI_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/portal_rift_gui"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Alchemia.MODID, "core/portal_rift_gui"))
            .build();
#else
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET,
                    RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/portal_rift"))
            .withVertexShader("core/rendertype_text")
            .withFragmentShader(Identifier.fromNamespaceAndPath(Alchemia.MODID, "core/portal_rift"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false))
            .build();

    public static final RenderPipeline GUI_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/portal_rift_gui"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Alchemia.MODID, "core/portal_rift_gui"))
            .build();
#endif

    private static final RenderType INTERIOR = RenderType.create("alchemia_portal_rift", RenderSetup.builder(PIPELINE)
            .withTexture("Sampler0", STARS, () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST))
            .useLightmap()
            .createRenderSetup());

    private PortalShader() {
    }

    public static TextureSetup guiStars() {
        return TextureSetup.singleTexture(Minecraft.getInstance().getTextureManager().getTexture(STARS).getTextureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
    }

    public static @Nullable RenderType interior() {
        return INTERIOR;
    }
}
