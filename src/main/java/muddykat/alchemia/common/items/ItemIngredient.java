package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemIngredient extends Item {

    protected final Ingredients ingredient;
    protected final IngredientType ingredientType;
    protected final IngredientAlignment primaryAlignment;
    protected IngredientAlignment secondaryAlignment;
    public ItemIngredient(Ingredients ingredient, IngredientType type, IngredientAlignment alignment) {
        super(new Properties().tab(Alchemia.ITEM_GROUP).food(ingredient.getFoodProperties()));
        this.ingredientType = type;
        this.primaryAlignment = alignment;
        this.ingredient = ingredient;
    }

    public ItemIngredient(Ingredients ingredient, IngredientType type, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment){
        this(ingredient, type, primaryAlignment);
        this.secondaryAlignment = secondaryAlignment;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(new TextComponent("Potency: " + (ingredient.getIngredientStrength() * (this instanceof ItemIngredientCrushed ? ingredient.getCrushedPotency() : 1))));
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
