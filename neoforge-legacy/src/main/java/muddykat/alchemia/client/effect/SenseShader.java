package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class SenseShader {

    private static final RenderType SEE_THROUGH = Types.seeThrough();
    private static final Function<ResourceLocation, RenderType> GLOW = Util.memoize(Types::glow);

    private SenseShader() {
    }

    public static RenderType seeThrough() {
        return SEE_THROUGH;
    }

    public static RenderType glow(ResourceLocation texture) {
        return GLOW.apply(texture);
    }

    public static void vertex(VertexConsumer vertex, int colour, float u, float v) {
        vertex.setColor(colour);
    }

    private static final class Types extends RenderType {

        private static final LayeringStateShard THROUGH_WALLS = new LayeringStateShard("alchemia_through_walls",
                RenderSystem::disableDepthTest, RenderSystem::enableDepthTest);

        private Types() {
            super("", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 0, false, false, () -> {
            }, () -> {
            });
        }

        private static RenderType seeThrough() {
            return create("alchemia_sense_see_through", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, true,
                    CompositeState.builder()
                            .setShaderState(POSITION_COLOR_SHADER)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setDepthTestState(NO_DEPTH_TEST)
                            .setLayeringState(THROUGH_WALLS)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false));
        }

        private static RenderType glow(ResourceLocation texture) {
            return create("alchemia_sense_glow", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 1536, false, true,
                    CompositeState.builder()
                            .setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setLightmapState(LIGHTMAP)
                            .setDepthTestState(NO_DEPTH_TEST)
                            .setLayeringState(THROUGH_WALLS)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false));
        }
    }
}
