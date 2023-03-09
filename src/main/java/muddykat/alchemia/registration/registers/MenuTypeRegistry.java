package muddykat.alchemia.registration.registers;

import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static muddykat.alchemia.Alchemia.*;

public class MenuTypeRegistry {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

    public static final RegistryObject<MenuType<AlchemicalCauldronMenu>> ALCHEMICAL_CAULDRON = MENU_TYPES
            .register("alchemical_cauldron", () -> IForgeMenuType.create(AlchemicalCauldronMenu::new));

}
