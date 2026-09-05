package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.world.level.block.AmethystClusterBlock;

public class BlockMineralClusterGeneric extends AmethystClusterBlock {
    private final BlockRegistry.BudSize cluster_size;
    private final Ingredients ingredient;

    public BlockMineralClusterGeneric(Ingredients ingredient, float height, float width, BlockRegistry.BudSize size, Properties properties) {
        super(height, width, properties);
        this.ingredient = ingredient;
        this.cluster_size = size;
    }

    public BlockRegistry.BudSize getSize() {
        return cluster_size;
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
