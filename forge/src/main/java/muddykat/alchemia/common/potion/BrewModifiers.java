package muddykat.alchemia.common.potion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum BrewModifiers {
    REDSTONE,
    GUNPOWDER;

    public static BrewModifiers of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.is(Items.REDSTONE)) return REDSTONE;
        if (stack.is(Items.GUNPOWDER)) return GUNPOWDER;
        return null;
    }

    public static boolean isModifier(ItemStack stack) {
        return of(stack) != null;
    }
}
