package muddykat.alchemia.common.crafting;

import net.minecraft.world.effect.MobEffects;
import javax.annotation.Nullable;

import java.util.List;

public final class MagnumOpus {

    public record Stage(String result, @Nullable String previous, List<PotionEffectIngredient> potions) {
        public int potionCount() {
            return potions.size();
        }
    }

    public static final List<Stage> STAGES = List.of(
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
                    PotionEffectIngredient.of(MobEffects.DOLPHINS_GRACE, 3))),

            new Stage("citrinitas", "albedo", List.of(
                    PotionEffectIngredient.of(MobEffects.GLOWING, 3),
                    PotionEffectIngredient.of(MobEffects.FIRE_RESISTANCE, 3),
                    PotionEffectIngredient.of(MobEffects.CONDUIT_POWER, 3),
                    PotionEffectIngredient.of(MobEffects.BAD_OMEN, 3),
                    PotionEffectIngredient.of(MobEffects.HARM, 3))),

            new Stage("rubedo", "citrinitas", List.of(
                    PotionEffectIngredient.of(MobEffects.SATURATION, 3),
                    PotionEffectIngredient.of(MobEffects.REGENERATION, 3),
                    PotionEffectIngredient.of(MobEffects.ABSORPTION, 3),
                    PotionEffectIngredient.of(MobEffects.WITHER, 3),
                    PotionEffectIngredient.of(MobEffects.HEALTH_BOOST, 3))),

            new Stage("philosopher_stone", "rubedo", List.of(
                    PotionEffectIngredient.of(MobEffects.LUCK, 3),
                    PotionEffectIngredient.of(MobEffects.CONFUSION, 3),
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

    public static int maxPotionCount() {
        return STAGES.stream().mapToInt(Stage::potionCount).max().orElse(0);
    }

    private MagnumOpus() {}
}
