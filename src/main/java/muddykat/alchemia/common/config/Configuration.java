package muddykat.alchemia.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Configuration {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.IntValue CHANCE_WILD_WINDBLOOM;
    public static ForgeConfigSpec.IntValue CHANCE_WILD_MARSHROOM;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        CHANCE_WILD_WINDBLOOM = commonConfigSetup("Windbloom", COMMON_BUILDER);
        CHANCE_WILD_MARSHROOM = commonConfigSetup("Marshroom", COMMON_BUILDER);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    private static ForgeConfigSpec.IntValue commonConfigSetup(String name, ForgeConfigSpec.Builder COMMON_BUILDER){
        COMMON_BUILDER.comment("Wild "+ name + " generation").push("wild_" + name.toLowerCase());
        ForgeConfigSpec.IntValue temp = COMMON_BUILDER.comment("Chance of generating clusters. Smaller value = more frequent.")
                .defineInRange("chance", 120, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        return temp;
    }
}
