package muddykat.alchemia.common.blocks.tileentity.cauldron;

import com.mojang.serialization.Codec;
import muddykat.alchemia.common.potion.Imbuements;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import muddykat.alchemia.common.utility.ValueInput;
import muddykat.alchemia.common.utility.ValueOutput;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class CauldronImbuement {

    private List<Imbuements.Attuner> attuners = new ArrayList<>();
    private boolean primed;
    private boolean radiant;

    public void load(ValueInput input) {
        primed = input.getBooleanOr("primed", false);
        radiant = input.getBooleanOr("radiant", false);
        attuners = new ArrayList<>();
        for (String step : input.read("imbuements", Codec.STRING.listOf()).orElse(List.of())) {
            Imbuements.Attuner read = Imbuements.parse(step);
            if (read != null) attuners.add(read);
        }
        input.read("attunement", Codec.STRING).map(Imbuements::parse).ifPresent(attuners::add);
        input.read("attunedBiome", ResourceKey.codec(Registries.BIOME))
                .ifPresent(biome -> attuners.add(new Imbuements.Attuner(Imbuements.bark(), biome)));
    }

    public void save(ValueOutput output) {
        output.putBoolean("primed", primed);
        output.putBoolean("radiant", radiant);
        if (!attuners.isEmpty()) {
            output.store("imbuements", Codec.STRING.listOf(), attuners.stream().map(Imbuements.Attuner::step).toList());
        }
    }

    public List<Imbuements.Attuner> attuners() {
        return List.copyOf(attuners);
    }

    public boolean clashing(Imbuements.Attuner offered) {
        for (Imbuements.Attuner held : attuners) {
            if (held.clashes(offered)) return true;
        }
        return false;
    }

    public void add(Imbuements.Attuner offered) {
        attuners.add(offered);
    }

    public WorldEffects.Imbued holder(@Nullable Level level) {
        return imbue(attuners, level);
    }

    public static WorldEffects.Imbued imbue(List<Imbuements.Attuner> with, @Nullable Level level) {
        if (with.isEmpty() || level == null) return WorldEffects.Imbued.NONE;
        var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        List<WorldEffects.Imbued> parts = new ArrayList<>();
        for (Imbuements.Attuner attuner : with) parts.add(attuner.imbue(registry));
        return Imbuements.merge(parts);
    }

    public boolean primed() {
        return primed;
    }

    public boolean radiant() {
        return radiant;
    }

    public void prime() {
        primed = true;
    }

    public void radiate() {
        primed = false;
        radiant = true;
    }

    public void reset() {
        primed = false;
        radiant = false;
        attuners = new ArrayList<>();
    }
}
