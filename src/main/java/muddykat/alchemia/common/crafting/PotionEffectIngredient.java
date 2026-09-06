package muddykat.alchemia.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.registration.registers.IngredientTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record PotionEffectIngredient(List<Requirement> required) implements ICustomIngredient {

    public record Requirement(Holder<MobEffect> effect, int amplifier) {
        public static final Codec<Requirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MobEffect.CODEC.fieldOf("effect").forGetter(Requirement::effect),
                Codec.INT.optionalFieldOf("amplifier", 0).forGetter(Requirement::amplifier)
        ).apply(instance, Requirement::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Requirement> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), Requirement::effect,
                ByteBufCodecs.VAR_INT, Requirement::amplifier,
                Requirement::new);
    }

    public static final MapCodec<PotionEffectIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Requirement.CODEC.listOf().fieldOf("effects").forGetter(PotionEffectIngredient::required)
    ).apply(instance, PotionEffectIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PotionEffectIngredient> STREAM_CODEC = StreamCodec.composite(
            Requirement.STREAM_CODEC.apply(ByteBufCodecs.list()), PotionEffectIngredient::required,
            PotionEffectIngredient::new);

    public static Requirement need(Holder<MobEffect> effect, int potency) {
        return new Requirement(effect, potency - 1);
    }

    public static PotionEffectIngredient of(Holder<MobEffect> effect, int potency) {
        return new PotionEffectIngredient(List.of(need(effect, potency)));
    }

    public static PotionEffectIngredient ofAll(Requirement... required) {
        return new PotionEffectIngredient(List.of(required));
    }

    @Override
    public boolean test(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;

        int held = 0;
        for (MobEffectInstance ignored : contents.getAllEffects()) held++;
        if (held != required.size()) return false;

        for (Requirement requirement : required) {
            boolean found = false;
            for (MobEffectInstance instance : contents.getAllEffects()) {
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

    private ItemStack sampleStack() {
        ItemStack sample = new ItemStack(Items.POTION);
        List<MobEffectInstance> effects = required.stream()
                .map(requirement -> new MobEffectInstance(requirement.effect(), 3600, requirement.amplifier()))
                .toList();

        sample.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.empty(), effects, Optional.of("alchemical")));
        return sample;
    }

    @Override
    public SlotDisplay display() {
        ItemStack sample = sampleStack();
        return new SlotDisplay.ItemStackSlotDisplay(
                new ItemStackTemplate(sample.getItem(), sample.getCount(), sample.getComponentsPatch()));
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Stream.of(Items.POTION.builtInRegistryHolder(),
                Items.SPLASH_POTION.builtInRegistryHolder(),
                Items.LINGERING_POTION.builtInRegistryHolder());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return IngredientTypeRegistry.POTION_EFFECT.get();
    }
}
