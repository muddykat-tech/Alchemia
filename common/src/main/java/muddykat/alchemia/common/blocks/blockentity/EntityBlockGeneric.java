package muddykat.alchemia.common.blocks.blockentity;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class EntityBlockGeneric extends BaseEntityBlock {
    public EntityBlockGeneric(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
