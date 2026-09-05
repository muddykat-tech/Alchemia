package muddykat.alchemia.data.worldgen;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
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

        wildPlantTag(AlchemiaBiomeTags.HAS_SPELLBLOOM)
                .addTag(Tags.Biomes.IS_TEMPERATE_OVERWORLD)
                .addTag(Tags.Biomes.IS_DARK_FOREST);

        wildPlantTag(AlchemiaBiomeTags.HAS_FROST_SAPPHIRE)
                .addTag(Tags.Biomes.IS_COLD_OVERWORLD);

        tag(AlchemiaBiomeTags.HAS_SULPHUR_SHELF)
                .add(ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace("sulfur_caves")));
    }

    private net.minecraft.data.tags.TagAppender<Biome> wildPlantTag(TagKey<Biome> tag) {
        return tag(tag)
                .remove(Tags.Biomes.IS_MUSHROOM)
                .remove(Tags.Biomes.IS_MOUNTAIN);
    }
}
