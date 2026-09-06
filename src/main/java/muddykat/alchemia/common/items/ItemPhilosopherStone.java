package muddykat.alchemia.common.items;

import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ItemPhilosopherStone extends Item {

    public static final int MAX_CHARGES = 3;

    public ItemPhilosopherStone(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static int chargesOf(ItemStack stack) {
        Integer charges = stack.get(DataComponentRegistry.CHARGES.get());
        return charges == null ? MAX_CHARGES : Math.max(0, Math.min(MAX_CHARGES, charges));
    }

    public static boolean consumeCharge(ItemStack stack) {
        int charges = chargesOf(stack);
        if (charges <= 0) return false;

        stack.set(DataComponentRegistry.CHARGES.get(), charges - 1);
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return chargesOf(stack) < MAX_CHARGES;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * chargesOf(stack) / MAX_CHARGES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xC93A3A;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltips, TooltipFlag flag) {
        tooltips.accept(TextUtils.brewingSource());
        tooltips.accept(Component.translatable("alchemia.tooltip.stone.charges",
                chargesOf(stack), MAX_CHARGES).withStyle(ChatFormatting.GRAY));
        tooltips.accept(Component.translatable("alchemia.tooltip.stone.effect").withStyle(ChatFormatting.DARK_GRAY));
    }
}
