package muddykat.alchemia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import muddykat.alchemia.common.blocks.helper.AlchemyMachineFlasks;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineUpgrade;
import muddykat.alchemia.common.utility.PotionStacks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AlchemyMachineUpgradeRenderer implements BlockEntityRenderer<TileEntityAlchemyMachineUpgrade> {

    private static final int ALPHA = 210;
    private static final float SURFACE_OFFSET = 0.004F;

    public AlchemyMachineUpgradeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityAlchemyMachineUpgrade blockEntity) {
        return true;
    }

    @Override
    public void render(TileEntityAlchemyMachineUpgrade blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        List<AlchemyMachineFlasks.Flask> flasks = AlchemyMachineFlasks.forSide(blockEntity.getSide());
        if (flasks.isEmpty()) return;

        TextureAtlasSprite sprite = BrewBaseTextures.sprite(BrewBaseTextures.WATER_SPRITE);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation(blockEntity.facing())));
        poseStack.translate(-0.5F, 0.0F, -0.5F);

        PoseStack.Pose pose = poseStack.last();
        int slots = Math.min(flasks.size(), blockEntity.slotCount());

        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = blockEntity.getStack(slot);
            int color = PotionStacks.isPotion(stack) ? PotionStacks.colorOf(stack) : 0;
            if (color == 0) continue;

            for (AlchemyMachineFlasks.Quad quad : flasks.get(slot).quads()) {
                emit(buffer, pose, quad, color, sprite, packedLight);
            }
        }

        poseStack.popPose();
    }

    private static void emit(VertexConsumer buffer, PoseStack.Pose pose, AlchemyMachineFlasks.Quad quad, int color,
                             TextureAtlasSprite sprite, int packedLight) {
        List<AlchemyMachineFlasks.Vertex> corners = quad.corners();
        float[] normal = normal(corners);
        float[] us = {sprite.getU0(), sprite.getU1(), sprite.getU1(), sprite.getU0()};
        float[] vs = {sprite.getV0(), sprite.getV0(), sprite.getV1(), sprite.getV1()};

        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;

        for (int i = 0; i < corners.size(); i++) {
            AlchemyMachineFlasks.Vertex corner = corners.get(i);
            buffer.vertex(pose.pose(),
                            corner.x() - normal[0] * SURFACE_OFFSET,
                            corner.y() - normal[1] * SURFACE_OFFSET,
                            corner.z() - normal[2] * SURFACE_OFFSET)
                    .color(red, green, blue, ALPHA)
                    .uv(us[i], vs[i])
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packedLight)
                    .normal(pose.normal(), normal[0], normal[1], normal[2])
                    .endVertex();
        }
    }

    private static float[] normal(List<AlchemyMachineFlasks.Vertex> corners) {
        AlchemyMachineFlasks.Vertex a = corners.get(0);
        AlchemyMachineFlasks.Vertex b = corners.get(1);
        AlchemyMachineFlasks.Vertex c = corners.get(2);
        float ax = b.x() - a.x(), ay = b.y() - a.y(), az = b.z() - a.z();
        float bx = c.x() - a.x(), by = c.y() - a.y(), bz = c.z() - a.z();
        float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        return length == 0 ? new float[] {0, 1, 0} : new float[] {nx / length, ny / length, nz / length};
    }

    private static float rotation(Direction facing) {
        return switch (facing) {
            case EAST -> -90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
    }
}
