package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
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
        size = PotionEnum.values().length * 2;
        middlePosition = Math.round(((float) size / 2));

        // Set minimum distance between effects
        int minDistance = 4;

        for (PotionEnum e : PotionEnum.values()) {
            MobEffect effect = e.getEffect();
            int strength = e.maxStrength;
            boolean placed = false;
            int attempts = 0;

            Alchemia.LOGGER.info("Attempting to place effect: " + e.name());

            while (!placed && attempts < 10) {
                // Calculate bias based on primary alignment
                IngredientAlignment primaryAlignment = e.getPrimaryAlignment();
                IngredientAlignment secondaryAlignment = e.getSecondaryAlignment();
                int biasX = (primaryAlignment.getX() + secondaryAlignment.getX()) == 0 ? 1 : primaryAlignment.getX() + secondaryAlignment.getX();
                int biasY = (primaryAlignment.getY() + secondaryAlignment.getY()) == 0 ? 1 : primaryAlignment.getY() + secondaryAlignment.getY();

                // Calculate initial position with bias
                int randX = middlePosition + (rand.nextInt(size / 2) * biasX);
                int randY = middlePosition + (rand.nextInt(size / 2) * biasY);

                // Check bounds
                randX = Math.max(0, Math.min(size - 1, randX));
                randY = Math.max(0, Math.min(size - 1, randY));

                // Check if the position meets the conditions
                boolean positionValid = true;

                // Check distance to other effects already placed
                for (String position : effectHashMap.keySet()) {
                    String[] parts = position.split(",");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);

                    double distance = Math.sqrt(Math.pow(x - randX, 2) + Math.pow(y - randY, 2));

                    if (distance < minDistance) {
                        positionValid = false;
                        break; // No need to check further
                    }
                }

                String position = randX + "," + randY;
                // Attempt to place the effect
                if (positionValid) {
                    effectHashMap.put(position, new PotionEffectPosition(effect, 1200, strength));
                    placed = true; // Effect placed successfully
                    Alchemia.LOGGER.info("Placed effect: " + e.name() + " at position: " + position);
                } else {
                    Alchemia.LOGGER.info("Position invalid for effect: " + e.name() + " at position: "+position+", attempting again.");
                }

                attempts++;
            }

            if (!placed) {
                Alchemia.LOGGER.info("Failed to place effect: " + e.name());
                // Handle case where the effect couldn't be placed after 10 attempts
                // For example, you might want to log this or handle it in some other way.
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
