package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemyMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class MenuTypeRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Alchemia.MODID);

    public static final RegistryObject<MenuType<AlchemicalCauldronMenu>> ALCHEMICAL_CAULDRON = MENU_TYPES
            .register("alchemical_cauldron", () -> IForgeMenuType.create(AlchemicalCauldronMenu::new));

    public static final RegistryObject<MenuType<AlchemyMachineMenu>> ALCHEMY_MACHINE = MENU_TYPES
            .register("alchemy_machine", () -> IForgeMenuType.create(AlchemyMachineMenu::new));
}
