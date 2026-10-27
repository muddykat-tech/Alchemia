package muddykat.alchemia.common.blocks.tileentity.cauldron;

import com.mojang.serialization.Codec;
import muddykat.alchemia.common.config.Configuration;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static muddykat.alchemia.common.blocks.tileentity.cauldron.PotencyPlan.REDSTONE_TOKEN;

public final class BrewPotency {

    public static final int REDSTONE_DURATION_BONUS = 1200;

    private List<MobEffectInstance> effects = new ArrayList<>();
    private List<String> order = new ArrayList<>();
    private int durationBonus;

    public static int max() {
        return Configuration.maxPotency();
    }

    public void load(ValueInput input) {
        effects.clear();
        effects.addAll(input.read("effects", MobEffectInstance.CODEC.listOf()).orElse(List.of()));
        durationBonus = input.getIntOr("durationBonus", 0);
        order = new ArrayList<>(input.read("potencyOrder", Codec.STRING.listOf()).orElse(List.of()));
        syncOrder();
    }

    public void save(ValueOutput output) {
        output.store("effects", MobEffectInstance.CODEC.listOf(), List.copyOf(effects));
        output.putInt("durationBonus", durationBonus);
        output.store("potencyOrder", Codec.STRING.listOf(), List.copyOf(order));
    }

    public List<MobEffectInstance> effects() {
        return effects;
    }

    public List<String> order() {
        return order;
    }

    public int durationBonus() {
        return durationBonus;
    }

    public int redstoneCharges() {
        return durationBonus / REDSTONE_DURATION_BONUS;
    }

    public int total() {
        int total = redstoneCharges();
        for (MobEffectInstance instance : effects) total += instance.getAmplifier() + 1;
        return total;
    }

    public int held(Holder<MobEffect> effect) {
        for (MobEffectInstance instance : effects) {
            if (instance.getEffect().equals(effect)) return instance.getAmplifier() + 1;
        }
        return 0;
    }

    public PotencyPlan current() {
        return new PotencyPlan(List.copyOf(effects), List.copyOf(order));
    }

    public @Nullable PotencyPlan rotated(@Nullable MobEffectInstance incoming) {
        String incomingToken = incoming == null ? REDSTONE_TOKEN : tokenOf(incoming.getEffect());
        int incomingPotency = incoming == null ? 1 : incoming.getAmplifier() + 1;

        Map<String, MobEffectInstance> byToken = new HashMap<>();
        for (MobEffectInstance instance : effects) byToken.put(tokenOf(instance.getEffect()), instance);

        List<String> older = new ArrayList<>(order);
        if (incoming != null) older.remove(incomingToken);

        int overflow = incomingPotency - max();
        for (String token : older) {
            MobEffectInstance instance = byToken.get(token);
            overflow += instance == null ? 1 : instance.getAmplifier() + 1;
        }

        List<String> kept = new ArrayList<>();
        List<MobEffectInstance> keptEffects = new ArrayList<>();
        for (String token : older) {
            MobEffectInstance instance = byToken.get(token);
            int potency = instance == null ? 1 : instance.getAmplifier() + 1;
            int lost = Math.clamp(overflow, 0, potency);
            overflow -= lost;
            if (lost == potency) continue;

            kept.add(token);
            if (instance != null) {
                keptEffects.add(lost == 0 ? instance
                        : new MobEffectInstance(instance.getEffect(), instance.getDuration(), instance.getAmplifier() - lost));
            }
        }
        if (overflow > 0) return null;

        kept.add(incomingToken);
        if (incoming != null) keptEffects.add(incoming);
        return new PotencyPlan(keptEffects, kept);
    }

    public void apply(PotencyPlan plan) {
        effects = new ArrayList<>(plan.effects());
        order = new ArrayList<>(plan.order());
        durationBonus = plan.redstone() * REDSTONE_DURATION_BONUS;
    }

    public void restore(List<MobEffectInstance> restored, List<String> restoredOrder, int bonus) {
        effects = new ArrayList<>(restored);
        order = new ArrayList<>(restoredOrder);
        durationBonus = bonus;
        syncOrder();
    }

    public void clear() {
        effects = new ArrayList<>();
        order = new ArrayList<>();
        durationBonus = 0;
    }

    public void add(MobEffectInstance instance) {
        effects.add(instance);
        syncOrder();
    }

    public boolean lacks(Holder<MobEffect> effect, int duration) {
        return effects.stream().noneMatch(instance -> instance.getEffect().equals(effect) && instance.getDuration() == duration);
    }

    public void ensureStrength(Holder<MobEffect> effect, int strength) {
        boolean replace = false;
        for (MobEffectInstance instance : effects) {
            if (instance.getEffect().equals(effect) && instance.getAmplifier() < strength) {
                replace = true;
            }
        }

        if (replace) {
            effects.removeIf(instance -> instance.getEffect().equals(effect));
            effects.add(new MobEffectInstance(effect, 1200, strength));
            syncOrder();
        }
    }

    private void syncOrder() {
        List<String> present = new ArrayList<>();
        for (MobEffectInstance instance : effects) present.add(tokenOf(instance.getEffect()));

        List<String> synced = new ArrayList<>();
        for (String token : order) {
            if (token.equals(REDSTONE_TOKEN) || (present.contains(token) && !synced.contains(token))) synced.add(token);
        }
        for (String token : present) {
            if (!synced.contains(token)) synced.add(token);
        }

        int charges = durationBonus / REDSTONE_DURATION_BONUS;
        int listed = 0;
        for (String token : synced) {
            if (token.equals(REDSTONE_TOKEN)) listed++;
        }
        while (listed > charges) {
            synced.remove(REDSTONE_TOKEN);
            listed--;
        }
        while (listed < charges) {
            synced.add(REDSTONE_TOKEN);
            listed++;
        }

        List<MobEffectInstance> sorted = new ArrayList<>();
        for (String token : synced) {
            for (MobEffectInstance instance : effects) {
                if (tokenOf(instance.getEffect()).equals(token)) sorted.add(instance);
            }
        }
        effects = sorted;
        order = synced;
    }

    private static String tokenOf(Holder<MobEffect> effect) {
        return effect.getRegisteredName();
    }
}
