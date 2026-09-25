package muddykat.alchemia.common.config;

import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;

public class Configuration {
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec SERVER_CONFIG;

    public static final int DEFAULT_MAX_POTENCY = 5;

    public static final HashMap<Ingredients, ForgeConfigSpec.IntValue> INGREDIENT_CONFIG = new HashMap<>();
    public static final ForgeConfigSpec.IntValue MAX_POTENCY;

    public static final ForgeConfigSpec.DoubleValue NOISE_SCALE;
    public static final ForgeConfigSpec.DoubleValue MIN_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue THRESHOLD_SPREAD;
    public static final ForgeConfigSpec.DoubleValue MAX_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue THRESHOLD_STEP;
    public static final ForgeConfigSpec.IntValue EFFECT_MARGIN;
    public static final ForgeConfigSpec.IntValue CENTRE_MARGIN;
    public static final ForgeConfigSpec.IntValue EFFECT_SPACING;
    public static final ForgeConfigSpec.IntValue MAX_CRYSTAL_GATED;
    public static final ForgeConfigSpec.IntValue RING_COUNT;
    public static final ForgeConfigSpec.IntValue RING_THICKNESS;
    public static final ForgeConfigSpec.IntValue RING_GATES;
    public static final ForgeConfigSpec.IntValue RING_GATE_DEGREES;
    public static final ForgeConfigSpec.DoubleValue RING_ROUGHNESS;
    public static final ForgeConfigSpec.DoubleValue FIELD_PATCH_SCALE;
    public static final ForgeConfigSpec.DoubleValue FIELD_CENTRE_CALM;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        for (Ingredients ingredient : Ingredients.values()) {
            INGREDIENT_CONFIG.put(ingredient, commonConfigSetup(ingredient, builder));
        }

        COMMON_CONFIG = builder.build();

        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
        serverBuilder.comment("Brewing rules").push("brewing");
        MAX_POTENCY = serverBuilder
                .comment("Total potency a single potion can hold before it spoils.",
                        "Each effect contributes its own potency, The default of 5 allows",
                        "five level 1 effects, or one level 3 alongside one level 2. and so forth.")
                .defineInRange("max_potency", DEFAULT_MAX_POTENCY, 1, 10);
        serverBuilder.pop();

        serverBuilder.comment("Shape of the generated potion map, measured in map pixels.",
                        "Each brew base builds its own map from the world seed mixed with the base id,",
                        "so effects and dead space sit in different places on every base.",
                        "Which effects a base can reach is set per base in " + BrewBaseConfig.FILE_NAME + ".")
                .push("map");
        NOISE_SCALE = serverBuilder
                .comment("Scale of the dead space noise, per map pixel.",
                        "Smaller values make larger, smoother blobs.")
                .defineInRange("deadzone_noise_scale_px", 0.007D, 0.0001D, 0.5D);
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
                .comment("Map pixels kept clear of dead space around every effect.",
                        "The clearing is a rough circle of roughly this radius, wobbled by the same noise.")
                .defineInRange("deadzone_effect_margin_px", 48, 0, 256);
        CENTRE_MARGIN = serverBuilder
                .comment("Map pixels kept clear of dead space around the starting position.")
                .defineInRange("deadzone_centre_margin_px", 96, 0, 512);
        EFFECT_SPACING = serverBuilder
                .comment("Minimum distance in map pixels between two effects when they are placed.")
                .defineInRange("effect_spacing_px", 96, 16, 512);
        MAX_CRYSTAL_GATED = serverBuilder
                .comment("How many effects may sit behind a crystal teleport before the map is regenerated.")
                .defineInRange("max_crystal_gated", 3, 0, 32);
        RING_COUNT = serverBuilder
                .comment("Rough rings of dead space that wall the map into layers.",
                        "Each ring sits between two rarity bands, so rarer effects live past a wall.")
                .defineInRange("ring_count", 3, 0, 4);
        RING_THICKNESS = serverBuilder
                .comment("Thickness of a ring wall in map pixels.")
                .defineInRange("ring_thickness", 24, 0, 128);
        RING_GATES = serverBuilder
                .comment("Gaps left in every ring so a brew can walk through without teleporting.")
                .defineInRange("ring_gates", 3, 1, 12);
        RING_GATE_DEGREES = serverBuilder
                .comment("Angular width of each gap, in degrees.")
                .defineInRange("ring_gate_degrees", 18, 2, 90);
        RING_ROUGHNESS = serverBuilder
                .comment("How much noise wobbles a ring's thickness. 0 draws perfect circles.")
                .defineInRange("ring_roughness", 0.55D, 0.0D, 1.0D);
        FIELD_PATCH_SCALE = serverBuilder
                .comment("Bases with a \"field\" dead space use patches this many times finer than the noise scale.",
                        "Larger values make smaller, more numerous patches.")
                .defineInRange("field_patch_scale", 4.0D, 1.0D, 16.0D);
        FIELD_CENTRE_CALM = serverBuilder
                .comment("How much calmer the middle of a field is than its rim.",
                        "The rim uses the cutoff above and the centre is raised by this much,",
                        "so patches thicken the further a brew travels. 0 spreads them evenly.")
                .defineInRange("field_centre_calm", 0.25D, 0.0D, 2.0D);
        serverBuilder.pop();

        SERVER_CONFIG = serverBuilder.build();
    }

    public static int maxPotency() {
        return SERVER_CONFIG.isLoaded() ? MAX_POTENCY.get() : DEFAULT_MAX_POTENCY;
    }

    public static double noiseScale() {
        return SERVER_CONFIG.isLoaded() ? NOISE_SCALE.get() : 0.007D;
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
        return SERVER_CONFIG.isLoaded() ? EFFECT_MARGIN.get() : 48;
    }

    public static int centreMargin() {
        return SERVER_CONFIG.isLoaded() ? CENTRE_MARGIN.get() : 96;
    }

    public static int effectSpacing() {
        return SERVER_CONFIG.isLoaded() ? EFFECT_SPACING.get() : 96;
    }

    public static int maxCrystalGated() {
        return SERVER_CONFIG.isLoaded() ? MAX_CRYSTAL_GATED.get() : 3;
    }

    public static int ringCount() {
        return SERVER_CONFIG.isLoaded() ? RING_COUNT.get() : 3;
    }

    public static int ringThickness() {
        return SERVER_CONFIG.isLoaded() ? RING_THICKNESS.get() : 24;
    }

    public static int ringGates() {
        return SERVER_CONFIG.isLoaded() ? RING_GATES.get() : 3;
    }

    public static int ringGateDegrees() {
        return SERVER_CONFIG.isLoaded() ? RING_GATE_DEGREES.get() : 18;
    }

    public static double ringRoughness() {
        return SERVER_CONFIG.isLoaded() ? RING_ROUGHNESS.get() : 0.55D;
    }

    public static double fieldPatchScale() {
        return SERVER_CONFIG.isLoaded() ? FIELD_PATCH_SCALE.get() : 4.0D;
    }

    public static double fieldCentreCalm() {
        return SERVER_CONFIG.isLoaded() ? FIELD_CENTRE_CALM.get() : 0.25D;
    }

    private static ForgeConfigSpec.IntValue commonConfigSetup(Ingredients ingredient, ForgeConfigSpec.Builder builder) {
        builder.comment("Wild " + ingredient.name() + " generation").push("wild_" + ingredient.name().toLowerCase());
        ForgeConfigSpec.IntValue chance = builder.comment("Chance of generating clusters. Smaller value = more frequent.",
                        "Minerals generate as geodes and are confined to a handful of biomes,",
                        "so the rarer a mineral's biome is, the denser its geodes are within it.")
                .defineInRange("chance", defaultChance(ingredient), 0, Integer.MAX_VALUE);
        builder.pop();

        return chance;
    }

    private static int defaultChance(Ingredients ingredient) {
        if (ingredient.getType() != IngredientType.Mineral) return 60;

        return switch (ingredient.getRarity()) {
            case EPIC -> 8;
            case RARE -> 16;
            default -> 24;
        };
    }
}
