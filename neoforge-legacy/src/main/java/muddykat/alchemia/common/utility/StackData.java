package muddykat.alchemia.common.utility;

import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.PotionGlass;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Map;

public class StackData {
    public static final StackComponent<List<String>> DISCOVERED_RECIPES = StackComponent.of(DataComponentRegistry.DISCOVERED_RECIPES);
    public static final StackComponent<List<String>> DISCOVERED_INGREDIENTS = StackComponent.of(DataComponentRegistry.DISCOVERED_INGREDIENTS);
    public static final StackComponent<List<BrewRecipe>> BREW_RECIPES = StackComponent.of(DataComponentRegistry.BREW_RECIPES);
    public static final StackComponent<Integer> CRUSH = StackComponent.of(DataComponentRegistry.CRUSH);
    public static final StackComponent<Integer> PATH_TRIM = StackComponent.of(DataComponentRegistry.PATH_TRIM);
    public static final StackComponent<Integer> PATH_ROTATION = StackComponent.of(DataComponentRegistry.PATH_ROTATION);
    public static final StackComponent<Integer> CHARGES = StackComponent.of(DataComponentRegistry.CHARGES);
    public static final StackComponent<List<String>> NOTED_EFFECTS = StackComponent.of(DataComponentRegistry.NOTED_EFFECTS);
    public static final StackComponent<String> SELECTED_RECIPE = StackComponent.of(DataComponentRegistry.SELECTED_RECIPE);
    public static final StackComponent<PotionGlass> POTION_GLASS = StackComponent.of(DataComponentRegistry.POTION_GLASS);
    public static final StackComponent<List<String>> GREAT_WORK = StackComponent.of(DataComponentRegistry.GREAT_WORK);
    public static final StackComponent<Integer> GUIDE_PAGES = StackComponent.of(DataComponentRegistry.GUIDE_PAGES);
    public static final StackComponent<BrewRecipe> PAGE_RECIPE = StackComponent.of(DataComponentRegistry.PAGE_RECIPE);
    public static final StackComponent<Map<String, String>> INGREDIENT_NOTES = StackComponent.of(DataComponentRegistry.INGREDIENT_NOTES);
    public static final StackComponent<ResourceKey<Biome>> BARK_BIOME = StackComponent.of(DataComponentRegistry.BARK_BIOME);
}
