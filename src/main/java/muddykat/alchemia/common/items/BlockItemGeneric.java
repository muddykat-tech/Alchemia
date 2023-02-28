package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BlockItemGeneric extends BlockItem {
    public BlockItemGeneric(Block block) {
        super(block, new Properties().tab(Alchemia.ITEM_GROUP));
    }
}
