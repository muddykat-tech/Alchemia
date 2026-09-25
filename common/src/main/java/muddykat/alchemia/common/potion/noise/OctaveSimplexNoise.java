package muddykat.alchemia.common.potion.noise;

import java.util.List;
import java.util.TreeSet;

public final class OctaveSimplexNoise {
    private final SimplexNoise[] noiseLevels;
    private final double highestFreqValueFactor;
    private final double highestFreqInputFactor;

    public OctaveSimplexNoise(long seed, List<Integer> octaves) {
        this(new LegacyRandom(seed), new TreeSet<>(octaves));
    }

    public OctaveSimplexNoise(LegacyRandom random, List<Integer> octaves) {
        this(random, new TreeSet<>(octaves));
    }

    private OctaveSimplexNoise(LegacyRandom random, TreeSet<Integer> octaveSet) {
        if (octaveSet.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }

        int lowFreqOctaves = -octaveSet.first();
        int highFreqOctaves = octaveSet.last();
        int octaves = lowFreqOctaves + highFreqOctaves + 1;
        if (octaves < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }

        SimplexNoise zeroOctave = new SimplexNoise(random);
        int zeroOctaveIndex = highFreqOctaves;
        this.noiseLevels = new SimplexNoise[octaves];

        if (zeroOctaveIndex >= 0 && zeroOctaveIndex < octaves && octaveSet.contains(0)) {
            this.noiseLevels[zeroOctaveIndex] = zeroOctave;
        }

        for (int i = zeroOctaveIndex + 1; i < octaves; i++) {
            if (i >= 0 && octaveSet.contains(zeroOctaveIndex - i)) {
                this.noiseLevels[i] = new SimplexNoise(random);
            } else {
                random.consumeCount(262);
            }
        }

        if (highFreqOctaves > 0) {
            long positiveOctaveSeed = (long) (zeroOctave.getValue(zeroOctave.xo, zeroOctave.yo, zeroOctave.zo) * 9.223372E18F);
            LegacyRandom highFreqRandom = new LegacyRandom(positiveOctaveSeed);

            for (int i = zeroOctaveIndex - 1; i >= 0; i--) {
                if (i < octaves && octaveSet.contains(zeroOctaveIndex - i)) {
                    this.noiseLevels[i] = new SimplexNoise(highFreqRandom);
                } else {
                    highFreqRandom.consumeCount(262);
                }
            }
        }

        this.highestFreqInputFactor = Math.pow(2.0, highFreqOctaves);
        this.highestFreqValueFactor = 1.0 / (Math.pow(2.0, octaves) - 1.0);
    }

    public double getValue(double x, double y) {
        return getValue(x, y, false);
    }

    public double getValue(double x, double y, boolean useNoiseStart) {
        double value = 0.0;
        double factor = this.highestFreqInputFactor;
        double valueFactor = this.highestFreqValueFactor;

        for (SimplexNoise noiseLevel : this.noiseLevels) {
            if (noiseLevel != null) {
                value += noiseLevel.getValue(
                        x * factor + (useNoiseStart ? noiseLevel.xo : 0.0),
                        y * factor + (useNoiseStart ? noiseLevel.yo : 0.0)) * valueFactor;
            }

            factor /= 2.0;
            valueFactor *= 2.0;
        }

        return value;
    }
}
