package muddykat.alchemia.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import java.util.function.Consumer;

public class ItemRubedo extends ItemGeneric {

    public ItemRubedo(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(Component.translatable("alchemia.tooltip.rubedo").withStyle(ChatFormatting.DARK_GRAY));
    }
}
