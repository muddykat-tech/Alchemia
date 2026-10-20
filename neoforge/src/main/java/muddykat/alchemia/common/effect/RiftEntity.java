package muddykat.alchemia.common.effect;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RiftEntity extends Entity {

    public static final int CHARGE = 0;
    public static final int ARRIVE = 1;
    public static final int CHARGE_TICKS = 30;
    public static final int CRACK_TICKS = 12;
    public static final int ARRIVE_HOLD = 12;
    public static final int CLOSE_TICKS = 22;
    public static final int SLAM_TICK = 14;
    public static final float ARRIVE_HEIGHT = 1.0F;

    private static final int CHARGE_LIMIT = 600;
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(RiftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLOSING = SynchedEntityData.defineId(RiftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(RiftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OPENED = SynchedEntityData.defineId(RiftEntity.class, EntityDataSerializers.INT);

    private boolean heardClosing;
    private boolean heardOpening;

    public RiftEntity(EntityType<? extends RiftEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void configure(int mode, int owner) {
        entityData.set(MODE, mode);
        entityData.set(OWNER, owner);
    }

    public int owner() {
        return entityData.get(OWNER);
    }

    public void startOpening() {
        if (entityData.get(OPENED) == 0) entityData.set(OPENED, tickCount + 1);
    }

    public float opened(float partialTicks) {
        int opened = entityData.get(OPENED);
        return opened == 0 ? -1.0F : tickCount - (opened - 1) + partialTicks;
    }

    public int mode() {
        return entityData.get(MODE);
    }

    public int closing() {
        return entityData.get(CLOSING);
    }

    public boolean isClosing() {
        return closing() > 0;
    }

    public void startClosing() {
        if (!isClosing()) entityData.set(CLOSING, 1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MODE, CHARGE);
        builder.define(CLOSING, 0);
        builder.define(OWNER, -1);
        builder.define(OPENED, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            clientTick();
        } else {
            serverTick();
        }
    }

    private void serverTick() {
        if (isClosing()) {
            int ticks = closing() + 1;
            entityData.set(CLOSING, ticks);
            if (ticks > CLOSE_TICKS) discard();
            return;
        }
        if (mode() == ARRIVE && tickCount >= ARRIVE_HOLD) startClosing();
        if (mode() == CHARGE && tickCount > CHARGE_LIMIT) startClosing();
    }

    private void clientTick() {
        if (mode() == ARRIVE) {
            if (tickCount == 1) {
                level().playLocalSound(getX(), getY() + ARRIVE_HEIGHT, getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6F, 0.6F, false);
                level().playLocalSound(getX(), getY() + ARRIVE_HEIGHT, getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F, false);
            }
        }
        if (mode() == CHARGE && tickCount == 1) {
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5F, 0.45F, false);
        }
        if (mode() == CHARGE && tickCount == CRACK_TICKS) {
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 0.35F, false);
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.3F, 1.4F, false);
        }
        if (opened(0.0F) >= 0.0F && !heardOpening) {
            heardOpening = true;
            level().playLocalSound(getX(), getY() + ARRIVE_HEIGHT, getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.5F, false);
            level().playLocalSound(getX(), getY() + ARRIVE_HEIGHT, getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.8F, 0.6F, false);
        }
        if (isClosing() && !heardClosing) {
            heardClosing = true;
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5F, 1.7F, false);
        }
        if (mode() == ARRIVE && closing() == SLAM_TICK) {
            level().playLocalSound(getX(), getY() + ARRIVE_HEIGHT, getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.6F, false);
            level().playLocalSound(getX(), getY() + ARRIVE_HEIGHT, getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 0.6F, false);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
