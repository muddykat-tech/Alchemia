package muddykat.alchemia.common.blocks.tileentity.cauldron;

import com.mojang.serialization.Codec;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewStrain;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public final class BrewStability {

    private final BrewStrain strain = new BrewStrain();
    private int instability;
    private int bonus;
    private boolean spoiled;
    private boolean revivalUsed;
    private List<MobEffectInstance> ruinedEffects = new ArrayList<>();
    private List<String> ruinedOrder = new ArrayList<>();
    private int ruinedBonus;

    public void load(ValueInput input) {
        spoiled = input.getBooleanOr("spoiled", false);
        revivalUsed = input.getBooleanOr("rubedoRevivalUsed", false);
        ruinedEffects = new ArrayList<>(input.read("ruinedEffects", MobEffectInstance.CODEC.listOf()).orElse(List.of()));
        ruinedOrder = new ArrayList<>(input.read("ruinedOrder", Codec.STRING.listOf()).orElse(List.of()));
        ruinedBonus = input.getIntOr("ruinedBonus", 0);
        instability = input.getIntOr("instability", 0);
        bonus = input.getIntOr("bonusInstability", 0);
        strain.restore(input.read("brewHealth", Codec.DOUBLE).orElse(1.0),
                input.read("brewCalm", Codec.DOUBLE).orElse(BrewStrain.RECOVER_DELAY));
    }

    public void save(ValueOutput output) {
        output.putBoolean("spoiled", spoiled);
        output.putBoolean("rubedoRevivalUsed", revivalUsed);
        if (spoiled) {
            output.store("ruinedEffects", MobEffectInstance.CODEC.listOf(), List.copyOf(ruinedEffects));
            output.store("ruinedOrder", Codec.STRING.listOf(), List.copyOf(ruinedOrder));
            output.putInt("ruinedBonus", ruinedBonus);
        }
        output.putInt("instability", instability);
        output.putInt("bonusInstability", bonus);
        output.store("brewHealth", Codec.DOUBLE, strain.health());
        output.store("brewCalm", Codec.DOUBLE, strain.calm());
    }

    public boolean spoiled() {
        return spoiled;
    }

    public boolean revivalUsed() {
        return revivalUsed;
    }

    public int instability() {
        return instability;
    }

    public int cap(BrewBase base) {
        return base.instabilityCap() + bonus;
    }

    public boolean hasBonus() {
        return bonus > 0;
    }

    public double health() {
        return strain.health();
    }

    public boolean strained(double distance, boolean dead) {
        return strain.travel(distance, dead);
    }

    public void addBonus() {
        bonus = 1;
    }

    public boolean destabilise(BrewBase base) {
        instability++;
        return instability >= cap(base);
    }

    public void spoil(BrewBase base, BrewPotency potency) {
        spoiled = true;
        instability = cap(base);
        ruinedEffects = new ArrayList<>(potency.effects());
        ruinedOrder = new ArrayList<>(potency.order());
        ruinedBonus = potency.durationBonus();
        potency.clear();
    }

    public void revive(BrewPotency potency) {
        revivalUsed = true;
        spoiled = false;
        strain.reset();
        bonus++;
        potency.restore(ruinedEffects, ruinedOrder, ruinedBonus);
        ruinedEffects = new ArrayList<>();
        ruinedOrder = new ArrayList<>();
        ruinedBonus = 0;
    }

    public void reset() {
        spoiled = false;
        ruinedEffects = new ArrayList<>();
        ruinedOrder = new ArrayList<>();
        ruinedBonus = 0;
        instability = 0;
        bonus = 0;
        strain.reset();
    }

    public void calm() {
        instability = 0;
    }
}
