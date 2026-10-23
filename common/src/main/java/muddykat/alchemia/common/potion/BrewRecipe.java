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
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record BrewRecipe(String id, String name, String base, List<BrewRecipe.BrewEffect> effects,
                         List<String> ingredients, List<String> steps, String notes) {

    public static final char STEP_ADD = '+';
    public static final char STEP_STIR = '~';
    public static final char STEP_FINE = '-';
    public static final char STEP_BREW = '!';
    public static final char STEP_REDSTONE = '*';
    public static final char STEP_SPLASH = '^';
    public static final char STEP_FINISH = '=';
    public static final char STEP_BARK = '%';
    public static final char STEP_GLOW = '#';
    public static final char STEP_BOTTLE = '&';
    public static final char STEP_WHIRL = '@';

    private static final int LEGACY_CRUSH_SCALE = 2;

    public BrewRecipe {
        if (id == null || id.isBlank()) {
            id = effects.stream().map(BrewEffect::recipe).sorted().collect(Collectors.joining("+"));
        }
        if (name == null) name = "";
        if (base == null || base.isBlank()) base = BrewBases.DEFAULT_ID;
        if (steps == null) steps = List.of();
        if (notes == null) notes = "";
    }

    public BrewRecipe(String id, String name, String base, List<BrewEffect> effects, List<String> ingredients, List<String> steps) {
        this(id, name, base, effects, ingredients, steps, "");
    }

    public BrewRecipe withName(String renamed) {
        return new BrewRecipe(id, renamed, base, effects, ingredients, steps, notes);
    }

    public BrewRecipe withNotes(String written) {
        return new BrewRecipe(id, name, base, effects, ingredients, steps, written);
    }

    public static BrewRecipe create(String name, BrewBase base, List<BrewEffect> effects,
                                    List<String> ingredients, List<String> steps) {
        return new BrewRecipe(java.util.UUID.randomUUID().toString(), name, base.id(), effects, ingredients, steps);
    }

    private static BrewRecipe stored(String id, String name, String base, List<BrewEffect> effects,
                                     List<String> ingredients, List<String> steps, String notes) {
        if (!steps.isEmpty()) return new BrewRecipe(id, name, base, effects, ingredients, steps, notes);

        List<String> upgraded = upgradeEntries(ingredients);
        return new BrewRecipe(id, name, base, effects, upgraded, legacySteps(upgraded), notes);
    }

    public static List<String> upgradeEntries(List<String> legacy) {
        List<String> upgraded = new ArrayList<>(legacy.size());
        for (String entry : legacy) upgraded.add(encode(nameOf(entry), crushOf(entry) * LEGACY_CRUSH_SCALE));
        return List.copyOf(upgraded);
    }

    public static List<String> legacySteps(List<String> entries) {
        String redstone = BuiltInRegistries.ITEM.getKey(Items.REDSTONE).toString();
        List<String> script = new ArrayList<>(entries.size() * 3);

        for (String entry : entries) {
            if (nameOf(entry).equals(redstone)) {
                script.add(String.valueOf(STEP_REDSTONE));
                continue;
            }
            script.add(STEP_ADD + entry);
            script.add(String.valueOf(STEP_FINISH));
            script.add(String.valueOf(STEP_BREW));
        }
        return List.copyOf(script);
    }

    public BrewBase brewBase() {
        return BrewBases.byId(base);
    }

    public boolean matches(BrewRecipe other) {
        return other != null
                && name.equals(other.name())
                && base.equals(other.base())
                && ingredients.equals(other.ingredients())
                && sortedEffects().equals(other.sortedEffects());
    }

    public PotionGlass glass() {
        for (String step : steps) {
            if (!step.isEmpty() && step.charAt(0) == STEP_BOTTLE) return PotionGlass.byName(step.substring(1));
        }
        return PotionGlass.NORMAL_POTION;
    }

    public static String bottleStep(PotionGlass glass) {
        return STEP_BOTTLE + glass.getSerializedName();
    }

    private List<BrewEffect> sortedEffects() {
        List<BrewEffect> copy = new ArrayList<>(effects);
        copy.sort(java.util.Comparator.comparing(BrewEffect::recipe).thenComparingInt(BrewEffect::potency));
        return copy;
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

        public AlchemiaEffect asPotion() {
            return RecipeDiscovery.byName(recipe);
        }

        public Component displayName() {
            AlchemiaEffect potion = asPotion();
            return potion != null ? potion.displayName() : Component.literal(recipe);
        }
    }

    private static final Codec<BrewRecipe> MODERN = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(BrewRecipe::id),
            Codec.STRING.optionalFieldOf("name", "").forGetter(BrewRecipe::name),
            Codec.STRING.optionalFieldOf("base", BrewBases.DEFAULT_ID).forGetter(BrewRecipe::base),
            BrewEffect.CODEC.listOf().fieldOf("effects").forGetter(BrewRecipe::effects),
            Codec.STRING.listOf().fieldOf("ingredients").forGetter(BrewRecipe::ingredients),
            Codec.STRING.listOf().optionalFieldOf("steps", List.of()).forGetter(BrewRecipe::steps),
            Codec.STRING.optionalFieldOf("notes", "").forGetter(BrewRecipe::notes)
    ).apply(instance, BrewRecipe::stored));

    private static final Codec<BrewRecipe> LEGACY = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("recipe").forGetter(brew -> brew.effects().getFirst().recipe()),
            Codec.STRING.listOf().fieldOf("ingredients").forGetter(BrewRecipe::ingredients),
            Codec.INT.fieldOf("potency").forGetter(brew -> brew.effects().getFirst().potency())
    ).apply(instance, (recipe, ingredients, potency) -> stored("", "",
            BrewBases.DEFAULT_ID, List.of(new BrewEffect(recipe, potency)), ingredients, List.of(), "")));

    public static final Codec<BrewRecipe> CODEC = Codec.either(MODERN, LEGACY).xmap(Either::unwrap, Either::left);

    public static final StreamCodec<ByteBuf, BrewRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BrewRecipe::id,
            ByteBufCodecs.STRING_UTF8, BrewRecipe::name,
            ByteBufCodecs.STRING_UTF8, BrewRecipe::base,
            BrewEffect.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), BrewRecipe::effects,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)), BrewRecipe::ingredients,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)), BrewRecipe::steps,
            ByteBufCodecs.STRING_UTF8, BrewRecipe::notes,
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
        return encode(registryName, crush, 0, 0);
    }

    public static String encode(String registryName, int crush, int trim, int rotation) {
        if (trim <= 0 && rotation <= 0) return crush <= 0 ? registryName : registryName + "|" + crush;
        return registryName + "|" + Math.max(0, crush) + "|" + Math.max(0, trim) + "|" + Math.max(0, rotation);
    }

    private static String nameOf(String entry) {
        int split = entry.indexOf('|');
        return split < 0 ? entry : entry.substring(0, split);
    }

    private static int field(String entry, int index) {
        String[] parts = entry.split("\\|");
        if (index >= parts.length) return 0;
        try {
            return Math.max(0, Integer.parseInt(parts[index]));
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private static int crushOf(String entry) {
        return field(entry, 1);
    }

    private static int trimOf(String entry) {
        return field(entry, 2);
    }

    private static int rotationOf(String entry) {
        return field(entry, 3);
    }

    public boolean requiresGrinding() {
        for (String entry : ingredients) {
            if (crushOf(entry) > 0) return true;
        }
        return false;
    }

    public boolean requiresTrimming() {
        for (String entry : ingredients) {
            if (trimOf(entry) > 0) return true;
        }
        return false;
    }

    public boolean requiresTurning() {
        for (String entry : ingredients) {
            if (rotationOf(entry) > 0) return true;
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
        int crush = crushOf(entry);
        if (crush > 0) stack.set(DataComponentRegistry.CRUSH.get(), crush);
        if (!(item instanceof ItemIngredient)) return stack;
        int trim = trimOf(entry);
        int rotation = rotationOf(entry);
        if (trim > 0) stack.set(DataComponentRegistry.PATH_TRIM.get(), trim);
        if (rotation > 0) stack.set(DataComponentRegistry.PATH_ROTATION.get(), rotation);
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
