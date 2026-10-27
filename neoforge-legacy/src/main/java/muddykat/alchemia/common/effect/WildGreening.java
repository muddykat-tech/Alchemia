package muddykat.alchemia.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.Alchemia;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.Comparator;
import java.util.List;

public record WildGreening(HolderSet<Biome> from, ResourceKey<Biome> to, HolderSet<Biome> greener, int tier, Counts counts, int steps,
                           int grass) {

    public enum Counts implements StringRepresentable {
        CONVERSION("conversion"),
        GROWTH("growth");

        public static final Codec<Counts> CODEC = StringRepresentable.fromEnum(Counts::values);

        private final String name;

        Counts(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record Named(ResourceLocation id, WildGreening greening) {
    }

    public static final int DEFAULT_GRASS = 10;

    public static final Codec<WildGreening> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WildConversion.BIOMES.fieldOf("from").forGetter(WildGreening::from),
            ResourceKey.codec(Registries.BIOME).fieldOf("to").forGetter(WildGreening::to),
            WildConversion.BIOMES.optionalFieldOf("greener", HolderSet.empty()).forGetter(WildGreening::greener),
            Codec.intRange(1, WildGrowth.MAX_STRENGTH).optionalFieldOf("tier", WildGrowth.MAX_STRENGTH).forGetter(WildGreening::tier),
            Counts.CODEC.fieldOf("counts").forGetter(WildGreening::counts),
            Codec.intRange(1, 10000).fieldOf("steps").forGetter(WildGreening::steps),
            Codec.intRange(0, 16).optionalFieldOf("grass", DEFAULT_GRASS).forGetter(WildGreening::grass)
    ).apply(instance, WildGreening::new));

    public static final ResourceKey<Registry<WildGreening>> REGISTRY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "wild_greening"));

    private record Index(Registry<WildGreening> registry, List<Named> greenings) {
    }

    private static volatile Index index;

    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, CODEC, CODEC);
    }

    public static void bootstrap(BootstrapContext<WildGreening> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(key("desert"), new WildGreening(HolderSet.direct(biomes.getOrThrow(Biomes.DESERT)), Biomes.PLAINS,
                HolderSet.direct(biomes.getOrThrow(Biomes.SUNFLOWER_PLAINS), biomes.getOrThrow(Biomes.FLOWER_FOREST)),
                WildGrowth.MAX_STRENGTH, Counts.GROWTH, 10, DEFAULT_GRASS));
        context.register(key("plains"), new WildGreening(
                HolderSet.direct(biomes.getOrThrow(Biomes.PLAINS), biomes.getOrThrow(Biomes.SUNFLOWER_PLAINS)), Biomes.FLOWER_FOREST,
                HolderSet.empty(), WildGrowth.MAX_STRENGTH, Counts.GROWTH, 40, DEFAULT_GRASS));
    }

    private static ResourceKey<WildGreening> key(String name) {
        return ResourceKey.create(REGISTRY, ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, name));
    }

    public boolean matches(Holder<Biome> biome) {
        return from.contains(biome);
    }

    public boolean greener(Holder<Biome> biome) {
        return biome.is(to) || greener.contains(biome);
    }

    public boolean fedBy(Counts kind, int recipeTier, int strength) {
        if (kind != counts) return false;
        return kind == Counts.CONVERSION ? recipeTier >= tier : strength >= tier;
    }

    public static List<Named> all(RegistryAccess access) {
        Registry<WildGreening> registry = access.registry(REGISTRY).orElse(null);
        if (registry == null) return List.of();
        Index current = index;
        if (current == null || current.registry() != registry) {
            current = new Index(registry, registry.holders()
                    .map(holder -> new Named(holder.key().location(), holder.value()))
                    .sorted(Comparator.comparing(named -> named.id().toString()))
                    .toList());
            index = current;
        }
        return current.greenings();
    }
}
