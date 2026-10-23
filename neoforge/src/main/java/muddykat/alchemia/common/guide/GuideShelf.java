package muddykat.alchemia.common.guide;

import muddykat.alchemia.common.blocks.tileentity.TileEntityResearchDesk;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class GuideShelf implements Iterable<ItemStack> {

    public static final int DESK_REACH = 16;

    private record Desk(TileEntityResearchDesk desk, ItemStack before) {
    }

    private final List<ItemStack> guides = new ArrayList<>();
    private final List<Desk> desks = new ArrayList<>();
    private @Nullable Player player;
    private GuideNews.@Nullable Snapshot before;

    private GuideShelf() {
    }

    public static GuideShelf of(@Nullable Player player) {
        GuideShelf shelf = new GuideShelf();
        shelf.player = player;
        shelf.guides.addAll(RecipeDiscovery.guidesOf(player));
        shelf.before = GuideNews.Snapshot.of(shelf.guides);
        return shelf;
    }

    public static GuideShelf around(@Nullable Player player, Level level, BlockPos pos) {
        GuideShelf shelf = of(player);
        if (level.isClientSide()) return shelf;

        for (TileEntityResearchDesk desk : desksNear(level, pos)) {
            ItemStack guide = desk.guide();
            if (guide.isEmpty()) continue;
            shelf.guides.add(guide);
            shelf.desks.add(new Desk(desk, guide.copy()));
        }
        shelf.before = GuideNews.Snapshot.of(shelf.guides);
        return shelf;
    }

    public static List<ItemStack> readable(@Nullable Player player, Level level, BlockPos pos) {
        List<ItemStack> guides = new ArrayList<>(RecipeDiscovery.guidesOf(player));
        for (TileEntityResearchDesk desk : desksNear(level, pos)) {
            if (desk.hasGuide()) guides.add(desk.guide());
        }
        return guides;
    }

    public static List<TileEntityResearchDesk> desksNear(Level level, BlockPos pos) {
        List<TileEntityResearchDesk> found = new ArrayList<>();
        int minX = SectionPos.blockToSectionCoord(pos.getX() - DESK_REACH);
        int maxX = SectionPos.blockToSectionCoord(pos.getX() + DESK_REACH);
        int minZ = SectionPos.blockToSectionCoord(pos.getZ() - DESK_REACH);
        int maxZ = SectionPos.blockToSectionCoord(pos.getZ() + DESK_REACH);

        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (entity instanceof TileEntityResearchDesk desk && !desk.isRemoved()
                            && desk.getBlockPos().closerThan(pos, DESK_REACH)) {
                        found.add(desk);
                    }
                }
            }
        }
        return found;
    }

    public boolean isEmpty() {
        return guides.isEmpty();
    }

    public void settle(BlockPos from) {
        settle(from, false);
    }

    public void settleQuietly(BlockPos from) {
        settle(from, true);
    }

    private void settle(BlockPos from, boolean quiet) {
        for (Desk entry : desks) {
            if (ItemStack.isSameItemSameComponents(entry.before(), entry.desk().guide())) continue;
            if (quiet) {
                entry.desk().guideChanged();
            } else {
                entry.desk().inscribed(from);
            }
        }
        GuideNews.announce(player, before);
        before = GuideNews.Snapshot.of(guides);
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return guides.iterator();
    }
}
