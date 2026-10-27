package muddykat.alchemia.common.guide;

import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketPrimer;
import muddykat.alchemia.registration.registers.AttachmentRegistry;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

public final class PrimerSync {

    private PrimerSync() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) send(player);
    }

    @SubscribeEvent
    public static void onEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) send(player);
    }

    @SubscribeEvent
    public static void onProgress(AdvancementEvent.AdvancementProgressEvent event) {
        if (event.getProgressType() != AdvancementEvent.AdvancementProgressEvent.ProgressType.REVOKE) return;
        if (event.getEntity() instanceof ServerPlayer player) send(player);
    }

    public static void unlock(ServerPlayer player, String chapter) {
        if (GuidePrimer.chapter(chapter) == null) return;
        List<String> chapters = player.getData(AttachmentRegistry.PRIMER_CHAPTERS);
        if (chapters.contains(chapter)) return;
        List<String> next = new ArrayList<>(chapters);
        next.add(chapter);
        player.setData(AttachmentRegistry.PRIMER_CHAPTERS, List.copyOf(next));
    }

    public static void send(ServerPlayer player) {
        List<ResourceLocation> earned = new ArrayList<>();
        for (AdvancementHolder holder : player.level().getServer().getAdvancements().getAllAdvancements()) {
            if (player.getAdvancements().getOrStartProgress(holder).isDone()) earned.add(holder.id());
        }
        NetworkHandler.sendToPlayer(new PacketPrimer(earned, player.getData(AttachmentRegistry.PRIMER_CHAPTERS)), player);
    }
}
