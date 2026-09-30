package muddykat.alchemia.data.worldgen;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.IngredientHabitat;
import muddykat.alchemia.common.world.WildHerbGeneration;
import net.minecraft.tags.BiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlchemiaBiomeModifiers {

    private static final Map<Ingredients, TagKey<Biome>> BIOME_OVERRIDES = new EnumMap<>(Ingredients.class);

    static {
        BIOME_OVERRIDES.put(Ingredients.Flameweed, BiomeTags.IS_OCEAN);
        BIOME_OVERRIDES.put(Ingredients.Tangleweed, BiomeTags.IS_OCEAN);
        BIOME_OVERRIDES.put(Ingredients.Druids_Rosemary, BiomeTags.IS_JUNGLE);
        BIOME_OVERRIDES.put(Ingredients.Thornstick, Tags.Biomes.IS_DESERT);
        BIOME_OVERRIDES.put(Ingredients.Marshroom, Tags.Biomes.IS_SWAMP);
        BIOME_OVERRIDES.put(Ingredients.Firebell, BiomeTags.IS_FOREST);
        BIOME_OVERRIDES.put(Ingredients.Sulphur_Shelf, AlchemiaBiomeTags.HAS_SULPHUR_SHELF);
        BIOME_OVERRIDES.put(Ingredients.Terrorbud, AlchemiaBiomeTags.HAS_TERRORBUD);
        BIOME_OVERRIDES.put(Ingredients.Magma_Morel, AlchemiaBiomeTags.HAS_MAGMA_MOREL);

        BIOME_OVERRIDES.put(Ingredients.Lava_Root, AlchemiaBiomeTags.HAS_LAVA_ROOT);
        BIOME_OVERRIDES.put(Ingredients.Terraria, AlchemiaBiomeTags.HAS_TERRARIA);
        BIOME_OVERRIDES.put(Ingredients.Dreambeet, AlchemiaBiomeTags.HAS_DREAMBEET);

        BIOME_OVERRIDES.put(Ingredients.Spellbloom, AlchemiaBiomeTags.HAS_SPELLBLOOM);
        BIOME_OVERRIDES.put(Ingredients.Boombloom, Tags.Biomes.IS_SPOOKY);
        BIOME_OVERRIDES.put(Ingredients.Arcane_Crystal, Tags.Biomes.IS_SPOOKY);
        BIOME_OVERRIDES.put(Ingredients.Frost_Sapphire, AlchemiaBiomeTags.HAS_FROST_SAPPHIRE);
        BIOME_OVERRIDES.put(Ingredients.Fire_Citrine, BiomeTags.IS_NETHER);
        BIOME_OVERRIDES.put(Ingredients.Fable_Bismuth, Tags.Biomes.IS_MUSHROOM);
        BIOME_OVERRIDES.put(Ingredients.Cloud_Crystal, Tags.Biomes.IS_MOUNTAIN);
        BIOME_OVERRIDES.put(Ingredients.Earth_Pyrite, AlchemiaBiomeTags.HAS_EARTH_PLANTS);
    }

    public static TagKey<Biome> biomeFor(Ingredients ingredient) {
        TagKey<Biome> override = BIOME_OVERRIDES.get(ingredient);
        return override != null ? override : AlchemiaBiomeTags.forAlignment(ingredient.getPrimaryAlignment());
    }

    public static GenerationStep.Decoration stepFor(Ingredients ingredient) {
        return IngredientHabitat.of(ingredient).generatesWithVegetation()
                ? GenerationStep.Decoration.VEGETAL_DECORATION
                : GenerationStep.Decoration.UNDERGROUND_DECORATION;
    }

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
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
                for (ResourceKey<PlacedFeature> key : WildHerbGeneration.placedFor(ingredient)) {
                    holders.add(features.getOrThrow(key));
                }
            }
            if (holders.isEmpty()) return;

            context.register(modifierKey(name), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(groupTag.get(name)), HolderSet.direct(holders), groupStep.get(name)));
        });

        context.register(modifierKey(WildHerbGeneration.PEAK_CLUSTER_NAME), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(Tags.Biomes.IS_PEAK),
                HolderSet.direct(features.getOrThrow(WildHerbGeneration.PEAK_CLUSTER)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
    }

    private static ResourceKey<BiomeModifier> modifierKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(Alchemia.MODID, name));
    }
}
