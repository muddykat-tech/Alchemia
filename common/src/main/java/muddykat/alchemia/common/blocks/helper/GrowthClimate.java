package muddykat.alchemia.common.blocks.helper;

import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

public final class GrowthClimate {

    private static final Map<Ingredients, TagKey<Biome>> ONLY_IN = new EnumMap<>(Ingredients.class);

    static {
        ONLY_IN.put(Ingredients.Hairy_Banana, BiomeTags.IS_JUNGLE);
    }

    private GrowthClimate() {
    }

    public static boolean allows(Ingredients ingredient, LevelReader level, BlockPos pos) {
        TagKey<Biome> climate = ONLY_IN.get(ingredient);
        return climate == null || level.getBiome(pos).is(climate);
    }

    public static boolean allows(BlockState state, LevelReader level, BlockPos pos) {
        return !(state.getBlock() instanceof BlockIngredient crop) || allows(crop.getIngredient(), level, pos);
    }
}
