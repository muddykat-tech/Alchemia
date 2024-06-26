package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.helper.IngredientAlignment;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

public enum PotionEnum {
    Healing(MobEffects.HEAL, 3, IngredientAlignment.Water, IngredientAlignment.Air),
    Harm(MobEffects.HARM, 3, IngredientAlignment.Fire, IngredientAlignment.Void),
    Fire_Protection(MobEffects.FIRE_RESISTANCE, 3, IngredientAlignment.Water, IngredientAlignment.Air),
    Regeneration(MobEffects.REGENERATION, 3, IngredientAlignment.Earth, IngredientAlignment.Water),
    Luck(MobEffects.LUCK, 3, IngredientAlignment.Air, IngredientAlignment.Void),
    Speed(MobEffects.MOVEMENT_SPEED, 3, IngredientAlignment.Air, IngredientAlignment.Fire),
    Slow(MobEffects.MOVEMENT_SLOWDOWN, 3, IngredientAlignment.Earth, IngredientAlignment.Void),
    Dig_Speed(MobEffects.DIG_SPEED, 3, IngredientAlignment.Fire, IngredientAlignment.Earth),
    Dig_Slowdown(MobEffects.DIG_SLOWDOWN, 3, IngredientAlignment.Water, IngredientAlignment.Void),
    Damage_Boost(MobEffects.DAMAGE_BOOST, 3, IngredientAlignment.Fire, IngredientAlignment.Air),
    Jump(MobEffects.JUMP, 3, IngredientAlignment.Fire, IngredientAlignment.Void),
    Confusion(MobEffects.CONFUSION, 3, IngredientAlignment.Void, IngredientAlignment.Water),
    Damage_Resistance(MobEffects.DAMAGE_RESISTANCE, 3, IngredientAlignment.Earth, IngredientAlignment.Void),
    Water_Breathing(MobEffects.WATER_BREATHING, 3, IngredientAlignment.Air, IngredientAlignment.Water),
    Invisibility(MobEffects.INVISIBILITY, 3, IngredientAlignment.Void, IngredientAlignment.Air),
    Blindness(MobEffects.BLINDNESS, 3, IngredientAlignment.Void, IngredientAlignment.Earth),
    Night_Vision(MobEffects.NIGHT_VISION, 3, IngredientAlignment.Air, IngredientAlignment.Void),
    Hunger(MobEffects.HUNGER, 3, IngredientAlignment.Void, IngredientAlignment.Fire),
    Weakness(MobEffects.WEAKNESS, 3, IngredientAlignment.Fire, IngredientAlignment.Earth),
    Poison(MobEffects.POISON, 3, IngredientAlignment.Fire, IngredientAlignment.Water),
    Wither(MobEffects.WITHER, 3, IngredientAlignment.Void, IngredientAlignment.Fire),
    Health_Boost(MobEffects.HEALTH_BOOST, 3, IngredientAlignment.Earth, IngredientAlignment.Water),
    Absorption(MobEffects.ABSORPTION, 3, IngredientAlignment.Water, IngredientAlignment.Void),
    Saturation(MobEffects.SATURATION, 3, IngredientAlignment.Air, IngredientAlignment.Void),
    Glowing(MobEffects.GLOWING, 3, IngredientAlignment.Void, IngredientAlignment.Air),
    Levitation(MobEffects.LEVITATION, 3, IngredientAlignment.Air, IngredientAlignment.Void),
    Unluck(MobEffects.UNLUCK, 3, IngredientAlignment.Void, IngredientAlignment.Water),
    Slow_Falling(MobEffects.SLOW_FALLING, 3, IngredientAlignment.Water, IngredientAlignment.Void),
    Conduit_Power(MobEffects.CONDUIT_POWER, 3, IngredientAlignment.Water, IngredientAlignment.Air),
    Dolphins_Grace(MobEffects.DOLPHINS_GRACE, 3, IngredientAlignment.Water, IngredientAlignment.Air),
    Bad_Omen(MobEffects.BAD_OMEN, 3, IngredientAlignment.Fire, IngredientAlignment.Void);

    MobEffect effect;
    int maxStrength;
    private final IngredientAlignment primaryAlignment;
    private final IngredientAlignment secondaryAlignment;

    PotionEnum(MobEffect effect, int maxStrength, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment){
        this.effect = effect;
        this.maxStrength = maxStrength;

        this.primaryAlignment = primaryAlignment;
        this.secondaryAlignment = secondaryAlignment;
    }

    public MobEffect getEffect() {
        return effect;
    }

    public IngredientAlignment getPrimaryAlignment() {
        return primaryAlignment;
    }

    public IngredientAlignment getSecondaryAlignment() {
        return secondaryAlignment;
    }

}
