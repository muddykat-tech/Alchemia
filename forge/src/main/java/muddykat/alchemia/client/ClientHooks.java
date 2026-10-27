package muddykat.alchemia.client;

import muddykat.alchemia.client.gui.GuideScreen;
import muddykat.alchemia.common.guide.GuideSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;

public class ClientHooks {
    public static void openGuide(InteractionHand hand) {
        openGuide(GuideSource.hand(hand));
    }

    public static void openGuide(GuideSource source) {
        show(GuideScreen.guide(source));
    }

    public static void openPage(InteractionHand hand) {
        show(GuideScreen.page(GuideSource.hand(hand)));
    }

    public static void repairHint(BlockPos pos, ItemStack item) {
        muddykat.alchemia.client.gui.RepairHint.show(pos, item);
    }

    private static void show(Screen screen) {
#if MC_VER >= 26200
        Minecraft.getInstance().gui.setScreen(screen);
#else
        Minecraft.getInstance().setScreen(screen);
#endif
    }
}
