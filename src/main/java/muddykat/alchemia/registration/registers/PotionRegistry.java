package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PotionRegistry {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Alchemia.MODID);

    public static final RegistryObject<Potion> ALCHEMICAL = POTIONS.register("alchemical", () -> new Potion("alchemical"));

    public static final RegistryObject<Potion> RUINED = POTIONS.register("ruined", () -> new Potion("ruined"));
}
