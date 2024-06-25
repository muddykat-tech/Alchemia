package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.*;

public class PotionMap {
    public static PotionMap INSTANCE;
    private final int size;
    private final int middlePosition;
    public PotionMap(long seed) {
        Alchemia.LOGGER.info("Potion Map has been Assigned");
        Random rand = new Random();
        rand.setSeed(seed);
        size = PotionEnum.values().length * 5;
        middlePosition = Math.round(((float) size / 2));

        for (PotionEnum e : PotionEnum.values()) {
            MobEffect effect = e.getEffect();
            int strength = e.maxStrength;
            int randX = rand.nextInt(size);
            int randY = rand.nextInt(size);

            // if the distance from the random position is far enough, allow it to be selected!
            if(Math.abs(randX - middlePosition) > 3 && Math.abs(randY - middlePosition) > 3) {
                String position = String.valueOf(randX) + "," + String.valueOf(randY);

                if (!effectHashMap.containsKey(position)) {
                    effectHashMap.put(position, new PotionEffectPosition(effect, 1200, strength));
                }
            }
        }
    }
    public final HashMap<String, PotionEffectPosition> effectHashMap = new HashMap<>();

    public static void scramble(long seed) {
        INSTANCE = new PotionMap(seed);
    }

    public PotionEffectPosition getEffectPotion(int[] alignment){
        String key = alignment[0] + "," + alignment[1];
        PotionEffectPosition defaultEffect = new PotionEffectPosition(MobEffects.UNLUCK, 10, 0);

        PotionEffectPosition closestEffect = null;
        double closestDistance = Double.MAX_VALUE;

        int maxDistance = 4;

        for (Map.Entry<String, PotionEffectPosition> entry : effectHashMap.entrySet()) {
            String positionKey = entry.getKey();
            PotionEffectPosition effectPosition = entry.getValue();

            String[] parts = positionKey.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            // Calculate Euclidean distance from input alignment to this position
            double distance = Math.sqrt(Math.pow(x - alignment[0], 2) + Math.pow(y - alignment[1], 2));

            if (distance <= maxDistance && (closestEffect == null || distance < closestDistance)) {
                closestEffect = effectPosition;
                closestDistance = distance;
            }
        }

        if (closestEffect != null) {
            // Calculate adjusted strength based on distance
            if (closestDistance == 0) {
                // If distance is 0, use the full strength
                return closestEffect;
            } else {
                // Otherwise, weaken the effect's strength
                int originalStrength = closestEffect.getStrength();
                int adjustedStrength = (int) Math.floor(originalStrength / closestDistance);
                return new PotionEffectPosition(closestEffect.getEffect(), closestEffect.getDuration(), adjustedStrength);
            }
        }

        return defaultEffect;
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
