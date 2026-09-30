package muddykat.alchemia.common.utility;

import com.mojang.serialization.Codec;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.PotionGlass;

import java.util.List;

public class StackData {
    public static final StackComponent<List<String>> DISCOVERED_RECIPES =
            StackComponent.of("discovered_recipes", Codec.STRING.listOf());

    public static final StackComponent<List<String>> DISCOVERED_INGREDIENTS =
            StackComponent.of("discovered_ingredients", Codec.STRING.listOf());

    public static final StackComponent<List<BrewRecipe>> BREW_RECIPES =
            StackComponent.of("brew_recipes", BrewRecipe.CODEC.listOf());

    public static final StackComponent<Integer> CRUSH =
            StackComponent.of("crush", Codec.intRange(0, Ingredients.MAX_CRUSH_UNITS));

    public static final StackComponent<Integer> PATH_TRIM =
            StackComponent.of("path_trim", Codec.intRange(0, 64));

    public static final StackComponent<Integer> PATH_ROTATION =
            StackComponent.of("path_rotation", Codec.intRange(0, 7));

    public static final StackComponent<Integer> CHARGES =
            StackComponent.of("charges", Codec.intRange(0, 64));

    public static final StackComponent<String> SELECTED_RECIPE =
            StackComponent.of("selected_recipe", Codec.STRING);

    public static final StackComponent<PotionGlass> POTION_GLASS =
            StackComponent.of("potion_glass", PotionGlass.CODEC);
}
