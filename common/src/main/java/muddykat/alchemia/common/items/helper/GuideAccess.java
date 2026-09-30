package muddykat.alchemia.common.items.helper;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import net.minecraft.world.entity.player.Player;

import java.util.function.BooleanSupplier;

public final class GuideAccess {

    private static BooleanSupplier reader = () -> false;

    private GuideAccess() {
    }

    public static void bindReader(BooleanSupplier supplier) {
        reader = supplier;
    }

    public static boolean reading() {
        return reader.getAsBoolean();
    }

    public static boolean carriedBy(Player player) {
        if (player == null) return false;
        if (player.containerMenu.getCarried().getItem() instanceof ItemAlchemiaGuide) return true;
        return player.getInventory().hasAnyMatching(stack -> stack.getItem() instanceof ItemAlchemiaGuide);
    }
}
