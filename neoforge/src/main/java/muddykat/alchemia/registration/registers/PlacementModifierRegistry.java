package muddykat.alchemia.registration.registers;

import com.mojang.serialization.MapCodec;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.filter.BiomeTagFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
#if MC_VER < 26300
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
#endif

public class PlacementModifierRegistry {
#if MC_VER >= 26300
    public static final DeferredRegister<MapCodec<? extends PlacementModifier>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Alchemia.MODID);

    public static final DeferredHolder<MapCodec<? extends PlacementModifier>, MapCodec<BiomeTagFilter>> BIOME_TAG =
            PLACEMENT_MODIFIERS.register("biome_tag", () -> BiomeTagFilter.CODEC);
#else
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Alchemia.MODID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<BiomeTagFilter>> BIOME_TAG =
            PLACEMENT_MODIFIERS.register("biome_tag", () -> typeConvert(BiomeTagFilter.CODEC));

    private static <P extends PlacementModifier> PlacementModifierType<P> typeConvert(MapCodec<P> codec) {
        return () -> codec;
    }
#endif
}
