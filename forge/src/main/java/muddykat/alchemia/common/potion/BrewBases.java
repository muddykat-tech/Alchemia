package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BrewBases {

    public static final String DEFAULT_ID = "water";

    private static volatile Map<String, BrewBase> BASES = Map.of();
    private static volatile int generation;

    private static final java.util.Set<String> WATER_ONLY = java.util.Set.of(
            "Water_Breathing", "Conduit_Power", "Dolphins_Grace", "Breath_Of_The_Nautilus", "Oozing",
            "Levitation", "Dig_Speed", "Darkness", "Fire_Protection", "Slow_Falling");

    private static final java.util.Set<String> HONEY_ONLY = java.util.Set.of(
            "Hunger", "Unluck", "Bad_Omen", "Raid_Omen", "Trial_Omen",
            "Hero_Of_The_Village", "Weaving", "Infested", "Wind_Charged", "Jump");

    private BrewBases() {
    }

    static {
        install(defaults());
    }

    public record Match(BrewBase base, BrewBase.Fill fill) {
    }

    public static List<String> effectsExcept(java.util.Set<String> excluded) {
        return java.util.Arrays.stream(PotionEnum.values())
                .map(Enum::name)
                .filter(name -> !excluded.contains(name))
                .collect(Collectors.toList());
    }

    public static List<BrewBase> defaults() {

        BrewBase water = new BrewBase(DEFAULT_ID,
                List.of(new BrewBase.Fill(id("minecraft:water_bucket"), Optional.empty(), 4, id("minecraft:bucket"), Optional.empty()),
                        new BrewBase.Fill(Optional.empty(), id("minecraft:water"), 4, Optional.empty(), Optional.empty())),
                1, Optional.of(0x3F76E4), true, id("minecraft:block/water_still"), id("minecraft:water"),
                Optional.empty(), Optional.of(effectsExcept(HONEY_ONLY)), BrewBase.DeadSpace.RINGS);

        BrewBase honey = new BrewBase("honey",
                List.of(new BrewBase.Fill(id("minecraft:honey_block"), Optional.empty(), 4, Optional.empty(), Optional.empty()),
                        new BrewBase.Fill(id("minecraft:honey_bottle"), Optional.empty(), 1, id("minecraft:glass_bottle"), Optional.empty())),
                2, Optional.of(0xD9871F), false, Optional.empty(), Optional.empty(),
                id("minecraft:honey_bottle"), Optional.of(effectsExcept(WATER_ONLY)), BrewBase.DeadSpace.FIELD);

        return List.of(water, honey);
    }

    public static List<String> allEffectNames() {
        return java.util.Arrays.stream(PotionEnum.values()).map(Enum::name).collect(Collectors.toList());
    }

    public static synchronized void install(List<BrewBase> bases) {
        Map<String, BrewBase> accepted = new LinkedHashMap<>();
        for (BrewBase base : bases) {
            String id = base.id().toLowerCase(Locale.ROOT);
            if (accepted.containsKey(id)) {
                Alchemia.LOGGER.warn("Ignoring duplicate brew base definition '{}'", id);
                continue;
            }
            if (base.fills().isEmpty()) {
                Alchemia.LOGGER.warn("Ignoring brew base '{}' because it defines no fills", id);
                continue;
            }
            accepted.put(id, base);
        }

        if (accepted.isEmpty()) {
            Alchemia.LOGGER.warn("No usable brew bases were defined, falling back to the built in ones");
            for (BrewBase base : defaults()) accepted.put(base.id(), base);
        } else if (!accepted.containsKey(DEFAULT_ID)) {
            Alchemia.LOGGER.warn("No '{}' brew base was defined, adding the built in one", DEFAULT_ID);
            Map<String, BrewBase> withWater = new LinkedHashMap<>();
            withWater.put(DEFAULT_ID, defaults().get(0));
            withWater.putAll(accepted);
            accepted = withWater;
        }

        BASES = java.util.Collections.unmodifiableMap(accepted);
        generation++;
    }

    public static int generation() {
        return generation;
    }

    public static Collection<BrewBase> values() {
        return BASES.values();
    }

    public static List<BrewBase> definitions() {
        return new ArrayList<>(BASES.values());
    }

    public static BrewBase defaultBase() {
        Map<String, BrewBase> bases = BASES;
        BrewBase base = bases.get(DEFAULT_ID);
        return base != null ? base : bases.values().iterator().next();
    }

    public static BrewBase byId(@Nullable String id) {
        if (id == null || id.isBlank()) return defaultBase();
        BrewBase base = BASES.get(id.toLowerCase(Locale.ROOT));
        return base != null ? base : defaultBase();
    }

    public static boolean exists(String id) {
        return BASES.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public static @Nullable Match forItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (BrewBase base : BASES.values()) {
            BrewBase.Fill fill = base.fillFor(stack);
            if (fill != null) return new Match(base, fill);
        }
        return null;
    }

    public static @Nullable Match forFluid(FluidStack resource) {
        if (resource.isEmpty()) return null;
        for (BrewBase base : BASES.values()) {
            BrewBase.Fill fill = base.fillFor(resource);
            if (fill != null) return new Match(base, fill);
        }
        return null;
    }

    private static Optional<ResourceLocation> id(String value) {
        return Optional.of(new ResourceLocation(value));
    }
}
