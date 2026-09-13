package muddykat.alchemia.common.items;

import net.minecraft.world.item.ItemStack;

public class ItemAlbedo extends ItemPathTool {

    public ItemAlbedo(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canApply(ItemStack target) {
        return ItemIngredient.canRotate(target);
    }

    @Override
    protected void apply(ItemStack target) {
        ItemIngredient.rotate(target, 1);
    }

    @Override
    public String tooltipKey() {
        return "alchemia.tooltip.albedo";
    }

    @Override
    public int progressValue(ItemStack target) {
        return ItemIngredient.rotationOf(target) * 45;
    }

    @Override
    public String progressKey() {
        return "alchemia.tooltip.albedo.progress";
    }
}
