package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.Ingredients;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VanillaIngredients {

    public static final int DRIFT_LENGTH = 5;
    public static final double GRIND_BONUS = 1.0;

    public static double grindScale(int crushUnits) {
        int crush = Math.max(0, Math.min(Ingredients.MAX_CRUSH_UNITS, crushUnits));
        return 1.0 + GRIND_BONUS * crush / Ingredients.MAX_CRUSH_UNITS;
    }

    public static double grindScale(ItemStack stack) {
        return grindScale(ItemIngredient.crushOf(stack));
    }

    public record Drift(PotionEnum target, boolean lure) {}

    private static final Map<Item, Drift> DRIFTS = new LinkedHashMap<>();

    private static void mobPart(Item item, PotionEnum target) {
        DRIFTS.put(item, new Drift(target, true));
    }

    private static void harvested(Item item, PotionEnum target) {
        DRIFTS.put(item, new Drift(target, false));
    }

    static {
        mobPart(Items.ROTTEN_FLESH, PotionEnum.Hunger);
        mobPart(Items.SPIDER_EYE, PotionEnum.Poison);
        mobPart(Items.FERMENTED_SPIDER_EYE, PotionEnum.Weakness);
        mobPart(Items.GHAST_TEAR, PotionEnum.Regeneration);
        mobPart(Items.BLAZE_POWDER, PotionEnum.Damage_Boost);
        mobPart(Items.MAGMA_CREAM, PotionEnum.Fire_Protection);
        mobPart(Items.PHANTOM_MEMBRANE, PotionEnum.Slow_Falling);
        mobPart(Items.SHULKER_SHELL, PotionEnum.Levitation);
        mobPart(Items.WITHER_SKELETON_SKULL, PotionEnum.Wither);
        mobPart(Items.RABBIT_FOOT, PotionEnum.Jump);
        mobPart(Items.PUFFERFISH, PotionEnum.Water_Breathing);
        mobPart(Items.NAUTILUS_SHELL, PotionEnum.Conduit_Power);
        mobPart(Items.GLOW_INK_SAC, PotionEnum.Glowing);
        mobPart(Items.SLIME_BALL, PotionEnum.Slow);

        harvested(Items.SUGAR, PotionEnum.Speed);
        harvested(Items.GLISTERING_MELON_SLICE, PotionEnum.Healing);
        harvested(Items.GOLDEN_CARROT, PotionEnum.Night_Vision);
        harvested(Items.GLOW_BERRIES, PotionEnum.Saturation);
        harvested(Items.NETHER_WART, PotionEnum.Absorption);
        harvested(Items.CHORUS_FRUIT, PotionEnum.Invisibility);
    }

    public static Drift driftOf(ItemStack stack) {
        return stack.isEmpty() ? null : DRIFTS.get(stack.getItem());
    }

    public static boolean isVanillaIngredient(ItemStack stack) {
        return driftOf(stack) != null;
    }

    public static boolean isLure(ItemStack stack) {
        Drift drift = driftOf(stack);
        return drift != null && drift.lure();
    }

    public static Map<Item, Drift> drifts() {
        return Map.copyOf(DRIFTS);
    }

    public static final List<Item> MODIFIERS = List.of(Items.REDSTONE, Items.GUNPOWDER, Items.GLOWSTONE_DUST, Items.DRAGON_BREATH);

    public static boolean isModifier(ItemStack stack) {
        return !stack.isEmpty() && MODIFIERS.contains(stack.getItem());
    }

    public static boolean isReagent(ItemStack stack) {
        return isVanillaIngredient(stack) || isModifier(stack);
    }

    public static List<Item> reagents() {
        List<Item> all = new ArrayList<>(DRIFTS.keySet());
        all.addAll(MODIFIERS);
        return all;
    }
}
