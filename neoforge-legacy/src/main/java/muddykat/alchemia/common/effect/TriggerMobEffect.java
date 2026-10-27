package muddykat.alchemia.common.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public abstract class TriggerMobEffect extends InstantenousMobEffect implements ShatterEffect {

    protected TriggerMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity owner, LivingEntity mob, int amplification, double scale) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        if (source instanceof AreaEffectCloud) return;
        if (source instanceof ThrownPotion) {
            splashed(level, owner, mob, amplification, scale);
        } else {
            drunk(level, mob, amplification);
        }
    }

    public void struck(ServerLevel level, LivingEntity mob, int amplifier) {
        drunk(level, mob, amplifier);
    }

    public boolean lingers() {
        return false;
    }

    protected abstract void drunk(ServerLevel level, LivingEntity drinker, int amplifier);

    protected void splashed(ServerLevel level, @Nullable Entity owner, LivingEntity mob, int amplifier, double scale) {
    }

    @Override
    public void shattered(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier, boolean lingering) {
    }
}
