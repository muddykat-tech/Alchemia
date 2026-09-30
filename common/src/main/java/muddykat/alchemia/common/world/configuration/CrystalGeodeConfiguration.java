package muddykat.alchemia.common.world.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.GeodeShaper;
import muddykat.alchemia.common.world.GeodeVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
#if MC_VER >= 26300
import net.minecraft.world.level.levelgen.feature.Feature;
#else
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
#endif

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public record CrystalGeodeConfiguration(Ingredients mineral, GeodeVariant variant)
#if MC_VER >= 26300
        implements Feature {
#else
        implements FeatureConfiguration {
#endif

    private static final Map<String, Ingredients> BY_NAME = new HashMap<>();

    static {
        for (Ingredients ingredient : Ingredients.values()) {
            BY_NAME.put(ingredient.name().toLowerCase(Locale.ROOT), ingredient);
        }
    }

    public static final Codec<Ingredients> MINERAL = Codec.STRING.comapFlatMap(
            name -> {
                Ingredients mineral = BY_NAME.get(name);
                return mineral == null
                        ? DataResult.error(() -> "Unknown Alchemia mineral: " + name)
                        : DataResult.success(mineral);
            },
            mineral -> mineral.name().toLowerCase(Locale.ROOT));

    public static final MapCodec<CrystalGeodeConfiguration> CODEC = RecordCodecBuilder.mapCodec(config -> config.group(
            MINERAL.fieldOf("mineral").forGetter(CrystalGeodeConfiguration::mineral),
            GeodeVariant.CODEC.fieldOf("variant").forGetter(CrystalGeodeConfiguration::variant)
    ).apply(config, CrystalGeodeConfiguration::new));

#if MC_VER >= 26300
    @Override
    public MapCodec<CrystalGeodeConfiguration> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return placeGeode(level, random, origin);
    }
#endif

    public boolean placeGeode(WorldGenLevel level, RandomSource random, BlockPos origin) {
        return GeodeShaper.place(level, random, origin, mineral, variant);
    }
}
