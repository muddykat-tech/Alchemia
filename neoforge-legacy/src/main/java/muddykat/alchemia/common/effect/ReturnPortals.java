package muddykat.alchemia.common.effect;

import muddykat.alchemia.registration.registers.EntityTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ReturnPortals {

    public static final double ARRIVAL_RANGE_SQR = 8.0 * 8.0;

    private static final double SPAWN_DISTANCE = 2.0;
    private static final int LIFETIME_SECONDS = 60;
    private static final int AWAY_TICKS = 20 * 60 * 20;
    private static final int COOLDOWN_TICKS = 40;

    private static final Map<UUID, Integer> GENERATIONS = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private ReturnPortals() {
    }

    public static long lifetime() {
        return LIFETIME_SECONDS * 20L;
    }

    public static void open(ServerPlayer player, Vec3 at, int trips, boolean thrown) {
        ServerLevel level = player.serverLevel();
        DimensionTransition home = player.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
        float yaw = player.getYRot();
        Vec3 forward = forward(yaw);
        Vec3 place = thrown ? at : at.add(forward.scale(SPAWN_DISTANCE));
        Vec3 back = thrown ? at.subtract(forward.scale(SPAWN_DISTANCE)) : at;
        int generation = GENERATIONS.merge(player.getUUID(), 1, Integer::sum);

        ReturnPortalEntity portal = new ReturnPortalEntity(EntityTypeRegistry.RETURN_PORTAL.get(), level);
        portal.setPos(place.x, place.y, place.z);
        portal.setYRot(yaw);
        portal.configure(player.getUUID(), generation, home.newLevel().dimension(), home.pos(), home.yRot(), back, yaw,
                trips, true, level.getGameTime() + lifetime());
        level.addFreshEntity(portal);
        level.playSound(null, place.x, place.y, place.z, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 0.7F);
        level.playSound(null, place.x, place.y, place.z, SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    public static boolean current(ReturnPortalEntity portal) {
        if (portal.owner() == null) return false;
        Integer generation = GENERATIONS.get(portal.owner());
        return generation == null || generation == portal.generation();
    }

    public static void traverse(ReturnPortalEntity portal, ServerPlayer player, ServerLevel here) {
        long now = here.getGameTime();
        Long until = COOLDOWNS.get(player.getUUID());
        if (until != null && now < until) return;

        ServerLevel there = portal.targetLevel() == null ? null : here.getServer().getLevel(portal.targetLevel());
        if (there == null) {
            portal.startCollapse();
            return;
        }

        portal.useTrip();
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        Vec3 from = player.position();
        Vec3 to = portal.target();
        here.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.3F);
        here.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1.0, from.z, 40, 0.4, 0.8, 0.4, 0.05);
        player.changeDimension(new DimensionTransition(there, to, Vec3.ZERO, portal.targetYaw(), player.getXRot(), DimensionTransition.DO_NOTHING));
        there.playSound(null, to.x, to.y, to.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        there.sendParticles(ParticleTypes.REVERSE_PORTAL, to.x, to.y + 1.0, to.z, 40, 0.4, 0.8, 0.4, 0.05);

        if (!portal.homeward()) return;
        portal.leftFor(now + AWAY_TICKS);
        Vec3 place = to.subtract(forward(portal.targetYaw()).scale(SPAWN_DISTANCE));
        ReturnPortalEntity backward = new ReturnPortalEntity(EntityTypeRegistry.RETURN_PORTAL.get(), there);
        backward.setPos(place.x, place.y, place.z);
        backward.setYRot(portal.targetYaw() + 180.0F);
        backward.configure(player.getUUID(), portal.generation(), here.dimension(), portal.back(), portal.backYaw(), portal.back(),
                portal.backYaw(), 1, false, now + lifetime());
        there.addFreshEntity(backward);
    }

    public static void collapsed(ServerLevel level, ReturnPortalEntity portal) {
        double centre = portal.getY() + ReturnPortalEntity.HALF_HEIGHT;
        level.playSound(null, portal.getX(), centre, portal.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 0.5F);
        level.playSound(null, portal.getX(), centre, portal.getZ(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private static Vec3 forward(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vec3(-Math.sin(radians), 0.0, Math.cos(radians));
    }
}
