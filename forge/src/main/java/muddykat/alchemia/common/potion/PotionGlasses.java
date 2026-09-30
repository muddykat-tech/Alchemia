package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.ItemPotionGlass;
import muddykat.alchemia.common.utility.StackData;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;

public final class PotionGlasses {

    private PotionGlasses() {
    }

    public static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    public static boolean isVessel(ItemStack stack) {
        return isPotion(stack) || stack.is(Items.GLASS_BOTTLE) || stack.getItem() instanceof ItemPotionGlass;
    }

    public static int liquidColor(ItemStack stack) {
        return PotionUtils.getColor(stack);
    }

    public static PotionGlass of(ItemStack stack) {
        if (stack.getItem() instanceof ItemPotionGlass empty) return empty.glass();
        return StackData.POTION_GLASS.getOrDefault(stack, PotionGlass.NORMAL_POTION);
    }

    public static void apply(ItemStack stack, PotionGlass glass) {
        if (!isPotion(stack) || !glass.hasEmptyItem()) return;
        StackData.POTION_GLASS.set(stack, glass);
    }

    public static Item emptyItem(PotionGlass glass) {
        return ItemRegistry.getItemFromRegistry(glass.getSerializedName()).asItem();
    }
}
