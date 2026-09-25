package muddykat.alchemia.common.items;

import muddykat.alchemia.common.items.helper.IngredientPath;
import net.minecraft.world.item.ItemStack;

public class ItemCitrinitas extends ItemPathTool {

    public ItemCitrinitas(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canApply(ItemStack target) {
        return ItemIngredient.canRotate(target);
    }

    @Override
    protected void apply(ItemStack target) {
        ItemIngredient.rotate(target, -1);
    }

    @Override
    public String tooltipKey() {
        return "alchemia.tooltip.citrinitas";
    }

    @Override
    public int progressValue(ItemStack target) {
        return ItemIngredient.rotationOf(target) * 45;
    }

    @Override
    public String progressKey() {
        return "alchemia.tooltip.citrinitas.progress";
    }

    @Override
    public String actionKey() {
        return "alchemia.gui.tool.rotate";
    }

    @Override
    public float progress(ItemStack target) {
        return (float) ItemIngredient.rotationOf(target) / IngredientPath.ROTATION_STEPS;
    }
}
