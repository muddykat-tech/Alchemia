package muddykat.alchemia.registration.registers;

import com.mojang.serialization.Codec;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewRecipe;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SELECTED_RECIPE = DATA_COMPONENTS.register("selected_recipe",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());
}
