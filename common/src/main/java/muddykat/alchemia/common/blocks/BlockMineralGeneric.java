package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.world.level.block.AmethystBlock;

public class BlockMineralGeneric extends AmethystBlock {

    private final Ingredients ingredient;

    public BlockMineralGeneric(Ingredients ingredient, Properties properties) {
        super(properties);
        this.ingredient = ingredient;
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
