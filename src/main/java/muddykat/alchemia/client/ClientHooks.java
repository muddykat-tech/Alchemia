package muddykat.alchemia.client;

import muddykat.alchemia.client.gui.GuideScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ClientHooks {
    public static void openGuide(ItemStack guide, InteractionHand hand) {
        Minecraft.getInstance().gui.setScreen(new GuideScreen(guide, hand));
    }
}
