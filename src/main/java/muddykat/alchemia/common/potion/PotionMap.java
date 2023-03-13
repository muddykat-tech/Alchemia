package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.AlchemicalScreen;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public class PotionMap {
    public static PotionMap INSTANCE;
    private Random rand = new Random();
    private int size;
    private int middlePosition;
    public PotionMap(long seed) {
        Alchemia.LOGGER.info("Potion Map has been Assigned");
        rand.setSeed(seed);
        size = PotionEnum.values().length * 5;
        middlePosition = Math.round((int) (size / 2));

        for (PotionEnum e : PotionEnum.values()) {
            MobEffect effect = e.getEffect();
            int strength = e.maxStrength;
            int randX = rand.nextInt(size);
            int randY = rand.nextInt(size);

            // if the distance from the random position is far enough, allow it to be selected!
            if(Math.abs(randX - middlePosition) > 2 && Math.abs(randY - middlePosition) > 2) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int modifierX = i % 2;
                        int modifierY = j % 2;
                        String position = String.valueOf(randX + i) + "," + String.valueOf(randY + j);

                        if (!effectHashMap.containsKey(position)) {
                            effectHashMap.put(position, new PotionEffectPosition(effect, 1200, strength - (modifierX + modifierY)));
                        }
                    }
                }
            }
        }
    }
    public final HashMap<String, PotionEffectPosition> effectHashMap = new HashMap<>();

    public static void scramble(long seed) {
        INSTANCE = new PotionMap(seed);
    }

    public PotionEffectPosition getEffectPotion(int[] alignment){

        return effectHashMap.getOrDefault(alignment[0] + "," + alignment[1], new PotionEffectPosition(MobEffects.UNLUCK, 10, 0));

    }

    public int getMiddlePosition() {
        return middlePosition;
    }

    public int getMaxAlignment() {
        return size;
    }

    public static class PotionEffectPosition {
        final MobEffect effect;
        final int duration;
        final int strength;
        PotionEffectPosition(MobEffect effect, int duration, int maxStrength) {
            this.effect = effect;
            this.duration = duration;
            this.strength = maxStrength;
        }

        public MobEffect getEffect() {
            return effect;
        }

        public int getDuration() {
            return duration;
        }

        public int getStrength() {
            return strength;
        }
    }
}
