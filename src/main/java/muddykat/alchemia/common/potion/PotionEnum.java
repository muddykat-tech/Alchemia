package muddykat.alchemia.common.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public enum PotionEnum {
    Healing(MobEffects.HEAL, 3),
    Harm(MobEffects.HARM, 3),
    Fire_Protection(MobEffects.FIRE_RESISTANCE, 3),
    Regeneration(MobEffects.REGENERATION, 3),
    Luck(MobEffects.LUCK, 3),
    Speed(MobEffects.MOVEMENT_SPEED, 3),
    Slow(MobEffects.MOVEMENT_SLOWDOWN, 3);

    MobEffect effect;
    int maxStrength;

    PotionEnum(MobEffect effect, int maxStrength){
        this.effect = effect;
        this.maxStrength = maxStrength;
    }
    public MobEffect getEffect() {
        return effect;
    }


}
