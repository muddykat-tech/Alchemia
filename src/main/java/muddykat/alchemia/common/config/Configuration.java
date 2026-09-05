package muddykat.alchemia.common.config;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;

public class Configuration {
    public static final ModConfigSpec COMMON_CONFIG;

    public static final HashMap<Ingredients, ModConfigSpec.IntValue> INGREDIENT_CONFIG = new HashMap<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        for (Ingredients ingredient : Ingredients.values()) {
            INGREDIENT_CONFIG.put(ingredient, commonConfigSetup(ingredient.name(), builder));
        }

        COMMON_CONFIG = builder.build();
    }

    private static ModConfigSpec.IntValue commonConfigSetup(String name, ModConfigSpec.Builder builder) {
        builder.comment("Wild " + name + " generation").push("wild_" + name.toLowerCase());
        ModConfigSpec.IntValue chance = builder.comment("Chance of generating clusters. Smaller value = more frequent.")
                .defineInRange("chance", 60, 0, Integer.MAX_VALUE);
        builder.pop();

        return chance;
    }
}
