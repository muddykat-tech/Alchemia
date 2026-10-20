package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketTreasureLoot;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class TreasureLoot {

    public static final int BASE_RANGE = 15;
    public static final int RANGE_PER_LEVEL = 10;
    private static final int INTERVAL = 20;
    private static final int LIMIT = 4096;

    private static Field brushLoot;
    private static boolean reflected;

    private TreasureLoot() {
    }

    public static int range(int amplifier) {
        return BASE_RANGE + RANGE_PER_LEVEL * amplifier;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % INTERVAL != 0) return;
        MobEffectInstance effect = player.getEffect(MobEffectRegistry.TREASURE_VISION);
        if (effect == null) return;

        ServerLevel level = player.level();
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
                entity -> entity instanceof ContainerEntity container && container.getContainerLootTable() != null
                        && entity.distanceToSqr(player) <= reachSqr)) {
            carts.add(entity.getId());
        }
        NetworkHandler.sendToPlayer(new PacketTreasureLoot(found, carts), player);
    }

    private static boolean natural(BlockEntity entity) {
        if (entity instanceof RandomizableContainer container) return container.getLootTable() != null;
        if (entity instanceof BrushableBlockEntity brushable) {
            Field field = brushField();
            if (field == null) return true;
            try {
                return field.get(brushable) != null;
            } catch (IllegalAccessException exception) {
                return true;
            }
        }
        return false;
    }

    private static Field brushField() {
        if (!reflected) {
            reflected = true;
            try {
                brushLoot = BrushableBlockEntity.class.getDeclaredField("lootTable");
                brushLoot.setAccessible(true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                brushLoot = null;
            }
        }
        return brushLoot;
    }
}
