package muddykat.alchemia.registration.registers;

import com.mojang.serialization.MapCodec;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.loot.BuriedTabletModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class LootModifierRegistry {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Alchemia.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<BuriedTabletModifier>> BURIED_TABLET =
            LOOT_MODIFIERS.register("buried_tablet", () -> BuriedTabletModifier.CODEC);
}
