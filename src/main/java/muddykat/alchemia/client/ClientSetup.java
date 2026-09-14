package muddykat.alchemia.client;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.AlchemicalScreen;
import muddykat.alchemia.client.gui.AlchemyMachineScreen;
import muddykat.alchemia.client.gui.ClientIngredientPathTooltip;
import muddykat.alchemia.client.gui.GrindOverlay;
import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.client.render.AlchemyMachineUpgradeRenderer;
import muddykat.alchemia.client.render.CrushProperty;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static muddykat.alchemia.Alchemia.MOD_NAME;

@Mod.EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Alchemia.LOGGER.info(MOD_NAME + " Client Side Initialization");
            MenuScreens.register(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalScreen::new);
            MenuScreens.register(MenuTypeRegistry.ALCHEMY_MACHINE.get(), AlchemyMachineScreen::new);

            for (RegistryObject<Item> entry : ItemRegistry.ITEM_REGISTRY.values()) {
                Item item = entry.get();
                if (item instanceof ItemIngredient) {
                    ItemProperties.register(item, CrushProperty.ID, CrushProperty.INSTANCE);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(IngredientPathTooltip.class, ClientIngredientPathTooltip::new);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("grind_path", GrindOverlay::render);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalCauldronRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMY_MACHINE_UPGRADE.get(), AlchemyMachineUpgradeRenderer::new);
    }
}
