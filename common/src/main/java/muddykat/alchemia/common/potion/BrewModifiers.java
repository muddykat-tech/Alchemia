package muddykat.alchemia.common.potion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum BrewModifiers {
    REDSTONE,
    GUNPOWDER,
    GLOWSTONE,
    IMBUEMENT;

    public static BrewModifiers of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.is(Items.REDSTONE)) return REDSTONE;
        if (stack.is(Items.GUNPOWDER)) return GUNPOWDER;
        if (stack.is(Items.GLOWSTONE_DUST)) return GLOWSTONE;
        if (Imbuements.isAttuner(stack)) return IMBUEMENT;
        return null;
    }

    public static boolean isModifier(ItemStack stack) {
        return of(stack) != null;
    }
}
