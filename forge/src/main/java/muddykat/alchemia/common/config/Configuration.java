package muddykat.alchemia.common.config;

import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.List;

public class Configuration {
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec CLIENT_CONFIG;
    public static final ForgeConfigSpec.BooleanValue FOG_OF_WAR;
    public static final ForgeConfigSpec.BooleanValue TREASURE_HIGHLIGHTS;
    public static final ForgeConfigSpec.BooleanValue DANGER_HIGHLIGHTS;

    public static final int DEFAULT_MAX_POTENCY = 5;

    public static final HashMap<Ingredients, ForgeConfigSpec.IntValue> INGREDIENT_CONFIG = new HashMap<>();
    public static final ForgeConfigSpec.IntValue MAX_POTENCY;
    public static final ForgeConfigSpec.BooleanValue GREAT_WORK_SEEDED;
    public static final ForgeConfigSpec.BooleanValue LOCK_MACHINE_RECIPES;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_MACHINE_REPAIR;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WORLD_EFFECTS;
    public static final List<String> DEFAULT_WORLD_EFFECTS = List.of(
            "Dolphins_Grace = rain",
            "Bad_Omen = new_moon",
            "Luck@2 = moonlight:0.5",
            "Luck@3 = full_moon",
            "Levitation@2 = above:90",
            "Levitation@3 = above:90, night",
            "Blindness@2 = night",
            "Blindness@3 = new_moon",
            "Invisibility@2 = night",
            "Invisibility@3 = new_moon",
            "Harm@3 = thunder",
            "Health_Boost@3 = day, clear",
            "Darkness = biome:minecraft:deep_dark");

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DANGER_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DANGER_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TREASURE_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TREASURE_ENTITIES;
    public static final List<String> DEFAULT_DANGER_BLOCKS = List.of(
            "#alchemia:dangers",
            "minecraft:spawner",
            "minecraft:trial_spawner",
            "minecraft:sculk_shrieker",
            "minecraft:sculk_sensor",
            "minecraft:calibrated_sculk_sensor");
    public static final List<String> DEFAULT_DANGER_ENTITIES = List.of("hostile", "aggressive");
    public static final List<String> DEFAULT_TREASURE_BLOCKS = List.of(
            "#forge:ores/diamond = #6FF2F0",
            "#forge:ores/emerald = #3CF07A",
            "#forge:ores/gold = #FFD84A",
            "#forge:ores/redstone = #FF3A2E",
            "#forge:ores/lapis = #3D6BFF",
            "#forge:ores/iron = #E8B48A",
            "#forge:ores/copper = #F08A4B",
            "#forge:ores/coal = #8C8C8C",
            "#forge:ores/quartz = #F4EEE4",
            "#forge:ores/netherite_scrap = #B06A4A",
            "#forge:ores = #FFD23A",
            "#forge:chests/wooden = #FFF4B0",
            "#forge:chests/trapped = #FFF4B0",
            "#forge:barrels = #FFF4B0",
            "minecraft:vault = #FFF4B0",
            "minecraft:suspicious_sand = #F2E3A0",
            "minecraft:suspicious_gravel = #B8B0A8");
    public static final List<String> DEFAULT_TREASURE_ENTITIES = List.of();

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

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        clientBuilder.comment("Sense potions").push("senses");
        TREASURE_HIGHLIGHTS = clientBuilder
                .comment("Draw coloured outlines over treasure found by Treasure Vision.",
                        "Off leaves only the tinted firefly glints.")
                .define("treasure_highlights", false);
        DANGER_HIGHLIGHTS = clientBuilder
                .comment("Draw red outlines over dangerous blocks found by Dangersense.",
                        "Off leaves only the red glints.")
                .define("danger_highlights", false);
        clientBuilder.pop();
        CLIENT_CONFIG = clientBuilder.build();

        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
        serverBuilder.comment("Brewing rules").push("brewing");
        MAX_POTENCY = serverBuilder
                .comment("Total potency a single potion can hold before it spoils.",
                        "Each effect contributes its own potency, The default of 5 allows",
                        "five level 1 effects, or one level 3 alongside one level 2. and so forth.")
                .defineInRange("max_potency", DEFAULT_MAX_POTENCY, 1, 10);
        serverBuilder.pop();

        serverBuilder.comment("The Great Work").push("great_work");
        GREAT_WORK_SEEDED = serverBuilder
                .comment("Build the potions each Great Work stage asks for from the world seed, each stage harder",
                        "than the last. Turn off to use the fixed classic recipes instead.",
                        "Generated recipes are saved with the world the first time it starts, so later changes",
                        "to brew bases, potency or world effects do not move them.")
                .define("seeded", true);
        LOCK_MACHINE_RECIPES = serverBuilder
                .comment("Keep each Alchemy Machine part's recipe hidden and uncraftable until a guide the player can read",
                        "has learned the Great Work stage that first needs that part. Turn off to always show them.")
                .define("lock_machine_recipes", true);
        REQUIRE_MACHINE_REPAIR = serverBuilder
                .comment("A player must restore a ruined Alchemy Machine part, found in Forgotten Alchemy Shack cellars,",
                        "before they can craft that part themselves.")
                .define("require_machine_repair", true);
        serverBuilder.pop();

        serverBuilder.comment("Effects that only wake under certain conditions in the world around the cauldron.",
                        "Until then they show on the potion map as a grey, dormant mark and cannot be brewed.",
                        "Each entry is 'Effect = condition, condition, ...' and every condition must hold.",
                        "The effect is a name from the potion list (Dolphins_Grace) or an effect id (minecraft:dolphins_grace).",
                        "Adding '@level' after the effect (Levitation@2) leaves the effect brewable at all times but holds",
                        "that level and every level above it back until the conditions hold. Levels stack: level 3 also",
                        "needs whatever level 2 needs.",
                        "Conditions: rain, thunder, clear, day, night, full_moon, new_moon, moonlight:<0-1> (how full the moon",
                        "must be, 0.5 is a half moon; all moon conditions also need night), above:<y> and below:<y> (the",
                        "cauldron's height), and biome:<id or #tag>|<id or #tag>... which holds when the cauldron's biome",
                        "matches any of them.")
                .push("world_effects");
        WORLD_EFFECTS = serverBuilder
                .defineListAllowEmpty("conditions", DEFAULT_WORLD_EFFECTS, value -> value instanceof String);
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
        FOG_OF_WAR = serverBuilder
                .comment("Hide the potion map behind fog until a brew has passed close by.",
                        "What a brew uncovers is written into the guides on the player's shelf and shared",
                        "between them, so charted ground stays clear on later brews.",
                        "Off shows the whole map and stops recording what has been charted.")
                .define("fog_of_war", true);
        serverBuilder.pop();

        serverBuilder.comment("What the sense potions reveal. Each entry is a block or entity id (minecraft:tnt) or a tag (#forge:ores).",
                        "Entity lists also accept 'hostile' (every monster) and 'aggressive' (any mob currently attacking).",
                        "These lists are sent to players when they join, so the server's choice applies to everyone.")
                .push("senses");
        DANGER_BLOCKS = serverBuilder
                .comment("Blocks Dangersense outlines.")
                .defineListAllowEmpty("danger_blocks", DEFAULT_DANGER_BLOCKS, value -> value instanceof String);
        DANGER_ENTITIES = serverBuilder
                .comment("Creatures Dangersense marks.")
                .defineListAllowEmpty("danger_entities", DEFAULT_DANGER_ENTITIES, value -> value instanceof String);
        TREASURE_BLOCKS = serverBuilder
                .comment("Blocks Treasure Vision reveals. Add ' = #RRGGBB' to an entry to colour its highlight;",
                        "the first entry a block matches decides its colour, so list specific ores before broad tags.",
                        "Containers and suspicious blocks only show while they still hold their generated loot.")
                .defineListAllowEmpty("treasure_blocks", DEFAULT_TREASURE_BLOCKS, value -> value instanceof String);
        TREASURE_ENTITIES = serverBuilder
                .comment("Creatures Treasure Vision reveals, for example minecraft:allay or #minecraft:axolotl_hunt_targets.")
                .defineListAllowEmpty("treasure_entities", DEFAULT_TREASURE_ENTITIES, value -> value instanceof String);
        serverBuilder.pop();

        SERVER_CONFIG = serverBuilder.build();
    }

    public static boolean treasureHighlights() {
        return CLIENT_CONFIG.isLoaded() && TREASURE_HIGHLIGHTS.get();
    }

    public static boolean dangerHighlights() {
        return CLIENT_CONFIG.isLoaded() && DANGER_HIGHLIGHTS.get();
    }

    public static List<? extends String> dangerBlocks() {
        return SERVER_CONFIG.isLoaded() ? DANGER_BLOCKS.get() : DEFAULT_DANGER_BLOCKS;
    }

    public static List<? extends String> dangerEntities() {
        return SERVER_CONFIG.isLoaded() ? DANGER_ENTITIES.get() : DEFAULT_DANGER_ENTITIES;
    }

    public static List<? extends String> treasureBlocks() {
        return SERVER_CONFIG.isLoaded() ? TREASURE_BLOCKS.get() : DEFAULT_TREASURE_BLOCKS;
    }

    public static List<? extends String> treasureEntities() {
        return SERVER_CONFIG.isLoaded() ? TREASURE_ENTITIES.get() : DEFAULT_TREASURE_ENTITIES;
    }

    public static List<? extends String> worldEffects() {
        return SERVER_CONFIG.isLoaded() ? WORLD_EFFECTS.get() : DEFAULT_WORLD_EFFECTS;
    }

    public static boolean lockMachineRecipes() {
        return !SERVER_CONFIG.isLoaded() || LOCK_MACHINE_RECIPES.get();
    }

    public static boolean requireMachineRepair() {
        return !SERVER_CONFIG.isLoaded() || REQUIRE_MACHINE_REPAIR.get();
    }

    public static boolean greatWorkSeeded() {
        return !SERVER_CONFIG.isLoaded() || GREAT_WORK_SEEDED.get();
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

    public static boolean fogOfWar() {
        return !SERVER_CONFIG.isLoaded() || FOG_OF_WAR.get();
    }
}
