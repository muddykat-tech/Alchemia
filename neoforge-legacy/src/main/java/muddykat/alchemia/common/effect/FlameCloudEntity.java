package muddykat.alchemia.common.effect;

import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.UUID;

public class FlameCloudEntity extends Entity {

    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(FlameCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(FlameCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> LINGERING = SynchedEntityData.defineId(FlameCloudEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int BLAST_TICKS = 16;
    public static final int PULSE_TICKS = 20;
    private static final float IGNITE_TICKS = 8.0F;
    private static final float FLAMES_PER_AREA = 0.55F;
    private static final float BASE_DAMAGE = 1.0F;
    private static final float PER_LEVEL = 0.75F;
    private static final float BLAST_DAMAGE = 2.0F;
    private static final float BLAST_PER_LEVEL = 1.5F;
    private static final float SHRINK_FROM = 0.75F;
    private static final float SHRINK_TO = 0.3F;

    private @Nullable UUID owner;
    private int strength;
    private boolean burstShown;

    public FlameCloudEntity(EntityType<? extends FlameCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void configure(@Nullable UUID owner, int strength, float radius, int duration, boolean lingering) {
        this.owner = owner;
        this.strength = strength;
        entityData.set(RADIUS, radius);
        entityData.set(DURATION, duration);
        entityData.set(LINGERING, lingering);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 2.0F);
        builder.define(DURATION, 100);
        builder.define(LINGERING, false);
    }

    public boolean lingering() {
        return entityData.get(LINGERING);
    }

    public float fullRadius() {
        return entityData.get(RADIUS);
    }

    public float radius() {
        return radius(tickCount);
    }

    public float radius(float age) {
        int duration = entityData.get(DURATION);
        if (age >= duration) return 0.0F;
        if (!lingering()) return entityData.get(RADIUS);

        float shrinkStart = duration * SHRINK_FROM;
        if (age >= shrinkStart) {
            float t = (age - shrinkStart) / (duration - shrinkStart);
            return entityData.get(RADIUS) * (SHRINK_TO + (1.0F - SHRINK_TO) * (1.0F - t));
        }
        return entityData.get(RADIUS);
    }

    public int duration() {
        return entityData.get(DURATION);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            serverTick(server);
        } else {
            clientTick();
        }
    }

    private void serverTick(ServerLevel server) {
        if (tickCount >= duration()) {
            discard();
            return;
        }

        if (lingering()) {
            if ((tickCount - 1) % PULSE_TICKS == 0) scorch(server, BASE_DAMAGE + PER_LEVEL * strength, 3.0F);
        } else if (tickCount == 1) {
            scorch(server, BLAST_DAMAGE + BLAST_PER_LEVEL * strength, 4.0F);
        }
    }

    private void scorch(ServerLevel server, float damage, float seconds) {
        float radius = radius();
        Vec3 center = position();
        DamageSource source = server.damageSources().source(FlameBurst.FLAME_BURST, this, owner == null ? null : server.getEntity(owner));
        AABB bounds = new AABB(center, center).inflate(radius, 2.0, radius);
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, bounds, entity -> inside(entity, center, radius))) {
            if (target.hurt(source, damage)) target.igniteForSeconds(seconds);
        }
    }

    private static boolean inside(LivingEntity entity, Vec3 center, float radius) {
        Vec3 at = entity.position();
        double dx = at.x - center.x;
        double dz = at.z - center.z;
        double dy = at.y - center.y;
        return dx * dx + dz * dz <= radius * radius && dy >= -1.0 && dy <= 2.0;
    }

    private void clientTick() {
        if (lingering()) {
            napalmParticles();
        } else if (!burstShown) {
            burstShown = true;
            blastParticles();
        }
    }

    private void blastParticles() {
        float radius = fullRadius();
        Vec3 at = position();
        int jets = 28 + (int) (radius * 8);
        for (int index = 0; index < jets; index++) {
            double angle = index * 2 * Math.PI / jets + random.nextDouble() * 0.2;
            double speed = radius * (0.09 + random.nextDouble() * 0.05);
            level().addParticle(ParticleTypes.FLAME, at.x, at.y + 0.15, at.z, Math.cos(angle) * speed, 0.01 + random.nextDouble() * 0.04,
                    Math.sin(angle) * speed);
        }
        for (int index = 0; index < 18; index++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 0.05 + random.nextDouble() * 0.08;
            level().addParticle(ParticleTypes.FLAME, at.x, at.y + 0.3, at.z, Math.cos(angle) * speed, 0.12 + random.nextDouble() * 0.12,
                    Math.sin(angle) * speed);
        }
        for (int index = 0; index < 10; index++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double reach = radius * 0.4 * random.nextDouble();
            level().addParticle(ParticleTypes.LARGE_SMOKE, at.x + Math.cos(angle) * reach, at.y + 0.6 + random.nextDouble() * 0.6,
                    at.z + Math.sin(angle) * reach, Math.cos(angle) * 0.02, 0.06 + random.nextDouble() * 0.05, Math.sin(angle) * 0.02);
        }
        for (int index = 0; index < 8 + (int) radius * 2; index++) {
            level().addParticle(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 0.0, 0.0, 0.0);
        }
    }

    private void napalmParticles() {
        float radius = Math.min(radius(), fullRadius() * Math.min(1.0F, tickCount / IGNITE_TICKS));
        if (radius <= 0.0F) return;
        Vec3 at = position();

        boolean flare = (tickCount - 1) % PULSE_TICKS == 0;
        int flames = 2 + (int) (radius * radius * FLAMES_PER_AREA * (flare ? 3.0F : 1.0F));
        for (int index = 0; index < flames; index++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double reach = radius * Math.sqrt(random.nextDouble());
            double edge = reach / radius;
            double scale = (1.6 - 0.8 * edge) * (0.8 + random.nextDouble() * 0.5) * (flare ? 1.4 : 1.0);
            level().addParticle(ParticleTypeRegistry.NAPALM_FLAME.get(), at.x + Math.cos(angle) * reach, at.y + 0.18 * scale,
                    at.z + Math.sin(angle) * reach, scale, 0.012 + random.nextDouble() * 0.025 * (1.2 - edge), 0.0);
        }
        if (random.nextFloat() < 0.35F * radius) {
            Vec3 spot = spot(at, radius);
            level().addParticle(ParticleTypes.SMOKE, spot.x, spot.y + 0.7 + random.nextDouble() * 0.4, spot.z, 0.0, 0.04, 0.0);
        }
        if (random.nextFloat() < 0.08F) {
            Vec3 spot = spot(at, radius * 0.6F);
            level().addParticle(ParticleTypes.LARGE_SMOKE, spot.x, spot.y + 1.0, spot.z, 0.0, 0.05, 0.0);
        }
        if (random.nextFloat() < 0.05F * radius) {
            Vec3 spot = spot(at, radius);
            level().addParticle(ParticleTypes.LAVA, spot.x, spot.y + 0.1, spot.z, 0.0, 0.0, 0.0);
        }
        if (random.nextFloat() < 0.15F) {
            Vec3 spot = spot(at, radius);
            level().addParticle(ParticleTypes.SMALL_FLAME, spot.x, spot.y + 0.05, spot.z, 0.0, 0.01, 0.0);
        }
        if (random.nextFloat() < 0.04F) {
            level().playLocalSound(at.x, at.y, at.z, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.8F + random.nextFloat() * 0.4F,
                    0.6F + random.nextFloat() * 0.4F, false);
        }
    }

    private Vec3 spot(Vec3 at, float radius) {
        double angle = random.nextDouble() * 2 * Math.PI;
        double reach = radius * Math.sqrt(random.nextDouble());
        return new Vec3(at.x + Math.cos(angle) * reach, at.y, at.z + Math.sin(angle) * reach);
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
        owner = input.contains("owner") ? UUID.fromString(input.getString("owner")) : null;
        strength = (input.contains("strength") ? input.getInt("strength") : 0);
        entityData.set(RADIUS, (input.contains("radius") ? input.getFloat("radius") : 2.0F));
        entityData.set(DURATION, (input.contains("duration") ? input.getInt("duration") : 100));
        entityData.set(LINGERING, (input.contains("lingering") ? input.getBoolean("lingering") : true));
        tickCount = (input.contains("age") ? input.getInt("age") : 0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag output) {
        if (owner != null) output.putString("owner", owner.toString());
        output.putInt("strength", strength);
        output.putFloat("radius", entityData.get(RADIUS));
        output.putInt("duration", entityData.get(DURATION));
        output.putBoolean("lingering", lingering());
        output.putInt("age", tickCount);
    }
}
