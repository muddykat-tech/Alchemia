package muddykat.alchemia.common.potion;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidStack;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class BrewBase {

    public static final int MAX_FILL_LEVEL = 4;
    public static final int DEFAULT_TINT = 0xFFFFFF;

    public static final Codec<Integer> COLOR_CODEC = Codec.either(Codec.STRING, Codec.INT)
            .comapFlatMap(either -> either.map(BrewBase::parseColor, DataResult::success),
                    color -> Either.left(String.format("#%06X", color & 0xFFFFFF)));

    public record Fill(Optional<ResourceLocation> item, Optional<ResourceLocation> fluid, int levels,
                       Optional<ResourceLocation> remainder, Optional<Integer> amount) {

        public static final Codec<Fill> CODEC = RecordCodecBuilder.<Fill>create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("item").forGetter(Fill::item),
                ResourceLocation.CODEC.optionalFieldOf("fluid").forGetter(Fill::fluid),
                Codec.intRange(1, MAX_FILL_LEVEL).optionalFieldOf("levels", 1).forGetter(Fill::levels),
                ResourceLocation.CODEC.optionalFieldOf("remainder").forGetter(Fill::remainder),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("amount").forGetter(Fill::amount)
        ).apply(instance, Fill::new)).validate(Fill::verify);

        private static DataResult<Fill> verify(Fill fill) {
            if (fill.item.isPresent() == fill.fluid.isPresent()) {
                return DataResult.error(() -> "A fill entry needs exactly one of \"item\" or \"fluid\"");
            }
            if (fill.isFluid() && fill.remainder.isPresent()) {
                return DataResult.error(() -> "\"remainder\" only applies to item fills");
            }
            if (!fill.isFluid() && fill.amount.isPresent()) {
                return DataResult.error(() -> "\"amount\" only applies to fluid fills");
            }
            return DataResult.success(fill);
        }

        public int fluidAmount() {
            return amount.orElseGet(() -> Math.max(1, FluidType.BUCKET_VOLUME * levels / MAX_FILL_LEVEL));
        }

        public boolean isFluid() {
            return fluid.isPresent();
        }

        public @Nullable Item itemValue() {
            return resolveItem(item);
        }

        public @Nullable Item remainderValue() {
            return resolveItem(remainder);
        }

        public Fluid fluidValue() {
            return resolveFluid(fluid);
        }

        public boolean matches(ItemStack stack) {
            Item match = itemValue();
            return match != null && !stack.isEmpty() && stack.is(match);
        }

        public boolean matches(FluidStack resource) {
            Fluid match = fluidValue();
            return match != Fluids.EMPTY && !resource.isEmpty() && resource.getFluid().isSame(match);
        }
    }

    public static final Codec<BrewBase> CODEC = RecordCodecBuilder.<BrewBase>create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(BrewBase::id),
            Fill.CODEC.listOf(1, 64).fieldOf("fills").forGetter(BrewBase::fills),
            Codec.intRange(1, 64).optionalFieldOf("instability_cap", 1).forGetter(BrewBase::instabilityCap),
            COLOR_CODEC.optionalFieldOf("tint").forGetter(BrewBase::tint),
            Codec.BOOL.optionalFieldOf("biome_tint", false).forGetter(BrewBase::biomeTint),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(BrewBase::texture),
            ResourceLocation.CODEC.optionalFieldOf("fluid").forGetter(BrewBase::fluid),
            ResourceLocation.CODEC.optionalFieldOf("unbrewed_result").forGetter(BrewBase::unbrewedResult),
            Codec.STRING.listOf().optionalFieldOf("effects").forGetter(BrewBase::effects)
    ).apply(instance, BrewBase::new)).validate(BrewBase::verify);

    public static final Codec<List<BrewBase>> LIST_CODEC = CODEC.listOf();

    private final String id;
    private final List<Fill> fills;
    private final int instabilityCap;
    private final Optional<Integer> tint;
    private final boolean biomeTint;
    private final Optional<ResourceLocation> texture;
    private final Optional<ResourceLocation> fluid;
    private final Optional<ResourceLocation> unbrewedResult;
    private final Optional<List<String>> effects;
    private final @Nullable Set<String> allowedEffects;

    public BrewBase(String id, List<Fill> fills, int instabilityCap, Optional<Integer> tint, boolean biomeTint,
                    Optional<ResourceLocation> texture, Optional<ResourceLocation> fluid, Optional<ResourceLocation> unbrewedResult,
                    Optional<List<String>> effects) {
        this.id = id;
        this.fills = List.copyOf(fills);
        this.instabilityCap = instabilityCap;
        this.tint = tint;
        this.biomeTint = biomeTint;
        this.texture = texture;
        this.fluid = fluid;
        this.unbrewedResult = unbrewedResult;
        this.effects = effects.map(List::copyOf);
        this.allowedEffects = this.effects
                .map(names -> names.stream().map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet()))
                .orElse(null);
    }

    private static DataResult<BrewBase> verify(BrewBase base) {
        if (base.id.isBlank()) return DataResult.error(() -> "A brew base needs a non-blank \"id\"");
        if (!base.id.equals(base.id.toLowerCase(Locale.ROOT))) {
            return DataResult.error(() -> "Brew base ids must be lower case: " + base.id);
        }
        return DataResult.success(base);
    }

    public String id() {
        return id;
    }

    public List<Fill> fills() {
        return fills;
    }

    public int instabilityCap() {
        return instabilityCap;
    }

    public Optional<Integer> tint() {
        return tint;
    }

    public int tintOrDefault() {
        return tint.orElse(DEFAULT_TINT);
    }

    public boolean biomeTint() {
        return biomeTint;
    }

    public Optional<ResourceLocation> texture() {
        return texture;
    }

    public Optional<ResourceLocation> fluid() {
        return fluid;
    }

    public Fluid fluidValue() {
        return resolveFluid(fluid);
    }

    public Optional<ResourceLocation> unbrewedResult() {
        return unbrewedResult;
    }

    public Optional<List<String>> effects() {
        return effects;
    }

    public ItemStack unbrewedStack() {
        Item item = resolveItem(unbrewedResult);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public boolean allows(@Nullable PotionEnum effect) {
        if (effect == null || allowedEffects == null) return true;
        return allowedEffects.contains(effect.name().toLowerCase(Locale.ROOT));
    }

    public @Nullable Fill fillFor(ItemStack stack) {
        for (Fill fill : fills) {
            if (!fill.isFluid() && fill.matches(stack)) return fill;
        }
        return null;
    }

    public @Nullable Fill fillFor(FluidStack resource) {
        for (Fill fill : fills) {
            if (fill.isFluid() && fill.matches(resource)) return fill;
        }
        return null;
    }

    public String translationKey() {
        return "alchemia.base." + id;
    }

    public Component displayName() {
        return Component.translatableWithFallback(translationKey(), prettyName());
    }

    private String prettyName() {
        String cleaned = id.replace('_', ' ');
        return cleaned.isEmpty() ? cleaned : Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private static @Nullable Item resolveItem(Optional<ResourceLocation> id) {
        return id.map(name -> BuiltInRegistries.ITEM.getValue(name)).orElse(null);
    }

    private static Fluid resolveFluid(Optional<ResourceLocation> id) {
        Fluid resolved = id.map(name -> BuiltInRegistries.FLUID.getValue(name)).orElse(null);
        return resolved == null ? Fluids.EMPTY : resolved;
    }

    private static DataResult<Integer> parseColor(String value) {
        String cleaned = value.trim();
        if (cleaned.startsWith("#")) cleaned = cleaned.substring(1);
        else if (cleaned.startsWith("0x") || cleaned.startsWith("0X")) cleaned = cleaned.substring(2);

        try {
            return DataResult.success((int) (Long.parseLong(cleaned, 16) & 0xFFFFFFFFL));
        } catch (NumberFormatException error) {
            return DataResult.error(() -> "Not a hex colour: " + value);
        }
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof BrewBase base && id.equals(base.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "BrewBase[" + id + "]";
    }
}
