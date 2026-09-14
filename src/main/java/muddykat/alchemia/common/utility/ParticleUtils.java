package muddykat.alchemia.common.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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
                    ParticleTypes.ENTITY_EFFECT,
                    pos.getX() + 0.1 + random.nextDouble() * 0.8,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.1 + random.nextDouble() * 0.8,
                    red(color),
                    green(color),
                    blue(color));
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
