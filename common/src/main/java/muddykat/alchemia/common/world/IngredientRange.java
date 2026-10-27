package muddykat.alchemia.common.world;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
#if MC_VER >= 26100
import org.jspecify.annotations.Nullable;
#else
import javax.annotation.Nullable;
#endif

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class IngredientRange {

    public static final String END_ISLANDS = "has_end_crystal";

    private static final Map<Ingredients, String> GROUND = new EnumMap<>(Ingredients.class);
    private static final Map<Ingredients, List<String>> ALSO = new EnumMap<>(Ingredients.class);

    static {
        GROUND.put(Ingredients.Waterbloom, "shallows");
        ALSO.put(Ingredients.Arcane_Crystal, List.of(END_ISLANDS));
    }

    private IngredientRange() {
    }

    public static @Nullable Component describe(Ingredients ingredient, @Nullable TagKey<Biome> biomes) {
        if (biomes == null) return null;
        List<String> regions = new ArrayList<>();
        regions.add(biomes.location().getPath());
        regions.addAll(ALSO.getOrDefault(ingredient, List.of()));

        MutableComponent where = Component.empty();
        for (int index = 0; index < regions.size(); index++) {
            if (index > 0) where.append(Component.translatable("alchemia.habitat.and"));
            where.append(Component.translatable("alchemia.habitat.biome." + regions.get(index)));
        }
        String ground = GROUND.getOrDefault(ingredient, IngredientHabitat.of(ingredient).name().toLowerCase(Locale.ROOT));
        return Component.translatable("alchemia.guide.ingredient.habitat", where, Component.translatable("alchemia.habitat.ground." + ground));
    }
}
