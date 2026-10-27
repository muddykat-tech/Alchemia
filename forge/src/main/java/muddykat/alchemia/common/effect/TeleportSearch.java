package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.registration.registers.EntityTypeRegistry;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketTeleportView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Alchemia.MODID)
public final class TeleportSearch {

    private static final int RANGE_PER_LEVEL = 500;
    private static final double MIN_FRACTION = 0.1;
    private static final int ATTEMPTS = 8;
    private static final float RECALL_BASE_CHARGE_RATE = 2.0F / 3.0F;
    private static final int VIEW_OPEN_TICKS = TeleportViewState.OPEN_TICKS;
    private static final int MAX_TICKS = 400;
    private static final int COLUMNS = 4;
    private static final float CAVE_CHANCE = 0.5F;

    private static final Map<UUID, Job> JOBS = new HashMap<>();

    private TeleportSearch() {
    }

    public static boolean active(LivingEntity entity) {
        return JOBS.containsKey(entity.getUUID());
    }

    public static void begin(LivingEntity entity, int level) {
        if (!(entity.level() instanceof ServerLevel server)) return;
        start(entity, server, new Job(server.dimension(), entity.position(), level * RANGE_PER_LEVEL, 1.0F));
    }

    public static void recall(LivingEntity entity, int strength) {
        if (!(entity.level() instanceof ServerLevel server)) return;
        TeleportDestination home = home(entity, server);
        float chargeRate = Math.max(1, strength) * RECALL_BASE_CHARGE_RATE;
        Job job = new Job(server.dimension(), entity.position(), 0, chargeRate);
        job.found = home.pos();
        job.target = home.level().dimension();
        job.targetYaw = home.yRot();
        job.recall = true;
        start(entity, server, job);
    }

    private static TeleportDestination home(LivingEntity entity, ServerLevel server) {
        if (entity instanceof ServerPlayer player) return TeleportDestination.respawn(player);
        return TeleportDestination.worldSpawn(server.getServer().overworld());
    }

    private static void start(LivingEntity entity, ServerLevel server, Job job) {
        Job previous = JOBS.remove(entity.getUUID());
        if (previous != null) {
            previous.end();
            view(entity, TeleportViewState.REVEAL);
        }
        job.rift = rift(server, entity.position(), RiftEntity.CHARGE, entity.getId());
        server.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, entity.getSoundSource(), 1.0F, 0.7F);
        JOBS.put(entity.getUUID(), job);
        view(entity, TeleportViewState.CHARGE, job.chargeRate);
    }

    private static RiftEntity rift(ServerLevel level, Vec3 at, int mode, int owner) {
        RiftEntity rift = new RiftEntity(EntityTypeRegistry.RIFT.get(), level);
        rift.setPos(at.x, at.y, at.z);
        rift.configure(mode, owner);
        level.addFreshEntity(rift);
        return rift;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator<Map.Entry<UUID, Job>> iterator = JOBS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Job> entry = iterator.next();
            Job job = entry.getValue();
            ServerLevel jobLevel = event.getServer().getLevel(job.dimension);
            LivingEntity entity = jobLevel != null && jobLevel.getEntity(entry.getKey()) instanceof LivingEntity living ? living : null;
            if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                job.end();
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player != null) view(player, TeleportViewState.REVEAL);
                iterator.remove();
                continue;
            }
            if (tick(entity, job)) {
                job.end();
                iterator.remove();
            }
        }
    }

    private static boolean tick(LivingEntity entity, Job job) {
        ServerLevel level = (ServerLevel) entity.level();
        job.ticks++;
        if (!job.teleported) job.chargeTicks += job.chargeRate;

        if (job.teleported) {
            if (++job.arrivalTicks >= RiftEntity.ARRIVE_HOLD) {
                view(entity, TeleportViewState.REVEAL);
                return true;
            }
            return false;
        }

        if (job.rift != null) job.rift.setPos(entity.getX(), entity.getY(), entity.getZ());

        if (job.found != null) {
            if (job.chargeTicks < RiftEntity.CHARGE_TICKS) return false;
            if (!job.opening) {
                job.opening = true;
                if (job.rift != null) job.rift.startOpening();
                view(entity, TeleportViewState.OPEN);
                return false;
            }
            if (++job.openTicks < VIEW_OPEN_TICKS) return false;
            arrive(entity, job);
            job.teleported = true;
            job.closeRift();
            view(entity, TeleportViewState.HOLD);
            return false;
        }

        if (job.pending == null) {
            aim(level, job);
        } else if (job.pending.isDone()) {
            Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> result = job.pending.getNow(null);
            job.pending = null;
            if (result != null && result.left().isPresent()) job.found = landing(level, job.targetX, job.targetZ);
            if (job.found == null) job.attempts--;
        }

        if (job.found == null && (job.attempts <= 0 || job.ticks > MAX_TICKS)) {
            if (entity instanceof ServerPlayer player) player.displayClientMessage(Component.translatable("alchemia.teleportation.fizzle"), true);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FIRE_EXTINGUISH, entity.getSoundSource(), 1.0F, 1.0F);
            view(entity, TeleportViewState.REVEAL);
            return true;
        }
        return false;
    }

    private static void aim(ServerLevel level, Job job) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * 2 * Math.PI;
        double floor = MIN_FRACTION * MIN_FRACTION;
        double distance = job.range * Math.sqrt(floor + random.nextDouble() * (1.0 - floor));
        int x = (int) Math.floor(job.origin.x + Math.cos(angle) * distance);
        int z = (int) Math.floor(job.origin.z + Math.sin(angle) * distance);
        if (!level.getWorldBorder().isWithinBounds(new BlockPos(x, 0, z))) {
            job.attempts--;
            return;
        }
        job.targetX = x;
        job.targetZ = z;
        job.pending = level.getChunkSource().getChunkFuture(x >> 4, z >> 4, ChunkStatus.FULL, true);
    }

    private static void arrive(LivingEntity entity, Job job) {
        ServerLevel from = (ServerLevel) entity.level();
        ServerLevel level = job.target == null ? null : from.getServer().getLevel(job.target);
        if (level == null) level = from;
        Vec3 origin = job.origin;
        Vec3 found = job.found;
        float yaw = job.recall ? job.targetYaw : entity.getYRot();
        Entity arrived = entity;
        arrived = TeleportDestination.send(entity, level, found, yaw, entity.getXRot());
        if (level != from) job.dimension = level.dimension();
        arrived.resetFallDistance();
        if (arrived instanceof Mob mob) mob.getNavigation().stop();
        from.playSound(null, origin.x, origin.y, origin.z, SoundEvents.CHORUS_FRUIT_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
        level.playSound(null, found.x, found.y, found.z, SoundEvents.CHORUS_FRUIT_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.PORTAL, found.x, found.y + 1.0, found.z, 40, 0.5, 0.8, 0.5, 0.2);
        rift(level, found, RiftEntity.ARRIVE, arrived.getId());
    }

    private static void view(LivingEntity entity, int phase) {
        view(entity, phase, 1.0F);
    }

    private static void view(LivingEntity entity, int phase, float chargeRate) {
        if (entity instanceof ServerPlayer player) NetworkHandler.sendToPlayer(new PacketTeleportView(phase, chargeRate), player);
    }

    private static @Nullable Vec3 landing(ServerLevel level, int x, int z) {
        Vec3 spot = safeSpot(level, x, z);
        int chunkX = x & ~15;
        int chunkZ = z & ~15;
        for (int column = 0; spot == null && column < COLUMNS * COLUMNS; column++) {
            spot = safeSpot(level, chunkX + 2 + 4 * (column % COLUMNS), chunkZ + 2 + 4 * (column / COLUMNS));
        }
        return spot;
    }

    private static @Nullable Vec3 safeSpot(ServerLevel level, int x, int z) {
        if (level.dimensionType().hasCeiling()) {
            int top = level.getMinBuildHeight() + level.dimensionType().logicalHeight() - 2;
            for (int y = top; y > level.getMinBuildHeight(); y--) {
                BlockPos feet = new BlockPos(x, y, z);
                if (standable(level, feet)) return Vec3.atBottomCenterOf(feet);
            }
            return null;
        }

        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (surface <= level.getMinBuildHeight()) return null;

        List<BlockPos> caves = new ArrayList<>();
        for (int y = surface - 2; y > level.getMinBuildHeight(); y--) {
            BlockPos feet = new BlockPos(x, y, z);
            if (standable(level, feet)) caves.add(feet);
        }
        boolean surfaceOk = standable(level, new BlockPos(x, surface, z));
        if (!caves.isEmpty() && (!surfaceOk || level.getRandom().nextFloat() < CAVE_CHANCE)) {
            return Vec3.atBottomCenterOf(caves.get(level.getRandom().nextInt(caves.size())));
        }
        return surfaceOk ? Vec3.atBottomCenterOf(new BlockPos(x, surface, z)) : null;
    }

    private static boolean standable(ServerLevel level, BlockPos feet) {
        if (!level.getWorldBorder().isWithinBounds(feet)) return false;

        BlockPos below = feet.below();
        BlockState ground = level.getBlockState(below);
        if (!ground.isFaceSturdy(level, below, Direction.UP) || !ground.getFluidState().isEmpty()) return false;
        if (ground.is(Blocks.MAGMA_BLOCK) || ground.is(Blocks.CACTUS) || ground.is(Blocks.CAMPFIRE)
                || ground.is(Blocks.SOUL_CAMPFIRE) || ground.is(Blocks.POINTED_DRIPSTONE)) return false;

        return open(level, feet) && open(level, feet.above());
    }

    private static boolean open(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty() && !state.is(BlockTags.FIRE)
                && !state.is(Blocks.POWDER_SNOW) && !state.is(Blocks.SWEET_BERRY_BUSH) && !state.is(Blocks.WITHER_ROSE)
                && !state.is(Blocks.CACTUS);
    }

    private static final class Job {
        private ResourceKey<Level> dimension;
        private @Nullable ResourceKey<Level> target;
        private float targetYaw;
        private boolean recall;
        private final Vec3 origin;
        private final int range;
        private final float chargeRate;
        private int attempts = ATTEMPTS;
        private int ticks;
        private float chargeTicks;
        private int targetX;
        private int targetZ;
        private @Nullable CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> pending;
        private @Nullable Vec3 found;
        private @Nullable RiftEntity rift;
        private boolean opening;
        private int openTicks;
        private boolean teleported;
        private int arrivalTicks;

        private Job(ResourceKey<Level> dimension, Vec3 origin, int range, float chargeRate) {
            this.dimension = dimension;
            this.origin = origin;
            this.range = range;
            this.chargeRate = chargeRate;
        }

        private void end() {
            closeRift();
        }

        private void closeRift() {
            if (rift != null) rift.startClosing();
            rift = null;
        }
    }
}
