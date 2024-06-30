package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;

public class BlockMineralClusterGeneric extends AmethystClusterBlock {
    private final BlockRegistry.BudSize cluster_size;

    public BlockMineralClusterGeneric(Ingredients ingredient, int var1, int var2, BlockRegistry.BudSize size)
    {
        super(var1, var2, Properties.copy(Blocks.AMETHYST_CLUSTER));
        this.ingredient = ingredient;
        this.cluster_size = size;
    }
    
    private final Ingredients ingredient;

    public BlockRegistry.BudSize getSize()
    {
        return cluster_size;
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
