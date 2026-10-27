package muddykat.alchemia.common.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public record TeleportDestination(ServerLevel level, Vec3 pos, float yRot) {

    public static TeleportDestination respawn(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(player.getRespawnDimension());
        BlockPos bed = player.getRespawnPosition();
        if (level != null && bed != null) {
            Optional<Vec3> spot = Player.findRespawnPositionAndUseSpawnBlock(level, bed, player.getRespawnAngle(), player.isRespawnForced(), true);
            if (spot.isPresent()) return new TeleportDestination(level, spot.get(), player.getRespawnAngle());
        }
        return worldSpawn(player.server.overworld());
    }

    public static TeleportDestination worldSpawn(ServerLevel overworld) {
        BlockPos ground = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, overworld.getSharedSpawnPos());
        return new TeleportDestination(overworld, Vec3.atBottomCenterOf(ground), overworld.getSharedSpawnAngle());
    }

    public static Entity send(Entity entity, ServerLevel level, Vec3 pos, float yRot, float xRot) {
        if (entity.level() == level) {
            entity.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), yRot, xRot);
            return entity;
        }
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(level, pos.x, pos.y, pos.z, yRot, xRot);
            return player;
        }
        Entity moved = entity.changeDimension(level, new ITeleporter() {
            @Override
            public PortalInfo getPortalInfo(Entity traveller, ServerLevel destination, Function<ServerLevel, PortalInfo> fallback) {
                return new PortalInfo(pos, Vec3.ZERO, yRot, xRot);
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel source, ServerLevel destination) {
                return false;
            }
        });
        return moved == null ? entity : moved;
    }
}
