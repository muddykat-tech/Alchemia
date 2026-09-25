package muddykat.alchemia.common.items;

import muddykat.alchemia.common.items.helper.IngredientPath;
import net.minecraft.world.item.ItemStack;

public class ItemNigredo extends ItemPathTool {

    public ItemNigredo(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canApply(ItemStack target) {
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

    @Override
    public String actionKey() {
        return "alchemia.gui.tool.erase";
    }

    @Override
    public float progress(ItemStack target) {
        double trimmed = ItemIngredient.trimOf(target) * IngredientPath.TRIM_STEP;
        double remaining = ItemIngredient.pathLength(target);
        return trimmed + remaining <= 0.0 ? 0.0F : (float) (trimmed / (trimmed + remaining));
    }
}
