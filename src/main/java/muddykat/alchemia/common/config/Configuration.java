package muddykat.alchemia.common.config;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;

public class Configuration {
    public static final ModConfigSpec COMMON_CONFIG;
    public static final ModConfigSpec SERVER_CONFIG;

    public static final int DEFAULT_MAX_POTENCY = 5;

    public static final HashMap<Ingredients, ModConfigSpec.IntValue> INGREDIENT_CONFIG = new HashMap<>();
    public static final ModConfigSpec.IntValue MAX_POTENCY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        for (Ingredients ingredient : Ingredients.values()) {
            INGREDIENT_CONFIG.put(ingredient, commonConfigSetup(ingredient.name(), builder));
        }

        COMMON_CONFIG = builder.build();

        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();
        serverBuilder.comment("Brewing rules").push("brewing");
        MAX_POTENCY = serverBuilder
                .comment("Total potency a single potion can hold before it spoils.",
                        "Each effect contributes its own potency, The default of 5 allows",
                        "five level 1 effects, or one level 3 alongside one level 2. and so forth.")
                .defineInRange("max_potency", DEFAULT_MAX_POTENCY, 1, 10);
        serverBuilder.pop();

        SERVER_CONFIG = serverBuilder.build();
    }

    public static int maxPotency() {
        return SERVER_CONFIG.isLoaded() ? MAX_POTENCY.get() : DEFAULT_MAX_POTENCY;
    }

    private static ModConfigSpec.IntValue commonConfigSetup(String name, ModConfigSpec.Builder builder) {
        builder.comment("Wild " + name + " generation").push("wild_" + name.toLowerCase());
        ModConfigSpec.IntValue chance = builder.comment("Chance of generating clusters. Smaller value = more frequent.")
                .defineInRange("chance", 60, 0, Integer.MAX_VALUE);
        builder.pop();

        return chance;
    }
}
