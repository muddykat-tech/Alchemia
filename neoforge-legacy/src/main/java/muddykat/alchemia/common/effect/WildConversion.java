package muddykat.alchemia.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.Alchemia;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
#if MC_VER >= 26300
import net.minecraft.core.registries.codec.RegistryCodecs;
#else
import net.minecraft.core.RegistryCodecs;
#endif
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record WildConversion(int tier, Block from, Block to, Kind kind, Optional<HolderSet<Biome>> biomes) {

    public enum Kind implements StringRepresentable {
        GROUND("ground"),
        PLANT("plant");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

#if MC_VER >= 26300
    public static final Codec<HolderSet<Biome>> BIOMES = RegistryCodecs.holderSet(Registries.BIOME);
#else
    public static final Codec<HolderSet<Biome>> BIOMES = RegistryCodecs.homogeneousList(Registries.BIOME);
#endif

    public static final Codec<WildConversion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, WildGrowth.MAX_STRENGTH).fieldOf("tier").forGetter(WildConversion::tier),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("from").forGetter(WildConversion::from),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("to").forGetter(WildConversion::to),
            Kind.CODEC.optionalFieldOf("kind", Kind.GROUND).forGetter(WildConversion::kind),
            BIOMES.optionalFieldOf("biomes").forGetter(WildConversion::biomes)
    ).apply(instance, WildConversion::new));

    public static final ResourceKey<Registry<WildConversion>> REGISTRY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "wild_growth"));

    private record Index(Registry<WildConversion> registry, List<WildConversion> conversions) {
    }

    private static volatile Index index;

    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, CODEC, CODEC);
    }

    public static void bootstrap(BootstrapContext<WildConversion> context) {
        ground(context, "dirt", 1, Blocks.DIRT, Blocks.GRASS_BLOCK);
        ground(context, "coarse_dirt", 2, Blocks.COARSE_DIRT, Blocks.DIRT);
        ground(context, "gravel", 3, Blocks.GRAVEL, Blocks.COARSE_DIRT);
        ground(context, "sand", 4, Blocks.SAND, Blocks.GRAVEL);
        ground(context, "red_sand", 4, Blocks.RED_SAND, Blocks.GRAVEL);
        plant(context, "dead_bush", Blocks.DEAD_BUSH, Blocks.SHORT_GRASS);
    }

    private static void ground(BootstrapContext<WildConversion> context, String name, int tier, Block from, Block to) {
        context.register(key(name), new WildConversion(tier, from, to, Kind.GROUND, Optional.empty()));
    }

    private static void plant(BootstrapContext<WildConversion> context, String name, Block from, Block to) {
        context.register(key(name), new WildConversion(1, from, to, Kind.PLANT, Optional.empty()));
    }

    private static ResourceKey<WildConversion> key(String name) {
        return ResourceKey.create(REGISTRY, ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, name));
    }

    public boolean allows(Holder<Biome> biome) {
        return biomes.isEmpty() || biomes.get().contains(biome);
    }

    public static List<WildConversion> all(RegistryAccess access) {
        Registry<WildConversion> registry = access.registry(REGISTRY).orElse(null);
        if (registry == null) return List.of();
        Index current = index;
        if (current == null || current.registry() != registry) {
            current = new Index(registry, registry.stream().sorted(Comparator.comparingInt(WildConversion::tier)).toList());
            index = current;
        }
        return current.conversions();
    }
}
