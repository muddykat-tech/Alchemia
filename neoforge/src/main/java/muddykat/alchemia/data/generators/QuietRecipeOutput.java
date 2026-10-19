package muddykat.alchemia.data.generators;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

public record QuietRecipeOutput(RecipeOutput output) implements RecipeOutput {

    @Override
    public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        output.accept(id, quiet(recipe), advancement, conditions);
    }

    @Override
    public Advancement.Builder advancement() {
        return output.advancement();
    }

#if MC_VER >= 26300
    @Override
    public <S> net.minecraft.core.HolderGetter<S> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends S>> registry) {
        return output.lookup(registry);
    }

    @Override
    public <S> java.util.stream.Stream<net.minecraft.core.Holder.Reference<S>> listContextElements(
            ResourceKey<? extends net.minecraft.core.Registry<? extends S>> registry) {
        return output.listContextElements(registry);
    }
#else
    @Override
    public void includeRootAdvancement() {
        output.includeRootAdvancement();
    }
#endif

    private static Recipe<?> quiet(Recipe<?> recipe) {
        if (recipe instanceof ShapelessRecipe shapeless && shapeless.showNotification()) {
            return new ShapelessRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(shapeless.category(), shapeless.group()),
                    shapeless.result(), shapeless.placementInfo().ingredients());
        }
        return recipe;
    }
}
