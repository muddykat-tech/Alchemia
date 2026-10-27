package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketTreasureLoot;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

import java.lang.reflect.Field;
import javax.annotation.Nullable;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Alchemia.MODID)
public final class TreasureLoot {

    private static final @Nullable Field CONTAINER_LOOT = field(RandomizableContainerBlockEntity.class, "f_59605_");
    private static final @Nullable Field BRUSH_LOOT = field(BrushableBlockEntity.class, "f_276466_");

    public static final int BASE_RANGE = 15;
    public static final int RANGE_PER_LEVEL = 10;
    private static final int INTERVAL = 20;
    private static final int LIMIT = 4096;


    private TreasureLoot() {
    }

    public static int range(int amplifier) {
        return BASE_RANGE + RANGE_PER_LEVEL * amplifier;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player) || player.tickCount % INTERVAL != 0) return;
        MobEffectInstance effect = player.getEffect(MobEffectRegistry.TREASURE_VISION.get());
        if (effect == null) return;

        ServerLevel level = player.serverLevel();
        int range = range(effect.getAmplifier());
        BlockPos centre = player.blockPosition();
        long reachSqr = (long) range * range;
        List<Long> found = new ArrayList<>();
        int fromX = SectionPos.blockToSectionCoord(centre.getX() - range);
        int toX = SectionPos.blockToSectionCoord(centre.getX() + range);
        int fromZ = SectionPos.blockToSectionCoord(centre.getZ() - range);
        int toZ = SectionPos.blockToSectionCoord(centre.getZ() + range);
        for (int x = fromX; x <= toX && found.size() < LIMIT; x++) {
            for (int z = fromZ; z <= toZ && found.size() < LIMIT; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (entity.getBlockPos().distSqr(centre) <= reachSqr && natural(entity)) found.add(entity.getBlockPos().asLong());
                }
            }
        }
        List<Integer> carts = new ArrayList<>();
        for (Entity entity : level.getEntities((Entity) null, player.getBoundingBox().inflate(range),
                entity -> entity instanceof ContainerEntity container && container.getLootTable() != null
                        && entity.distanceToSqr(player) <= reachSqr)) {
            carts.add(entity.getId());
        }
        NetworkHandler.sendToPlayer(new PacketTreasureLoot(found, carts), player);
    }

    private static boolean natural(BlockEntity entity) {
        if (entity instanceof RandomizableContainerBlockEntity container) return lootOf(CONTAINER_LOOT, container);
        if (entity instanceof BrushableBlockEntity brushable) return lootOf(BRUSH_LOOT, brushable);
        return false;
    }

    private static boolean lootOf(@Nullable Field field, BlockEntity entity) {
        if (field == null) return true;
        try {
            return field.get(entity) != null;
        } catch (IllegalAccessException exception) {
            return true;
        }
    }

    private static @Nullable Field field(Class<?> owner, String srg) {
        try {
            return ObfuscationReflectionHelper.findField(owner, srg);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
