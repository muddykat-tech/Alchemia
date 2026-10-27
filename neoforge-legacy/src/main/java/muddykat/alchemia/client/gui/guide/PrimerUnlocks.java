package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.DiscoveryToast;
import muddykat.alchemia.common.guide.GuideNews;
import muddykat.alchemia.common.guide.GuidePrimer;
import muddykat.alchemia.common.network.packets.PacketPrimerUnlock;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class PrimerUnlocks {

    private static final int CHECK_TICKS = 20;

    private PrimerUnlocks() {
    }

    static List<PrimerBook.Unlock> requirements(GuidePrimer.Chapter chapter) {
        return PrimerBook.section(chapter.sections().get(0)).unlocks();
    }

    static boolean unlocked(GuidePrimer.Chapter chapter) {
        return GuidePrimer.latched(chapter.key()) || requirements(chapter).isEmpty();
    }

    static boolean met(PrimerBook.Unlock unlock, LocalPlayer player) {
        return switch (unlock.gate()) {
            case ADVANCEMENT -> GuidePrimer.earned(unlock.id());
            case HOLD -> {
                if (!BuiltInRegistries.ITEM.containsKey(unlock.id())) yield false;
                Item item = BuiltInRegistries.ITEM.get(unlock.id());
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    if (player.getInventory().getItem(slot).is(item)) yield true;
                }
                yield false;
            }
        };
    }

    static Component describe(PrimerBook.Unlock unlock) {
        return switch (unlock.gate()) {
            case ADVANCEMENT -> {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                AdvancementHolder holder = connection == null ? null : connection.getAdvancements().get(unlock.id());
#if MC_VER >= 26300
                Component title = holder == null ? null : holder.value().display().map(DisplayInfo::title).orElse(null);
#else
                Component title = holder == null ? null : holder.value().display().map(DisplayInfo::getTitle).orElse(null);
#endif
                yield Component.translatable("alchemia.guide.primer.unlock.advancement", title != null ? title : Component.literal(unlock.id().toString()));
            }
            case HOLD -> {
                Component name = BuiltInRegistries.ITEM.containsKey(unlock.id())
                        ? new ItemStack(BuiltInRegistries.ITEM.get(unlock.id())).getHoverName() : Component.literal(unlock.id().toString());
                yield Component.translatable("alchemia.guide.primer.unlock.hold", name);
            }
        };
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !GuidePrimer.synced() || player.tickCount % CHECK_TICKS != 0) return;
        List<GuideNews.Entry> news = new ArrayList<>();
        for (int number = 0; number < GuidePrimer.CHAPTERS.size(); number++) {
            GuidePrimer.Chapter chapter = GuidePrimer.CHAPTERS.get(number);
            if (GuidePrimer.latched(chapter.key())) continue;
            List<PrimerBook.Unlock> needs = requirements(chapter);
            if (needs.isEmpty()) continue;
            boolean ready = true;
            for (PrimerBook.Unlock unlock : needs) {
                if (!met(unlock, player)) {
                    ready = false;
                    break;
                }
            }
            if (!ready) continue;
            GuidePrimer.latch(chapter.key());
            PacketDistributor.sendToServer(new PacketPrimerUnlock(chapter.key()));
            String numeral = Component.translatable("enchantment.level." + (number + 1)).getString();
            news.add(new GuideNews.Entry(GuideNews.Kind.CHAPTER, numeral, ItemStack.EMPTY,
                    Component.literal(PrimerBook.section(chapter.sections().get(0)).title())));
        }
        if (!news.isEmpty()) DiscoveryToast.show(news);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        GuidePrimer.reset();
    }
}
