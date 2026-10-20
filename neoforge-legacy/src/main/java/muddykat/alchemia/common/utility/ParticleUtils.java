package muddykat.alchemia.common.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

public class ParticleUtils {
    public static void generatePotionParticles(Level level, BlockPos pos, int color, boolean generateMultiple) {
        if (!level.isClientSide()) return;
        ParticleStatus particleStatus = Minecraft.getInstance().options.particles().get();
        if (particleStatus == ParticleStatus.MINIMAL) return;

        BlockState stateOfBlockAbove = level.getBlockState(pos.above());
        if (stateOfBlockAbove.canOcclude()) return;

        Random random = new Random();
        int multiplier, numberOfParticles = 1;

        if (generateMultiple) {
            multiplier = particleStatus == ParticleStatus.DECREASED ? 1 : 2;
            numberOfParticles = random.nextInt(3 * multiplier, 5 * multiplier);
        } else {
            if (particleStatus == ParticleStatus.DECREASED && random.nextInt(10) % 5 != 0) return;
            else if (random.nextInt(10) % 3 != 0) return;
        }

        for (int i = 1; i <= numberOfParticles; i++) {
            Minecraft.getInstance().particleEngine.createParticle(
                    ParticleTypes.EFFECT,
                    pos.getX() + 0.45 + random.nextDouble() * 0.2,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.45 + random.nextDouble() * 0.2,
                    red(color),
                    green(color),
                    blue(color));
        }
    }

    public static void generateStirredUpParticles(Level level, BlockPos pos, double surface,
                                                  @Nullable ParticleOptions accent, boolean rising, int ticksLeft) {
        if (!level.isClientSide()) return;
        if (level.getBlockState(pos.above()).canOcclude()) return;

        Random random = new Random();
        int bubbles = ticksLeft > 30 ? 2 : random.nextInt(2);
        for (int i = 0; i < bubbles; i++) {
            level.addParticle(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.2 + random.nextDouble() * 0.6,
                    pos.getY() + surface + 0.02,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                    0.0, 0.02 + random.nextDouble() * 0.03, 0.0);
        }

        if (!rising && ticksLeft % 6 == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + 0.3 + random.nextDouble() * 0.4,
                    pos.getY() + surface + 0.05,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0.0, 0.03, 0.0);
        }

        if (accent == null || ticksLeft % 4 != 0) return;
        level.addParticle(accent,
                pos.getX() + 0.25 + random.nextDouble() * 0.5,
                pos.getY() + surface + 0.1,
                pos.getZ() + 0.25 + random.nextDouble() * 0.5,
                (random.nextDouble() - 0.5) * 0.02, rising ? 0.04 : 0.01, (random.nextDouble() - 0.5) * 0.02);
    }

    public static void generateEvaporationParticles(Level level, BlockPos pos, int color) {
        if (!level.isClientSide()) return;
        Random random = new Random();
        ParticleStatus particleStatus = Minecraft.getInstance().options.particles().get();
        if (particleStatus == ParticleStatus.MINIMAL) return;

        BlockState stateOfBlockAbove = level.getBlockState(pos.above());
        if (stateOfBlockAbove.canOcclude()) return;

        int maxParticles = particleStatus == ParticleStatus.DECREASED ? 2 : 5;
        int noParticles = random.nextInt(1, 2 * maxParticles);

        for (int i = 1; i <= noParticles; i++) {
            Minecraft.getInstance().particleEngine.createParticle(
                    net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000 | color),
                    pos.getX() + 0.1 + random.nextDouble() * 0.8,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.1 + random.nextDouble() * 0.8,
                    0.0, 0.0, 0.0);
        }
    }

    private static double red(int color) {
        return (color >> 16 & 255) / 255.0;
    }

    private static double green(int color) {
        return (color >> 8 & 255) / 255.0;
    }

    private static double blue(int color) {
        return (color & 255) / 255.0;
    }
}
