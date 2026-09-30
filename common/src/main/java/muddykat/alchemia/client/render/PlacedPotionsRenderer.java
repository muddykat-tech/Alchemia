package muddykat.alchemia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddykat.alchemia.common.blocks.helper.PotionCluster;
import muddykat.alchemia.common.blocks.helper.PotionLiquid;
import muddykat.alchemia.common.blocks.tileentity.TileEntityPlacedPotions;
import muddykat.alchemia.common.potion.PotionGlasses;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class PlacedPotionsRenderer implements BlockEntityRenderer<TileEntityPlacedPotions, PlacedPotionsRenderState> {

    private static final int ALPHA = 205;

    private final SpriteGetter sprites;

    public PlacedPotionsRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public PlacedPotionsRenderState createRenderState() {
        return new PlacedPotionsRenderState();
    }

    @Override
    public void extractRenderState(TileEntityPlacedPotions blockEntity, PlacedPotionsRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        List<ItemStack> contents = blockEntity.contents();
        state.count = Math.min(contents.size(), PotionCluster.MAX_POTIONS);
        for (int slot = 0; slot < state.count; slot++) {
            ItemStack vessel = contents.get(slot);
            state.glasses[slot] = PotionGlasses.of(vessel);
            state.filled[slot] = PotionGlasses.isPotion(vessel);
            state.colors[slot] = state.filled[slot] ? PotionGlasses.liquidColor(vessel) : 0;
        }
    }

    @Override
    public void submit(PlacedPotionsRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.count == 0) return;

        TextureAtlasSprite sprite = sprites.get(BrewBaseTextures.WATER_SPRITE);
        RenderType renderType = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
        int lightCoords = state.lightCoords;

        for (int slot = 0; slot < state.count; slot++) {
            if (!state.filled[slot]) continue;

            int packed = (ALPHA << 24) | (state.colors[slot] & 0xFFFFFF);
            int[] offset = PotionCluster.offset(state.count, slot);
            List<PotionLiquid.Face> faces = PotionLiquid.faces(state.glasses[slot]);

            poseStack.pushPose();
            poseStack.translate(offset[0] / 16.0F, 0.0F, offset[1] / 16.0F);
            collector.order(0).submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
                for (PotionLiquid.Face face : faces) emit(buffer, pose, face, packed, sprite, lightCoords);
            });
            poseStack.popPose();
        }
    }

    private static void emit(VertexConsumer buffer, PoseStack.Pose pose, PotionLiquid.Face face, int packed,
                             TextureAtlasSprite sprite, int lightCoords) {
        float du = sprite.getU1() - sprite.getU0();
        float dv = sprite.getV1() - sprite.getV0();
        for (PotionLiquid.Corner corner : face.corners()) {
            buffer.addVertex(pose, corner.x(), corner.y(), corner.z())
                    .setColor(packed)
                    .setUv(sprite.getU0() + du * corner.u(), sprite.getV0() + dv * corner.v())
                    .setLight(lightCoords)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(pose, face.nx(), face.ny(), face.nz());
        }
    }
}
