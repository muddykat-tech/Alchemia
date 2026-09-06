package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class IngredientTypeRegistry {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Alchemia.MODID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<PotionEffectIngredient>> POTION_EFFECT =
            INGREDIENT_TYPES.register("potion_effect",
                    () -> new IngredientType<>(PotionEffectIngredient.CODEC, PotionEffectIngredient.STREAM_CODEC));

    public static DeferredRegister<IngredientType<?>> getRegistry() {
        return INGREDIENT_TYPES;
    }
}
