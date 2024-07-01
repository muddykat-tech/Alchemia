package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.Blocks;

public class BlockMineralGeneric extends AmethystBlock {

    public BlockMineralGeneric(Ingredients ingredient)
    {
        super(Properties.copy(Blocks.AMETHYST_BLOCK));
        this.ingredient = ingredient;
    }

    private final Ingredients ingredient;

    public Ingredients getIngredient() {
        return ingredient;
    }
}
