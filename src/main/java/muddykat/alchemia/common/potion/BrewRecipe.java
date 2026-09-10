package muddykat.alchemia.common.potion;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record BrewRecipe(String id, String name, String base, List<BrewRecipe.BrewEffect> effects, List<String> ingredients) {

    public BrewRecipe {
        if (id == null || id.isBlank()) {
            id = effects.stream().map(BrewEffect::recipe).sorted().collect(Collectors.joining("+"));
        }
        if (name == null) name = "";
        if (base == null || base.isBlank()) base = BrewBases.DEFAULT_ID;
    }

    public static BrewRecipe create(String name, BrewBase base, List<BrewEffect> effects, List<String> ingredients) {
        return new BrewRecipe(java.util.UUID.randomUUID().toString(), name, base.id(), effects, ingredients);
    }

    public BrewBase brewBase() {
        return BrewBases.byId(base);
    }


    public record BrewEffect(String recipe, int potency) {
        public static final Codec<BrewEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("recipe").forGetter(BrewEffect::recipe),
                Codec.INT.fieldOf("potency").forGetter(BrewEffect::potency)
        ).apply(instance, BrewEffect::new));

        public static final StreamCodec<ByteBuf, BrewEffect> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, BrewEffect::recipe,
                ByteBufCodecs.VAR_INT, BrewEffect::potency,
                BrewEffect::new);

        public PotionEnum asPotion() {
            return RecipeDiscovery.byName(recipe);
        }

        public Component displayName() {
            PotionEnum potion = asPotion();
            return potion != null ? potion.getEffect().value().getDisplayName() : Component.literal(recipe);
        }
    }

    private static final Codec<BrewRecipe> MODERN = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(BrewRecipe::id),
            Codec.STRING.optionalFieldOf("name", "").forGetter(BrewRecipe::name),
            Codec.STRING.optionalFieldOf("base", BrewBases.DEFAULT_ID).forGetter(BrewRecipe::base),
            BrewEffect.CODEC.listOf().fieldOf("effects").forGetter(BrewRecipe::effects),
            Codec.STRING.listOf().fieldOf("ingredients").forGetter(BrewRecipe::ingredients)
    ).apply(instance, BrewRecipe::new));

    private static final Codec<BrewRecipe> LEGACY = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("recipe").forGetter(brew -> brew.effects().getFirst().recipe()),
            Codec.STRING.listOf().fieldOf("ingredients").forGetter(BrewRecipe::ingredients),
            Codec.INT.fieldOf("potency").forGetter(brew -> brew.effects().getFirst().potency())
    ).apply(instance, (recipe, ingredients, potency) -> new BrewRecipe("", "",
            BrewBases.DEFAULT_ID, List.of(new BrewEffect(recipe, potency)), ingredients)));

    public static final Codec<BrewRecipe> CODEC = Codec.either(MODERN, LEGACY).xmap(Either::unwrap, Either::left);

    public static final StreamCodec<ByteBuf, BrewRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BrewRecipe::id,
            ByteBufCodecs.STRING_UTF8, BrewRecipe::name,
            ByteBufCodecs.STRING_UTF8, BrewRecipe::base,
            BrewEffect.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), BrewRecipe::effects,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)), BrewRecipe::ingredients,
            BrewRecipe::new);

    public Component displayName() {
        if (!name.isBlank()) return Component.literal(name);
        return effectSummary();
    }

    public Component effectSummary() {
        if (effects.isEmpty()) return Component.translatable("alchemia.guide.unnamed");
        Component first = effects.getFirst().displayName();
        return effects.size() == 1 ? first : Component.translatable("alchemia.guide.combo", first, effects.size() - 1);
    }

    public static String encode(String registryName, int crush) {
        return crush <= 0 ? registryName : registryName + "|" + crush;
    }

    private static String nameOf(String entry) {
        int split = entry.indexOf('|');
        return split < 0 ? entry : entry.substring(0, split);
    }

    private static int crushOf(String entry) {
        int split = entry.indexOf('|');
        if (split < 0) return 0;
        try {
            return Integer.parseInt(entry.substring(split + 1));
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    public boolean requiresGrinding() {
        for (String entry : ingredients) {
            if (crushOf(entry) > 0) return true;
        }
        return false;
    }

    public Map<ItemStack, Integer> tally() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String entry : ingredients) counts.merge(entry, 1, Integer::sum);

        Map<ItemStack, Integer> resolved = new LinkedHashMap<>();
        counts.forEach((entry, count) -> {
            ItemStack stack = stackFor(entry);
            if (!stack.isEmpty()) resolved.put(stack, count);
        });
        return resolved;
    }

    public List<ItemStack> resolve() {
        List<ItemStack> resolved = new ArrayList<>();
        for (String entry : ingredients) {
            ItemStack stack = stackFor(entry);
            if (!stack.isEmpty()) resolved.add(stack);
        }
        return resolved;
    }

    public boolean isResolvable() {
        return resolve().size() == ingredients.size() && !ingredients.isEmpty();
    }

    public static ItemStack stackFor(String entry) {
        Item item = itemFor(nameOf(entry));
        if (item == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(item);
        if (!(item instanceof ItemIngredient)) return stack;
        int crush = crushOf(entry);
        if (crush > 0) stack.set(DataComponentRegistry.CRUSH.get(), crush);
        return stack;
    }

    public static Item itemFor(String registryName) {
        if (registryName.indexOf(':') >= 0) {
            return BuiltInRegistries.ITEM.getValue(Identifier.parse(registryName));
        }

        var holder = ItemRegistry.ITEM_REGISTRY.get(registryName);
        return holder == null ? null : holder.get();
    }
}
