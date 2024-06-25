package muddykat.alchemia.client;

import muddykat.alchemia.client.gui.AlchemicalScreen;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ClientSetup {

    public static void init(final FMLClientSetupEvent event) {
        List<Block> collectedBlocks = BlockRegister.BLOCK_REGISTRY.values().stream().map(RegistryObject::get).toList();
        for (Block b : collectedBlocks) {
            ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutoutMipped());
        }

        event.enqueueWork(() ->
        {
            MenuScreens.register(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalScreen::new);
        });
    }

}
