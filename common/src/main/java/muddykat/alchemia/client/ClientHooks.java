package muddykat.alchemia.client;

import muddykat.alchemia.client.gui.GuideScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ClientHooks {
    public static void openGuide(ItemStack guide, InteractionHand hand) {
#if MC_VER >= 26200
        Minecraft.getInstance().gui.setScreen(new GuideScreen(guide, hand));
#else
        Minecraft.getInstance().setScreen(new GuideScreen(guide, hand));
#endif
    }
}
