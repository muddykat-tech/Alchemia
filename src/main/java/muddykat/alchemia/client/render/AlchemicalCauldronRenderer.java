package muddykat.alchemia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AlchemicalCauldronRenderer implements BlockEntityRenderer<TileEntityAlchemyCauldron> {


    public AlchemicalCauldronRenderer(BlockEntityRendererProvider.Context context){

    }

    @Override
    public void render(TileEntityAlchemyCauldron pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

    }
}
