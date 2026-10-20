package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.crafting.QuietShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializerRegistry {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Alchemia.MODID);

    public static final RegistryObject<RecipeSerializer<QuietShapelessRecipe>> QUIET_SHAPELESS =
            RECIPE_SERIALIZERS.register("quiet_shapeless", QuietShapelessRecipe.Serializer::new);
}
