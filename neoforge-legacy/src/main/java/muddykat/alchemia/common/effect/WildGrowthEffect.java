package muddykat.alchemia.common.effect;

import muddykat.alchemia.registration.registers.EntityTypeRegistry;
import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public class WildGrowthEffect extends MobEffect implements ShatterEffect {

    private static final int ARROW_RADIUS = 1;
    private static final int PULSE_TICKS = 10;
    private static final int REACH = 4;
    private static final int TICKS_PER_LEVEL = 12;
    private static final int FIELD_TICKS = 300;
    private static final int FIELD_TICKS_PER_LEVEL = 100;
    private static final float FIELD_RADIUS = 3.5F;
    private static final float FIELD_RADIUS_PER_LEVEL = 0.5F;

    public WildGrowthEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x5FB83A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public net.minecraft.core.particles.ParticleOptions createParticleOptions(net.minecraft.world.effect.MobEffectInstance instance) {
        return ParticleTypeRegistry.WILD_SWIRL.get();
    }

    @Override
    public boolean applyEffectTick(LivingEntity mob, int amplifier) {
        if (!(mob.level() instanceof ServerLevel level)) return true;
        int strength = amplifier + 1;
        WildGrowth.walk(level, mob, strength);
        if (mob.tickCount % PULSE_TICKS == 0) {
            WildGrowth.quicken(level, mob.blockPosition(), REACH + strength, TICKS_PER_LEVEL * strength, mob.getRandom());
        }
        return true;
    }

    @Override
    public void arrowLanded(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier) {
        int strength = amplifier + 1;
        WildGrowth.overflow(level, at, strength, ARROW_RADIUS + strength, level.getRandom());
    }

    @Override
    public void shattered(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier, boolean lingering) {
        int strength = amplifier + 1;
        if (!lingering) {
            WildGrowth.overflow(level, at, strength, level.getRandom());
            return;
        }
        Vec3 ground = FlameBurst.ground(level, at);
        GrowthFieldEntity field = new GrowthFieldEntity(EntityTypeRegistry.GROWTH_FIELD.get(), level);
        field.setPos(ground.x, ground.y, ground.z);
        field.configure(strength, FIELD_RADIUS + FIELD_RADIUS_PER_LEVEL * strength, FIELD_TICKS + FIELD_TICKS_PER_LEVEL * strength);
        level.addFreshEntity(field);
    }
}
