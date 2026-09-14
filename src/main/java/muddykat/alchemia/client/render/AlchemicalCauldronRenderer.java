package muddykat.alchemia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.BrewBase;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class AlchemicalCauldronRenderer implements BlockEntityRenderer<TileEntityAlchemyCauldron> {

    private static final float[] FLUID_HEIGHT = {0.337f, 0.5625f, 0.75f, 0.9375f};
    private static final int ALPHA = 190;

    public AlchemicalCauldronRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEntityAlchemyCauldron blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int waterLevel = blockEntity.getWaterLevel();
        if (waterLevel <= 0) return;

        BrewBase base = blockEntity.getBase();
        TextureAtlasSprite water = BrewBaseTextures.stillSprite(base);

        int color = blockEntity.getPotionColor();
        if (color == 0) {
            color = base.biomeTint() && blockEntity.getLevel() != null
                    ? BiomeColors.getAverageWaterColor(blockEntity.getLevel(), blockEntity.getBlockPos())
                    : base.tintOrDefault();
        }

        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;

        int liquidLevel = Math.min(waterLevel, FLUID_HEIGHT.length) - 1;

        poseStack.pushPose();
        poseStack.translate(0, FLUID_HEIGHT[liquidLevel], 0);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();

        float sizeFactor = 0.05f;
        float maxV = (water.getV1() - water.getV0()) * sizeFactor;
        float minV = (water.getV1() - water.getV0()) * (1 - sizeFactor);

        quad(buffer, pose, sizeFactor, 1 - sizeFactor, water.getU0(), water.getV0() + maxV, red, green, blue, packedLight);
        quad(buffer, pose, 1 - sizeFactor, 1 - sizeFactor, water.getU1(), water.getV0() + maxV, red, green, blue, packedLight);
        quad(buffer, pose, 1 - sizeFactor, sizeFactor, water.getU1(), water.getV0() + minV, red, green, blue, packedLight);
        quad(buffer, pose, sizeFactor, sizeFactor, water.getU0(), water.getV0() + minV, red, green, blue, packedLight);

        poseStack.popPose();
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float x, float z, float u, float v,
                             int red, int green, int blue, int packedLight) {
        buffer.vertex(pose.pose(), x, 0, z)
                .color(red, green, blue, ALPHA)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }
}
