package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Properties;

public class ItemIngredient extends BlockItem {

    private final Ingredients ingredient;
    private final IngredientType ingredientType;
    private final IngredientAlignment primaryAlignment;
    private IngredientAlignment secondaryAlignment;
    public ItemIngredient(Block block, Ingredients ingredient, IngredientType type, IngredientAlignment alignment) {
        super(block, new Properties().tab(CreativeModeTab.TAB_BREWING).food(new FoodProperties.Builder().nutrition(2).alwaysEat().saturationMod(2).build()));
        this.ingredientType = type;
        this.primaryAlignment = alignment;
        this.ingredient = ingredient;
    }

    public ItemIngredient(Block block, Ingredients ingredient, IngredientType type, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment){
        this(block, ingredient, type, primaryAlignment);
        this.secondaryAlignment = secondaryAlignment;
    }

    public IngredientType getIngredientType() {
        return this.ingredientType;
    }

    public IngredientAlignment getPrimaryAlignment(){
        return this.primaryAlignment;
    }

    public IngredientAlignment getSecondaryAlignment() throws NullPointerException {
        return this.secondaryAlignment;
    }

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(Alchemia.MODID, "item/" + this.ingredientType.name().toLowerCase() + "s/" + ingredient.name().toLowerCase());
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
