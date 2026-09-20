package muddykat.alchemia.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import muddykat.alchemia.common.utility.PotionStacks;
import muddykat.alchemia.registration.registers.IngredientTypeRegistry;
import muddykat.alchemia.registration.registers.PotionRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PotionEffectIngredient extends AbstractIngredient {

    public record Requirement(MobEffect effect, int amplifier) {}

    private final List<Requirement> required;

    protected PotionEffectIngredient(List<Requirement> required) {
        super(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.POTION)),
                new Ingredient.ItemValue(new ItemStack(Items.SPLASH_POTION)),
                new Ingredient.ItemValue(new ItemStack(Items.LINGERING_POTION))));
        this.required = List.copyOf(required);
    }

    public List<Requirement> required() {
        return required;
    }

    public static Requirement need(MobEffect effect, int potency) {
        return new Requirement(effect, potency - 1);
    }

    public static PotionEffectIngredient of(MobEffect effect, int potency) {
        return new PotionEffectIngredient(List.of(need(effect, potency)));
    }

    public static PotionEffectIngredient ofAll(Requirement... required) {
        return new PotionEffectIngredient(List.of(required));
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || !PotionStacks.isPotion(stack)) return false;
        List<MobEffectInstance> contents = PotionStacks.effectsOf(stack);

        if (contents.size() != required.size()) return false;

        for (Requirement requirement : required) {
            boolean found = false;
            for (MobEffectInstance instance : contents) {
                if (instance.getEffect().equals(requirement.effect())
                        && instance.getAmplifier() == requirement.amplifier()) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public ItemStack sampleStack() {
        ItemStack sample = new ItemStack(Items.POTION);
        List<MobEffectInstance> effects = required.stream()
                .map(requirement -> new MobEffectInstance(requirement.effect(), 3600, requirement.amplifier()))
                .toList();

        PotionStacks.apply(sample, PotionRegistry.ALCHEMICAL.get(), effects, null);
        return sample;
    }

    @Override
    public ItemStack[] getItems() {
        return new ItemStack[]{sampleStack()};
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return IngredientTypeRegistry.POTION_EFFECT;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", IngredientTypeRegistry.POTION_EFFECT_ID.toString());

        JsonArray effects = new JsonArray();
        for (Requirement requirement : required) {
            JsonObject entry = new JsonObject();
            entry.addProperty("effect", BuiltInRegistries.MOB_EFFECT.getKey(requirement.effect()).toString());
            entry.addProperty("amplifier", requirement.amplifier());
            effects.add(entry);
        }
        json.add("effects", effects);
        return json;
    }

    public static PotionEffectIngredient fromJson(JsonObject json) {
        List<Requirement> required = new ArrayList<>();
        for (var element : json.getAsJsonArray("effects")) {
            JsonObject entry = element.getAsJsonObject();
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(entry.get("effect").getAsString()));
            if (effect == null) continue;
            int amplifier = entry.has("amplifier") ? entry.get("amplifier").getAsInt() : 0;
            required.add(new Requirement(effect, amplifier));
        }
        return new PotionEffectIngredient(required);
    }

    public static PotionEffectIngredient fromNetwork(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<Requirement> required = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.byId(buffer.readVarInt());
            required.add(new Requirement(effect, buffer.readVarInt()));
        }
        return new PotionEffectIngredient(required);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(required.size());
        for (Requirement requirement : required) {
            buffer.writeVarInt(BuiltInRegistries.MOB_EFFECT.getId(requirement.effect()));
            buffer.writeVarInt(requirement.amplifier());
        }
    }
}
