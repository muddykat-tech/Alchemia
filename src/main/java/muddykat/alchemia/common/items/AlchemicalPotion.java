package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

public class AlchemicalPotion extends PotionItem {
    public AlchemicalPotion() {
        super(new Properties().tab(Alchemia.ITEM_GROUP));
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack);
    }
}
