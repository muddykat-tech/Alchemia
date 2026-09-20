package muddykat.alchemia.common.crafting;

import net.minecraft.world.effect.MobEffects;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class MagnumOpus {

    public record Stage(String result, @Nullable String previous, List<PotionEffectIngredient> potions) {
        public int potionCount() {
            return potions.size();
        }
    }

    public static final List<Stage> STAGES = List.of(
            new Stage("nigredo", null, List.of(
                    PotionEffectIngredient.of(MobEffects.SLOWNESS, 3),
                    PotionEffectIngredient.of(MobEffects.RESISTANCE, 3),
                    PotionEffectIngredient.of(MobEffects.POISON, 3),
                    PotionEffectIngredient.of(MobEffects.STRENGTH, 3),
                    PotionEffectIngredient.of(MobEffects.BLINDNESS, 3))),

            new Stage("albedo", "nigredo", List.of(
                    PotionEffectIngredient.of(MobEffects.INVISIBILITY, 3),
                    PotionEffectIngredient.of(MobEffects.SPEED, 3),
                    PotionEffectIngredient.of(MobEffects.NIGHT_VISION, 3),
                    PotionEffectIngredient.of(MobEffects.HASTE, 3),
                    PotionEffectIngredient.of(MobEffects.LEVITATION, 3),
                    PotionEffectIngredient.of(MobEffects.DOLPHINS_GRACE, 3))),

            new Stage("citrinitas", "albedo", List.of(
                    PotionEffectIngredient.of(MobEffects.GLOWING, 3),
                    PotionEffectIngredient.of(MobEffects.FIRE_RESISTANCE, 3),
                    PotionEffectIngredient.of(MobEffects.CONDUIT_POWER, 3),
                    PotionEffectIngredient.of(MobEffects.BAD_OMEN, 3),
                    PotionEffectIngredient.of(MobEffects.INSTANT_DAMAGE, 3))),

            new Stage("rubedo", "citrinitas", List.of(
                    PotionEffectIngredient.of(MobEffects.SATURATION, 3),
                    PotionEffectIngredient.of(MobEffects.REGENERATION, 3),
                    PotionEffectIngredient.of(MobEffects.ABSORPTION, 3),
                    PotionEffectIngredient.of(MobEffects.WITHER, 3),
                    PotionEffectIngredient.of(MobEffects.HEALTH_BOOST, 3))),

            new Stage("philosopher_stone", "rubedo", List.of(
                    PotionEffectIngredient.of(MobEffects.LUCK, 3),
                    PotionEffectIngredient.of(MobEffects.NAUSEA, 3),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.POISON, 1),
                            PotionEffectIngredient.need(MobEffects.FIRE_RESISTANCE, 1),
                            PotionEffectIngredient.need(MobEffects.INSTANT_DAMAGE, 1),
                            PotionEffectIngredient.need(MobEffects.SLOW_FALLING, 1),
                            PotionEffectIngredient.need(MobEffects.SLOWNESS, 1)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.ABSORPTION, 1),
                            PotionEffectIngredient.need(MobEffects.GLOWING, 1),
                            PotionEffectIngredient.need(MobEffects.INSTANT_HEALTH, 1),
                            PotionEffectIngredient.need(MobEffects.HEALTH_BOOST, 1),
                            PotionEffectIngredient.need(MobEffects.DOLPHINS_GRACE, 1)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.BAD_OMEN, 1),
                            PotionEffectIngredient.need(MobEffects.WITHER, 1),
                            PotionEffectIngredient.need(MobEffects.WEAKNESS, 1),
                            PotionEffectIngredient.need(MobEffects.INVISIBILITY, 1),
                            PotionEffectIngredient.need(MobEffects.INSTANT_DAMAGE, 1)),
                    PotionEffectIngredient.ofAll(
                            PotionEffectIngredient.need(MobEffects.ABSORPTION, 2),
                            PotionEffectIngredient.need(MobEffects.FIRE_RESISTANCE, 1),
                            PotionEffectIngredient.need(MobEffects.RESISTANCE, 2)))));

    public static int maxPotionCount() {
        return STAGES.stream().mapToInt(Stage::potionCount).max().orElse(0);
    }

    private MagnumOpus() {}
}
