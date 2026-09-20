package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemIngredientSeed extends BlockItem {

    private final Ingredients ingredient;
    private final IngredientType ingredientType;
    private final IngredientAlignment primaryAlignment;

    public ItemIngredientSeed(Block block, Ingredients ingredient, IngredientType type, IngredientAlignment alignment, Properties properties) {
        super(block, properties);
        this.ingredientType = type;
        this.primaryAlignment = alignment;
        this.ingredient = ingredient;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.alchemia." + ingredientType.name().toLowerCase(), Component.translatable("item.alchemia." + ingredient.getRegistryName()));
    }

    public IngredientType getIngredientType() {
        return this.ingredientType;
    }

    public IngredientAlignment getPrimaryAlignment() {
        return this.primaryAlignment;
    }

    public Identifier getTextureLocation() {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + this.ingredientType.name().toLowerCase() + "s/" + ingredient.name().toLowerCase() + "_seed");
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
