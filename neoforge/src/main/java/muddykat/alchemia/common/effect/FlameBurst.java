package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.registration.registers.EntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FlameBurst {

    public static final ResourceKey<DamageType> FLAME_BURST = ResourceKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(Alchemia.MODID, "flame_burst"));

    private static final double BASE_RADIUS = 1.5;
    private static final double RADIUS_PER_LEVEL = 1.25;
    private static final int BASE_DURATION = 60;
    private static final int DURATION_PER_LEVEL = 40;
    private static final int GROUND_SEARCH = 4;

    private FlameBurst() {
    }

    public static double radius(int level) {
        return BASE_RADIUS + RADIUS_PER_LEVEL * level;
    }

    public static void burst(ServerLevel level, Vec3 at, @Nullable Entity cause, int strength, boolean lingering) {
        Vec3 ground = ground(level, at);
        double radius = radius(strength);

        FlameCloudEntity cloud = new FlameCloudEntity(EntityTypeRegistry.FLAME_CLOUD.get(), level);
        cloud.setPos(ground.x, ground.y, ground.z);
        int duration = lingering ? BASE_DURATION + DURATION_PER_LEVEL * strength : FlameCloudEntity.BLAST_TICKS;
        cloud.configure(cause == null ? null : cause.getUUID(), strength, (float) radius, duration, lingering);
        level.addFreshEntity(cloud);

        level.playSound(null, ground.x, ground.y, ground.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9F, 0.6F);
        level.playSound(null, ground.x, ground.y, ground.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.7F, 0.5F);
        if (lingering) level.playSound(null, ground.x, ground.y, ground.z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.2F, 0.7F);
    }

    static Vec3 ground(ServerLevel level, Vec3 at) {
        BlockPos cursor = BlockPos.containing(at);
        for (int depth = 0; depth < GROUND_SEARCH; depth++) {
            BlockPos below = cursor.below();
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) return new Vec3(at.x, cursor.getY(), at.z);
            cursor = below;
        }
        return at;
    }
}
