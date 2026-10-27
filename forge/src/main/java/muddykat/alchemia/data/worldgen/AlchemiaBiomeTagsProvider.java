package muddykat.alchemia.data.worldgen;

import muddykat.alchemia.Alchemia;
import net.minecraft.tags.BiomeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

public class AlchemiaBiomeTagsProvider extends BiomeTagsProvider {

    public AlchemiaBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, registries, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        wildPlantTag(AlchemiaBiomeTags.HAS_FIRE_PLANTS)
                .addTag(Tags.Biomes.IS_HOT_OVERWORLD)
                .remove(Tags.Biomes.IS_SPOOKY);

        wildPlantTag(AlchemiaBiomeTags.HAS_WATER_PLANTS)
                .addTag(Tags.Biomes.IS_COLD_OVERWORLD)
                .addTag(Tags.Biomes.IS_WET_OVERWORLD)
                .remove(Tags.Biomes.IS_SPOOKY);

        wildPlantTag(AlchemiaBiomeTags.HAS_AIR_PLANTS)
                .addTag(Tags.Biomes.IS_PLAINS)
                .remove(Tags.Biomes.IS_SPOOKY);

        wildPlantTag(AlchemiaBiomeTags.HAS_EARTH_PLANTS)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(Tags.Biomes.IS_PLAINS)
                .remove(Tags.Biomes.IS_SPOOKY);

        tag(AlchemiaBiomeTags.HAS_ANCIENT_OAK)
                .add(Biomes.FOREST)
                .add(Biomes.FLOWER_FOREST)
                .add(Biomes.DARK_FOREST);

        wildPlantTag(AlchemiaBiomeTags.HAS_SPELLBLOOM)
                .addTag(Tags.Biomes.IS_PLAINS)
                .addTag(Tags.Biomes.IS_SPOOKY);

        wildPlantTag(AlchemiaBiomeTags.HAS_FROST_SAPPHIRE)
                .addTag(Tags.Biomes.IS_COLD_OVERWORLD);

        rootTag(AlchemiaBiomeTags.HAS_LAVA_ROOT, AlchemiaBiomeTags.HAS_FIRE_PLANTS);
        rootTag(AlchemiaBiomeTags.HAS_TERRARIA, AlchemiaBiomeTags.HAS_EARTH_PLANTS);
        rootTag(AlchemiaBiomeTags.HAS_DREAMBEET, AlchemiaBiomeTags.HAS_AIR_PLANTS);
        rootTag(AlchemiaBiomeTags.HAS_GRASPING_ROOT, AlchemiaBiomeTags.HAS_WATER_PLANTS);

        tag(AlchemiaBiomeTags.HAS_SULPHUR_SHELF)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA)
                .addTag(BiomeTags.IS_JUNGLE)
                .addTag(Tags.Biomes.IS_SWAMP);

        tag(AlchemiaBiomeTags.HAS_TERRORBUD)
                .addTag(AlchemiaBiomeTags.HAS_EARTH_PLANTS)
                .add(Biomes.CRIMSON_FOREST);

        tag(AlchemiaBiomeTags.HAS_MAGMA_MOREL)
                .addTag(BiomeTags.IS_OVERWORLD)
                .addTag(BiomeTags.IS_NETHER);

        tag(AlchemiaBiomeTags.HAS_END_CRYSTAL)
                .add(Biomes.END_HIGHLANDS)
                .add(Biomes.END_MIDLANDS)
                .add(Biomes.END_BARRENS);
    }

    private void rootTag(TagKey<Biome> tag, TagKey<Biome> alignment) {
        tag(tag)
                .addTag(alignment)
                .add(Biomes.LUSH_CAVES);
    }

    private TagsProvider.TagAppender<Biome> wildPlantTag(TagKey<Biome> tag) {
        return tag(tag)
                .remove(Tags.Biomes.IS_MUSHROOM)
                .remove(Tags.Biomes.IS_MOUNTAIN);
    }
}
