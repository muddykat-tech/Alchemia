package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.Whirlpool;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import javax.annotation.Nullable;

import java.util.Random;
import java.util.UUID;

public final class CauldronStirrer {

    private static final int SWIRL_SOUND_INTERVAL = 16;
    private static final int BUDGET = 10;
    private static final int SOUND_INTERVAL = 12;

    private final TileEntityAlchemyCauldron cauldron;
    private final Random random = new Random();
    private int stirCount;
    private int budget = BUDGET;
    private @Nullable UUID lastStirrer;
    private int swirlTicks;

    public CauldronStirrer(TileEntityAlchemyCauldron cauldron) {
        this.cauldron = cauldron;
    }

    public int count() {
        return stirCount;
    }

    public void restoreCount(int count) {
        stirCount = count;
    }

    public void regain() {
        if (budget < BUDGET) budget++;
    }

    public boolean hasStirrer() {
        return lastStirrer != null;
    }

    public @Nullable Player lastStirrer(Level level) {
        return lastStirrer == null ? null : level.getPlayerByUUID(lastStirrer);
    }

    public boolean teleporting() {
        return PotionMap.isReady() && cauldron.getCourse().teleporting(map());
    }

    public boolean stir(@Nullable Player player, int stirs) {
        Level level = cauldron.getLevel();
        if (level == null || level.isClientSide()) return false;

        stirCount++;
        boolean stirred = false;
        if (budget > 0) {
            for (int count = 0; count < stirs && !teleporting() && stirOnce(false); count++) stirred = true;
        }

        if (stirred) {
            budget--;
            cauldron.refreshAlignment();
            if (player != null) lastStirrer = player.getUUID();
            cauldron.journal().reveal(player);
            cauldron.journal().reached(player);
            cauldron.watch().chimeIfLinedUp();

            if (stirCount % SOUND_INTERVAL == 0) {
                level.playSound(null, cauldron.getBlockPos(), SoundEvents.BOAT_PADDLE_WATER, SoundSource.BLOCKS,
                        0.25F, 0.8F + random.nextFloat() * 0.4F);
            }
        }

        cauldron.setChanged();
        cauldron.sync();
        return stirred;
    }

    public void stirRepeatedly(int limit, boolean fine) {
        for (int count = 0; count < limit; count++) {
            if (!stirOnce(fine)) return;
        }
    }

    public boolean stirOnce(boolean fine) {
        if (!cauldron.canStir() || !PotionMap.isReady()) return false;

        PotionMap map = map();
        BrewCourse course = cauldron.getCourse();
        PotionMap.RouteLeg leg = course.current(map);
        boolean walking = leg != null && !leg.path().teleport();
        PathPoint before = course.position(map);
        if (!course.stir(map, fine ? TileEntityAlchemyCauldron.STIR_FINE_STEP : TileEntityAlchemyCauldron.STIR_STEP, this::arrive)) return false;

        cauldron.script().stir(fine);
        if (walking && !cauldron.isSpoiled()) {
            PathPoint after = course.position(map);
            if (cauldron.stability().strained(before.distanceTo(after), map.isDeadzone(after))) cauldron.destabilise();
        }
        return true;
    }

    public void swirl() {
        Level level = cauldron.getLevel();
        double intensity = cauldron.heatIntensity();
        if (level == null || intensity <= 0.0 || teleporting()) return;

        PotionMap map = map();
        BrewCourse course = cauldron.getCourse();
        PathPoint at = course.position(map);
        Whirlpool pool = map.whirlpoolAt(at);
        if (pool == null) return;

        PathPoint next = pool.carry(at, intensity, CauldronBoiler.TICK_SECONDS);
        boolean swallowed = pool.swallows(next);
        if (swallowed) {
            next = pool.exit();
        } else if (!map.inBounds(next) || map.isDeadzone(next)) {
            return;
        }

        PathPoint moved = course.carry(map, next);
        cauldron.script().drift(moved.x(), moved.y());

        BlockPos pos = cauldron.getBlockPos();
        Player heater = cauldron.boiler().lastHeater() == null ? null : level.getPlayerByUUID(cauldron.boiler().lastHeater());
        if (swallowed) {
            level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundSource.BLOCKS, 0.9F, 1.1F);
            level.playSound(null, pos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.BLOCKS, 0.35F, 1.5F);
            cauldron.journal().exit(pool, heater);
        } else if (swirlTicks++ % SWIRL_SOUND_INTERVAL == 0) {
            level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 0.5F, 0.9F + random.nextFloat() * 0.2F);
        }

        cauldron.refreshAlignment();
        cauldron.watch().chimeIfLinedUp();
        cauldron.journal().reveal(heater);
        cauldron.setChanged();
        cauldron.sync();
    }

    private boolean arrive(BrewCourse.Arrival arrival) {
        Level level = cauldron.getLevel();
        if (level == null) return true;
        BlockPos pos = cauldron.getBlockPos();

        if (arrival.path().dead()) return cauldron.destabilise();

        if (arrival.path().stalled()) {
            level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 0.7F, 0.6F);
            return true;
        }

        level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.SPLASH, pos);
        return true;
    }

    private PotionMap map() {
        return PotionMap.get(cauldron.getBase());
    }
}
