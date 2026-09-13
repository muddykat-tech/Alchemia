package muddykat.alchemia.common.items;

import net.minecraft.world.item.ItemStack;

public class ItemNigredo extends ItemPathTool {

    public ItemNigredo(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canApply(ItemStack target) {
        return ItemIngredient.canTrim(target);
    }

    @Override
    protected void apply(ItemStack target) {
        ItemIngredient.trim(target);
    }

    @Override
    public String tooltipKey() {
        return "alchemia.tooltip.nigredo";
    }

    @Override
    public int progressValue(ItemStack target) {
        return ItemIngredient.trimOf(target);
    }

    @Override
    public String progressKey() {
        return "alchemia.tooltip.nigredo.progress";
    }
}
