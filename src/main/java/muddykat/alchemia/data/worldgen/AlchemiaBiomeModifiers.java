package muddykat.alchemia.data.worldgen;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.IngredientHabitat;
import muddykat.alchemia.common.world.WildHerbGeneration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlchemiaBiomeModifiers {

    private static final Map<Ingredients, TagKey<Biome>> BIOME_OVERRIDES = new EnumMap<>(Ingredients.class);

    static {
        BIOME_OVERRIDES.put(Ingredients.Flameweed, Tags.Biomes.IS_OCEAN);
        BIOME_OVERRIDES.put(Ingredients.Tangleweed, Tags.Biomes.IS_OCEAN);
        BIOME_OVERRIDES.put(Ingredients.Druids_Rosemary, Tags.Biomes.IS_JUNGLE);
        BIOME_OVERRIDES.put(Ingredients.Thornstick, Tags.Biomes.IS_DESERT);
        BIOME_OVERRIDES.put(Ingredients.Marshroom, Tags.Biomes.IS_SWAMP);
        BIOME_OVERRIDES.put(Ingredients.Firebell, Tags.Biomes.IS_FOREST);
        BIOME_OVERRIDES.put(Ingredients.Sulphur_Shelf, AlchemiaBiomeTags.HAS_SULPHUR_SHELF);

        BIOME_OVERRIDES.put(Ingredients.Spellbloom, AlchemiaBiomeTags.HAS_SPELLBLOOM);
        BIOME_OVERRIDES.put(Ingredients.Boombloom, Tags.Biomes.IS_DARK_FOREST);
        BIOME_OVERRIDES.put(Ingredients.Arcane_Crystal, Tags.Biomes.IS_DARK_FOREST);
        BIOME_OVERRIDES.put(Ingredients.Frost_Sapphire, AlchemiaBiomeTags.HAS_FROST_SAPPHIRE);
        BIOME_OVERRIDES.put(Ingredients.Fire_Citrine, Tags.Biomes.IS_NETHER);
        BIOME_OVERRIDES.put(Ingredients.Fable_Bismuth, Tags.Biomes.IS_MUSHROOM);
        BIOME_OVERRIDES.put(Ingredients.Cloud_Crystal, Tags.Biomes.IS_MOUNTAIN);
        BIOME_OVERRIDES.put(Ingredients.Earth_Pyrite, AlchemiaBiomeTags.HAS_EARTH_PLANTS);
    }

    public static TagKey<Biome> biomeFor(Ingredients ingredient) {
        TagKey<Biome> override = BIOME_OVERRIDES.get(ingredient);
        return override != null ? override : AlchemiaBiomeTags.forAlignment(ingredient.getPrimaryAlignment());
    }

    public static GenerationStep.Decoration stepFor(Ingredients ingredient) {
        return IngredientHabitat.of(ingredient).isUnderground()
                ? GenerationStep.Decoration.UNDERGROUND_DECORATION
                : GenerationStep.Decoration.VEGETAL_DECORATION;
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);

        Map<String, List<Ingredients>> groups = new LinkedHashMap<>();
        Map<String, TagKey<Biome>> groupTag = new LinkedHashMap<>();
        Map<String, GenerationStep.Decoration> groupStep = new LinkedHashMap<>();

        for (Ingredients ingredient : Ingredients.values()) {
            if (!WildHerbGeneration.generates(ingredient)) continue;

            TagKey<Biome> tag = biomeFor(ingredient);
            if (tag == null) continue;

            GenerationStep.Decoration step = stepFor(ingredient);
            String name = tag.location().getNamespace() + "_" + tag.location().getPath().replace('/', '_')
                    + "_" + step.getSerializedName();

            groups.computeIfAbsent(name, key -> new ArrayList<>()).add(ingredient);
            groupTag.put(name, tag);
            groupStep.put(name, step);
        }

        groups.forEach((name, ingredients) -> {
            List<Holder<PlacedFeature>> holders = new ArrayList<>();
            for (Ingredients ingredient : ingredients) {
                ResourceKey<PlacedFeature> key = WildHerbGeneration.PLACED_FEATURES.get(ingredient);
                if (key != null) holders.add(features.getOrThrow(key));
            }
            if (holders.isEmpty()) return;

            context.register(modifierKey(name), new BiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(groupTag.get(name)), HolderSet.direct(holders), groupStep.get(name)));
        });
    }

    private static ResourceKey<BiomeModifier> modifierKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Identifier.fromNamespaceAndPath(Alchemia.MODID, name));
    }
}
