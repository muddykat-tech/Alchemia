package muddykat.alchemia.registration.registers;

import com.google.gson.JsonObject;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class IngredientTypeRegistry {

    public static final ResourceLocation POTION_EFFECT_ID = new ResourceLocation(Alchemia.MODID, "potion_effect");

    public static final IIngredientSerializer<PotionEffectIngredient> POTION_EFFECT = new IIngredientSerializer<>() {
        @Override
        public PotionEffectIngredient parse(FriendlyByteBuf buffer) {
            return PotionEffectIngredient.fromNetwork(buffer);
        }

        @Override
        public PotionEffectIngredient parse(JsonObject json) {
            return PotionEffectIngredient.fromJson(json);
        }

        @Override
        public void write(FriendlyByteBuf buffer, PotionEffectIngredient ingredient) {
            ingredient.write(buffer);
        }
    };

    public static void register() {
        CraftingHelper.register(POTION_EFFECT_ID, POTION_EFFECT);
    }
}
