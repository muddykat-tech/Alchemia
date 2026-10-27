package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public final class ConditionWatch {

    private final TileEntityAlchemyCauldron cauldron;

    private @Nullable PathPoint watchedAt;
    private @Nullable AlchemiaEffect watchedEffect;
    private int watchedStrength;
    private Set<WorldEffects.Condition> watchedHolding = Set.of();
    private boolean linedUp;

    public ConditionWatch(TileEntityAlchemyCauldron cauldron) {
        this.cauldron = cauldron;
    }

    public void chimeIfLinedUp() {
        boolean now = linedUpHere();
        Level level = cauldron.getLevel();
        if (now && !linedUp && level != null) {
            level.playSound(null, cauldron.getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        linedUp = now;
    }

    public void watch() {
        if (cauldron.isSpoiled() || cauldron.getWaterLevel() <= 0 || !PotionMap.isReady()) {
            watchedAt = null;
            watchedEffect = null;
            watchedHolding = Set.of();
            return;
        }

        Level level = cauldron.getLevel();
        BlockPos pos = cauldron.getBlockPos();
        PathPoint at = cauldron.getBrewPosition();
        PotionMap.PotionEffectPosition found = cauldron.effectAt(at);
        int strength = brewableStrength(found);

        Set<WorldEffects.Condition> holding = found.getRecipe() == null
                ? Set.of() : WorldEffects.holding(found.getRecipe(), level, pos, cauldron.attunedHolder());

        boolean unmoved = at.equals(watchedAt) && Objects.equals(found.getRecipe(), watchedEffect);
        if (unmoved && found.getRecipe() != null && strength != watchedStrength && level != null) {
            boolean rising = strength > watchedStrength;
            WorldEffects.Rule cause = WorldEffects.shifted(watchedHolding, holding);
            int param = (rising ? 1 : 0) | (cause == null ? 0 : cause.ordinal() + 1) << 1;

            level.playSound(null, pos, rising ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            level.blockEvent(pos, cauldron.getBlockState().getBlock(), CauldronAmbience.EVENT_CONDITIONS_SHIFTED, param);
        }

        watchedAt = at;
        watchedEffect = found.getRecipe();
        watchedStrength = strength;
        watchedHolding = holding;
        linedUp = linedUpHere();
    }

    private boolean linedUpHere() {
        if (cauldron.isSpoiled() || cauldron.getWaterLevel() <= 0 || !PotionMap.isReady()) return false;

        PotionMap.PotionEffectPosition found = cauldron.effectAt(cauldron.getBrewPosition());
        return found.getEffect() != null && found.isPerfect() && !cauldron.isDormant(found);
    }

    private int brewableStrength(PotionMap.PotionEffectPosition found) {
        if (found.getEffect() == null || cauldron.isDormant(found)) return 0;
        return found.getStrength() + 1;
    }
}
