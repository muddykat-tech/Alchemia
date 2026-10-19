package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.ItemPotionGlass;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.UseRemainder;

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
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
    }

    public static PotionGlass of(ItemStack stack) {
        if (stack.getItem() instanceof ItemPotionGlass empty) return empty.glass();
        PotionGlass glass = stack.get(DataComponentRegistry.POTION_GLASS.get());
        return glass == null ? PotionGlass.NORMAL_POTION : glass;
    }

    public static void apply(ItemStack stack, PotionGlass glass) {
        if (!isPotion(stack) || !glass.hasEmptyItem()) return;

        stack.set(DataComponentRegistry.POTION_GLASS.get(), glass);
        if (stack.is(Items.POTION)) {
            stack.set(DataComponents.USE_REMAINDER, new UseRemainder(new ItemStackTemplate(emptyItem(glass))));

            Consumable drink = stack.getOrDefault(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK);
            stack.set(DataComponents.CONSUMABLE, new Consumable(glass.drinkSeconds(), drink.animation(), drink.sound(),
                    drink.hasConsumeParticles(), drink.onConsumeEffects()));
        }
    }

    public static Item emptyItem(PotionGlass glass) {
        return ItemRegistry.getItemFromRegistry(glass.getSerializedName()).asItem();
    }
}
