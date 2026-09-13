package muddykat.alchemia.client;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.AlchemicalScreen;
import muddykat.alchemia.client.gui.AlchemyMachineScreen;
import muddykat.alchemia.client.render.AlchemyMachineUpgradeRenderer;
import muddykat.alchemia.client.gui.ClientIngredientPathTooltip;
import muddykat.alchemia.client.gui.GrindOverlay;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.client.render.CrushProperty;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiLayersEvent;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;
import net.minecraftforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

import static muddykat.alchemia.Alchemia.MOD_NAME;

@Mod.EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        Alchemia.LOGGER.info(MOD_NAME + " Client Side Initialization");
        event.register(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalScreen::new);
        event.register(MenuTypeRegistry.ALCHEMY_MACHINE.get(), AlchemyMachineScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(new ResourceLocation(Alchemia.MODID, "crush"), CrushProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(IngredientPathTooltip.class, ClientIngredientPathTooltip::new);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(new ResourceLocation(Alchemia.MODID, "grind_path"), GrindOverlay::render);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalCauldronRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMY_MACHINE_UPGRADE.get(), AlchemyMachineUpgradeRenderer::new);
    }
}
