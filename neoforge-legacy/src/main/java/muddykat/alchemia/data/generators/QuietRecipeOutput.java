package muddykat.alchemia.data.generators;

import muddykat.alchemia.common.crafting.QuietShapelessRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;

public record QuietRecipeOutput(RecipeOutput output) implements RecipeOutput {

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        output.accept(id, recipe instanceof ShapelessRecipe shapeless ? new QuietShapelessRecipe(shapeless) : recipe, advancement, conditions);
    }

    @Override
    public Advancement.Builder advancement() {
        return output.advancement();
    }
}
