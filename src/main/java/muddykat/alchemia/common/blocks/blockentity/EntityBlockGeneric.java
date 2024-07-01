package muddykat.alchemia.common.blocks.blockentity;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public abstract class EntityBlockGeneric extends BaseEntityBlock {
    public EntityBlockGeneric(BlockBehaviour.Properties props) {
        super(props);
    }

    public EntityBlockGeneric() {
        this(Properties.of(Material.METAL));
    }
}
