package muddykat.alchemia.data.worldgen;

import muddykat.alchemia.Alchemia;
import net.minecraft.tags.BiomeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
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

        wildPlantTag(AlchemiaBiomeTags.HAS_SPELLBLOOM)
                .addTag(Tags.Biomes.IS_PLAINS)
                .addTag(Tags.Biomes.IS_SPOOKY);

        wildPlantTag(AlchemiaBiomeTags.HAS_FROST_SAPPHIRE)
                .addTag(Tags.Biomes.IS_COLD_OVERWORLD);

        tag(AlchemiaBiomeTags.HAS_SULPHUR_SHELF)
                .add(Biomes.BASALT_DELTAS)
                .addOptional(new ResourceLocation("sulfur_caves"));
    }

    private TagsProvider.TagAppender<Biome> wildPlantTag(TagKey<Biome> tag) {
        return tag(tag)
                .remove(Tags.Biomes.IS_MUSHROOM)
                .remove(Tags.Biomes.IS_MOUNTAIN);
    }
}
