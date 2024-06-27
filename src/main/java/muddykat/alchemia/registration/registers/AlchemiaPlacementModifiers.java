package muddykat.alchemia.registration.registers;

import com.mojang.serialization.Codec;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.filter.BiomeTagFilter;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AlchemiaPlacementModifiers {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registry.PLACEMENT_MODIFIER_REGISTRY, Alchemia.MODID);

    public static final RegistryObject<PlacementModifierType<BiomeTagFilter>> BIOME_TAG = PLACEMENT_MODIFIERS.register("biome_tag", () -> typeConvert(BiomeTagFilter.CODEC));

    private static <P extends PlacementModifier> PlacementModifierType<P> typeConvert(Codec<P> codec) {
        return () -> codec;
    }
}
