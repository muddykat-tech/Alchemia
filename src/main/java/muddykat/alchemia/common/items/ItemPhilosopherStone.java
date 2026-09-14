package muddykat.alchemia.common.items;

import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.common.utility.StackData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import java.util.function.Consumer;

public class ItemPhilosopherStone extends Item {

    public static final int MAX_CHARGES = 3;

    public ItemPhilosopherStone(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static int chargesOf(ItemStack stack) {
        Integer charges = StackData.CHARGES.get(stack);
        return charges == null ? MAX_CHARGES : Math.max(0, Math.min(MAX_CHARGES, charges));
    }

    public static boolean consumeCharge(ItemStack stack) {
        int charges = chargesOf(stack);
        if (charges <= 0) return false;

        StackData.CHARGES.set(stack, charges - 1);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(TextUtils.brewingSource());
        tooltips.add(Component.translatable("alchemia.tooltip.stone.charges",
                chargesOf(stack), MAX_CHARGES).withStyle(ChatFormatting.GRAY));
        tooltips.add(Component.translatable("alchemia.tooltip.stone.effect").withStyle(ChatFormatting.DARK_GRAY));
    }
}
