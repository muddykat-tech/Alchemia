package muddykat.alchemia.common.effect;

import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

public class GrowthFieldEntity extends Entity {

    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(GrowthFieldEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(GrowthFieldEntity.class, EntityDataSerializers.INT);

    private static final int QUICKEN_TICKS = 5;
    private static final int TEND_TICKS = 4;
    private static final int COLUMNS_BASE = 2;
    private static final int COLUMNS_PER_LEVEL = 2;
    private static final int TICKS_BASE = 6;
    private static final int TICKS_PER_LEVEL = 3;
    private static final int FADE_TICKS = 40;

    private int strength = 1;

    public GrowthFieldEntity(EntityType<? extends GrowthFieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void configure(int strength, float radius, int duration) {
        this.strength = strength;
        entityData.set(RADIUS, radius);
        entityData.set(DURATION, duration);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 3.0F);
        builder.define(DURATION, 400);
    }

    public float radius() {
        return entityData.get(RADIUS);
    }

    public int duration() {
        return entityData.get(DURATION);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            if (tickCount >= duration()) {
                discard();
                return;
            }
            RandomSource random = server.getRandom();
            if (tickCount % QUICKEN_TICKS == 0) {
                WildGrowth.quicken(server, blockPosition(), Mth.ceil(radius()), TICKS_BASE + TICKS_PER_LEVEL * strength, random);
            }
            if (tickCount % TEND_TICKS == 0) WildGrowth.tend(server, blockPosition(), radius(), strength, COLUMNS_BASE + COLUMNS_PER_LEVEL * strength, random);
        } else {
            clientTick();
        }
    }

    private void clientTick() {
        RandomSource random = this.random;
        float radius = radius();
        int left = duration() - tickCount;
        float density = Mth.clamp(left / (float) FADE_TICKS, 0.0F, 1.0F) * Math.min(1.0F, radius / 3.0F);
        if (tickCount == 1) {
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.NEUTRAL, 0.9F, 0.8F, false);
            for (int index = 0; index < 40; index++) {
                level().addParticle(ParticleTypeRegistry.WILD_SWIRL.get(), getX(), getY() + 0.1, getZ(), radius * (0.2 + random.nextFloat() * 0.8),
                        0.03 + random.nextFloat() * 0.05, (random.nextBoolean() ? 1.0 : -1.0) * (0.06 + random.nextFloat() * 0.08));
            }
        }
        if (random.nextFloat() < 0.9F * density) {
            level().addParticle(ParticleTypeRegistry.WILD_SWIRL.get(), getX(), getY() + 0.05 + random.nextFloat() * 0.3, getZ(),
                    radius * (0.35 + random.nextFloat() * 0.65), 0.012 + random.nextFloat() * 0.025, 0.045 + random.nextFloat() * 0.04);
        }
        if (random.nextFloat() < 0.35F * density) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double reach = radius * Math.sqrt(random.nextDouble());
            level().addParticle(ParticleTypeRegistry.WILD_LEAF.get(), getX() + Math.cos(angle) * reach, getY() + 0.1, getZ() + Math.sin(angle) * reach,
                    0.0, 0.06 + random.nextFloat() * 0.05, 0.0);
        }
        if (random.nextFloat() < 0.15F * density) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double reach = radius * Math.sqrt(random.nextDouble());
            level().addParticle(ParticleTypes.HAPPY_VILLAGER, getX() + Math.cos(angle) * reach, getY() + 0.2 + random.nextFloat() * 0.6,
                    getZ() + Math.sin(angle) * reach, 0.0, 0.0, 0.0);
        }
        if (random.nextFloat() < 0.01F * density) {
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.AZALEA_LEAVES_STEP, SoundSource.NEUTRAL, 0.5F, 0.8F + random.nextFloat() * 0.4F, false);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag input) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag output) {
    }
}
