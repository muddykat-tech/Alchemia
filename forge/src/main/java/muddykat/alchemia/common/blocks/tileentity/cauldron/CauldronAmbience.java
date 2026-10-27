package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.common.utility.ParticleUtils;
import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Random;

public final class CauldronAmbience {

    public static final int EVENT_CONDITIONS_SHIFTED = 1;
    public static final int EVENT_BREWED = 2;

    private static final int BUBBLE_LIFE = 60;
    private static final int BUBBLE_RUSH_TICKS = 40;
    private static final int AMBIENT_BUBBLES = 3;
    private static final int RUSH_BUBBLES = 8;
    private static final int STIRRED_UP_TICKS = 60;

    private final TileEntityAlchemyCauldron cauldron;
    private final Random random = new Random();
    private final ArrayDeque<Long> bubbles = new ArrayDeque<>();
    private int bubbleRush;
    private int stirredUp;
    private boolean stirredUpRising;
    private @Nullable WorldEffects.Rule stirredUpBy;

    public CauldronAmbience(TileEntityAlchemyCauldron cauldron) {
        this.cauldron = cauldron;
    }

    public boolean event(int id, int param) {
        if (id == EVENT_BREWED) {
            bubbleRush = BUBBLE_RUSH_TICKS;
            return true;
        }
        if (id != EVENT_CONDITIONS_SHIFTED) return false;

        int cause = (param >> 1) - 1;
        stirredUp = STIRRED_UP_TICKS;
        stirredUpRising = (param & 1) != 0;
        stirredUpBy = cause >= 0 && cause < WorldEffects.Rule.values().length ? WorldEffects.Rule.values()[cause] : null;
        return true;
    }

    public void tick(Level level, BlockPos pos) {
        int waterLevel = cauldron.getWaterLevel();
        if (random.nextFloat() < 0.2F && waterLevel > 0 && !cauldron.getEffectList().isEmpty()) {
            int color = cauldron.getEffectList().stream().findFirst()
                    .map(instance -> instance.getEffect().getColor())
                    .orElse(MobEffects.WATER_BREATHING.getColor());

            ParticleUtils.generatePotionParticles(level, pos, color, false);
        }

        if (stirredUp > 0 && waterLevel > 0) {
            ParticleUtils.generateStirredUpParticles(level, pos, AlchemicalCauldronRenderer.surfaceHeight(waterLevel),
                    accent(stirredUpBy), stirredUpRising, stirredUp);
        }
        if (stirredUp > 0) stirredUp--;

        bubble(level, pos);

        if (waterLevel > 0 && cauldron.isPrimed() && random.nextFloat() < 0.35F) {
            float surface = AlchemicalCauldronRenderer.surfaceHeight(waterLevel);
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.2 + random.nextDouble() * 0.6,
                    pos.getY() + surface + 0.05, pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0D, 0.03D, 0.0D);
        }

        if (random.nextFloat() < 0.2f) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + .1D;
            double z = pos.getZ() + random.nextDouble();
            ParticleOptions options = ParticleTypes.FLAME;
            level.addParticle(options, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }

    private void bubble(Level level, BlockPos pos) {
        long now = level.getGameTime();
        while (!bubbles.isEmpty() && bubbles.peekFirst() <= now) bubbles.pollFirst();

        int waterLevel = cauldron.getWaterLevel();
        boolean brewing = waterLevel > 0 && !cauldron.isSpoiled() && !cauldron.getEffectList().isEmpty();
        boolean rushing = bubbleRush > 0;
        if (rushing) bubbleRush--;
        if (!brewing && !rushing) return;
        if (waterLevel <= 0 || level.getBlockState(pos.above()).canOcclude()) return;

        int cap = rushing ? RUSH_BUBBLES : AMBIENT_BUBBLES;
        float chance = rushing ? 0.6F : bubbles.isEmpty() ? 0.08F : 0.025F;
        if (bubbles.size() >= cap || random.nextFloat() >= chance) return;

        int color = cauldron.getPotionColor();
        level.addParticle(ParticleTypeRegistry.BREW_BUBBLE.get(),
                pos.getX() + 0.2 + random.nextDouble() * 0.6,
                pos.getY() + AlchemicalCauldronRenderer.surfaceHeight(waterLevel) + 0.003,
                pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                ((color >> 16) & 255) / 255.0, ((color >> 8) & 255) / 255.0, (color & 255) / 255.0);
        bubbles.addLast(now + BUBBLE_LIFE);
    }

    private static @Nullable ParticleOptions accent(@Nullable WorldEffects.Rule cause) {
        if (cause == null) return null;
        return switch (cause) {
            case FULL_MOON, NEW_MOON, MOONLIGHT -> ParticleTypes.END_ROD;
            case THUNDER -> ParticleTypes.ELECTRIC_SPARK;
            case RAIN -> ParticleTypes.SPLASH;
            case CLEAR -> ParticleTypes.WAX_OFF;
            case DAY -> ParticleTypes.WAX_ON;
            case NIGHT -> ParticleTypes.GLOW;
            default -> null;
        };
    }
}
