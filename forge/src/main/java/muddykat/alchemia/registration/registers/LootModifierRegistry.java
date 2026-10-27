package muddykat.alchemia.registration.registers;

import com.mojang.serialization.Codec;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.loot.AlchemicalPotionModifier;
import muddykat.alchemia.common.loot.BuriedTabletModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LootModifierRegistry {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Alchemia.MODID);

    public static final RegistryObject<Codec<BuriedTabletModifier>> BURIED_TABLET =
            LOOT_MODIFIERS.register("buried_tablet", () -> BuriedTabletModifier.CODEC);

    public static final RegistryObject<Codec<AlchemicalPotionModifier>> ALCHEMICAL_POTION =
            LOOT_MODIFIERS.register("alchemical_potion", () -> AlchemicalPotionModifier.CODEC);
}
