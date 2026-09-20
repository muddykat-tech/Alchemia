package muddykat.alchemia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.BrewBase;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AlchemicalCauldronRenderer implements BlockEntityRenderer<TileEntityAlchemyCauldron, AlchemicalCauldronRenderState> {

    private static final float[] FLUID_HEIGHT = {0.337f, 0.5625f, 0.75f, 0.9375f};
    private static final int ALPHA = 190;

    private final SpriteGetter sprites;

    public AlchemicalCauldronRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public AlchemicalCauldronRenderState createRenderState() {
        return new AlchemicalCauldronRenderState();
    }

    @Override
    public void extractRenderState(TileEntityAlchemyCauldron blockEntity, AlchemicalCauldronRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        BrewBase base = blockEntity.getBase();
        state.waterLevel = blockEntity.getWaterLevel();
        state.fluidSprite = BrewBaseTextures.stillSprite(base, sprites);

        int color = blockEntity.getPotionColor();
        if (color == 0) {
            color = base.biomeTint() && blockEntity.getLevel() != null
                    ? BiomeColors.getAverageWaterColor((BlockAndTintGetter) blockEntity.getLevel(), blockEntity.getBlockPos())
                    : base.tintOrDefault();
        }
        state.potionColor = color;
    }

    @Override
    public void submit(AlchemicalCauldronRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.waterLevel <= 0) return;

        int red = (state.potionColor >> 16) & 255;
        int green = (state.potionColor >> 8) & 255;
        int blue = state.potionColor & 255;
        int packedColor = (ALPHA << 24) | (red << 16) | (green << 8) | blue;

        TextureAtlasSprite water = state.fluidSprite != null ? state.fluidSprite : sprites.get(BrewBaseTextures.WATER_SPRITE);
        int liquidLevel = Math.min(state.waterLevel, FLUID_HEIGHT.length) - 1;
        int lightCoords = state.lightCoords;

        poseStack.pushPose();
        poseStack.translate(0, FLUID_HEIGHT[liquidLevel], 0);

        RenderType renderType = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);

        collector.order(0).submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            float sizeFactor = 0.05f;
            float maxV = (water.getV1() - water.getV0()) * sizeFactor;
            float minV = (water.getV1() - water.getV0()) * (1 - sizeFactor);

            buffer.addVertex(pose, sizeFactor, 0, 1 - sizeFactor).setColor(packedColor).setUv(water.getU0(), water.getV0() + maxV)
                    .setLight(lightCoords).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, 1 - sizeFactor, 0, 1 - sizeFactor).setColor(packedColor).setUv(water.getU1(), water.getV0() + maxV)
                    .setLight(lightCoords).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, 1 - sizeFactor, 0, sizeFactor).setColor(packedColor).setUv(water.getU1(), water.getV0() + minV)
                    .setLight(lightCoords).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, sizeFactor, 0, sizeFactor).setColor(packedColor).setUv(water.getU0(), water.getV0() + minV)
                    .setLight(lightCoords).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        });

        poseStack.popPose();
    }
}
