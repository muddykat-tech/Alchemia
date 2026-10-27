package muddykat.alchemia.common.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public interface ShatterEffect {

    void shattered(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier, boolean lingering);

    default void arrowLanded(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier) {
    }
}
