package muddykat.alchemia.common.guide;

import net.minecraft.world.item.ItemStack;

public interface GuideHolder {

    ItemStack guide();

    void guideChanged();
}
