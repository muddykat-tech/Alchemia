package muddykat.alchemia.common.config;

import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.PotionEnum;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Configuration {
    public static final ModConfigSpec COMMON_CONFIG;
    public static final ModConfigSpec SERVER_CONFIG;

    public static final int DEFAULT_MAX_POTENCY = 5;

    public static final HashMap<Ingredients, ModConfigSpec.IntValue> INGREDIENT_CONFIG = new HashMap<>();
    public static final ModConfigSpec.IntValue MAX_POTENCY;
    public static final Map<BrewBase, ModConfigSpec.ConfigValue<List<? extends String>>> BASE_EFFECTS = new EnumMap<>(BrewBase.class);

    public static final ModConfigSpec.DoubleValue NOISE_SCALE;
    public static final ModConfigSpec.DoubleValue MIN_THRESHOLD;
    public static final ModConfigSpec.DoubleValue THRESHOLD_SPREAD;
    public static final ModConfigSpec.DoubleValue MAX_THRESHOLD;
    public static final ModConfigSpec.DoubleValue THRESHOLD_STEP;
    public static final ModConfigSpec.IntValue EFFECT_MARGIN;
    public static final ModConfigSpec.IntValue CENTRE_MARGIN;
    public static final ModConfigSpec.IntValue EFFECT_SPACING;
    public static final ModConfigSpec.IntValue MAX_CRYSTAL_GATED;

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

        serverBuilder.comment("Which effects each brew base can reach on the potion map.",
                        "An effect left out of a base's list never spawns for brews made on that base.")
                .push("bases");
        for (BrewBase base : BrewBase.values()) {
            BASE_EFFECTS.put(base, serverBuilder
                    .comment("Effects reachable on a " + base.getSerializedName() + " base.")
                    .defineList(base.getSerializedName() + "_effects", Configuration::allEffectNames,
                            () -> PotionEnum.values()[0].name(), Configuration::isEffectName));
        }
        serverBuilder.pop();

        serverBuilder.comment("Shape of the generated potion map.",
                        "Each brew base builds its own map from the world seed mixed with the base id,",
                        "so effects and dead space sit in different places on every base.")
                .push("map");
        NOISE_SCALE = serverBuilder
                .comment("Scale of the dead space noise. Smaller values make larger, smoother blobs.")
                .defineInRange("deadzone_noise_scale", 0.11D, 0.01D, 1.0D);
        MIN_THRESHOLD = serverBuilder
                .comment("Lowest noise cutoff tried. Lower values mean more dead space.")
                .defineInRange("deadzone_min_threshold", 0.09D, -1.0D, 2.0D);
        THRESHOLD_SPREAD = serverBuilder
                .comment("Random amount added to the starting cutoff, so seeds differ in density.")
                .defineInRange("deadzone_threshold_spread", 0.22D, 0.0D, 2.0D);
        MAX_THRESHOLD = serverBuilder
                .comment("Cutoff at which generation gives up and produces no dead space at all.")
                .defineInRange("deadzone_max_threshold", 1.80D, 0.0D, 4.0D);
        THRESHOLD_STEP = serverBuilder
                .comment("Step used while relaxing the cutoff until the map is solvable.")
                .defineInRange("deadzone_threshold_step", 0.05D, 0.001D, 1.0D);
        EFFECT_MARGIN = serverBuilder
                .comment("Cells kept clear of dead space around every effect.")
                .defineInRange("deadzone_effect_margin", 2, 0, 16);
        CENTRE_MARGIN = serverBuilder
                .comment("Cells kept clear of dead space around the starting position.")
                .defineInRange("deadzone_centre_margin", 6, 0, 32);
        EFFECT_SPACING = serverBuilder
                .comment("Minimum distance between two effects when they are placed.")
                .defineInRange("effect_spacing", 6, 1, 32);
        MAX_CRYSTAL_GATED = serverBuilder
                .comment("How many effects may sit behind a crystal teleport before the map is regenerated.")
                .defineInRange("max_crystal_gated", 3, 0, 32);
        serverBuilder.pop();

        SERVER_CONFIG = serverBuilder.build();
    }

    public static int maxPotency() {
        return SERVER_CONFIG.isLoaded() ? MAX_POTENCY.get() : DEFAULT_MAX_POTENCY;
    }

    public static double noiseScale() {
        return SERVER_CONFIG.isLoaded() ? NOISE_SCALE.get() : 0.11D;
    }

    public static double minThreshold() {
        return SERVER_CONFIG.isLoaded() ? MIN_THRESHOLD.get() : 0.09D;
    }

    public static double thresholdSpread() {
        return SERVER_CONFIG.isLoaded() ? THRESHOLD_SPREAD.get() : 0.22D;
    }

    public static double maxThreshold() {
        return SERVER_CONFIG.isLoaded() ? MAX_THRESHOLD.get() : 1.80D;
    }

    public static double thresholdStep() {
        return SERVER_CONFIG.isLoaded() ? THRESHOLD_STEP.get() : 0.05D;
    }

    public static int effectMargin() {
        return SERVER_CONFIG.isLoaded() ? EFFECT_MARGIN.get() : 2;
    }

    public static int centreMargin() {
        return SERVER_CONFIG.isLoaded() ? CENTRE_MARGIN.get() : 6;
    }

    public static int effectSpacing() {
        return SERVER_CONFIG.isLoaded() ? EFFECT_SPACING.get() : 6;
    }

    public static int maxCrystalGated() {
        return SERVER_CONFIG.isLoaded() ? MAX_CRYSTAL_GATED.get() : 3;
    }

    private static List<String> allEffectNames() {
        return java.util.Arrays.stream(PotionEnum.values()).map(Enum::name).collect(Collectors.toList());
    }

    private static boolean isEffectName(Object value) {
        if (!(value instanceof String name)) return false;
        for (PotionEnum effect : PotionEnum.values()) {
            if (effect.name().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private static final Map<BrewBase, Set<String>> BASE_EFFECT_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean baseAllowsEffect(BrewBase base, PotionEnum effect) {
        ModConfigSpec.ConfigValue<List<? extends String>> config = BASE_EFFECTS.get(base);
        if (config == null || !SERVER_CONFIG.isLoaded()) return true;

        Set<String> allowed = BASE_EFFECT_CACHE.computeIfAbsent(base, key -> config.get().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet()));
        return allowed.contains(effect.name().toLowerCase(Locale.ROOT));
    }

    public static void clearBaseEffectCache() {
        BASE_EFFECT_CACHE.clear();
    }

    private static ModConfigSpec.IntValue commonConfigSetup(String name, ModConfigSpec.Builder builder) {
        builder.comment("Wild " + name + " generation").push("wild_" + name.toLowerCase());
        ModConfigSpec.IntValue chance = builder.comment("Chance of generating clusters. Smaller value = more frequent.")
                .defineInRange("chance", 60, 0, Integer.MAX_VALUE);
        builder.pop();

        return chance;
    }
}
