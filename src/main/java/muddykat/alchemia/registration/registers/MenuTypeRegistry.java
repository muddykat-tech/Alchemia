package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuTypeRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Alchemia.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AlchemicalCauldronMenu>> ALCHEMICAL_CAULDRON = MENU_TYPES
            .register("alchemical_cauldron", () -> IMenuTypeExtension.create(AlchemicalCauldronMenu::new));
}
