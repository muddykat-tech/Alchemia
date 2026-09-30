package muddykat.alchemia.common.items;

import muddykat.alchemia.common.potion.PotionGlass;
import net.minecraft.world.item.Item;

public class ItemPotionGlass extends Item {

    private final PotionGlass glass;

    public ItemPotionGlass(PotionGlass glass, Properties properties) {
        super(properties);
        this.glass = glass;
    }

    public PotionGlass glass() {
        return glass;
    }
}
