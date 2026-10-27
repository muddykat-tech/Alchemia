package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import javax.annotation.Nullable;
import java.io.IOException;

public final class PortalShader {

    public static final ResourceLocation STARS = ResourceLocation.withDefaultNamespace("textures/entity/end_portal.png");

    private static @Nullable ShaderInstance world;
    private static @Nullable ShaderInstance gui;

    private static final RenderType INTERIOR = RenderType.create("alchemia_portal_rift", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS, 1536, false, true, RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> world))
                    .setTextureState(new RenderStateShard.TextureStateShard(STARS, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private PortalShader() {
    }

    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "portal_rift"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), loaded -> world = loaded);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "portal_rift_gui"),
                DefaultVertexFormat.POSITION_TEX_COLOR), loaded -> gui = loaded);
    }

    public static @Nullable ShaderInstance gui() {
        return gui;
    }

    public static @Nullable RenderType interior() {
        return world == null ? null : INTERIOR;
    }
}
