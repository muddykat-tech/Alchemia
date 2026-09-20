package muddykat.alchemia.common.blocks.blockentity;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class EntityBlockGeneric extends BaseEntityBlock {
    public EntityBlockGeneric(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
