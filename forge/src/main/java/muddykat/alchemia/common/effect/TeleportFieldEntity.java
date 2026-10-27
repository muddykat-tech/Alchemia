package muddykat.alchemia.common.effect;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportFieldEntity extends Entity {

    public static final int FIELD = 0;
    public static final int SCAR = 1;
    public static final int SCAR_TICKS = 72;
    public static final int SCAR_SNAP = 52;
    public static final int COLLAPSE_TICKS = PortalTimeline.COLLAPSE_TICKS;
    public static final int SLAM_TICK = PortalTimeline.SLAM_TICK;
    public static final float FIELD_RADIUS = 2.0F;
    public static final float SPLASH_REACH = 4.0F;

    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(TeleportFieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(TeleportFieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(TeleportFieldEntity.class, EntityDataSerializers.LONG);

    private static final int OPEN_AT = PortalTimeline.TEAR_START + 9;
    private static final int COOLDOWN = 100;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private int strength = 1;
    private boolean recall;
    private long heard = Long.MIN_VALUE;

    public TeleportFieldEntity(EntityType<? extends TeleportFieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void configure(int mode, int strength, int duration) {
        this.strength = strength;
        entityData.set(MODE, mode);
        entityData.set(DURATION, duration);
        entityData.set(START, level().getGameTime());
    }

    public void recall() {
        recall = true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MODE, FIELD);
        entityData.define(DURATION, 200);
        entityData.define(START, 0L);
    }

    public int mode() {
        return entityData.get(MODE);
    }

    public int duration() {
        return entityData.get(DURATION);
    }

    public float age(float partialTicks) {
        return level().getGameTime() - entityData.get(START) + partialTicks;
    }

    public float collapse(float age) {
        return Math.max(0.0F, age - (duration() - COLLAPSE_TICKS));
    }

    @Override
    public void tick() {
        super.tick();
        long age = level().getGameTime() - entityData.get(START);
        if (level() instanceof ServerLevel server) {
            if (age >= duration()) {
                discard();
                return;
            }
            if (mode() == FIELD && age >= OPEN_AT && age < duration() - COLLAPSE_TICKS) pull(server);
        } else {
            clientTick(age);
        }
    }

    private void pull(ServerLevel server) {
        long now = server.getGameTime();
        cooldowns.values().removeIf(until -> until < now);
        Vec3 centre = position();
        AABB bounds = new AABB(centre, centre).inflate(FIELD_RADIUS, 2.0, FIELD_RADIUS);
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, bounds, entity -> inside(entity, centre))) {
            if (cooldowns.containsKey(target.getUUID()) || target.isSpectator()) continue;
            cooldowns.put(target.getUUID(), now + COOLDOWN);
            if (TeleportSearch.active(target)) continue;
            if (recall) {
                TeleportSearch.recall(target, strength);
            } else {
                TeleportSearch.begin(target, strength);
            }
        }
    }

    private static boolean inside(LivingEntity entity, Vec3 centre) {
        double dx = entity.getX() - centre.x;
        double dz = entity.getZ() - centre.z;
        double dy = entity.getY() - centre.y;
        return dx * dx + dz * dz <= FIELD_RADIUS * FIELD_RADIUS && dy >= -0.5 && dy <= 1.5;
    }

    private void clientTick(long age) {
        long previous = heard == Long.MIN_VALUE ? age - 1 : heard;
        heard = age;
        Vec3 at = position();
        if (mode() == SCAR) {
            if (crossed(previous, age, 1)) {
                level().playLocalSound(at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.5F, 1.3F, false);
            }
            if (crossed(previous, age, SCAR_SNAP + 3)) {
                level().playLocalSound(at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 0.8F, 0.8F, false);
            }
            return;
        }

        int collapseAt = duration() - COLLAPSE_TICKS;
        for (int level = 0; level < PortalTimeline.HIT_TICKS.length; level++) {
            if (crossed(previous, age, PortalTimeline.HIT_TICKS[level])) {
                level().playLocalSound(at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, PortalTimeline.HIT_VOLUME[level] + 0.1F,
                        PortalTimeline.HIT_PITCH[level], false);
            }
            if (crossed(previous, age, collapseAt + PortalTimeline.unhitTick(level))) {
                level().playLocalSound(at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, PortalTimeline.HIT_VOLUME[level],
                        2.0F - PortalTimeline.HIT_PITCH[level] * 0.5F, false);
            }
        }
        for (int tick = PortalTimeline.FRAME_FROM; tick < PortalTimeline.SLAM_TICK; tick += 2) {
            if (crossed(previous, age, collapseAt + tick)) {
                level().playLocalSound(at.x, at.y, at.z, SoundEvents.GLASS_HIT, SoundSource.NEUTRAL, 0.45F,
                        tick < PortalTimeline.HOLE_FROM ? 1.5F + random.nextFloat() * 0.4F : 0.9F + random.nextFloat() * 0.3F, false);
            }
        }
        if (crossed(previous, age, PortalTimeline.TEAR_START)) {
            level().playLocalSound(at.x, at.y, at.z, SoundEvents.PORTAL_TRIGGER, SoundSource.NEUTRAL, 0.25F, 1.5F, false);
        }
        if (crossed(previous, age, collapseAt + 6)) {
            level().playLocalSound(at.x, at.y, at.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 0.5F, 1.6F, false);
        }
        if (crossed(previous, age, collapseAt + SLAM_TICK)) {
            level().playLocalSound(at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.8F, 1.5F, false);
            level().playLocalSound(at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 1.0F, 0.6F, false);
            for (int index = 0; index < 40; index++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double reach = FIELD_RADIUS * Math.sqrt(random.nextDouble());
                level().addParticle(ParticleTypes.END_ROD, at.x + Math.cos(angle) * reach, at.y + 0.05, at.z + Math.sin(angle) * reach,
                        Math.cos(angle) * reach * 0.03, 0.06 + random.nextDouble() * 0.08, Math.sin(angle) * reach * 0.03);
            }
        }
        if (age >= OPEN_AT && age < collapseAt) {
            if (random.nextFloat() < 0.5F) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double reach = FIELD_RADIUS * 0.85 * Math.sqrt(random.nextDouble());
                level().addParticle(ParticleTypes.REVERSE_PORTAL, at.x + Math.cos(angle) * reach, at.y + 0.05, at.z + Math.sin(angle) * reach,
                        0.0, 0.03 + random.nextDouble() * 0.04, 0.0);
            }
            if (random.nextFloat() < 0.02F) {
                level().playLocalSound(at.x, at.y, at.z, SoundEvents.PORTAL_AMBIENT, SoundSource.NEUTRAL, 0.3F, 1.2F + random.nextFloat() * 0.3F, false);
            }
        }
    }

    private static boolean crossed(long previous, long age, int at) {
        return previous < at && age >= at;
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
