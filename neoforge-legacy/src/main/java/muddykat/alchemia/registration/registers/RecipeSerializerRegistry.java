package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.crafting.QuietShapelessRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipeSerializerRegistry {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Alchemia.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<QuietShapelessRecipe>> QUIET_SHAPELESS =
            RECIPE_SERIALIZERS.register("quiet_shapeless", QuietShapelessRecipe.Serializer::new);
}
