package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.utility.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Random;

public final class CauldronBrewing {

    private static final Random RANDOM = new Random();

    private CauldronBrewing() {
    }

    public static InteractionResult brew(TileEntityAlchemyCauldron cauldron, Player player) {
        Level level = cauldron.getLevel();
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;

        switch (cauldron.checkBrew(cauldron.getBrewPosition())) {
            case NOTHING -> TileEntityAlchemyCauldron.message(player, "alchemia.brew.nothing");
            case WEAKER -> TileEntityAlchemyCauldron.message(player, "alchemia.brew.weaker");
            case TOO_POTENT -> TileEntityAlchemyCauldron.message(player, "alchemia.brew.too_potent");
            case DORMANT -> TileEntityAlchemyCauldron.message(player, "alchemia.brew.dormant");
            case UNAVAILABLE -> TileEntityAlchemyCauldron.message(player,
                    cauldron.getWaterLevel() <= 0 ? "alchemia.brew.no_water" : "alchemia.brew.ruined");
            case READY -> {
                Holder<MobEffect> effect = cauldron.effectAt(cauldron.getBrewPosition()).getEffect();
                if (brewEffect(cauldron)) cauldron.journal().brewed(player, effect);

                BlockPos pos = cauldron.getBlockPos();
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.1F);
                level.gameEvent(null, GameEvent.SPLASH, pos);
                cauldron.setChanged();
                cauldron.sync();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    public static boolean brewEffect(TileEntityAlchemyCauldron cauldron) {
        PathPoint at = cauldron.getBrewPosition();
        if (!cauldron.effectReady(at)) return false;

        apply(cauldron, cauldron.effectAt(at));
        cauldron.script().step(BrewRecipe.STEP_BREW);
        cauldron.updateWaterColor();
        Level level = cauldron.getLevel();
        if (level != null && !level.isClientSide()) {
            level.blockEvent(cauldron.getBlockPos(), cauldron.getBlockState().getBlock(), CauldronAmbience.EVENT_BREWED, 0);
        }
        return true;
    }

    private static void apply(TileEntityAlchemyCauldron cauldron, PotionMap.PotionEffectPosition found) {
        Level level = cauldron.getLevel();
        if (level == null) return;

        Holder<MobEffect> mobEffect = found.getEffect();
        PotencyPlan brewed = cauldron.brewedWith(found);
        if (mobEffect == null || brewed == null) return;

        BlockPos pos = cauldron.getBlockPos();
        boolean perfect = found.isPerfect();
        boolean held = cauldron.heldPotency(mobEffect) > 0;
        boolean glowing = cauldron.glowBoost(found) > 0;

        cauldron.potency().apply(brewed);
        if (glowing) {
            cauldron.imbuement().radiate();
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
            }
        }

        if (held) {
            if (perfect) {
                if (!level.isClientSide()) {
                    level.playSound(null, pos, SoundEvents.GLOW_SQUID_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                ParticleUtils.generateEvaporationParticles(level, pos, cauldron.getPotionColor());
            }
        } else {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            ParticleUtils.generateEvaporationParticles(level, pos, cauldron.getPotionColor());
        }

        if (perfect && level.isClientSide()) {
            for (int i = 0; i < 10; i++) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + RANDOM.nextFloat(),
                        pos.getY() + 1D, pos.getZ() + RANDOM.nextFloat(), 0, 0.1, 0);
            }
        }
    }
}
