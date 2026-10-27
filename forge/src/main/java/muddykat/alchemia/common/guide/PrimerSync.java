package muddykat.alchemia.common.guide;

import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketPrimer;
import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

public final class PrimerSync {

    private static final String CHAPTERS_KEY = "alchemia_primer_chapters";

    private PrimerSync() {
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag before = event.getOriginal().getPersistentData();
        if (before.contains(CHAPTERS_KEY, Tag.TAG_LIST)) {
            event.getEntity().getPersistentData().put(CHAPTERS_KEY, before.getList(CHAPTERS_KEY, Tag.TAG_STRING).copy());
        }
    }

    private static List<String> chapters(Player player) {
        List<String> chapters = new ArrayList<>();
        for (Tag entry : player.getPersistentData().getList(CHAPTERS_KEY, Tag.TAG_STRING)) chapters.add(entry.getAsString());
        return chapters;
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
        List<String> chapters = chapters(player);
        if (chapters.contains(chapter)) return;
        ListTag next = new ListTag();
        for (String known : chapters) next.add(StringTag.valueOf(known));
        next.add(StringTag.valueOf(chapter));
        player.getPersistentData().put(CHAPTERS_KEY, next);
    }

    public static void send(ServerPlayer player) {
        List<ResourceLocation> earned = new ArrayList<>();
        for (Advancement advancement : player.getServer().getAdvancements().getAllAdvancements()) {
            if (player.getAdvancements().getOrStartProgress(advancement).isDone()) earned.add(advancement.getId());
        }
        NetworkHandler.sendToPlayer(new PacketPrimer(earned, chapters(player)), player);
    }
}
