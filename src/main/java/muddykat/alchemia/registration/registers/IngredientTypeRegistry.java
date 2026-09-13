package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class IngredientTypeRegistry {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Alchemia.MODID);

    public static final RegistryObject<IngredientType<PotionEffectIngredient>> POTION_EFFECT =
            INGREDIENT_TYPES.register("potion_effect",
                    () -> new IngredientType<>(PotionEffectIngredient.CODEC, PotionEffectIngredient.STREAM_CODEC));

    public static DeferredRegister<IngredientType<?>> getRegistry() {
        return INGREDIENT_TYPES;
    }
}
