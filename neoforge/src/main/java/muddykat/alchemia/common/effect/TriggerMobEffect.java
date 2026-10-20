package muddykat.alchemia.common.effect;

import net.minecraft.server.level.ServerLevel;
#if MC_VER >= 26200
import net.minecraft.world.effect.InstantaneousMobEffect;
#else
import net.minecraft.world.effect.InstantenousMobEffect;
#endif
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

#if MC_VER >= 26200
public abstract class TriggerMobEffect extends InstantaneousMobEffect {
#else
public abstract class TriggerMobEffect extends InstantenousMobEffect {
#endif

    protected TriggerMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
#if MC_VER >= 26200
    public void applyInstantaneousEffect(ServerLevel level, @Nullable Entity source, @Nullable Entity owner, LivingEntity mob,
                                         int amplification, double scale) {
#else
    public void applyInstantenousEffect(ServerLevel level, @Nullable Entity source, @Nullable Entity owner, LivingEntity mob,
                                        int amplification, double scale) {
#endif
        if (source instanceof AreaEffectCloud) return;
        if (source instanceof AbstractThrownPotion) {
            splashed(level, owner, mob, amplification, scale);
        } else {
            drunk(level, mob, amplification);
        }
    }

    public boolean lingers() {
        return false;
    }

    protected abstract void drunk(ServerLevel level, LivingEntity drinker, int amplifier);

    protected void splashed(ServerLevel level, @Nullable Entity owner, LivingEntity mob, int amplifier, double scale) {
    }

    public void shattered(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier, boolean lingering) {
    }
}
