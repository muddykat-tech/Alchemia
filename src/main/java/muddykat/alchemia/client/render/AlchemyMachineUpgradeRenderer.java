package muddykat.alchemia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import muddykat.alchemia.common.blocks.helper.AlchemyMachineFlasks;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineUpgrade;
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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import muddykat.alchemia.common.utility.PotionStacks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AlchemyMachineUpgradeRenderer implements BlockEntityRenderer<TileEntityAlchemyMachineUpgrade, AlchemyMachineUpgradeRenderState> {

    private static final int ALPHA = 210;
    private static final float SURFACE_OFFSET = 0.004F;

    private final SpriteGetter sprites;

    public AlchemyMachineUpgradeRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AlchemyMachineUpgradeRenderState createRenderState() {
        return new AlchemyMachineUpgradeRenderState();
    }

    @Override
    public void extractRenderState(TileEntityAlchemyMachineUpgrade blockEntity, AlchemyMachineUpgradeRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.facing = blockEntity.facing();
        state.flasks = AlchemyMachineFlasks.forSide(blockEntity.getSide());

        int slots = blockEntity.slotCount();
        if (state.potionColors.length != slots) state.potionColors = new int[slots];

        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = blockEntity.getStack(slot);
            state.potionColors[slot] = PotionStacks.isPotion(stack) ? PotionStacks.colorOf(stack) : 0;
        }
    }

    @Override
    public void submit(AlchemyMachineUpgradeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.flasks.isEmpty()) return;

        TextureAtlasSprite sprite = sprites.get(BrewBaseTextures.WATER_SPRITE);
        RenderType renderType = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
        int lightCoords = state.lightCoords;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation(state.facing)));
        poseStack.translate(-0.5F, 0.0F, -0.5F);

        for (int slot = 0; slot < state.flasks.size() && slot < state.potionColors.length; slot++) {
            int color = state.potionColors[slot];
            if (color == 0) continue;

            int packed = (ALPHA << 24) | (color & 0xFFFFFF);
            List<AlchemyMachineFlasks.Quad> quads = state.flasks.get(slot).quads();

            collector.order(0).submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
                for (AlchemyMachineFlasks.Quad quad : quads) {
                    emit(buffer, pose, quad, packed, sprite, lightCoords);
                }
            });
        }

        poseStack.popPose();
    }

    private static void emit(VertexConsumer buffer, PoseStack.Pose pose, AlchemyMachineFlasks.Quad quad, int packed,
                             TextureAtlasSprite sprite, int lightCoords) {
        List<AlchemyMachineFlasks.Vertex> corners = quad.corners();
        float[] normal = normal(corners);
        float[] us = {sprite.getU0(), sprite.getU1(), sprite.getU1(), sprite.getU0()};
        float[] vs = {sprite.getV0(), sprite.getV0(), sprite.getV1(), sprite.getV1()};

        for (int i = 0; i < corners.size(); i++) {
            AlchemyMachineFlasks.Vertex corner = corners.get(i);
            buffer.addVertex(pose,
                            corner.x() - normal[0] * SURFACE_OFFSET,
                            corner.y() - normal[1] * SURFACE_OFFSET,
                            corner.z() - normal[2] * SURFACE_OFFSET)
                    .setColor(packed)
                    .setUv(us[i], vs[i])
                    .setLight(lightCoords)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(pose, normal[0], normal[1], normal[2]);
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
