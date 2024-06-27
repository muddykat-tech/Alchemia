package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import java.util.*;

public class PotionMap {
    public static PotionMap INSTANCE;
    private final int size;
    private final int middlePosition;
    public final HashMap<String, PotionEffectPosition> effectHashMap = new HashMap<>();

    public PotionMap(long seed) {
        System.out.println("Potion Map has been Assigned");
        Random rand = new Random(seed);
        size = PotionEnum.values().length * 2;
        middlePosition = size / 2;

        // Set minimum distance between effects
        int minDistance = 6;

        for (PotionEnum e : PotionEnum.values()) {
            MobEffect effect = e.getEffect();
            int strength = e.maxStrength;
            boolean placed = false;
            int attempts = 0;

            //System.out.println("Attempting to place effect: " + e.name());

            while (!placed && attempts < 250) {
                // Calculate combined bias vectors
                int biasX = 0;
                int biasY = 0;
                for (IngredientAlignment alignment : e.getAlignments()) {
                    biasX += alignment.getX();
                    biasY += alignment.getY();
                }

                // Adjust bias to allow for free placement if bias is 0
                if (biasX == 0 && biasY == 0) {
                    biasX = rand.nextInt(3) - 1; // -1, 0, 1
                    biasY = rand.nextInt(3) - 1; // -1, 0, 1
                }

                // Calculate position based on rarity
                int rarityOrdinal = e.getRarity().ordinal();
                double baseRadius = 15.0; // Adjust as needed

                // Calculate random radius within the appropriate baseRadius range
                double randomRadius;
                if (rarityOrdinal == 0) {
                    randomRadius = baseRadius * 0.75 + rand.nextInt(-2,2); // Common
                } else if (rarityOrdinal == 1) {
                    randomRadius = baseRadius * 1.25 + rand.nextInt(-2,2); // Uncommon
                } else if (rarityOrdinal == 2) {
                    randomRadius = baseRadius * 2  + rand.nextInt(-2,2); // Rare
                } else {
                    randomRadius = baseRadius * 3.25  + rand.nextInt(-2,2); // Epic or higher
                }

                // Calculate random angle with a bias!
                double angle = Math.atan2(biasY, biasX) + (rand.nextDouble(-1,1)) * Math.PI / 4.0;

                // Calculate position relative to middlePosition with radius and angle
                int randX = middlePosition + (int) (randomRadius * Math.cos(angle));
                int randY = middlePosition + (int) (randomRadius * Math.sin(angle));

                // Check bounds
                randX = Math.max(0, Math.min(size - 1, randX));
                randY = Math.max(0, Math.min(size - 1, randY));

                // Check if the position meets the conditions
                boolean positionValid = true;

                // Check distance to other effects already placed
                for (Map.Entry<String, PotionEffectPosition> entry : effectHashMap.entrySet()) {
                    String position = entry.getKey();
                    String[] parts = position.split(",");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);

                    double distance = Math.sqrt(Math.pow(x - randX, 2) + Math.pow(y - randY, 2));

                    if (distance < minDistance) {
                        positionValid = false;
                        break; // No need to check further
                    }
                }

                // Attempt to place the effect
                if (positionValid) {
                    String position = randX + "," + randY;
                    effectHashMap.put(position, new PotionEffectPosition(effect, 1200, strength, e.getPotion(), true));
                    placed = true; // Effect placed successfully
                    //System.out.println("Placed effect: " + e.name() + " at position: " + position);
                }

                attempts++;
            }

            if (!placed) {
                System.out.println("Failed to place effect: " + e.name());
                // Handle case where the effect couldn't be placed after 10 attempts
                // For example, you might want to log this or handle it in some other way.
            }
        }
    }

    public static void scramble(long seed) {
        INSTANCE = new PotionMap(seed);
    }

    public PotionEffectPosition getEffectPotion(int[] alignment){
        String key = alignment[0] + "," + alignment[1];
        PotionEffectPosition defaultEffect = new PotionEffectPosition(null, 10, 0, Potions.AWKWARD, false);

        PotionEffectPosition closestEffect = null;
        double closestDistance = Double.MAX_VALUE;

        int maxDistance = 2;

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
                return new PotionEffectPosition(closestEffect.getEffect(), closestEffect.getDuration(), Math.max(1, adjustedStrength-1), closestEffect.getPotion(), false);
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
        final Potion potion;
        final boolean perfect;
        PotionEffectPosition(MobEffect effect, int duration, int maxStrength, Potion potion, boolean isPerfect) {
            this.effect = effect;
            this.duration = duration;
            this.strength = maxStrength;
            this.potion = potion;
            this.perfect = isPerfect;
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

        public Potion getPotion() {
            // Returns a Potion (Used to get the proper Colored Potion Icons...
            return potion;
        }

        public boolean isPerfect() {
            return perfect;
        }
    }
}
