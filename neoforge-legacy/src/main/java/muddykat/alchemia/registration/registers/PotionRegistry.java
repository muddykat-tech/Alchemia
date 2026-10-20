package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PotionRegistry {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Alchemia.MODID);

    public static final DeferredHolder<Potion, Potion> ALCHEMICAL = POTIONS.register("alchemical", () -> new Potion("alchemical"));

    public static final DeferredHolder<Potion, Potion> RUINED = POTIONS.register("ruined", () -> new Potion("ruined"));
}
