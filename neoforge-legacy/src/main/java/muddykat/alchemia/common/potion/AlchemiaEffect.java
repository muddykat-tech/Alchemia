package muddykat.alchemia.common.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Rarity;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class AlchemiaEffect {

    public static final int MAX_LEVEL = 10;
    public static final int DEFAULT_LEVEL = 3;
    public static final int DEFAULT_DURATION = 60;
    public static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

    public static final Codec<IngredientAlignment> ALIGNMENT_CODEC = Codec.STRING.comapFlatMap(AlchemiaEffect::parseAlignment,
            alignment -> alignment.name().toLowerCase(Locale.ROOT));

    public static final Codec<AlchemiaEffect> CODEC = RecordCodecBuilder.<AlchemiaEffect>create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(AlchemiaEffect::id),
            ResourceLocation.CODEC.fieldOf("effect").forGetter(AlchemiaEffect::effectId),
            defaulted(Codec.intRange(1, MAX_LEVEL), "max_level", DEFAULT_LEVEL).forGetter(AlchemiaEffect::maxLevel),
            defaulted(Rarity.CODEC, "rarity", Rarity.COMMON).forGetter(AlchemiaEffect::rarity),
            ALIGNMENT_CODEC.fieldOf("primary").forGetter(AlchemiaEffect::primary),
            defaulted(ALIGNMENT_CODEC, "secondary", IngredientAlignment.Void).forGetter(AlchemiaEffect::secondary),
            defaulted(Codec.intRange(1, 86400), "duration", DEFAULT_DURATION).forGetter(AlchemiaEffect::duration),
            ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(AlchemiaEffect::icon),
            Codec.STRING.optionalFieldOf("description").forGetter(AlchemiaEffect::description),
            Codec.STRING.listOf().optionalFieldOf("bases", List.of()).forGetter(AlchemiaEffect::bases)
    ).apply(instance, AlchemiaEffect::new)).validate(AlchemiaEffect::verify);

    public static final Codec<List<AlchemiaEffect>> LIST_CODEC = CODEC.listOf();

    private final String id;
    private final ResourceLocation effectId;
    private final int maxLevel;
    private final Rarity rarity;
    private final IngredientAlignment primary;
    private final IngredientAlignment secondary;
    private final int duration;
    private final Optional<ResourceLocation> icon;
    private final Optional<String> description;
    private final List<String> bases;
    private final @Nullable Holder<MobEffect> effect;

    public AlchemiaEffect(String id, ResourceLocation effectId, int maxLevel, Rarity rarity, IngredientAlignment primary,
                          IngredientAlignment secondary, int duration, Optional<ResourceLocation> icon, Optional<String> description,
                          List<String> bases) {
        this.id = id;
        this.effectId = effectId;
        this.maxLevel = maxLevel;
        this.rarity = rarity;
        this.primary = primary;
        this.secondary = secondary;
        this.duration = duration;
        this.icon = icon;
        this.description = description;
        this.bases = bases.stream().map(base -> base.toLowerCase(Locale.ROOT)).toList();
        this.effect = BuiltInRegistries.MOB_EFFECT.getHolder(effectId).<Holder<MobEffect>>map(holder -> holder).orElse(null);
    }

    public static AlchemiaEffect builtIn(String id, String effect, IngredientAlignment primary, IngredientAlignment secondary,
                                         Rarity rarity) {
        return new AlchemiaEffect(id, ResourceLocation.withDefaultNamespace(effect), DEFAULT_LEVEL, rarity, primary, secondary,
                DEFAULT_DURATION, Optional.empty(), Optional.empty(), List.of());
    }

    private static <T> MapCodec<T> defaulted(Codec<T> codec, String name, T fallback) {
        return codec.optionalFieldOf(name).xmap(value -> value.orElse(fallback), Optional::of);
    }

    private static DataResult<AlchemiaEffect> verify(AlchemiaEffect effect) {
        if (!ID_PATTERN.matcher(effect.id).matches()) {
            return DataResult.error(() -> "Effect id '" + effect.id + "' may only use letters, digits and underscores");
        }
        return DataResult.success(effect);
    }

    private static DataResult<IngredientAlignment> parseAlignment(String value) {
        for (IngredientAlignment alignment : IngredientAlignment.values()) {
            if (alignment.name().equalsIgnoreCase(value.trim())) return DataResult.success(alignment);
        }
        return DataResult.error(() -> "Unknown alignment '" + value + "', expected fire, water, earth, air or void");
    }

    public static ResourceLocation defaultSprite(ResourceLocation effectId) {
        return effectId.withPrefix("mob_effect/");
    }

    public String id() {
        return id;
    }

    public ResourceLocation effectId() {
        return effectId;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public int maxAmplifier() {
        return maxLevel - 1;
    }

    public Rarity rarity() {
        return rarity;
    }

    public IngredientAlignment primary() {
        return primary;
    }

    public IngredientAlignment secondary() {
        return secondary;
    }

    public int duration() {
        return duration;
    }

    public int durationTicks() {
        return duration * 20;
    }

    public Optional<ResourceLocation> icon() {
        return icon;
    }

    public Optional<String> description() {
        return description;
    }

    public List<String> bases() {
        return bases;
    }

    public boolean resolved() {
        return effect != null;
    }

    public Holder<MobEffect> effect() {
        if (effect == null) throw new IllegalStateException("Effect " + effectId + " for " + id + " is not registered");
        return effect;
    }

    public ResourceLocation sprite() {
        return icon.orElseGet(() -> defaultSprite(effectId));
    }

    public Component displayName() {
        return effect().value().getDisplayName();
    }

    public @Nullable Component descriptionText() {
        return description.map(text -> (Component) Component.translatableWithFallback(text, text)).orElse(null);
    }

    public int color() {
        return effect().value().getColor();
    }

    public int alignmentX() {
        return primary.getX() + secondary.getX();
    }

    public int alignmentY() {
        return primary.getY() + secondary.getY();
    }

    public boolean listsBase(String base) {
        return bases.contains(base.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AlchemiaEffect effect && effect.id.equalsIgnoreCase(id);
    }

    @Override
    public int hashCode() {
        return id.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
