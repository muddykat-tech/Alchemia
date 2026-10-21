package muddykat.alchemia.client.effect;

import net.minecraft.util.Mth;

public record Impacts(float[] front, float[] grow, float[] flash, float[] shift, float[] looseReach, float[] looseChance,
                      int chipLevel, float chipReach, float chipChance, float chipFall, float chipReturn, float wave, boolean jagged) {

    public static final float NEVER = -1000.0F;
    public static final float RETRACT_TICKS = 4.0F;
    public static final float UNHIT_FLASH = 0.8F;
    public static final float SETTLE_TICKS = 4.0F;
    public static final float TREMBLE_TICKS = 4.0F;
    public static final float TREMBLE = 0.14F;

    public interface Clock {

        float age(int level);

        float undone(int level);

        default boolean reached(int level) {
            return age(level) >= 0.0F;
        }
    }

    public int levels() {
        return front.length;
    }

    public static float jolt(float since) {
        if (since < 0.0F) return 0.0F;
        return Math.min(1.0F, since / 0.8F) * (1.0F + 0.45F * (float) Math.exp(-since / 2.0F) * (float) Math.sin(since * 1.7F));
    }

    public static float pulse(float since) {
        return since < 0.0F ? 0.0F : (float) Math.exp(-since / 3.5F) * Math.min(1.0F, since / 0.4F);
    }

    public static float ring(float since) {
        return since < 0.0F ? 0.0F : (float) Math.exp(-since / 2.2F) * (float) Math.sin(since * 2.8F);
    }

    public static float stepped(float since) {
        if (since < 0.0F) return 1.0F;
        if (since < 1.0F) return 0.4F;
        if (since < 1.8F) return 0.12F;
        return 0.0F;
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float random(int seed, int index, int salt) {
        long mixed = seed * 0x9E3779B97F4A7C15L + index * 0xC2B2AE3D27D4EB4FL + salt * 0x165667B19E3779F9L;
        mixed ^= mixed >>> 31;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 29;
        return (mixed >>> 40) / (float) (1L << 24);
    }

    public float on(Clock clock, int level) {
        if (!clock.reached(level)) return 0.0F;
        float undone = clock.undone(level);
        if (undone >= SETTLE_TICKS) return 0.0F;
        if (undone >= 0.0F) return jagged ? stepped(undone) : 1.0F - jolt(undone);
        float held = jolt(clock.age(level));
        if (undone > -TREMBLE_TICKS) held += TREMBLE * (1.0F + undone / TREMBLE_TICKS) * (float) Math.sin(undone * 4.5F);
        return held;
    }

    public float spread(Clock clock, int level) {
        if (!clock.reached(level)) return 0.0F;
        float undone = clock.undone(level);
        if (undone >= 0.0F) return 1.0F - smooth(undone / RETRACT_TICKS);
        float t = Math.min(1.0F, clock.age(level) / grow[level]);
        return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
    }

    public float severity(Clock clock) {
        float total = 0.0F;
        for (int level = 0; level < levels(); level++) total += Mth.clamp(on(clock, level), 0.0F, 1.0F);
        return total;
    }

    public float crackFront(Clock clock) {
        float reach = 0.0F;
        for (int level = 0; level < levels(); level++) {
            reach += spread(clock, level) * (front[level] - (level == 0 ? 0.0F : front[level - 1]));
        }
        return reach;
    }

    public float lit(Clock clock) {
        float lit = 0.0F;
        for (int level = 0; level < levels(); level++) lit += spread(clock, level);
        return lit;
    }

    public float crackGlow(Clock clock, float time) {
        float lit = 0.0F;
        for (int level = 0; level < levels(); level++) lit += spread(clock, level);
        float glow = 0.3F * Math.min(1.0F, lit) + 0.15F * lit;
        glow += 0.12F * (float) Math.sin(time * 0.35F) * Math.min(1.0F, lit);
        for (int level = 0; level < levels(); level++) {
            if (clock.reached(level)) glow += 0.35F * pulse(clock.age(level));
        }
        return Mth.clamp(glow, 0.0F, 1.0F);
    }

    public float shake(Clock clock, float[] strength, float unhit, float offset) {
        float total = 0.0F;
        for (int level = 0; level < levels(); level++) {
            if (!clock.reached(level)) continue;
            total += strength[level] * ring(clock.age(level) + offset * 0.12F) * (offset == 0.0F ? 1.0F : (float) Math.cos(offset + level));
            float undone = clock.undone(level);
            if (undone >= 0.0F) total += strength[level] * unhit * ring(undone + offset * 0.12F);
        }
        return total;
    }

    public boolean chip(FracturePattern.Cell cell, int seed, int index) {
        return cell.reach() < chipReach && random(seed, index, 2) < chipChance;
    }

    public boolean chipGone(Clock clock, FracturePattern.Cell cell, int seed, int index) {
        return chip(cell, seed, index) && clock.reached(chipLevel) && clock.undone(chipLevel) < 0.0F;
    }

    private int looseLevel(FracturePattern.Cell cell, int seed, int index) {
        float roll = random(seed, index, 1);
        for (int level = 0; level < levels(); level++) {
            if (cell.reach() < looseReach[level] && roll < looseChance[level]) return level;
        }
        return -1;
    }

    public float flash(Clock clock, FracturePattern.Cell cell) {
        float lit = 0.0F;
        for (int level = 0; level < levels(); level++) {
            if (!clock.reached(level) || cell.reach() > front[level] + 0.1F) continue;
            lit = Math.max(lit, flash[level] * pulse(clock.age(level) - cell.reach() * wave));
            float undone = clock.undone(level);
            float inner = level == 0 ? 0.0F : front[level - 1] - 0.05F;
            if (undone >= 0.0F && cell.reach() >= inner) {
                lit = Math.max(lit, flash[level] * UNHIT_FLASH * pulse(undone - Math.max(0.0F, front[level] - cell.reach()) * wave));
            }
        }
        return Math.min(1.0F, lit);
    }

    public RealityBreak.CellState cracked(Clock clock, FracturePattern.Cell cell, int seed, int index, float lit) {
        if (chip(cell, seed, index) && clock.reached(chipLevel)) {
            float flight = Math.min(1.0F, clock.age(chipLevel) / chipFall);
            float undone = clock.undone(chipLevel);
            if (undone >= 0.0F) {
                float back = undone / chipReturn;
                float returning = jagged ? Math.min(1.0F, stepped(undone) * 1.4F) : back >= 1.0F ? 0.0F : 1.0F - back * back;
                flight = Math.min(flight, returning);
            }
            if (flight > 0.0F) return new RealityBreak.CellState(flight, true, lit);
        }
        int loose = looseLevel(cell, seed, index);
        if (loose < 0) return new RealityBreak.CellState(0.0F, false, lit);
        float falloff = 1.0F - 0.4F * Math.min(1.0F, cell.reach() / looseReach[levels() - 1]);
        float amount = 0.0F;
        for (int level = loose; level < levels(); level++) {
            float step = shift[level] - (level == loose ? 0.0F : shift[level - 1]);
            amount += on(clock, level) * step * falloff;
        }
        if (amount <= 0.001F) return new RealityBreak.CellState(0.0F, false, lit);
        return new RealityBreak.CellState(0.0F, true, lit, amount);
    }
}
