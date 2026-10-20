package muddykat.alchemia.common.utility;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class Handover {

    private Handover() {
    }

    public static void give(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
#if MC_VER >= 26300
        player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
#else
        player.getInventory().placeItemBackInInventory(stack);
#endif
    }
}
