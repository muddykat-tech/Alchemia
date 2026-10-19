package muddykat.alchemia.data.worldgen;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BiomeTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class AlchemiaBiomeTagsProvider extends BiomeTagsProvider {

    public AlchemiaBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Alchemia.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        wildPlantTag(AlchemiaBiomeTags.HAS_FIRE_PLANTS)
                .addTag(Tags.Biomes.IS_HOT_OVERWORLD)
                .remove(Tags.Biomes.IS_DARK_FOREST);

        wildPlantTag(AlchemiaBiomeTags.HAS_WATER_PLANTS)
                .addTag(Tags.Biomes.IS_COLD_OVERWORLD)
                .addTag(Tags.Biomes.IS_WET_OVERWORLD)
                .remove(Tags.Biomes.IS_DARK_FOREST);

        wildPlantTag(AlchemiaBiomeTags.HAS_AIR_PLANTS)
                .addTag(Tags.Biomes.IS_TEMPERATE_OVERWORLD)
                .remove(Tags.Biomes.IS_DARK_FOREST);

        wildPlantTag(AlchemiaBiomeTags.HAS_EARTH_PLANTS)
                .addTag(Tags.Biomes.IS_FOREST)
                .addTag(Tags.Biomes.IS_PLAINS)
                .remove(Tags.Biomes.IS_DARK_FOREST);

        tag(AlchemiaBiomeTags.HAS_ANCIENT_OAK)
                .add(Biomes.FOREST)
                .add(Biomes.FLOWER_FOREST)
                .add(Biomes.DARK_FOREST);

        wildPlantTag(AlchemiaBiomeTags.HAS_SPELLBLOOM)
                .addTag(Tags.Biomes.IS_TEMPERATE_OVERWORLD)
                .addTag(Tags.Biomes.IS_DARK_FOREST);

        wildPlantTag(AlchemiaBiomeTags.HAS_FROST_SAPPHIRE)
                .addTag(Tags.Biomes.IS_COLD_OVERWORLD);

        rootTag(AlchemiaBiomeTags.HAS_LAVA_ROOT, AlchemiaBiomeTags.HAS_FIRE_PLANTS);
        rootTag(AlchemiaBiomeTags.HAS_TERRARIA, AlchemiaBiomeTags.HAS_EARTH_PLANTS);
        rootTag(AlchemiaBiomeTags.HAS_DREAMBEET, AlchemiaBiomeTags.HAS_AIR_PLANTS);
        rootTag(AlchemiaBiomeTags.HAS_GRASPING_ROOT, AlchemiaBiomeTags.HAS_WATER_PLANTS);

        tag(AlchemiaBiomeTags.HAS_SULPHUR_SHELF)
                .addTag(Tags.Biomes.IS_FOREST)
                .addTag(Tags.Biomes.IS_TAIGA)
                .addTag(Tags.Biomes.IS_JUNGLE)
                .addTag(Tags.Biomes.IS_SWAMP);

        tag(AlchemiaBiomeTags.HAS_TERRORBUD)
                .addTag(AlchemiaBiomeTags.HAS_EARTH_PLANTS)
                .add(biome("crimson_forest"));

        tag(AlchemiaBiomeTags.HAS_MAGMA_MOREL)
                .addTag(BiomeTags.IS_OVERWORLD)
                .addTag(BiomeTags.IS_NETHER);
    }

    private static ResourceKey<Biome> biome(String name) {
        return ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace(name));
    }

    private void rootTag(TagKey<Biome> tag, TagKey<Biome> alignment) {
        tag(tag)
                .addTag(alignment)
                .add(ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace("lush_caves")));
    }

#if MC_VER >= 26200
    private net.minecraft.data.tags.TagAppender<Biome> wildPlantTag(TagKey<Biome> tag) {
#else
    private net.minecraft.data.tags.TagAppender<ResourceKey<Biome>, Biome> wildPlantTag(TagKey<Biome> tag) {
#endif
        return tag(tag)
                .remove(Tags.Biomes.IS_MUSHROOM)
                .remove(Tags.Biomes.IS_MOUNTAIN);
    }
}
