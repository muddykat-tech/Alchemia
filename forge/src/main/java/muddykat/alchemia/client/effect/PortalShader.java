package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;

import javax.annotation.Nullable;
import java.io.IOException;

public final class PortalShader {

    public static final ResourceLocation STARS = new ResourceLocation("textures/entity/end_portal.png");

    private static @Nullable ShaderInstance world;
    private static @Nullable ShaderInstance gui;

    private static final RenderType INTERIOR = Types.interior();

    private PortalShader() {
    }

    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Alchemia.MODID, "portal_rift"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), loaded -> world = loaded);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Alchemia.MODID, "portal_rift_gui"),
                DefaultVertexFormat.POSITION_TEX_COLOR), loaded -> gui = loaded);
    }

    public static @Nullable ShaderInstance gui() {
        return gui;
    }

    public static @Nullable RenderType interior() {
        return world == null ? null : INTERIOR;
    }

    private static final class Types extends RenderType {

        private Types() {
            super("", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 0, false, false, () -> {
            }, () -> {
            });
        }

        private static RenderType interior() {
            return create("alchemia_portal_rift", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                    VertexFormat.Mode.QUADS, 1536, false, true, CompositeState.builder()
                            .setShaderState(new ShaderStateShard(() -> world))
                            .setTextureState(new TextureStateShard(STARS, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(LIGHTMAP)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false));
        }
    }
}
