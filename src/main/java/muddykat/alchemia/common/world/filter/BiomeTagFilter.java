package muddykat.alchemia.common.world.filter;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.registration.registers.PlacementModifierRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class BiomeTagFilter extends PlacementFilter {
    public static final MapCodec<BiomeTagFilter> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    TagKey.codec(Registries.BIOME).fieldOf("biome_tag").forGetter(instance -> instance.biomeTag)
            ).apply(builder, BiomeTagFilter::new));

    private final TagKey<Biome> biomeTag;

    private BiomeTagFilter(TagKey<Biome> biomeTag) {
        this.biomeTag = biomeTag;
    }

    public static BiomeTagFilter biomeIsInTag(TagKey<Biome> biomeTag) {
        return new BiomeTagFilter(biomeTag);
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        Holder<Biome> biome = context.getLevel().getBiome(pos);
        return biome.is(biomeTag);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierRegistry.BIOME_TAG.get();
    }
}
