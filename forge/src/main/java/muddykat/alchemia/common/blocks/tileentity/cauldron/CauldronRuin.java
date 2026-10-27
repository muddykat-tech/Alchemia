package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.utility.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public final class CauldronRuin {

    private CauldronRuin() {
    }

    public static boolean rubedo(TileEntityAlchemyCauldron cauldron, Player player) {
        BrewStability stability = cauldron.stability();
        if (cauldron.getWaterLevel() <= 0) return false;
        if (stability.spoiled()) {
            if (stability.revivalUsed()) {
                TileEntityAlchemyCauldron.message(player, "alchemia.brew.rubedo_revival_used");
                return false;
            }
            return revive(cauldron, player);
        }

        if (stability.hasBonus()) {
            TileEntityAlchemyCauldron.message(player, "alchemia.brew.rubedo_already");
            return false;
        }

        stability.addBonus();
        Level level = cauldron.getLevel();
        if (level != null && !level.isClientSide()) {
            level.playSound(null, cauldron.getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.2F);
            TileEntityAlchemyCauldron.message(player, "alchemia.brew.rubedo_added");
        }
        cauldron.sync();
        return true;
    }

    public static boolean destabilise(TileEntityAlchemyCauldron cauldron) {
        if (cauldron.stability().destabilise(cauldron.getBase())) {
            spoil(cauldron);
            return false;
        }

        Level level = cauldron.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockPos pos = cauldron.getBlockPos();
            level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 0.9F, 0.4F);
            level.playSound(null, pos, SoundEvents.WITHER_HURT, SoundSource.BLOCKS, 0.3F, 0.8F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            ParticleUtils.generateEvaporationParticles(level, pos, TileEntityAlchemyCauldron.SPOILED_COLOR);
            cauldron.sync();
        }
        return true;
    }

    private static boolean revive(TileEntityAlchemyCauldron cauldron, Player player) {
        if (cauldron.stability().revivalUsed()) return false;

        cauldron.stability().revive(cauldron.potency());
        cauldron.updateWaterColor();

        Level level = cauldron.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockPos pos = cauldron.getBlockPos();
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.8F);
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.6F, 1.4F);
            ParticleUtils.generateEvaporationParticles(level, pos, cauldron.getPotionColor());
            TileEntityAlchemyCauldron.message(player, "alchemia.brew.rubedo_revived");
        }
        cauldron.sync();
        return true;
    }

    private static void spoil(TileEntityAlchemyCauldron cauldron) {
        cauldron.stability().spoil(cauldron.getBase(), cauldron.potency());
        cauldron.updateWaterColor();

        Level level = cauldron.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockPos pos = cauldron.getBlockPos();
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.5F);
            level.playSound(null, pos, SoundEvents.WITHER_HURT, SoundSource.BLOCKS, 0.5F, 0.5F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            ParticleUtils.generateEvaporationParticles(level, pos, TileEntityAlchemyCauldron.SPOILED_COLOR);
            cauldron.sync();
        }
    }
}
