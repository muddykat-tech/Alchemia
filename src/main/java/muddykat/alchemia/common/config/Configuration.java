package muddykat.alchemia.common.config;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

@Mod.EventBusSubscriber
public class Configuration {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static HashMap<Ingredients, ForgeConfigSpec.IntValue> INGREDIENT_CONFIG = new HashMap<>();

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        for(Ingredients ingredient : Ingredients.values())
        {
            INGREDIENT_CONFIG.put(ingredient, commonConfigSetup(ingredient.name(), COMMON_BUILDER));
        }

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    private static ForgeConfigSpec.IntValue commonConfigSetup(String name, ForgeConfigSpec.Builder COMMON_BUILDER){
        COMMON_BUILDER.comment("Wild "+ name + " generation").push("wild_" + name.toLowerCase());
        ForgeConfigSpec.IntValue temp = COMMON_BUILDER.comment("Chance of generating clusters. Smaller value = more frequent.")
                .defineInRange("chance", 60, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        return temp;
    }
}
