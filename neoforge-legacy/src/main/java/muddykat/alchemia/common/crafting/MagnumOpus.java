package muddykat.alchemia.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffects;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;

public final class MagnumOpus {

    public record Stage(String result, @Nullable String previous, List<PotionEffectIngredient> potions) {
        public static final Codec<Stage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("result").forGetter(Stage::result),
                Codec.STRING.optionalFieldOf("previous").forGetter(stage -> Optional.ofNullable(stage.previous())),
                PotionEffectIngredient.CODEC.codec().listOf().fieldOf("potions").forGetter(Stage::potions)
        ).apply(instance, (result, previous, potions) -> new Stage(result, previous.orElse(null), List.copyOf(potions))));

        public static final StreamCodec<RegistryFriendlyByteBuf, Stage> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Stage::result,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), stage -> Optional.ofNullable(stage.previous()),
                PotionEffectIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), Stage::potions,
                (result, previous, potions) -> new Stage(result, previous.orElse(null), List.copyOf(potions)));

        public int potionCount() {
            return potions.size();
        }
    }

    public static final List<Stage> CLASSIC = List.of(
            new Stage("nigredo", null, List.of(
                    PotionEffectIngredient.of(MobEffects.MOVEMENT_SLOWDOWN, 3),
                    PotionEffectIngredient.of(MobEffects.DAMAGE_RESISTANCE, 3),
                    PotionEffectIngredient.of(MobEffects.POISON, 3),
                    PotionEffectIngredient.of(MobEffects.DAMAGE_BOOST, 3),
                    PotionEffectIngredient.of(MobEffects.BLINDNESS, 3))),

            new Stage("albedo", "nigredo", List.of(
                    PotionEffectIngredient.of(MobEffects.INVISIBILITY, 3),
                    PotionEffectIngredient.of(MobEffects.MOVEMENT_SPEED, 3),
                    PotionEffectIngredient.of(MobEffects.NIGHT_VISION, 3),
                    PotionEffectIngredient.of(MobEffects.DIG_SPEED, 3),
                    PotionEffectIngredient.of(MobEffects.LEVITATION, 3),
                    PotionEffectIngredient.of(MobEffects.DOLPHINS_GRACE, 3),
                    PotionEffectIngredient.of(MobEffects.JUMP, 3))),

            new Stage("citrinitas", "albedo", List.of(
                    PotionEffectIngredient.of(MobEffects.GLOWING, 3),
                    PotionEffectIngredient.of(MobEffects.FIRE_RESISTANCE, 3),
                    PotionEffectIngredient.of(MobEffects.CONDUIT_POWER, 3),
                    PotionEffectIngredient.of(MobEffects.BAD_OMEN, 3),
                    PotionEffectIngredient.of(MobEffects.HARM, 3),
                    PotionEffectIngredient.of(MobEffects.DAMAGE_BOOST, 3),
                    PotionEffectIngredient.of(MobEffects.DIG_SPEED, 3),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.FIRE_RESISTANCE, 2),
                            PotionEffectIngredient.need(MobEffects.WATER_BREATHING, 2)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.GLOWING, 1),
                            PotionEffectIngredient.need(MobEffects.NIGHT_VISION, 2),
                            PotionEffectIngredient.need(MobEffects.MOVEMENT_SPEED, 2)))),

            new Stage("rubedo", "citrinitas", List.of(
                    PotionEffectIngredient.of(MobEffects.SATURATION, 3),
                    PotionEffectIngredient.of(MobEffects.REGENERATION, 3),
                    PotionEffectIngredient.of(MobEffects.ABSORPTION, 3),
                    PotionEffectIngredient.of(MobEffects.WITHER, 3),
                    PotionEffectIngredient.of(MobEffects.HEALTH_BOOST, 3),
                    PotionEffectIngredient.of(MobEffects.HEAL, 3),
                    PotionEffectIngredient.of(MobEffects.DAMAGE_RESISTANCE, 3),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.REGENERATION, 2),
                            PotionEffectIngredient.need(MobEffects.POISON, 2)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.HEALTH_BOOST, 1),
                            PotionEffectIngredient.need(MobEffects.WITHER, 2),
                            PotionEffectIngredient.need(MobEffects.HEAL, 2)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.ABSORPTION, 2),
                            PotionEffectIngredient.need(MobEffects.HUNGER, 1),
                            PotionEffectIngredient.need(MobEffects.SATURATION, 2)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.DAMAGE_BOOST, 2),
                            PotionEffectIngredient.need(MobEffects.WEAKNESS, 2)))),

            new Stage("philosopher_stone", "rubedo", List.of(
                    PotionEffectIngredient.of(MobEffects.LUCK, 3),
                    PotionEffectIngredient.of(MobEffects.CONFUSION, 3),
                    PotionEffectIngredient.of(MobEffects.INVISIBILITY, 3),
                    PotionEffectIngredient.of(MobEffects.HEALTH_BOOST, 3),
                    PotionEffectIngredient.of(MobEffects.FIRE_RESISTANCE, 3),
                    PotionEffectIngredient.of(MobEffects.SLOW_FALLING, 3),
                    PotionEffectIngredient.of(MobEffects.DAMAGE_RESISTANCE, 3),
                    PotionEffectIngredient.of(MobEffects.REGENERATION, 3),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.POISON, 1),
                            PotionEffectIngredient.need(MobEffects.FIRE_RESISTANCE, 1),
                            PotionEffectIngredient.need(MobEffects.HARM, 1),
                            PotionEffectIngredient.need(MobEffects.SLOW_FALLING, 1),
                            PotionEffectIngredient.need(MobEffects.MOVEMENT_SLOWDOWN, 1)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.ABSORPTION, 1),
                            PotionEffectIngredient.need(MobEffects.GLOWING, 1),
                            PotionEffectIngredient.need(MobEffects.HEAL, 1),
                            PotionEffectIngredient.need(MobEffects.HEALTH_BOOST, 1),
                            PotionEffectIngredient.need(MobEffects.DOLPHINS_GRACE, 1)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.BAD_OMEN, 1),
                            PotionEffectIngredient.need(MobEffects.WITHER, 1),
                            PotionEffectIngredient.need(MobEffects.WEAKNESS, 1),
                            PotionEffectIngredient.need(MobEffects.INVISIBILITY, 1),
                            PotionEffectIngredient.need(MobEffects.HARM, 1)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.ABSORPTION, 2),
                            PotionEffectIngredient.need(MobEffects.FIRE_RESISTANCE, 1),
                            PotionEffectIngredient.need(MobEffects.DAMAGE_RESISTANCE, 2)))));

    private static volatile List<Stage> active = CLASSIC;
    private static volatile int generation;

    public static List<Stage> stages() {
        return active;
    }

    public static int generation() {
        return generation;
    }

    public static synchronized void install(List<Stage> stages) {
        List<Stage> next = stages.size() == CLASSIC.size() ? List.copyOf(stages) : CLASSIC;
        if (next.equals(active)) return;
        active = next;
        generation++;
    }

    public static int maxPotionCount() {
        return active.stream().mapToInt(Stage::potionCount).max().orElse(0);
    }

    private MagnumOpus() {}
}
