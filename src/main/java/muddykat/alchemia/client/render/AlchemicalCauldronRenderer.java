package muddykat.alchemia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class AlchemicalCauldronRenderer implements BlockEntityRenderer<TileEntityAlchemyCauldron> {


    public AlchemicalCauldronRenderer(BlockEntityRendererProvider.Context context){

    }

    private static final float[] FLUID_HEIGHT = { 0, 0.5625f, 0.75f, 0.9375f };
    private static final Material WATER_MATERIAL = new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE,"block/water_still"));

    @Override
    public void render(TileEntityAlchemyCauldron pBlockEntity, float pPartialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if(pBlockEntity.getPotion() == null || pBlockEntity.getEffectList().stream().findFirst().isEmpty()) return;

        int color = pBlockEntity.getEffectList().stream().findFirst().get().getEffect().getColor();
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        int alpha = 190;

        int liquidLevel = pBlockEntity.getWaterLevel() - 1;

        TextureAtlasSprite water = WATER_MATERIAL.sprite();

        poseStack.pushPose();
        poseStack.translate(0, FLUID_HEIGHT[liquidLevel % 4], 0);

        VertexConsumer consumer = buffer.getBuffer(RenderType.translucentMovingBlock());
        Matrix4f matrix = poseStack.last().pose();

        float sizeFactor = 0.125f;
        float maxV = (water.getV1() - water.getV0()) * sizeFactor;
        float minV = (water.getV1() - water.getV0()) * (1 - sizeFactor);

        consumer.vertex(matrix, sizeFactor, 0, 1 - sizeFactor).color(red, green, blue, alpha).uv(water.getU0(), water.getV0() + maxV).uv2(packedLight).overlayCoords(packedOverlay).normal(1, 1, 1);
        consumer.vertex(matrix, 1 - sizeFactor, 0, 1 - sizeFactor).color(red, green, blue, alpha).uv(water.getU1(), water.getV0() + maxV).uv2(packedLight).overlayCoords(packedOverlay).normal(1, 1, 1);
        consumer.vertex(matrix, 1 - sizeFactor, 0, sizeFactor).color(red, green, blue, alpha).uv(water.getU1(), water.getV0() + minV).uv2(packedLight).overlayCoords(packedOverlay).normal(1, 1, 1);
        consumer.vertex(matrix, sizeFactor, 0, sizeFactor).color(red, green, blue, alpha).uv(water.getU0(), water.getV0() + minV).uv2(packedLight).overlayCoords(packedOverlay).normal(1, 1, 1);

        poseStack.popPose();
    }
}
