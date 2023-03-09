package muddykat.alchemia.common.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class BlockGeneric extends Block {
    public BlockGeneric() {
        super(Properties.copy(Blocks.CAULDRON));
    }
}
