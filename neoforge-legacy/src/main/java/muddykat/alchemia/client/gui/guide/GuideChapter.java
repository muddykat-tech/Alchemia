package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.common.guide.GuidePrimer;
import muddykat.alchemia.common.items.ItemAlchemyTablet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public enum GuideChapter {
    CONTENTS(GuideIcon.SEAL),
    RECIPES(GuideIcon.RECIPES),
    EFFECTS(GuideIcon.EFFECTS),
    INGREDIENTS(GuideIcon.INGREDIENTS),
    OPUS(GuideIcon.OPUS);

    final GuideIcon icon;

    GuideChapter(GuideIcon icon) {
        this.icon = icon;
    }

    public boolean shown() {
        if (this != OPUS || GuidePrimer.earned(GuidePrimer.SHACK)) return true;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof ItemAlchemyTablet) return true;
        }
        return false;
    }

    public Component title() {
        return Component.translatable("alchemia.guide.chapter." + name().toLowerCase());
    }

    public Component blurb() {
        return Component.translatable("alchemia.guide.chapter." + name().toLowerCase() + ".blurb");
    }
}
