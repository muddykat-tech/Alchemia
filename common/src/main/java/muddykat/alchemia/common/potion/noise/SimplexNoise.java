package muddykat.alchemia.common.potion.noise;

public final class SimplexNoise {
    private static final int[][] GRADIENT = new int[][]{
            {1, 1, 0},
            {-1, 1, 0},
            {1, -1, 0},
            {-1, -1, 0},
            {1, 0, 1},
            {-1, 0, 1},
            {1, 0, -1},
            {-1, 0, -1},
            {0, 1, 1},
            {0, -1, 1},
            {0, 1, -1},
            {0, -1, -1},
            {1, 1, 0},
            {0, -1, 1},
            {-1, 1, 0},
            {0, -1, -1}
    };

    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final double F2 = 0.5 * (SQRT_3 - 1.0);
    private static final double G2 = (3.0 - SQRT_3) / 6.0;
    private static final double F3 = 0.3333333333333333;
    private static final double G3 = 0.16666666666666666;

    private final int[] p = new int[512];

    public final double xo;
    public final double yo;
    public final double zo;

    public SimplexNoise(LegacyRandom random) {
        this.xo = random.nextDouble() * 256.0;
        this.yo = random.nextDouble() * 256.0;
        this.zo = random.nextDouble() * 256.0;

        int i = 0;
        while (i < 256) {
            this.p[i] = i++;
        }

        for (int ix = 0; ix < 256; ix++) {
            int offset = random.nextInt(256 - ix);
            int tmp = this.p[ix];
            this.p[ix] = this.p[offset + ix];
            this.p[offset + ix] = tmp;
        }
    }

    private static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private int p(int x) {
        return this.p[x & 0xFF];
    }

    private static double dot(int[] g, double x, double y, double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }

    private double getCornerNoise3D(int index, double x, double y, double z, double base) {
        double t0 = base - x * x - y * y - z * z;
        if (t0 < 0.0) {
            return 0.0;
        }

        t0 *= t0;
        return t0 * t0 * dot(GRADIENT[index], x, y, z);
    }

    public double getValue(double xin, double yin) {
        double s = (xin + yin) * F2;
        int i = floor(xin + s);
        int j = floor(yin + s);
        double t = (i + j) * G2;
        double x0 = xin - (i - t);
        double y0 = yin - (j - t);

        int i1;
        int j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;

        int ii = i & 0xFF;
        int jj = j & 0xFF;
        int gi0 = p(ii + p(jj)) % 12;
        int gi1 = p(ii + i1 + p(jj + j1)) % 12;
        int gi2 = p(ii + 1 + p(jj + 1)) % 12;

        double n0 = getCornerNoise3D(gi0, x0, y0, 0.0, 0.5);
        double n1 = getCornerNoise3D(gi1, x1, y1, 0.0, 0.5);
        double n2 = getCornerNoise3D(gi2, x2, y2, 0.0, 0.5);

        return 70.0 * (n0 + n1 + n2);
    }

    public double getValue(double xin, double yin, double zin) {
        double s = (xin + yin + zin) * F3;
        int i = floor(xin + s);
        int j = floor(yin + s);
        int k = floor(zin + s);
        double t = (i + j + k) * G3;
        double x0 = xin - (i - t);
        double y0 = yin - (j - t);
        double z0 = zin - (k - t);

        int i1;
        int j1;
        int k1;
        int i2;
        int j2;
        int k2;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else if (y0 < z0) {
            i1 = 0;
            j1 = 0;
            k1 = 1;
            i2 = 0;
            j2 = 1;
            k2 = 1;
        } else if (x0 < z0) {
            i1 = 0;
            j1 = 1;
            k1 = 0;
            i2 = 0;
            j2 = 1;
            k2 = 1;
        } else {
            i1 = 0;
            j1 = 1;
            k1 = 0;
            i2 = 1;
            j2 = 1;
            k2 = 0;
        }

        double x1 = x0 - i1 + G3;
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + F3;
        double y2 = y0 - j2 + F3;
        double z2 = z0 - k2 + F3;
        double x3 = x0 - 1.0 + 0.5;
        double y3 = y0 - 1.0 + 0.5;
        double z3 = z0 - 1.0 + 0.5;

        int ii = i & 0xFF;
        int jj = j & 0xFF;
        int kk = k & 0xFF;
        int gi0 = p(ii + p(jj + p(kk))) % 12;
        int gi1 = p(ii + i1 + p(jj + j1 + p(kk + k1))) % 12;
        int gi2 = p(ii + i2 + p(jj + j2 + p(kk + k2))) % 12;
        int gi3 = p(ii + 1 + p(jj + 1 + p(kk + 1))) % 12;

        double n0 = getCornerNoise3D(gi0, x0, y0, z0, 0.6);
        double n1 = getCornerNoise3D(gi1, x1, y1, z1, 0.6);
        double n2 = getCornerNoise3D(gi2, x2, y2, z2, 0.6);
        double n3 = getCornerNoise3D(gi3, x3, y3, z3, 0.6);

        return 32.0 * (n0 + n1 + n2 + n3);
    }
}
