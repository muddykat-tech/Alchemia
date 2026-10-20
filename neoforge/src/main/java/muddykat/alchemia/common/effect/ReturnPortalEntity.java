package muddykat.alchemia.common.effect;

import muddykat.alchemia.client.effect.PortalParticles;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class ReturnPortalEntity extends Entity {

    public static final int OPEN_TICKS = PortalTimeline.OPEN_TICKS;
    public static final int TEAR_START = PortalTimeline.TEAR_START;
    public static final int COLLAPSE_TICKS = PortalTimeline.COLLAPSE_TICKS;
    public static final double HALF_WIDTH = PortalTimeline.HALF_WIDTH;
    public static final double HALF_HEIGHT = PortalTimeline.HALF_HEIGHT;
    public static final int LAVENDER = 0xB497E6;
    public static final int POWDER_BLUE = 0xA8CFF0;

    private static final EntityDataAccessor<Integer> COLLAPSE = SynchedEntityData.defineId(ReturnPortalEntity.class, EntityDataSerializers.INT);

    private @Nullable UUID owner;
    private int generation;
    private @Nullable ResourceKey<Level> targetLevel;
    private Vec3 target = Vec3.ZERO;
    private float targetYaw;
    private Vec3 back = Vec3.ZERO;
    private float backYaw;
    private int trips;
    private boolean homeward;
    private long expiresAt;
    private boolean away;
    private double lastSide = Double.NaN;

    public ReturnPortalEntity(EntityType<? extends ReturnPortalEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void configure(UUID owner, int generation, ResourceKey<Level> targetLevel, Vec3 target, float targetYaw,
                          Vec3 back, float backYaw, int trips, boolean homeward, long expiresAt) {
        this.owner = owner;
        this.generation = generation;
        this.targetLevel = targetLevel;
        this.target = target;
        this.targetYaw = targetYaw;
        this.back = back;
        this.backYaw = backYaw;
        this.trips = trips;
        this.homeward = homeward;
        this.expiresAt = expiresAt;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLLAPSE, 0);
    }

    public @Nullable UUID owner() {
        return owner;
    }

    public int generation() {
        return generation;
    }

    public @Nullable ResourceKey<Level> targetLevel() {
        return targetLevel;
    }

    public Vec3 target() {
        return target;
    }

    public float targetYaw() {
        return targetYaw;
    }

    public Vec3 back() {
        return back;
    }

    public float backYaw() {
        return backYaw;
    }

    public boolean homeward() {
        return homeward;
    }

    public int collapse() {
        return entityData.get(COLLAPSE);
    }

    public boolean collapsing() {
        return collapse() > 0;
    }

    public void useTrip() {
        trips--;
    }

    public void leftFor(long until) {
        away = true;
        expiresAt = until;
    }

    public Vec3 normal() {
        double yaw = Math.toRadians(getYRot());
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    public boolean faces(Vec3 point) {
        return side(point) < 0.0;
    }

    public void startCollapse() {
        if (!collapsing()) entityData.set(COLLAPSE, 1);
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
        if (collapsing()) {
            int ticks = collapse() + 1;
            entityData.set(COLLAPSE, ticks);
            if (ticks > COLLAPSE_TICKS) discard();
            return;
        }

        long now = server.getGameTime();
        ServerPlayer player = owner == null ? null : server.getServer().getPlayerList().getPlayer(owner);
        if (away && player != null && player.level() == server && player.distanceToSqr(this) < ReturnPortals.ARRIVAL_RANGE_SQR) {
            away = false;
            expiresAt = now + ReturnPortals.lifetime();
        }
        if (trips <= 0 || now > expiresAt || !ReturnPortals.current(this)) {
            startCollapse();
            ReturnPortals.collapsed(server, this);
            return;
        }

        if (player == null || player.level() != server || !player.isAlive() || player.isSpectator()) {
            lastSide = Double.NaN;
            return;
        }
        double side = side(player.position());
        boolean crossed = lastSide < 0.0 && side >= 0.0 && inOpening(player);
        lastSide = side;
        if (crossed && tickCount >= OPEN_TICKS) ReturnPortals.traverse(this, player, server);
    }

    private double side(Vec3 point) {
        Vec3 normal = normal();
        return (point.x - getX()) * normal.x + (point.z - getZ()) * normal.z;
    }

    private boolean inOpening(ServerPlayer player) {
        double yaw = Math.toRadians(getYRot());
        double lateral = (player.getX() - getX()) * Math.cos(yaw) + (player.getZ() - getZ()) * Math.sin(yaw);
        if (Math.abs(lateral) > HALF_WIDTH + player.getBbWidth() / 2.0) return false;
        return player.getY() < getY() + HALF_HEIGHT * 2.0 && player.getY() + player.getBbHeight() > getY();
    }

    private void clientTick() {
        double centreY = getY() + HALF_HEIGHT;
        if (!collapsing()) {
            if (tickCount == 1) {
                level().playLocalSound(getX(), centreY, getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.35F, 0.55F, false);
            }
            if (tickCount == TEAR_START) {
                level().playLocalSound(getX(), centreY, getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.6F, 0.4F, false);
                level().playLocalSound(getX(), centreY, getZ(), SoundEvents.PORTAL_TRIGGER, SoundSource.NEUTRAL, 0.25F, 1.5F, false);
            }
        } else if (collapse() == 6) {
            level().playLocalSound(getX(), centreY, getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 0.5F, 1.6F, false);
        } else if (collapse() == 15) {
            level().playLocalSound(getX(), centreY, getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.7F, 1.6F, false);
            level().playLocalSound(getX(), centreY, getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 1.0F, 0.6F, false);
        }

        PortalParticles.tick(this);
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
        owner = input.getString("owner").map(UUID::fromString).orElse(null);
        generation = input.getIntOr("generation", 0);
        targetLevel = input.getString("target_level").map(Identifier::tryParse)
                .map(id -> ResourceKey.create(Registries.DIMENSION, id)).orElse(null);
        target = new Vec3(input.getDoubleOr("target_x", 0.0), input.getDoubleOr("target_y", 0.0), input.getDoubleOr("target_z", 0.0));
        targetYaw = input.getFloatOr("target_yaw", 0.0F);
        back = new Vec3(input.getDoubleOr("back_x", 0.0), input.getDoubleOr("back_y", 0.0), input.getDoubleOr("back_z", 0.0));
        backYaw = input.getFloatOr("back_yaw", 0.0F);
        trips = input.getIntOr("trips", 0);
        homeward = input.getBooleanOr("homeward", true);
        expiresAt = input.getLongOr("expires_at", 0L);
        away = input.getBooleanOr("away", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (owner != null) output.putString("owner", owner.toString());
        output.putInt("generation", generation);
        if (targetLevel != null) output.putString("target_level", targetLevel.identifier().toString());
        output.putDouble("target_x", target.x);
        output.putDouble("target_y", target.y);
        output.putDouble("target_z", target.z);
        output.putFloat("target_yaw", targetYaw);
        output.putDouble("back_x", back.x);
        output.putDouble("back_y", back.y);
        output.putDouble("back_z", back.z);
        output.putFloat("back_yaw", backYaw);
        output.putInt("trips", trips);
        output.putBoolean("homeward", homeward);
        output.putLong("expires_at", expiresAt);
        output.putBoolean("away", away);
    }
}
