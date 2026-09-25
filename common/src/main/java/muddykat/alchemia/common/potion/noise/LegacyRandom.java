package muddykat.alchemia.common.potion.noise;

public final class LegacyRandom {
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private static final long MODULUS_MASK = 281474976710655L;

    private long seed;

    public LegacyRandom(long seed) {
        this.seed = (seed ^ MULTIPLIER) & MODULUS_MASK;
    }

    public int next(int bits) {
        this.seed = (this.seed * MULTIPLIER + INCREMENT) & MODULUS_MASK;
        return (int) (this.seed >> (48 - bits));
    }

    public int nextInt() {
        return next(32);
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }

        if ((bound & (bound - 1)) == 0) {
            return (int) (((long) bound * next(31)) >> 31);
        }

        int sample;
        int modulo;
        do {
            sample = next(31);
            modulo = sample % bound;
        } while (sample - modulo + (bound - 1) < 0);

        return modulo;
    }

    public long nextLong() {
        int upper = next(32);
        int lower = next(32);
        return ((long) upper << 32) + lower;
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    public float nextFloat() {
        return next(24) * 5.9604645E-8F;
    }

    public double nextDouble() {
        int upper = next(26);
        int lower = next(27);
        long combined = ((long) upper << 27) + lower;
        return combined * 1.110223E-16F;
    }

    public void consumeCount(int count) {
        for (int i = 0; i < count; i++) {
            nextInt();
        }
    }
}
