package muddykat.alchemia.client;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.AlchemicalScreen;
import muddykat.alchemia.client.gui.AlchemyMachineScreen;
import muddykat.alchemia.client.gui.ClientIngredientPathTooltip;
import muddykat.alchemia.client.gui.GrindOverlay;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.client.render.CrushProperty;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

import static muddykat.alchemia.Alchemia.MOD_NAME;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        Alchemia.LOGGER.info(MOD_NAME + " Client Side Initialization");
        event.register(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalScreen::new);
        event.register(MenuTypeRegistry.ALCHEMY_MACHINE.get(), AlchemyMachineScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(Alchemia.MODID, "crush"), CrushProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(IngredientPathTooltip.class, ClientIngredientPathTooltip::new);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(Alchemia.MODID, "grind_path"), GrindOverlay::render);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalCauldronRenderer::new);
    }
}
