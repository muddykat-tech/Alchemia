package muddykat.alchemia.registration.registers;

import com.mojang.serialization.Codec;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.PotionGlass;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class DataComponentRegistry {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Alchemia.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> DISCOVERED_RECIPES = DATA_COMPONENTS.register("discovered_recipes",
            () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> DISCOVERED_INGREDIENTS = DATA_COMPONENTS.register("discovered_ingredients",
            () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> GREAT_WORK = DATA_COMPONENTS.register("great_work",
            () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> NOTED_EFFECTS = DATA_COMPONENTS.register("noted_effects",
            () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> WHIRL_EXITS = DATA_COMPONENTS.register("whirl_exits",
            () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, List<Long>>>> MAP_REVEALS = DATA_COMPONENTS.register("map_reveals",
            () -> DataComponentType.<Map<String, List<Long>>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.LONG.listOf()))
                    .networkSynchronized(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list())))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<BrewRecipe>>> BREW_RECIPES = DATA_COMPONENTS.register("brew_recipes",
            () -> DataComponentType.<List<BrewRecipe>>builder()
                    .persistent(BrewRecipe.CODEC.listOf())
                    .networkSynchronized(BrewRecipe.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CRUSH = DATA_COMPONENTS.register("crush",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, Ingredients.MAX_CRUSH_UNITS))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PATH_TRIM = DATA_COMPONENTS.register("path_trim",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 64))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PATH_ROTATION = DATA_COMPONENTS.register("path_rotation",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 7))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGES = DATA_COMPONENTS.register("charges",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 64))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SELECTED_RECIPE = DATA_COMPONENTS.register("selected_recipe",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> GUIDE_BOOKMARK = DATA_COMPONENTS.register("guide_bookmark",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GUIDE_PAGES = DATA_COMPONENTS.register("guide_pages",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, BrewRecipeBook.MAX_PAGES))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BrewRecipe>> PAGE_RECIPE = DATA_COMPONENTS.register("page_recipe",
            () -> DataComponentType.<BrewRecipe>builder()
                    .persistent(BrewRecipe.CODEC)
                    .networkSynchronized(BrewRecipe.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, String>>> INGREDIENT_NOTES = DATA_COMPONENTS.register("ingredient_notes",
            () -> DataComponentType.<Map<String, String>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.STRING))
                    .networkSynchronized(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<Biome>>> BARK_BIOME = DATA_COMPONENTS.register("bark_biome",
            () -> DataComponentType.<ResourceKey<Biome>>builder()
                    .persistent(ResourceKey.codec(Registries.BIOME))
                    .networkSynchronized(ResourceKey.streamCodec(Registries.BIOME))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PotionGlass>> POTION_GLASS = DATA_COMPONENTS.register("potion_glass",
            () -> DataComponentType.<PotionGlass>builder()
                    .persistent(PotionGlass.CODEC)
                    .networkSynchronized(ByteBufCodecs.idMapper(index -> PotionGlass.values()[index], PotionGlass::ordinal))
                    .build());
}
