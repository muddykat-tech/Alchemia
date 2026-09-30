package muddykat.alchemia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddykat.alchemia.common.blocks.helper.PotionCluster;
import muddykat.alchemia.common.blocks.helper.PotionLiquid;
import muddykat.alchemia.common.blocks.tileentity.TileEntityPlacedPotions;
import muddykat.alchemia.common.potion.PotionGlasses;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PlacedPotionsRenderer implements BlockEntityRenderer<TileEntityPlacedPotions> {

    private static final int ALPHA = 205;

    public PlacedPotionsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEntityPlacedPotions blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        List<ItemStack> contents = blockEntity.contents();
        int count = Math.min(contents.size(), PotionCluster.MAX_POTIONS);
        if (count == 0) return;

        TextureAtlasSprite sprite = BrewBaseTextures.sprite(BrewBaseTextures.WATER_SPRITE);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));

        for (int slot = 0; slot < count; slot++) {
            ItemStack vessel = contents.get(slot);
            if (!PotionGlasses.isPotion(vessel)) continue;

            int color = PotionGlasses.liquidColor(vessel);
            int[] offset = PotionCluster.offset(count, slot);

            poseStack.pushPose();
            poseStack.translate(offset[0] / 16.0F, 0.0F, offset[1] / 16.0F);
            PoseStack.Pose pose = poseStack.last();
            for (PotionLiquid.Face face : PotionLiquid.faces(PotionGlasses.of(vessel))) {
                emit(buffer, pose, face, color, sprite, packedLight);
            }
            poseStack.popPose();
        }
    }

    private static void emit(VertexConsumer buffer, PoseStack.Pose pose, PotionLiquid.Face face, int color,
                             TextureAtlasSprite sprite, int packedLight) {
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        float du = sprite.getU1() - sprite.getU0();
        float dv = sprite.getV1() - sprite.getV0();

        for (PotionLiquid.Corner corner : face.corners()) {
            buffer.vertex(pose.pose(), corner.x(), corner.y(), corner.z())
                    .color(red, green, blue, ALPHA)
                    .uv(sprite.getU0() + du * corner.u(), sprite.getV0() + dv * corner.v())
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packedLight)
                    .normal(pose.normal(), face.nx(), face.ny(), face.nz())
                    .endVertex();
        }
    }
}
