package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemIngredientCrushed extends ItemIngredient {
    public ItemIngredientCrushed(Ingredients ingredient, IngredientType type, IngredientAlignment alignment) {
        super(ingredient, type, alignment);
    }

    @Override
    public Component getName(ItemStack pStack) {

        return new TranslatableComponent("item.alchemia.crushed", I18n.get("item.alchemia." + ingredient.getRegistryName()));
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(Alchemia.MODID, "item/" + this.getIngredientType().name().toLowerCase() + "s/" + ingredient.name().toLowerCase() + "_crushed");
    }

}
