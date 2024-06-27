package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.helper.IngredientAlignment;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

public enum PotionEnum {
    Healing(MobEffects.HEAL, 3, IngredientAlignment.Water, IngredientAlignment.Air, Rarity.RARE),
    Harm(MobEffects.HARM, 3, IngredientAlignment.Fire, IngredientAlignment.Void, Rarity.RARE),
    Fire_Protection(MobEffects.FIRE_RESISTANCE, 3, IngredientAlignment.Water, IngredientAlignment.Void, Rarity.UNCOMMON),
    Regeneration(MobEffects.REGENERATION, 3, IngredientAlignment.Earth, IngredientAlignment.Water, Rarity.UNCOMMON),
    Luck(MobEffects.LUCK, 3, IngredientAlignment.Air, IngredientAlignment.Void, Rarity.EPIC),
    Speed(MobEffects.MOVEMENT_SPEED, 3, IngredientAlignment.Air, IngredientAlignment.Fire, Rarity.COMMON),
    Slow(MobEffects.MOVEMENT_SLOWDOWN, 3, IngredientAlignment.Earth, IngredientAlignment.Void, Rarity.COMMON),
    Dig_Speed(MobEffects.DIG_SPEED, 3, IngredientAlignment.Fire, IngredientAlignment.Earth, Rarity.UNCOMMON),
    Dig_Slowdown(MobEffects.DIG_SLOWDOWN, 3, IngredientAlignment.Water, IngredientAlignment.Void, Rarity.UNCOMMON),
    Damage_Boost(MobEffects.DAMAGE_BOOST, 3, IngredientAlignment.Fire, IngredientAlignment.Air, Rarity.RARE),
    Jump(MobEffects.JUMP, 3, IngredientAlignment.Fire, IngredientAlignment.Void, Rarity.EPIC),
    Confusion(MobEffects.CONFUSION, 3, IngredientAlignment.Void, IngredientAlignment.Water, Rarity.EPIC),
    Damage_Resistance(MobEffects.DAMAGE_RESISTANCE, 3, IngredientAlignment.Earth, IngredientAlignment.Void, Rarity.EPIC),
    Water_Breathing(MobEffects.WATER_BREATHING, 3, IngredientAlignment.Air, IngredientAlignment.Water, Rarity.RARE),
    Invisibility(MobEffects.INVISIBILITY, 3, IngredientAlignment.Void, IngredientAlignment.Air, Rarity.EPIC),
    Blindness(MobEffects.BLINDNESS, 3, IngredientAlignment.Void, IngredientAlignment.Earth, Rarity.UNCOMMON),
    Night_Vision(MobEffects.NIGHT_VISION, 3, IngredientAlignment.Air, IngredientAlignment.Fire, Rarity.RARE),
    Hunger(MobEffects.HUNGER, 3, IngredientAlignment.Void, IngredientAlignment.Air, Rarity.COMMON),
    Weakness(MobEffects.WEAKNESS, 3, IngredientAlignment.Fire, IngredientAlignment.Earth, Rarity.UNCOMMON),
    Poison(MobEffects.POISON, 3, IngredientAlignment.Fire, IngredientAlignment.Water, Rarity.COMMON),
    Wither(MobEffects.WITHER, 3, IngredientAlignment.Void, IngredientAlignment.Fire, Rarity.EPIC),
    Health_Boost(MobEffects.HEALTH_BOOST, 3, IngredientAlignment.Earth, IngredientAlignment.Water, Rarity.EPIC),
    Absorption(MobEffects.ABSORPTION, 3, IngredientAlignment.Water, IngredientAlignment.Void, Rarity.RARE),
    Saturation(MobEffects.SATURATION, 3, IngredientAlignment.Earth, IngredientAlignment.Void, Rarity.UNCOMMON),
    Glowing(MobEffects.GLOWING, 3, IngredientAlignment.Air, IngredientAlignment.Fire, Rarity.RARE),
    Levitation(MobEffects.LEVITATION, 3, IngredientAlignment.Air, IngredientAlignment.Void, Rarity.UNCOMMON),
    Unluck(MobEffects.UNLUCK, 3, IngredientAlignment.Earth, IngredientAlignment.Void, Rarity.EPIC),
    Slow_Falling(MobEffects.SLOW_FALLING, 3, IngredientAlignment.Water, IngredientAlignment.Void, Rarity.RARE),
    Conduit_Power(MobEffects.CONDUIT_POWER, 3, IngredientAlignment.Water, IngredientAlignment.Void, Rarity.EPIC),
    Dolphins_Grace(MobEffects.DOLPHINS_GRACE, 3, IngredientAlignment.Water, IngredientAlignment.Air, Rarity.RARE),
    Bad_Omen(MobEffects.BAD_OMEN, 3, IngredientAlignment.Fire, IngredientAlignment.Void, Rarity.EPIC);

    MobEffect effect;
    int maxStrength;
    private final IngredientAlignment primaryAlignment;
    private final IngredientAlignment secondaryAlignment;
    private final Rarity rarity;

    PotionEnum(MobEffect effect, int maxStrength, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment, Rarity rarity){
        this.effect = effect;
        this.maxStrength = maxStrength - 1;
        this.rarity = rarity;
        this.primaryAlignment = primaryAlignment;
        this.secondaryAlignment = secondaryAlignment;
    }

    public MobEffect getEffect() {
        return effect;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public IngredientAlignment getPrimaryAlignment() {
        return primaryAlignment;
    }

    public IngredientAlignment getSecondaryAlignment() {
        return secondaryAlignment;
    }

    public Potion getPotion() {
        // Map each MobEffect to its corresponding Potion
        if (this == PotionEnum.Healing) {
            return Potions.REGENERATION;
        } else if (this == PotionEnum.Harm) {
            return Potions.HARMING;
        } else if (this == PotionEnum.Fire_Protection) {
            return Potions.FIRE_RESISTANCE;
        } else if (this == PotionEnum.Regeneration) {
            return Potions.REGENERATION;
        } else if (this == PotionEnum.Luck) {
            return Potions.LUCK;
        } else if (this == PotionEnum.Speed) {
            return Potions.SWIFTNESS;
        } else if (this == PotionEnum.Slow) {
            return Potions.SLOWNESS;
        } else if (this == PotionEnum.Dig_Speed) {
            return Potions.STRONG_SWIFTNESS;
        } else if (this == PotionEnum.Dig_Slowdown) {
            return Potions.STRONG_SLOWNESS;
        } else if (this == PotionEnum.Damage_Boost) {
            return Potions.STRENGTH;
        } else if (this == PotionEnum.Jump) {
            return Potions.LEAPING;
        } else if (this == PotionEnum.Confusion) {
            return Potions.WEAKNESS;
        } else if (this == PotionEnum.Damage_Resistance) {
            return Potions.TURTLE_MASTER;
        } else if (this == PotionEnum.Water_Breathing) {
            return Potions.WATER_BREATHING;
        } else if (this == PotionEnum.Invisibility) {
            return Potions.INVISIBILITY;
        } else if (this == PotionEnum.Blindness) {
            return Potions.NIGHT_VISION;
        } else if (this == PotionEnum.Night_Vision) {
            return Potions.NIGHT_VISION;
        } else if (this == PotionEnum.Hunger) {
            return Potions.THICK;
        } else if (this == PotionEnum.Weakness) {
            return Potions.WEAKNESS;
        } else if (this == PotionEnum.Poison) {
            return Potions.POISON;
        } else if (this == PotionEnum.Wither) {
            return Potions.HARMING;
        } else if (this == PotionEnum.Health_Boost) {
            return Potions.STRONG_HEALING;
        } else if (this == PotionEnum.Absorption) {
            return Potions.LONG_FIRE_RESISTANCE; // Example mapping, adjust as needed
        } else if (this == PotionEnum.Saturation) {
            return Potions.STRONG_REGENERATION;
        } else if (this == PotionEnum.Glowing) {
            return Potions.FIRE_RESISTANCE;
        } else if (this == PotionEnum.Levitation) {
            return Potions.SLOW_FALLING;
        } else if (this == PotionEnum.Unluck) {
            return Potions.HARMING;
        } else if (this == PotionEnum.Slow_Falling) {
            return Potions.SLOW_FALLING;
        } else if (this == PotionEnum.Conduit_Power) {
            return Potions.NIGHT_VISION;
        } else if (this == PotionEnum.Dolphins_Grace) {
            return Potions.SWIFTNESS;
        } else if (this == PotionEnum.Bad_Omen) {
            return Potions.HARMING;
        } else {
            return Potions.AWKWARD;
        }
    }

    public IngredientAlignment[] getAlignments() {
        IngredientAlignment[] alignments = new IngredientAlignment[2];
        alignments[0] = primaryAlignment;
        alignments[1] = secondaryAlignment;
        return alignments;
    }
}
