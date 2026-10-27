package muddykat.alchemia.common.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public class FlamesEffect extends TriggerMobEffect {

    public FlamesEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF7A1A);
    }

    @Override
    public boolean lingers() {
        return true;
    }

    @Override
    protected void drunk(ServerLevel level, LivingEntity drinker, int amplifier) {
        FlameBurst.burst(level, drinker.position().add(0.0, drinker.getBbHeight() / 2.0, 0.0), drinker, amplifier + 1, false);
    }

    @Override
    public void struck(ServerLevel level, LivingEntity mob, int amplifier) {
    }

    @Override
    public void arrowLanded(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier) {
        FlameBurst.arrowBurst(level, at, owner, amplifier + 1);
    }

    @Override
    public void shattered(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier, boolean lingering) {
        FlameBurst.burst(level, at, owner, amplifier + 1, lingering);
    }
}
