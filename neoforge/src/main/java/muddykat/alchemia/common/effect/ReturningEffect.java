package muddykat.alchemia.common.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ReturningEffect extends TriggerMobEffect {

    public ReturningEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB497E6);
    }

    @Override
    protected void drunk(ServerLevel level, LivingEntity drinker, int amplifier) {
        if (drinker instanceof ServerPlayer player) ReturnPortals.open(player, player.position(), amplifier + 1, false);
    }

    @Override
    public void shattered(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier, boolean lingering) {
        if (owner instanceof ServerPlayer player && player.level() == level) ReturnPortals.open(player, at, amplifier + 1, true);
    }
}
